package hu.upscale.spring.demo.repository.entity;


import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @author László Zoltán
 */
@Value
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "financial_transaction")
public class FinancialTransaction implements Persistable<String> {

    @Id
    @Column("transaction_id")
    String transactionId;

    @Column("previous_transaction_id")
    String previousTransactionId;

    @Column("data")
    String data;

    @Transient
    boolean newEntity;

    @PersistenceCreator
    @SuppressWarnings("unused")
    FinancialTransaction(String transactionId, String previousTransactionId, String data) {
        this.transactionId = transactionId;
        this.previousTransactionId = previousTransactionId;
        this.data = data;

        newEntity = false;
    }

    @Builder(toBuilder = true)
    @SuppressWarnings("unused")
    FinancialTransaction(String transactionId, String previousTransactionId, String data, boolean newEntity) {
        this.transactionId = transactionId;
        this.previousTransactionId = previousTransactionId;
        this.data = data;
        this.newEntity = newEntity;
    }

    @Override
    public String getId() {
        return transactionId;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }
}
