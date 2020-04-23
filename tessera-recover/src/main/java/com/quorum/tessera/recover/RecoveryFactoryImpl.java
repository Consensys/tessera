package com.quorum.tessera.recover;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.PartyInfoService;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class RecoveryFactoryImpl implements RecoveryFactory {
    @Override
    public Recovery create(Config config) {


        String username = config.getJdbcConfig().getUsername();
        String password = config.getJdbcConfig().getPassword();
        String url = config.getJdbcConfig().getUrl();

        Map properties = new HashMap();
        properties.put("javax.persistence.jdbc.url",url);
        properties.put("javax.persistence.jdbc.user",username);
        properties.put("javax.persistence.jdbc.password",password);

        EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory("tessera-recover",properties);


        PartyInfoService partyInfoService = PartyInfoServiceFactory.create(config);

        return new RecoveryImpl(entityManagerFactory,partyInfoService);
    }
}
