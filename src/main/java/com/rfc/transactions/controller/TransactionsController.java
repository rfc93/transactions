package com.rfc.transactions.controller;

import com.rfc.transactions.model.dto.ChannelDto;
import com.rfc.transactions.model.dto.SortDirectionDto;
import com.rfc.transactions.model.dto.TransactionDto;
import com.rfc.transactions.model.dto.TransactionStatusDto;
import com.rfc.transactions.service.OperationsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/transactions")
public class TransactionsController {

    private OperationsService operationsService;

    @PostMapping
    public ResponseEntity<TransactionDto> postTransaction(@Valid @RequestBody TransactionDto transactionDto){
        operationsService.createTransaction(transactionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionDto);
    }

    @GetMapping
    public ResponseEntity<List<TransactionDto>> getTransactions(@RequestParam(name="IBAN", required = false) String IBAN, @RequestParam(name="sortAmmount", required = false) SortDirectionDto sortAmmount) {
        return ResponseEntity.ok(operationsService.getFilterTransactions(IBAN, sortAmmount));
    }

    @GetMapping("/status")
    public ResponseEntity<TransactionStatusDto> getTransactionStatus(@RequestParam(name="reference") String reference, @RequestParam(name="channel", required = false) ChannelDto channel) {
        return ResponseEntity.ok(operationsService.getTransactionsStatus(reference, channel));
    }

}
