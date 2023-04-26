package hu.upscale.spring.demo;

import hu.upscale.spring.demo.repository.FinancialTransactionRepository;
import hu.upscale.spring.demo.repository.entity.FinancialTransaction;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static hu.upscale.spring.demo.util.ResourceUtil.readResourceFileLines;

@SpringBootApplication
public class BlockingSpringServiceDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockingSpringServiceDemoApplication.class, args);
    }

    @Component
    @AllArgsConstructor
    public static final class RandomDataGenerator {

        private static final Logger LOGGER = LoggerFactory.getLogger(RandomDataGenerator.class);

        // 10 Mb
        private static final int DATA_SIZE = 10_485_760;
        private static final int CHAIN_LENGTH = 10;
        private static final int NUMBER_OF_CHAINS = 8;

        private static final Random RANDOM = new Random();
        private static final Map<Integer, List<String>> DICTIONARY = readDictionary();
        private static final IntSummaryStatistics WORD_LENGTH_SUMMARY_STATISTICS = DICTIONARY.keySet().stream().mapToInt(i -> i).summaryStatistics();

        private final FinancialTransactionRepository financialTransactionRepository;

        @PostConstruct
        public void generateRandomData() {
            try {
                if (financialTransactionRepository.count() == 0) {
                    LOGGER.warn("Load up database with test data");

                    Set<String> topTransactionIds = IntStream.range(0, NUMBER_OF_CHAINS)
                            .parallel()
                            .mapToObj(ignore ->
                                    IntStream.range(0, CHAIN_LENGTH)
                                            .mapToObj(ignored -> {
                                                FinancialTransaction financialTransaction = new FinancialTransaction();
                                                financialTransaction.setTransactionId(UUID.randomUUID().toString());
                                                financialTransaction.setData(Base64.getEncoder().encodeToString(getRandomData()));
                                                return financialTransaction;
                                            })
                                            .reduce(null, (left, right) -> {
                                                if (left != null) {
                                                    right.setPreviousTransactionId(left.getTransactionId());
                                                }

                                                financialTransactionRepository.save(right);
                                                LOGGER.info("Test data inserted - transactionId: [{}]", right.getTransactionId());

                                                return right;
                                            })
                            )
                            .map(FinancialTransaction::getTransactionId).collect(Collectors.toUnmodifiableSet());

                    LOGGER.info("{} test data chain inserted", topTransactionIds.size());

                    String topTransactionsText = topTransactionIds.stream().collect(Collectors.joining(System.lineSeparator()));
                    Files.writeString(Paths.get("./top_transaction_ids.txt"), topTransactionsText, StandardCharsets.UTF_8,
                            StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to save top_transaction_ids", e);
            }
        }

        private static byte[] getRandomData() {
            ByteBuffer byteBuffer = ByteBuffer.allocate(DATA_SIZE);
            int remaining = byteBuffer.remaining();
            while (remaining > 0) {
                byte[] nextBytes = getRandomWord().getBytes(StandardCharsets.UTF_8);
                if (nextBytes.length > remaining) {
                    nextBytes = IntStream.range(0, remaining).mapToObj(ignore -> " ").collect(Collectors.joining()).getBytes(StandardCharsets.UTF_8);
                }

                byteBuffer.put(nextBytes);
                remaining = byteBuffer.remaining();
            }

            return byteBuffer.array();
        }

        private static String getRandomWord() {
            int randomWordLength = -1;
            while (randomWordLength < 0) {
                randomWordLength = RANDOM.nextInt(WORD_LENGTH_SUMMARY_STATISTICS.getMax() - WORD_LENGTH_SUMMARY_STATISTICS.getMin() + 1)
                        + WORD_LENGTH_SUMMARY_STATISTICS.getMin();
                if (!DICTIONARY.containsKey(randomWordLength)) {
                    randomWordLength = -1;
                }
            }

            List<String> words = DICTIONARY.get(randomWordLength);
            int randomWordIndex = RANDOM.nextInt(words.size());

            return words.get(randomWordIndex);
        }

        private static Map<Integer, List<String>> readDictionary() {
            return readResourceFileLines("words_alpha.txt", StandardCharsets.UTF_8)
                    .filter(Predicate.not(String::isBlank))
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.collectingAndThen(Collectors.groupingBy(String::length, Collectors.toUnmodifiableList()), Collections::unmodifiableMap));
        }

    }

}
