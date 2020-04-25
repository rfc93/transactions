package com.rfc.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rfc.transactions.model.dto.StatusDto;
import com.rfc.transactions.model.dto.TransactionStatusDto;
import com.rfc.transactions.model.entity.TransactionsEntity;
import com.rfc.transactions.repository.AccountRepository;
import com.rfc.transactions.repository.TransactionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
@ExtendWith(SpringExtension.class)
public class BusinessAcceptanceTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        transactionsRepository.deleteAll();
        accountRepository.deleteAll();
    }

    //A
    @Test
    void givenATransactionThatIsNotStoredInOurSystem_thenTheSystemReturnTheStatusInvalid() throws Exception {
        String reference = "TxReference";
        String channel = "CLIENT";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions/status?reference=" + reference + "&channel=" + channel)
        ).andExpect(status().isOk()).andReturn();

        TransactionStatusDto transactionStatusDto = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionStatusDto.class);
        assertNotNull(transactionStatusDto);
        assertEquals(reference, transactionStatusDto.getReference());
        assertEquals(StatusDto.INVALID, transactionStatusDto.getStatus());
        assertNull(transactionStatusDto.getAmmount());
        assertNull(transactionStatusDto.getFee());
    }

    //B
    @Test
    void givenATransactionStoredInOurSystemWithChannelClientOrAtmAndDateBeforeToday_thenTheSystemReturnTheStatusSettledAndAmountSubstractingTheFee() throws Exception {
        TransactionsEntity transactionsEntity = new TransactionsEntity("TxReference1", "ES10123456789098765432", Date.from(Instant.now().minusSeconds(86400)), BigDecimal.TEN, BigDecimal.ONE, "Tx1 Description");
        transactionsRepository.save(transactionsEntity);
        String reference = transactionsEntity.getReference();
        String channel = "CLIENT";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions/status?reference=" + reference + "&channel=" + channel)
        ).andExpect(status().isOk()).andReturn();

        TransactionStatusDto transactionStatusDto = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionStatusDto.class);
        assertNotNull(transactionStatusDto);
        assertEquals(reference, transactionStatusDto.getReference());
        assertEquals(StatusDto.SETTLED, transactionStatusDto.getStatus());
        assertEquals(transactionsEntity.getAmmount().subtract(transactionsEntity.getFee()).doubleValue(), transactionStatusDto.getAmmount().doubleValue());
        assertNull(transactionStatusDto.getFee());
    }

    //C
    @Test
    void givenATransactionStoredInOurSystemWithChannelInternalAndDateBeforeToday_thenTheSystemReturnTheStatusSettledAndAmountAndFee() throws Exception {
        TransactionsEntity transactionsEntity = new TransactionsEntity("TxReference1", "ES10123456789098765432", Date.from(Instant.now().minusSeconds(86400)), BigDecimal.TEN, BigDecimal.ONE, "Tx1 Description");
        transactionsRepository.save(transactionsEntity);
        String reference = transactionsEntity.getReference();
        String channel = "INTERNAL";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions/status?reference=" + reference + "&channel=" + channel)
        ).andExpect(status().isOk()).andReturn();

        TransactionStatusDto transactionStatusDto = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionStatusDto.class);
        assertNotNull(transactionStatusDto);
        assertEquals(reference, transactionStatusDto.getReference());
        assertEquals(StatusDto.SETTLED, transactionStatusDto.getStatus());
        assertEquals(transactionsEntity.getAmmount().doubleValue(), transactionStatusDto.getAmmount().doubleValue());
        assertEquals(transactionsEntity.getFee().doubleValue(), transactionStatusDto.getFee().doubleValue());
    }

    //D
    @Test
    void givenATransactionStoredInOurSystemWithChannelClientOrAtmAndDateEqualsToday_thenTheSystemReturnTheStatusPendingAndAmountsubstractingFee() throws Exception {
        TransactionsEntity transactionsEntity = new TransactionsEntity("TxReference1", "ES10123456789098765432", Date.from(Instant.now()), BigDecimal.TEN, BigDecimal.ONE, "Tx1 Description");
        transactionsRepository.save(transactionsEntity);
        String reference = transactionsEntity.getReference();
        String channel = "ATM";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions/status?reference=" + reference + "&channel=" + channel)
        ).andExpect(status().isOk()).andReturn();

        TransactionStatusDto transactionStatusDto = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionStatusDto.class);
        assertNotNull(transactionStatusDto);
        assertEquals(reference, transactionStatusDto.getReference());
        assertEquals(StatusDto.PENDING, transactionStatusDto.getStatus());
        assertEquals(transactionsEntity.getAmmount().subtract(transactionsEntity.getFee()).doubleValue(), transactionStatusDto.getAmmount().doubleValue());
        assertNull(transactionStatusDto.getFee());
    }

    //E
    @Test
    void givenATransactionStoredInOurSystemWithChannelInternalAndDateEqualsToday_thenTheSystemReturnTheStatusPendingAndAmountAndFee() throws Exception {
        TransactionsEntity transactionsEntity = new TransactionsEntity("TxReference1", "ES10123456789098765432", Date.from(Instant.now()), BigDecimal.TEN, BigDecimal.ONE, "Tx1 Description");
        transactionsRepository.save(transactionsEntity);
        String reference = transactionsEntity.getReference();
        String channel = "INTERNAL";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions/status?reference=" + reference + "&channel=" + channel)
        ).andExpect(status().isOk()).andReturn();

        TransactionStatusDto transactionStatusDto = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionStatusDto.class);
        assertNotNull(transactionStatusDto);
        assertEquals(reference, transactionStatusDto.getReference());
        assertEquals(StatusDto.PENDING, transactionStatusDto.getStatus());
        assertEquals(transactionsEntity.getAmmount().doubleValue(), transactionStatusDto.getAmmount().doubleValue());
        assertEquals(transactionsEntity.getFee().doubleValue(), transactionStatusDto.getFee().doubleValue());
    }

    //F
    @Test
    void givenATransactionStoredInOurSystemWithChannelClientAndDateAfterToday_thenTheSystemReturnTheStatusFutureAndAmountSubstractingFee() throws Exception {
        TransactionsEntity transactionsEntity = new TransactionsEntity("TxReference1", "ES10123456789098765432", Date.from(Instant.now().plusSeconds(86400)), BigDecimal.TEN, BigDecimal.ONE, "Tx1 Description");
        transactionsRepository.save(transactionsEntity);
        String reference = transactionsEntity.getReference();
        String channel = "CLIENT";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions/status?reference=" + reference + "&channel=" + channel)
        ).andExpect(status().isOk()).andReturn();

        TransactionStatusDto transactionStatusDto = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionStatusDto.class);
        assertNotNull(transactionStatusDto);
        assertEquals(reference, transactionStatusDto.getReference());
        assertEquals(StatusDto.FUTURE, transactionStatusDto.getStatus());
        assertEquals(transactionsEntity.getAmmount().subtract(transactionsEntity.getFee()).doubleValue(), transactionStatusDto.getAmmount().doubleValue());
        assertNull(transactionStatusDto.getFee());
    }

    //G
    @Test
    void givenATransactionStoredInOurSystemWithChannelAtmAndDateAfterToday_thenTheSystemReturnTheStatusPendingAndAmountSubstractingFee() throws Exception {
        TransactionsEntity transactionsEntity = new TransactionsEntity("TxReference1", "ES10123456789098765432", Date.from(Instant.now().plusSeconds(86400)), BigDecimal.TEN, BigDecimal.ONE, "Tx1 Description");
        transactionsRepository.save(transactionsEntity);
        String reference = transactionsEntity.getReference();
        String channel = "ATM";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions/status?reference=" + reference + "&channel=" + channel)
        ).andExpect(status().isOk()).andReturn();

        TransactionStatusDto transactionStatusDto = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionStatusDto.class);
        assertNotNull(transactionStatusDto);
        assertEquals(reference, transactionStatusDto.getReference());
        assertEquals(StatusDto.PENDING, transactionStatusDto.getStatus());
        assertEquals(transactionsEntity.getAmmount().subtract(transactionsEntity.getFee()).doubleValue(), transactionStatusDto.getAmmount().doubleValue());
        assertNull(transactionStatusDto.getFee());
    }

    //H
    @Test
    void givenATransactionStoredInOurSystemWithChannelInternalAndDateAfterToday_thenTheSystemReturnTheStatusFutureAndAmountAndFee() throws Exception {
        TransactionsEntity transactionsEntity = new TransactionsEntity("TxReference1", "ES10123456789098765432", Date.from(Instant.now().plusSeconds(86400)), BigDecimal.TEN, BigDecimal.ONE, "Tx1 Description");
        transactionsRepository.save(transactionsEntity);
        String reference = transactionsEntity.getReference();
        String channel = "INTERNAL";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions/status?reference=" + reference + "&channel=" + channel)
        ).andExpect(status().isOk()).andReturn();

        TransactionStatusDto transactionStatusDto = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionStatusDto.class);
        assertNotNull(transactionStatusDto);
        assertEquals(reference, transactionStatusDto.getReference());
        assertEquals(StatusDto.FUTURE, transactionStatusDto.getStatus());
        assertEquals(transactionsEntity.getAmmount().doubleValue(), transactionStatusDto.getAmmount().doubleValue());
        assertEquals(transactionsEntity.getFee().doubleValue(), transactionStatusDto.getFee().doubleValue());
    }
}
