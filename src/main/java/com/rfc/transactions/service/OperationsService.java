package com.rfc.transactions.service;

import com.rfc.transactions.Exceptions.ZeroBalanceException;
import com.rfc.transactions.model.dto.*;
import com.rfc.transactions.model.entity.AccountEntity;
import com.rfc.transactions.model.entity.TransactionsEntity;
import lombok.AllArgsConstructor;
import org.dozer.Mapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.rfc.transactions.utils.Cons.BALANCE_ZERO_ERROR;

@AllArgsConstructor
@Service
public class OperationsService {

    private Mapper mapper;

    private TransactionsService transactionsService;

    private AccountService accountService;

    public void createTransaction(TransactionDto transactionDto) {
        transactionDto.setReference(transactionDto.getReference());
        AccountEntity accountEntity = accountService.getAccountById();
        calculateBalance(accountEntity, transactionDto);
        accountService.updateAccount(accountEntity);
        transactionsService.saveTransaction(mapper.map(transactionDto, TransactionsEntity.class));
    }

    private void calculateBalance(AccountEntity accountEntity, TransactionDto transactionDto) {
        BigDecimal newAccountBalance = accountEntity.getBalance().add(transactionDto.getAmmount().subtract(transactionDto.getFee()));
        if (newAccountBalance.doubleValue() <= 0) {
            throw new ZeroBalanceException(BALANCE_ZERO_ERROR);
        }
        accountEntity.setBalance(newAccountBalance);
    }

    public List<TransactionDto> getFilterTransactions(String IBAN, SortDirectionDto sortAmmount) {
        List<TransactionsEntity> transactionsEntityList = transactionsService.getFilterTransactions(IBAN, sortAmmount);
        return transactionsEntityList.stream().map(t -> mapper.map(t, TransactionDto.class)).collect(Collectors.toList());
    }

    public TransactionStatusDto getTransactionsStatus(String reference, ChannelDto channel) {
        TransactionStatusDto transactionStatusDto = new TransactionStatusDto();
        transactionStatusDto.setReference(reference);
        TransactionsEntity transactionsEntity = transactionsService.getTransactionByReference(reference);
        if (Objects.isNull(transactionsEntity)) {
            transactionStatusDto.setStatus(StatusDto.INVALID);
        } else {
            checkDate(transactionsEntity, transactionStatusDto, channel);
        }
        return transactionStatusDto;
    }

    private void checkDate(TransactionsEntity transactionsEntity, TransactionStatusDto transactionStatusDto, ChannelDto channel) {
        LocalDate today = LocalDate.now();
        if (converToLocalDate(transactionsEntity.getDate()).isBefore(today)) {
            transactionStatusDto.setStatus(StatusDto.SETTLED);
            checkChannel(transactionsEntity, transactionStatusDto, channel);
        } else if (converToLocalDate(transactionsEntity.getDate()).equals(today)) {
            transactionStatusDto.setStatus(StatusDto.PENDING);
            checkChannel(transactionsEntity, transactionStatusDto, channel);
        } else if (converToLocalDate(transactionsEntity.getDate()).isAfter(today)) {
            transactionStatusDto.setStatus(channel.equals(ChannelDto.ATM) ? StatusDto.PENDING : StatusDto.FUTURE);
            checkChannel(transactionsEntity, transactionStatusDto, channel);
        }
    }

    private void checkChannel(TransactionsEntity transactionsEntity, TransactionStatusDto transactionStatusDto, ChannelDto channel) {
        if (channel == ChannelDto.CLIENT || channel == ChannelDto.ATM) {
            transactionStatusDto.setAmmount(transactionsEntity.getAmmount().subtract(transactionsEntity.getFee()));
        } else {
            transactionStatusDto.setAmmount(transactionsEntity.getAmmount());
            transactionStatusDto.setFee(transactionsEntity.getFee());
        }
    }

    private LocalDate converToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
