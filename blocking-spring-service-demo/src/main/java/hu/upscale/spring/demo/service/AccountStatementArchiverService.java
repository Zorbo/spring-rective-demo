package hu.upscale.spring.demo.service;

import hu.upscale.spring.demo.repository.ArchiveFinancialTransactionRepository;
import hu.upscale.spring.demo.repository.FinancialTransactionRepository;
import hu.upscale.spring.demo.repository.entity.ArchiveFinancialTransaction;
import hu.upscale.spring.demo.repository.entity.ArchiveFinancialTransaction.ArchiveFinancialTransactionId;
import hu.upscale.spring.demo.repository.entity.FinancialTransaction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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
    public void archiveFinancialTransaction(UUID accountStatementId, int transactionNumber, FinancialTransaction financialTransaction) {
        ArchiveFinancialTransaction archiveFinancialTransaction = new ArchiveFinancialTransaction();

        ArchiveFinancialTransactionId archiveFinancialTransactionId = new ArchiveFinancialTransactionId();
        archiveFinancialTransactionId.setAccountStatementId(accountStatementId.toString());
        archiveFinancialTransactionId.setTransactionId(financialTransaction.getTransactionId());

        archiveFinancialTransaction.setArchiveFinancialTransactionId(archiveFinancialTransactionId);
        archiveFinancialTransaction.setTransactionNumber(transactionNumber);

        var rawData = Base64.getDecoder().decode(financialTransaction.getData());
        var compressedData = Base64.getEncoder().encodeToString(zipService.compress(rawData));
        log.info(
                "Financial transaction data compressed - transactionNumber: [{}], transactionId: [{}]",
                transactionNumber,
                financialTransaction.getTransactionId()
        );

        var signature = Base64.getEncoder().encodeToString(rsaSignatureService.signData(rawData));
        log.info(
                "Financial transaction data signed - transactionNumber: [{}], transactionId: [{}]",
                transactionNumber,
                financialTransaction.getTransactionId()
        );

        archiveFinancialTransaction.setCompressedData(compressedData);
        archiveFinancialTransaction.setSignature(signature);

        archiveFinancialTransactionRepository.save(archiveFinancialTransaction);
        log.info(
                "Archive financial transaction saved - transactionNumber: [{}], accountStatementId: [{}], transactionId: [{}]",
                transactionNumber,
                accountStatementId,
                financialTransaction.getTransactionId()
        );

        financialTransactionRepository.deleteById(financialTransaction.getTransactionId());
        log.info(
                "Financial transaction deleted - transactionNumber: [{}], transactionId: [{}]",
                transactionNumber,
                financialTransaction.getTransactionId()
        );
    }
}
