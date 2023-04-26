package hu.upscale.spring.demo.repository.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author László Zoltán
 */
@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "archive_financial_transaction")
public final class ArchiveFinancialTransaction {

    @EmbeddedId
    @EqualsAndHashCode.Include
    ArchiveFinancialTransactionId archiveFinancialTransactionId;

    @Column(name = "transaction_number")
    int transactionNumber;

    @Column(name = "compressed_data")
    String compressedData;

    @Column(name = "signature")
    String signature;

    @Data
    @Embeddable
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static final class ArchiveFinancialTransactionId implements Serializable {

        private static final long serialVersionUID = 1L;

        @Column(name = "account_statement_id")
        String accountStatementId;

        @Column(name = "transaction_id", unique = true)
        String transactionId;

    }

}
