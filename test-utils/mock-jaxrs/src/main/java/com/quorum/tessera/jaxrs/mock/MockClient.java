package com.quorum.tessera.jaxrs.mock;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;

public class MockClient implements Client {
    
    private Map<String,Object> properties = new HashMap<>();
    
    private MockWebTarget webTarget = new MockWebTarget(this);
    
    @Override
    public void close() {
    }
    
    @Override
    public WebTarget target(String arg0) {
        return webTarget;
    }
    
    @Override
    public WebTarget target(URI uri) {
        return webTarget;
    }
    
    @Override
    public WebTarget target(UriBuilder uriBuilder) {
        return webTarget;
    }
    
    @Override
    public WebTarget target(Link arg0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Invocation.Builder invocation(Link arg0) {
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public SSLContext getSslContext() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public HostnameVerifier getHostnameVerifier() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Configuration getConfiguration() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Client property(String arg0, Object arg1) {
        properties.put(arg0, arg1);
        return this;
    }
    
    @Override
    public Client register(Class<?> arg0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Client register(Class<?> arg0, int arg1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Client register(Class<?> arg0, Class<?>... arg1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Client register(Class<?> arg0, Map<Class<?>, Integer> arg1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Client register(Object arg0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Client register(Object arg0, int arg1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Client register(Object arg0, Class<?>... arg1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Client register(Object arg0, Map<Class<?>, Integer> arg1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public MockWebTarget getWebTarget() {
        return webTarget;
    }
    
    
   
    
}
