package com.rfc.transactions.service;

import com.rfc.transactions.model.dto.SortDirectionDto;
import com.rfc.transactions.model.entity.TransactionsEntity;
import com.rfc.transactions.repository.TransactionsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionsServiceTest {

    @Mock
    private TransactionsRepository transactionsRepository;

    @InjectMocks
    private TransactionsService transactionsService;

    @Test
    void shouldGetTransactionByReference_whenTransactionExists() {
        String reference = "TxReference";
        TransactionsEntity transactionEntity = new TransactionsEntity(1, reference, "IBAN", null, null, null, "");
        when(transactionsRepository.findByReference(eq(reference))).thenReturn(Optional.of(transactionEntity));

        TransactionsEntity transactionsEntityFetched = transactionsService.getTransactionByReference(reference);

        verify(transactionsRepository).findByReference(eq(reference));
        assertNotNull(transactionsEntityFetched);
        assertEquals(reference, transactionsEntityFetched.getReference());
    }

    @Test
    void shouldGetTransactionByReference_whenTransactionNotExists() {
        String reference = "TxReference";
        when(transactionsRepository.findByReference(eq(reference))).thenReturn(Optional.empty());

        TransactionsEntity transactionsEntityFetched = transactionsService.getTransactionByReference(reference);

        verify(transactionsRepository).findByReference(eq(reference));
        assertNull(transactionsEntityFetched);
    }

    @Test
    void shouldSaveANewTransaction() {
        TransactionsEntity transactionsEntityToSave = new TransactionsEntity("TxReference", "ES101234567890987654", Date.from(Instant.now()), BigDecimal.ONE, BigDecimal.ZERO, "TxDescription");
        TransactionsEntity transactionEntitySaved = new TransactionsEntity(1, transactionsEntityToSave.getReference(), transactionsEntityToSave.getAccountIBAN(), transactionsEntityToSave.getDate(), transactionsEntityToSave.getAmmount(), transactionsEntityToSave.getFee(), transactionsEntityToSave.getDescription());
        when(transactionsRepository.save(transactionsEntityToSave)).thenReturn(transactionEntitySaved);

        transactionEntitySaved = transactionsService.saveTransaction(transactionsEntityToSave);

        verify(transactionsRepository).save(any());
        assertNotNull(transactionEntitySaved);
    }

    @Test
    void shouldGetFilterTransactionsWithoutFilters() {
        when(transactionsRepository.findAll(any(Sort.class))).thenReturn(Arrays.asList(new TransactionsEntity()));

        List<TransactionsEntity> transactionsEntityList = transactionsService.getFilterTransactions(null, null);

        verify(transactionsRepository).findAll(any(Sort.class));
        assertNotNull(transactionsEntityList);
        assertTrue(!transactionsEntityList.isEmpty());
    }

    @Test
    void shouldGetFilterTransactionsWithFilters() {
        String IBAN = "ES101234567890987654";
        SortDirectionDto sortDirectionDto = SortDirectionDto.ASC;
        when(transactionsRepository.findByAccountIBAN(eq(IBAN), any(Sort.class))).thenReturn(Arrays.asList(new TransactionsEntity()));

        List<TransactionsEntity> transactionsEntityList = transactionsService.getFilterTransactions(IBAN, sortDirectionDto);

        verify(transactionsRepository).findByAccountIBAN(eq(IBAN), any(Sort.class));
        assertNotNull(transactionsEntityList);
    }
}
