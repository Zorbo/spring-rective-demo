package hu.upscale.spring.demo.repository;

import hu.upscale.spring.demo.repository.entity.FinancialTransaction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author László Zoltán
 */
@Repository
public interface FinancialTransactionRepository extends ReactiveCrudRepository<FinancialTransaction, String> {
}
