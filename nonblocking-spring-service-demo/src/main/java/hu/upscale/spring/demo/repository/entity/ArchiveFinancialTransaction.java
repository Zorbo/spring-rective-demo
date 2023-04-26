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
@Table(name = "archive_financial_transaction")
public class ArchiveFinancialTransaction implements Persistable<String> {

    @Column("account_statement_id")
    String accountStatementId;

    @Id
    @Column("transaction_id")
    @EqualsAndHashCode.Include
    String transactionId;

    @Column("transaction_number")
    int transactionNumber;

    @Column("compressed_data")
    String compressedData;

    @Column("signature")
    String signature;

    @Transient
    boolean newEntity;

    @PersistenceCreator
    @SuppressWarnings("unused")
    ArchiveFinancialTransaction(String accountStatementId, String transactionId, int transactionNumber, String compressedData, String signature) {
        this.accountStatementId = accountStatementId;
        this.transactionId = transactionId;
        this.transactionNumber = transactionNumber;
        this.compressedData = compressedData;
        this.signature = signature;

        newEntity = false;
    }

    @Builder(toBuilder = true)
    @SuppressWarnings("unused")
    ArchiveFinancialTransaction(String accountStatementId, String transactionId, int transactionNumber, String compressedData, String signature, boolean newEntity) {
        this.accountStatementId = accountStatementId;
        this.transactionId = transactionId;
        this.transactionNumber = transactionNumber;
        this.compressedData = compressedData;
        this.signature = signature;
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
