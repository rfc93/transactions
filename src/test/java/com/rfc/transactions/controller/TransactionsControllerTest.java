package com.rfc.transactions.controller;

import com.rfc.transactions.model.dto.ChannelDto;
import com.rfc.transactions.model.dto.SortDirectionDto;
import com.rfc.transactions.model.dto.TransactionDto;
import com.rfc.transactions.model.dto.TransactionStatusDto;
import com.rfc.transactions.service.OperationsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionsControllerTest {

    @Mock
    private OperationsService operationsService;

    @InjectMocks
    private TransactionsController transactionsController;

    @Test
    void shouldCallOperationServiceToSaveNewTransaction() {
        TransactionDto transactionDto = new TransactionDto(null, "ES101234567890987654", Date.from(Instant.now()), BigDecimal.ONE, BigDecimal.ZERO, "TxDescription");

        ResponseEntity<TransactionDto> response = transactionsController.postTransaction(transactionDto);

        verify(operationsService).createTransaction(transactionDto);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(transactionDto, response.getBody());
    }

    @Test
    void shouldCallOperationServiceToGetAFilterTransactionList() {
        String IBAN = "IBAN";
        SortDirectionDto sortDirectionDto = SortDirectionDto.ASC;
        when(operationsService.getFilterTransactions(eq(IBAN), eq(sortDirectionDto))).thenReturn(new ArrayList<>());

        ResponseEntity<List<TransactionDto>> response = transactionsController.getTransactions(IBAN, sortDirectionDto);

        verify(operationsService).getFilterTransactions(eq(IBAN), eq(sortDirectionDto));
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldCallOperationServiceToGetATransactionStatus() {
        String reference = "TxReference";
        ChannelDto channelDto = ChannelDto.ATM;
        when(operationsService.getTransactionsStatus(eq(reference), eq(channelDto))).thenReturn(new TransactionStatusDto());

        ResponseEntity<TransactionStatusDto> response = transactionsController.getTransactionStatus(reference, channelDto);

        verify(operationsService).getTransactionsStatus(eq(reference), eq(channelDto));
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
