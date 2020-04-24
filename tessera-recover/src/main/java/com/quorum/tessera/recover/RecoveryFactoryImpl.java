package com.quorum.tessera.recover;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PartyInfoServiceFactory;
import com.quorum.tessera.sync.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class RecoveryFactoryImpl implements RecoveryFactory {
    @Override
    public Recovery create(Config config) {


        final String username = config.getJdbcConfig().getUsername();
        final String password = config.getJdbcConfig().getPassword();
        final String url = config.getJdbcConfig().getUrl();

        Map properties = new HashMap();
        properties.put("javax.persistence.jdbc.url",url);
        properties.put("javax.persistence.jdbc.user",username);
        properties.put("javax.persistence.jdbc.password",password);
        properties.put("javax.persistence.schema-generation.database.action","drop-and-create");
        properties.put("eclipselink.logging.logger","org.eclipse.persistence.logging.slf4j.SLF4JLogger");
        properties.put("eclipselink.logging.level","FINE");
        properties.put("eclipselink.logging.parameters","true");
        properties.put("eclipselink.logging.level.sql","FINE");

        EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory("tessera-recover",properties);

        PartyInfoServiceFactory partyInfoServiceFactory = PartyInfoServiceFactory.create(config);

        PartyInfoService partyInfoService = partyInfoServiceFactory.partyInfoService();

        TransactionRequester transactionRequester = TransactionRequesterFactory.newFactory().createBatchTransactionRequester(config);

        return new RecoveryImpl(entityManagerFactory,partyInfoService,transactionRequester);
    }
}
