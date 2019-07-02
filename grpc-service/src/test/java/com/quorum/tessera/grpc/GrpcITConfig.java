package com.quorum.tessera.grpc;

import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.transaction.TransactionManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import static org.mockito.Mockito.mock;

@Configuration
@ImportResource(locations = "classpath:tessera-grpc-spring.xml")
public class GrpcITConfig {

    @Bean(name = "transactionManager")
    public TransactionManagerImpl enclaveMediator() {
        return mock(TransactionManagerImpl.class);
    }

    @Bean
    public PartyInfoService partyInfoService() {
        return mock(PartyInfoService.class);
    }

    @Bean
    public PartyInfoParser partyInfoParser() {
        return mock(PartyInfoParser.class);
    }
}
