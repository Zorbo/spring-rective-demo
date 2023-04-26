package hu.upscale.spring.demo.repository;

import hu.upscale.spring.demo.repository.entity.ArchiveFinancialTransaction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * @author László Zoltán
 */
@Repository
public interface ArchiveFinancialTransactionRepository extends ReactiveCrudRepository<ArchiveFinancialTransaction, String> {
    Mono<ArchiveFinancialTransaction> findByAccountStatementIdAndTransactionId(String accountStatementId, String transactionId);
}
