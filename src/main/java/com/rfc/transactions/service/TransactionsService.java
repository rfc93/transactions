package com.rfc.transactions.service;

import com.rfc.transactions.model.dto.SortDirectionDto;
import com.rfc.transactions.model.entity.TransactionsEntity;
import com.rfc.transactions.repository.TransactionsRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

import static com.rfc.transactions.utils.Cons.AMMOUNT_FIELD;

@AllArgsConstructor
@Service
public class TransactionsService {

    private TransactionsRepository transactionsRepository;

    public TransactionsEntity getTransactionByReference(String reference) {
        return transactionsRepository.findByReference(reference).orElse(null);
    }

    public TransactionsEntity saveTransaction(TransactionsEntity transactionsEntity) {
        return transactionsRepository.save(transactionsEntity);
    }

    public List<TransactionsEntity> getFilterTransactions(String IBAN, SortDirectionDto sortAmmount) {
        List<TransactionsEntity> transactionsEntityList;
        Sort generatedSort = generateSortBy(sortAmmount, AMMOUNT_FIELD);
        if(!StringUtils.isEmpty(IBAN)) {
            transactionsEntityList = transactionsRepository.findByAccountIBAN(IBAN, generatedSort);
        }
        else {
            transactionsEntityList = transactionsRepository.findAll(generatedSort);
        }
        return transactionsEntityList;
    }

    private Sort generateSortBy(SortDirectionDto sortAmmount, String fieldToSort) {
        if(Objects.nonNull(sortAmmount)) {
            return sortAmmount == SortDirectionDto.ASC ? Sort.by(Sort.Direction.ASC, fieldToSort) : Sort.by(Sort.Direction.DESC, fieldToSort);
        }
        return Sort.unsorted();
    }

}
