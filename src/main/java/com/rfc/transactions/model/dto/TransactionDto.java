package com.rfc.transactions.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDto {

    private String reference;

    @NotNull
    private String accountIBAN;

    private Date date;

    @NotNull
    private BigDecimal ammount;

    private BigDecimal fee;

    private String description;

    public void setReference(String reference) {
        this.reference = checkEmptyReference(reference);
    }

    private String checkEmptyReference(String reference) {
        return !StringUtils.isEmpty(reference) ? reference : UUID.nameUUIDFromBytes(LocalDate.now().toString().getBytes()).toString();
    }

}