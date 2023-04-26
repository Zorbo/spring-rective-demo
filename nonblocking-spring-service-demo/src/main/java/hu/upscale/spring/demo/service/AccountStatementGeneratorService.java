package hu.upscale.spring.demo.service;

import hu.upscale.spring.demo.repository.FinancialTransactionRepository;
import hu.upscale.spring.demo.repository.entity.FinancialTransaction;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author László Zoltán
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountStatementGeneratorService {

    FinancialTransactionRepository financialTransactionRepository;
    AccountStatementArchiverService accountStatementArchiverService;

    public Mono<UUID> generateAccountStatement(UUID lastTransactionId) {
        UUID accountStatementId = UUID.randomUUID();
        AtomicInteger transactionNumberCounter = new AtomicInteger(1);

        return fetchAllFinancialTransactions(transactionNumberCounter, lastTransactionId)
                .parallel()
                .runOn(Schedulers.parallel())
//                .collectList()
//                .flatMapIterable(Function.identity())
                .flatMap(transactionNumberAndFinancialTransaction ->
                        accountStatementArchiverService.archiveFinancialTransaction(
                                accountStatementId,
                                transactionNumberAndFinancialTransaction.getT1(),
                                transactionNumberAndFinancialTransaction.getT2())
                )
                .then()
                .thenReturn(accountStatementId);

    }

    private Flux<Tuple2<Integer, FinancialTransaction>> fetchAllFinancialTransactions(AtomicInteger transactionNumberCounter, UUID lastTransactionId) {
        return fetchFinancialTransaction(transactionNumberCounter, lastTransactionId.toString())
                .expand(transactionNumberAndFinancialTransaction ->
                        Optional.ofNullable(transactionNumberAndFinancialTransaction.getT2().getPreviousTransactionId())
                                .map(nextFinancialTransactionId ->
                                        fetchFinancialTransaction(transactionNumberCounter, nextFinancialTransactionId)
                                )
                                .orElse(Mono.empty())
                );
    }

    private Mono<Tuple2<Integer, FinancialTransaction>> fetchFinancialTransaction(AtomicInteger transactionNumberCounter, String transactionId) {
        var transactionNumber = transactionNumberCounter.getAndIncrement();
        return financialTransactionRepository.findById(transactionId)
                .map(financialTransaction ->
                        Tuples.of(transactionNumber, financialTransaction)
                )
                .doOnSuccess(transactionNumberAndFinancialTransaction ->
                        log.info(
                                "Financial transaction fetched - transactionNumber: [{}], transactionId: [{}]",
                                transactionNumber,
                                transactionId
                        )
                );
    }
}
