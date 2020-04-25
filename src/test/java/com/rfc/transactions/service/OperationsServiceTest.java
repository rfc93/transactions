package com.rfc.transactions.service;

import com.rfc.transactions.Exceptions.ZeroBalanceException;
import com.rfc.transactions.model.dto.ChannelDto;
import com.rfc.transactions.model.dto.StatusDto;
import com.rfc.transactions.model.dto.TransactionDto;
import com.rfc.transactions.model.dto.TransactionStatusDto;
import com.rfc.transactions.model.entity.AccountEntity;
import com.rfc.transactions.model.entity.TransactionsEntity;
import org.dozer.Mapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OperationsServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionsService transactionsService;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private OperationsService operationsService;

    @Captor
    private ArgumentCaptor<AccountEntity> accountEntityCaptor;

    @Test
    void shouldSaveANewTransactionWithPositiveAmmount() {
        TransactionDto transactionDto = new TransactionDto(null, "ES10123456789098765", Date.from(Instant.now()), BigDecimal.TEN, BigDecimal.ONE, "Transaction Description");
        BigDecimal accountBalance = BigDecimal.valueOf(100);
        AccountEntity accountEntity = new AccountEntity(1, accountBalance);
        when(accountService.getAccountById()).thenReturn(accountEntity);
        when(mapper.map(eq(transactionDto), eq(TransactionsEntity.class))).thenReturn(new TransactionsEntity());

        operationsService.createTransaction(transactionDto);

        verify(accountService).getAccountById();
        verify(accountService).updateAccount(accountEntityCaptor.capture());
        verify(transactionsService).saveTransaction(any(TransactionsEntity.class));
        verify(mapper).map(eq(transactionDto), eq(TransactionsEntity.class));
        AccountEntity updatedAccountEntity = accountEntityCaptor.getValue();
        assertNotNull(updatedAccountEntity);
        assertEquals(accountEntity.getId(), updatedAccountEntity.getId());
        assertEquals(accountBalance.add(transactionDto.getAmmount()).subtract(transactionDto.getFee()), updatedAccountEntity.getBalance());
    }

    @Test
    void shouldSaveANewTransactionWithNegativeAmmount() {
        TransactionDto transactionDto = new TransactionDto(null, "ES10123456789098765", Date.from(Instant.now()), BigDecimal.valueOf(-10), BigDecimal.ONE, "Transaction Description");
        BigDecimal accountBalance = BigDecimal.valueOf(100);
        AccountEntity accountEntity = new AccountEntity(1, accountBalance);
        when(accountService.getAccountById()).thenReturn(accountEntity);
        when(mapper.map(eq(transactionDto), eq(TransactionsEntity.class))).thenReturn(new TransactionsEntity());

        operationsService.createTransaction(transactionDto);

        verify(accountService).getAccountById();
        verify(accountService).updateAccount(accountEntityCaptor.capture());
        verify(transactionsService).saveTransaction(any(TransactionsEntity.class));
        verify(mapper).map(eq(transactionDto), eq(TransactionsEntity.class));
        AccountEntity updatedAccountEntity = accountEntityCaptor.getValue();
        assertNotNull(updatedAccountEntity);
        assertEquals(accountEntity.getId(), updatedAccountEntity.getId());
        assertEquals(accountBalance.add(transactionDto.getAmmount()).subtract(transactionDto.getFee()), updatedAccountEntity.getBalance());
    }

    @Test
    void shouldNotSaveANewTransaction_whenResultAccountBalanceIsZeroOrless() {
        TransactionDto transactionDto = new TransactionDto(null, "ES10123456789098765", Date.from(Instant.now()), BigDecimal.valueOf(-10), BigDecimal.ONE, "Transaction Description");
        BigDecimal accountBalance = BigDecimal.TEN;
        AccountEntity accountEntity = new AccountEntity(1, accountBalance);
        when(accountService.getAccountById()).thenReturn(accountEntity);

        assertThrows(ZeroBalanceException.class, () -> operationsService.createTransaction(transactionDto));

        verify(accountService).getAccountById();
        verify(accountService, times(0)).updateAccount(any(AccountEntity.class));
        verify(transactionsService, times(0)).saveTransaction(any(TransactionsEntity.class));
        verify(mapper, times(0)).map(eq(transactionDto), eq(TransactionsEntity.class));
    }

    @Test
    void shouldGetFilterTransactionsListAndTransformEntityToDto() {
        String IBAN = "IBAN";
        TransactionsEntity transactionsEntity = new TransactionsEntity(1, "TxReference", IBAN, null, BigDecimal.ONE, BigDecimal.ZERO, "");
        TransactionDto transactionDto = new TransactionDto(transactionsEntity.getReference(), transactionsEntity.getAccountIBAN(), null, transactionsEntity.getAmmount(), transactionsEntity.getFee(), "");
        when(transactionsService.getFilterTransactions(eq(IBAN), eq(null))).thenReturn(Arrays.asList(transactionsEntity));
        when(mapper.map(any(TransactionsEntity.class), eq(TransactionDto.class))).thenReturn(transactionDto);

        List<TransactionDto> transactionDtoList = operationsService.getFilterTransactions(IBAN, null);

        verify(transactionsService).getFilterTransactions(eq(IBAN), eq(null));
        verify(mapper).map(any(TransactionsEntity.class), eq(TransactionDto.class));
        assertNotNull(transactionDtoList);
        TransactionDto transactionDtoMapped = transactionDtoList.get(0);
        assertEquals(transactionsEntity.getReference(), transactionDtoMapped.getReference());
        assertEquals(transactionsEntity.getAccountIBAN(), transactionDtoMapped.getAccountIBAN());
        assertEquals(transactionsEntity.getAmmount(), transactionDtoMapped.getAmmount());
        assertEquals(transactionsEntity.getFee(), transactionDtoMapped.getFee());
    }

    @Test
    void shouldGetTransactionStatusInvalid_whenTransactionsNotExists() {
        String reference = "TxReference";
        ChannelDto channelDto = ChannelDto.ATM;
        when(transactionsService.getTransactionByReference(eq(reference))).thenReturn(null);

        TransactionStatusDto transactionStatusDto = operationsService.getTransactionsStatus(reference, channelDto);

        verify(transactionsService).getTransactionByReference(eq(reference));
        assertNotNull(transactionStatusDto);
        assertEquals(reference, transactionStatusDto.getReference());
        assertEquals(StatusDto.INVALID, transactionStatusDto.getStatus());
    }

    @Test
    void shouldGetTransactionStatusSettled_whenTransactionsExistsAndDateIsBeforeToday() {
        String reference = "TxReference";
        ChannelDto channelDto = ChannelDto.ATM;
        TransactionsEntity transactionsEntity = new TransactionsEntity(1, reference, "IBAN", Date.from(Instant.now().minusSeconds(86400)), BigDecimal.TEN, BigDecimal.ONE, "");
        when(transactionsService.getTransactionByReference(eq(reference))).thenReturn(transactionsEntity);

        TransactionStatusDto transactionStatusDto = operationsService.getTransactionsStatus(reference, channelDto);

        verify(transactionsService).getTransactionByReference(eq(reference));
        assertNotNull(transactionStatusDto);
        assertEquals(reference, transactionStatusDto.getReference());
        assertEquals(StatusDto.SETTLED, transactionStatusDto.getStatus());
        assertEquals(transactionsEntity.getAmmount().subtract(transactionsEntity.getFee()).doubleValue(), transactionStatusDto.getAmmount().doubleValue());
    }

    @Test
    void shouldGetTransactionStatusPENDING_whenTransactionsExistsAndDateIsToday() {
        String reference = "TxReference";
        ChannelDto channelDto = ChannelDto.INTERNAL;
        TransactionsEntity transactionsEntity = new TransactionsEntity(1, reference, "IBAN", Date.from(Instant.now()), BigDecimal.TEN, BigDecimal.ONE, "");
        when(transactionsService.getTransactionByReference(eq(reference))).thenReturn(transactionsEntity);

        TransactionStatusDto transactionStatusDto = operationsService.getTransactionsStatus(reference, channelDto);

        verify(transactionsService).getTransactionByReference(eq(reference));
        assertNotNull(transactionStatusDto);
        assertEquals(reference, transactionStatusDto.getReference());
        assertEquals(StatusDto.PENDING, transactionStatusDto.getStatus());
        assertEquals(transactionsEntity.getAmmount().doubleValue(), transactionStatusDto.getAmmount().doubleValue());
        assertEquals(transactionsEntity.getFee().doubleValue(), transactionStatusDto.getFee().doubleValue());
    }

    @Test
    void shouldGetTransactionStatusFUTURE_whenTransactionsExistsAndDateAfterToday() {
        String reference = "TxReference";
        ChannelDto channelDto = ChannelDto.CLIENT;
        TransactionsEntity transactionsEntity = new TransactionsEntity(1, reference, "IBAN", Date.from(Instant.now().plusSeconds(86400)), BigDecimal.TEN, BigDecimal.ONE, "");
        when(transactionsService.getTransactionByReference(eq(reference))).thenReturn(transactionsEntity);

        TransactionStatusDto transactionStatusDto = operationsService.getTransactionsStatus(reference, channelDto);

        verify(transactionsService).getTransactionByReference(eq(reference));
        assertNotNull(transactionStatusDto);
        assertEquals(reference, transactionStatusDto.getReference());
        assertEquals(StatusDto.FUTURE, transactionStatusDto.getStatus());
        assertEquals(transactionsEntity.getAmmount().subtract(transactionsEntity.getFee()).doubleValue(), transactionStatusDto.getAmmount().doubleValue());
    }

}