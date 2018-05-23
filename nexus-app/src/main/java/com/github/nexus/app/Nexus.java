package com.github.nexus.app;

import com.github.nexus.service.locator.ServiceLocator;
import java.util.Set;
import javax.ws.rs.core.Application;

public class Nexus extends Application {
    
    @Override
    public Set<Object> getSingletons() {
       
       return ServiceLocator.create().getServices();
    }
    
}
