
package com.github.nexus.config;

import java.lang.reflect.Method;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class PrivateKeyTest {
    
    @Test
    public void create() throws Exception {
    
       Method createMethod =  PrivateKey.class.getDeclaredMethod("create");
       createMethod.setAccessible(true);
       
       PrivateKey result = (PrivateKey) createMethod.invoke(null);
       assertThat(result).isNotNull();

    }
    
}
