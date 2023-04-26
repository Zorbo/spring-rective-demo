package hu.upscale.spring.demo.service;

import hu.upscale.spring.demo.repository.FinancialTransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author László Zoltán
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountStatementGeneratorService {

    private static final int LAST_TRANSACTION_NUMBER_ON_ACCOUNT_STATEMENT = 1;

    FinancialTransactionRepository financialTransactionRepository;
    AccountStatementArchiverService accountStatementArchiverService;

    public UUID generateAccountStatement(UUID lastTransactionId) {
        UUID accountStatementId = UUID.randomUUID();

        String transactionId = lastTransactionId.toString();
        for (int transactionNumber = 1; transactionId != null; transactionNumber++) {
            var optionalFinancialTransaction = financialTransactionRepository.findById(transactionId);
            if (optionalFinancialTransaction.isPresent()) {
                var financialTransaction = optionalFinancialTransaction.get();
                log.info(
                        "Financial transaction fetched - transactionNumber: [{}], transactionId: [{}]",
                        transactionNumber,
                        transactionId
                );

                accountStatementArchiverService.archiveFinancialTransaction(accountStatementId, transactionNumber, financialTransaction);
                transactionId = financialTransaction.getPreviousTransactionId();
            } else {
                transactionId = null;
            }
        }

        return accountStatementId;
    }
}
