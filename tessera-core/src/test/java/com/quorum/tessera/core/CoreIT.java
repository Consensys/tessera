
package com.quorum.tessera.core;

import com.quorum.tessera.config.cli.CliDelegate;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.transaction.TransactionService;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:tessera-core-spring.xml")
public class CoreIT {
    
    @Inject
    private Enclave enclave;
    
    @Inject
    private TransactionService transactionService;
    
    @PersistenceContext(unitName = "tessera")
    private EntityManager entityManager;
    
    @BeforeClass
    public static void onSetup() throws Exception {
        String configPath = CoreIT.class.getResource("/config1.json").getPath();
        CliDelegate.INSTANCE.execute("-configfile",configPath);
}
    
    @Test
    public void doStuff() throws Exception {
        assertThat(enclave).isNotNull();
        assertThat(transactionService).isNotNull();
        assertThat(entityManager).isNotNull();
    }
    
}
