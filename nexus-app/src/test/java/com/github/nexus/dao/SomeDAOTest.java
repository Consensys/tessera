
package com.github.nexus.dao;

import com.github.nexus.entity.SomeEntity;
import javax.inject.Inject;
import javax.transaction.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@Transactional
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = JpaConfig.class)
public class SomeDAOTest {

    @Inject
    private SomeDAO someDAO;
    
    public SomeDAOTest() {
    }

    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    
    @Test
    public void doStuff() {
        assertThat(someDAO).isNotNull();
        SomeEntity someEntity = new SomeEntity();
        
        someDAO.save(someEntity);
                
        assertThat(someEntity.getId()).isNotNull();
    }


}
