package com.rfc.transactions.configuration;

import com.rfc.transactions.model.dto.TransactionDto;
import com.rfc.transactions.model.entity.TransactionsEntity;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.dozer.loader.api.TypeMappingOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DozerMapperConfiguration {

    @Bean
    public Mapper mapper() {
        BeanMappingBuilder beanMappingBuilder = new BeanMappingBuilder() {
            @Override
            protected void configure() {
                mapping(TransactionDto.class, TransactionsEntity.class, TypeMappingOptions.dateFormat("yyyy-MM-dd"));
            }
        };
        DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
        dozerBeanMapper.addMapping(beanMappingBuilder);
        return dozerBeanMapper;
    }
}
