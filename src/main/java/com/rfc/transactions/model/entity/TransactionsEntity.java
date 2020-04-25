package com.rfc.transactions.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class TransactionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(unique = true)
    private String reference;

    private String accountIBAN;

    private Date date;

    private BigDecimal ammount;

    private BigDecimal fee;

    private String description;

    public TransactionsEntity(String reference, String accountIBAN, Date date, BigDecimal ammount, BigDecimal fee, String description) {
        this.reference = reference;
        this.accountIBAN = accountIBAN;
        this.date = date;
        this.ammount = ammount;
        this.fee = fee;
        this.description = description;
    }
}
