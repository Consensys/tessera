package com.quorum.tessera.core;

import com.quorum.tessera.transaction.TransactionManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:tessera-core-spring.xml")
public class CoreIT {

    @Inject private TransactionManager transactionManager;

    @PersistenceContext(unitName = "tessera")
    private EntityManager entityManager;

    @BeforeClass
    public static void onSetup() throws Exception {
        String configPath = CoreIT.class.getResource("/config1.json").getPath();
        // TODO(cjh) introduces a circular dependency between jaxrs-client module and picocli module
        //        PicoCliDelegate picoCliDelegate = new PicoCliDelegate();
        //        picoCliDelegate.execute("-configfile", configPath);
    }

    @Test
    public void doStuff() throws Exception {
        assertThat(transactionManager).isNotNull();
        assertThat(entityManager).isNotNull();
    }
}
