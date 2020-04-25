package com.rfc.transactions.service;

import com.rfc.transactions.Exceptions.AccountNotFoundException;
import com.rfc.transactions.model.entity.AccountEntity;
import com.rfc.transactions.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldFindAnAccountEntity_whenIdExists() {
        when(accountRepository.findById(eq(1))).thenReturn(Optional.of(new AccountEntity(1, BigDecimal.ONE)));

        AccountEntity accountEntity = accountService.getAccountById();

        assertNotNull(accountEntity);
    }

    @Test
    void shouldThrowAnAccountNotFoundException_whenIdNotExists() {
        when(accountRepository.findById(eq(1))).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById());
    }

    @Test
    void shouldSaveAnAccountEntity() {
        AccountEntity accountEntityToSave = new AccountEntity(BigDecimal.TEN);
        AccountEntity accountEntitySaved = new AccountEntity(1, accountEntityToSave.getBalance());
        when(accountRepository.save(eq(accountEntityToSave))).thenReturn(accountEntitySaved);

        accountEntitySaved = accountService.updateAccount(accountEntityToSave);

        verify(accountRepository).save(any());
        assertNotNull(accountEntitySaved);
    }
}
