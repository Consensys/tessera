
package com.quorum.tessera.grpc;

import com.quorum.tessera.transaction.TransactionManagerImpl;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import static org.mockito.Mockito.mock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;


@Configuration
@ImportResource(locations="classpath:tessera-grpc-spring.xml")
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
