package hu.upscale.spring.demo.repository.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author László Zoltán
 */
@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "financial_transaction")
public final class FinancialTransaction {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "transaction_id")
    String transactionId;

    @Column(name = "previous_transaction_id")
    String previousTransactionId;

    @Column(name = "data")
    String data;

}
