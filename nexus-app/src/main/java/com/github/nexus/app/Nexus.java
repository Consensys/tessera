
package com.github.nexus.app;

import com.github.nexus.api.PublicAPI;
import java.util.Collections;
import java.util.Set;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

@Path("/")
public class Nexus extends Application {

    @Override
    public Set<Object> getSingletons() {
        
        return Collections.singleton(new PublicAPI());
    }
    
}
