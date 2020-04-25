package com.rfc.transactions.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rfc.transactions.model.dto.TransactionDto;
import com.rfc.transactions.model.entity.AccountEntity;
import com.rfc.transactions.model.entity.TransactionsEntity;
import com.rfc.transactions.repository.AccountRepository;
import com.rfc.transactions.repository.TransactionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
@ExtendWith(SpringExtension.class)
public class TransactionsControllerIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private List<TransactionsEntity> transactionsEntityList;
    private AccountEntity accountEntity;

    @BeforeEach
    void setUp() {
        transactionsRepository.deleteAll();
        accountRepository.deleteAll();
        transactionsEntityList = new ArrayList<>();
        transactionsEntityList.add(new TransactionsEntity("TxReference1", "ES10123456789098765432", null, BigDecimal.ONE, BigDecimal.ZERO, "Tx1 Description"));
        transactionsEntityList.add(new TransactionsEntity("TxReference2", "ES10123456789098765432", null, BigDecimal.TEN, BigDecimal.ZERO, "Tx2 Description"));
        transactionsEntityList.add(new TransactionsEntity("TxReference3", "ES10123456789098765432", null, BigDecimal.valueOf(5), BigDecimal.ZERO, "Tx3 Description"));
        transactionsEntityList.add(new TransactionsEntity("TxReference4a", "ES10123456789098765433", null, BigDecimal.valueOf(3), BigDecimal.ZERO, "Tx4 Description"));
        transactionsRepository.save(transactionsEntityList.get(0));
        transactionsRepository.save(transactionsEntityList.get(1));
        transactionsRepository.save(transactionsEntityList.get(2));
        transactionsRepository.save(transactionsEntityList.get(3));
        accountEntity = accountRepository.save(new AccountEntity(1, BigDecimal.valueOf(100)));
    }

    @Test
    void shouldSaveNewPositiveTransactionAndUpdateBalance_whenPostTransactionsEndpointIsCalled() throws Exception {
        String body = "{\"accountIBAN\": \"ES10123456789098765432\", \"ammount\": \"2.50\", \"fee\":\"1.00\"}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/transactions")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated()).andReturn();

        TransactionDto transactionDto = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionDto.class);
        AccountEntity accountEntityUpdated = accountRepository.findById(accountEntity.getId()).get();
        assertNotNull(transactionDto);
        assertNotNull(transactionDto.getReference());
        assertEquals(accountEntity.getBalance().add(transactionDto.getAmmount().subtract(transactionDto.getFee())), accountEntityUpdated.getBalance());
    }

    @Test
    void shouldSaveNewNegativeTransactionAndUpdateBalance_whenPostTransactionsEndpointIsCalled() throws Exception {
        String reference = "TxReference";
        String body = "{\"reference\": \"" + reference + "\", \"accountIBAN\": \"ES10123456789098765432\", \"ammount\": \"-2.50\", \"fee\":\"1.00\"}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/transactions")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated()).andReturn();

        TransactionDto transactionDto = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionDto.class);
        AccountEntity accountEntityUpdated = accountRepository.findById(accountEntity.getId()).get();
        assertNotNull(transactionDto);
        assertEquals(reference, transactionDto.getReference());
        assertEquals(accountEntity.getBalance().add(transactionDto.getAmmount().subtract(transactionDto.getFee())), accountEntityUpdated.getBalance());
    }

    @Test
    void shouldNotSaveNewTransactionIfUpdatedBalanceIsZero_whenPostTransactionsEndpointIsCalled() throws Exception {
        String IBAN = "IBANTEST";
        String body = "{\"accountIBAN\": \"" + IBAN + "\", \"ammount\": \"-100\", \"fee\":\"0\"}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/transactions")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest()).andReturn();

        AccountEntity accountEntityUpdated = accountRepository.findById(accountEntity.getId()).get();
        assertTrue(result.getResponse().getContentAsString().contains("Account updated balance must not be Zero or Less!"));
        assertEquals(0, transactionsRepository.findByAccountIBAN(IBAN, Sort.unsorted()).size());
        assertEquals(accountEntity.getBalance().doubleValue(), accountEntityUpdated.getBalance().doubleValue());
    }

    @Test
    void shouldReturnAListOfTransactionsWithoutFilters_whenGetTransactionsEndpointIsCalled() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions")
        ).andExpect(status().isOk()).andReturn();

        List<TransactionDto> transactionDtoList = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<TransactionDto>>() {});
        assertEquals(transactionsEntityList.size(), transactionDtoList.size());
        assertEquals(transactionsEntityList.get(0).getAmmount().doubleValue(), transactionDtoList.get(0).getAmmount().doubleValue());
        assertEquals(transactionsEntityList.get(1).getAmmount().doubleValue(), transactionDtoList.get(1).getAmmount().doubleValue());
        assertEquals(transactionsEntityList.get(2).getAmmount().doubleValue(), transactionDtoList.get(2).getAmmount().doubleValue());
        assertEquals(transactionsEntityList.get(3).getAmmount().doubleValue(), transactionDtoList.get(3).getAmmount().doubleValue());
    }

    @Test
    void shouldReturnAListOfTransactionsFilterByIban_whenGetTransactionsEndpointIsCalled() throws Exception {
        String IBAN = "ES10123456789098765432";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions?IBAN=" + IBAN)
        ).andExpect(status().isOk()).andReturn();

        List<TransactionDto> transactionDtoList = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<TransactionDto>>() {});
        assertEquals(transactionsEntityList.size()-1, transactionDtoList.size());
        assertEquals(IBAN, transactionDtoList.get(0).getAccountIBAN());
        assertEquals(transactionsEntityList.get(0).getAmmount().doubleValue(), transactionDtoList.get(0).getAmmount().doubleValue());
        assertEquals(IBAN, transactionDtoList.get(1).getAccountIBAN());
        assertEquals(transactionsEntityList.get(1).getAmmount().doubleValue(), transactionDtoList.get(1).getAmmount().doubleValue());
        assertEquals(IBAN, transactionDtoList.get(2).getAccountIBAN());
        assertEquals(transactionsEntityList.get(2).getAmmount().doubleValue(), transactionDtoList.get(2).getAmmount().doubleValue());
    }

    @Test
    void shouldReturnAListOfTransactionsOrderDESC_whenGetTransactionsEndpointIsCalled() throws Exception {
        String sortAmmount = "DESC";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions?sortAmmount=" + sortAmmount)
        ).andExpect(status().isOk()).andReturn();

        List<TransactionDto> transactionDtoList = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<TransactionDto>>() {});
        assertEquals(transactionsEntityList.size(), transactionDtoList.size());
        assertEquals(transactionsEntityList.get(1).getAmmount().doubleValue(), transactionDtoList.get(0).getAmmount().doubleValue());
        assertEquals(transactionsEntityList.get(2).getAmmount().doubleValue(), transactionDtoList.get(1).getAmmount().doubleValue());
        assertEquals(transactionsEntityList.get(3).getAmmount().doubleValue(), transactionDtoList.get(2).getAmmount().doubleValue());
        assertEquals(transactionsEntityList.get(0).getAmmount().doubleValue(), transactionDtoList.get(3).getAmmount().doubleValue());
    }

    @Test
    void shouldReturnAListOfTransactionsFilterByIbanAndSortedASC_whenGetTransactionsEndpointIsCalled() throws Exception {
        String IBAN = "ES10123456789098765432";
        String sortAmmount = "ASC";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions?IBAN=" + IBAN + "&sortAmmount=" + sortAmmount)
        ).andExpect(status().isOk()).andReturn();

        List<TransactionDto> transactionDtoList = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<TransactionDto>>() {});
        assertEquals(transactionsEntityList.size()-1, transactionDtoList.size());
        assertEquals(IBAN, transactionDtoList.get(0).getAccountIBAN());
        assertEquals(transactionsEntityList.get(0).getAmmount().doubleValue(), transactionDtoList.get(0).getAmmount().doubleValue());
        assertEquals(IBAN, transactionDtoList.get(1).getAccountIBAN());
        assertEquals(transactionsEntityList.get(2).getAmmount().doubleValue(), transactionDtoList.get(1).getAmmount().doubleValue());
        assertEquals(IBAN, transactionDtoList.get(2).getAccountIBAN());
        assertEquals(transactionsEntityList.get(1).getAmmount().doubleValue(), transactionDtoList.get(2).getAmmount().doubleValue());
    }

}
