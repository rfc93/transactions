package com.rfc.transactions.service;

import com.rfc.transactions.Exceptions.AccountNotFoundException;
import com.rfc.transactions.model.entity.AccountEntity;
import com.rfc.transactions.repository.AccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import static com.rfc.transactions.utils.Cons.ACCOUNT_NOT_FOUND;

@AllArgsConstructor
@Service
public class AccountService {

    private AccountRepository accountRepository;

    public AccountEntity getAccountById() {
        return accountRepository.findById(1).orElseThrow(() -> new AccountNotFoundException(ACCOUNT_NOT_FOUND));
    }

    public AccountEntity updateAccount(AccountEntity accountEntity) {
        return accountRepository.save(accountEntity);
    }


}
