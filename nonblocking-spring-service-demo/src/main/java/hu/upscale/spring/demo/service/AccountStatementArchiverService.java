package hu.upscale.spring.demo.service;

import hu.upscale.spring.demo.repository.ArchiveFinancialTransactionRepository;
import hu.upscale.spring.demo.repository.FinancialTransactionRepository;
import hu.upscale.spring.demo.repository.entity.ArchiveFinancialTransaction;
import hu.upscale.spring.demo.repository.entity.FinancialTransaction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Base64;
import java.util.UUID;

/**
 * @author László Zoltán
 */
@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountStatementArchiverService {

    FinancialTransactionRepository financialTransactionRepository;
    ArchiveFinancialTransactionRepository archiveFinancialTransactionRepository;
    ZipService zipService;
    RsaSignatureService rsaSignatureService;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Mono<Void> archiveFinancialTransaction(UUID accountStatementId, int transactionNumber, FinancialTransaction financialTransaction) {
        var rawData = Base64.getDecoder().decode(financialTransaction.getData());

        return Mono.zip(
                        Mono.fromSupplier(() -> zipService.compress(rawData))
                                .subscribeOn(Schedulers.parallel())
                                .doOnSuccess(ignore ->
                                        log.info(
                                                "Financial transaction data compressed - transactionNumber: [{}], transactionId: [{}]",
                                                transactionNumber,
                                                financialTransaction.getTransactionId()
                                        )
                                ),
                        Mono.fromSupplier(() -> rsaSignatureService.signData(rawData))
                                .subscribeOn(Schedulers.parallel())
                                .doOnSuccess(ignore ->
                                        log.info(
                                                "Financial transaction data signed - transactionNumber: [{}], transactionId: [{}]",
                                                transactionNumber,
                                                financialTransaction.getTransactionId()
                                        )
                                )
                )
                .flatMap(compressedDataAndSignature ->
                        archiveFinancialTransactionRepository.save(
                                        ArchiveFinancialTransaction.builder()
                                                .accountStatementId(accountStatementId.toString())
                                                .transactionId(financialTransaction.getTransactionId())
                                                .transactionNumber(transactionNumber)
                                                .compressedData(Base64.getEncoder().encodeToString(compressedDataAndSignature.getT1()))
                                                .signature(Base64.getEncoder().encodeToString(compressedDataAndSignature.getT2()))
                                                .newEntity(true)
                                                .build()
                                )
                                .doOnSuccess(ignore ->
                                        log.info(
                                                "Archive financial transaction saved - transactionNumber: [{}], accountStatementId: [{}], transactionId: [{}]",
                                                transactionNumber,
                                                accountStatementId,
                                                financialTransaction.getTransactionId()
                                        )
                                )
                )
                .then(
                        financialTransactionRepository
                                .deleteById(financialTransaction.getTransactionId())
                                .doOnSuccess(ignore ->
                                        log.info(
                                                "Financial transaction deleted - transactionNumber: [{}], transactionId: [{}]",
                                                transactionNumber,
                                                financialTransaction.getTransactionId()
                                        )
                                )
                );
    }
}
