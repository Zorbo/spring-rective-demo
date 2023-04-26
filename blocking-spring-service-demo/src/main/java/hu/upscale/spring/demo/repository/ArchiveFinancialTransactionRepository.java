package hu.upscale.spring.demo.repository;

import hu.upscale.spring.demo.repository.entity.ArchiveFinancialTransaction;
import hu.upscale.spring.demo.repository.entity.ArchiveFinancialTransaction.ArchiveFinancialTransactionId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author László Zoltán
 */
@Repository
public interface ArchiveFinancialTransactionRepository extends CrudRepository<ArchiveFinancialTransaction, ArchiveFinancialTransactionId> {
}
