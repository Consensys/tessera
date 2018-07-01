package com.github.nexus.config;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class PublicKeyTest {

    @Test
    public void create() throws Exception {
    
       Method createMethod =  PublicKey.class.getDeclaredMethod("create");
       createMethod.setAccessible(true);
       
       PublicKey result = (PublicKey) createMethod.invoke(null);
       assertThat(result).isNotNull();

    }
    
    @Test
    public void readPathDataWhenValueIsNull() throws Exception {

        final URI uri = ClassLoader.getSystemResource("keyfile.txt").toURI();

        final PublicKey publicKey = new PublicKey(Paths.get(uri), null);
        final Field value = PublicKey.class.getDeclaredField("path");
        value.setAccessible(true);

        value.set(publicKey, Paths.get(uri));

        assertThat(publicKey.getValue()).isEqualTo("SOMEDATA");

    }
    
    @Test
    public void getValueLazyLoad() throws Exception {
      
            URI uri = getClass().getResource("/keyfile.txt").toURI();
                    
            Path path = Paths.get(uri);
        
            PublicKey publicKey = new PublicKey(path, null);
            
            String result = publicKey.getValue();
            
            assertThat(result).isEqualTo("SOMEDATA");
            
            
        
    }


}
