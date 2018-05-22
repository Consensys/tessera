
package com.github.nexus.app;

import com.github.nexus.api.PublicAPI;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

@Path("/")
public class Nexus extends Application {

    @Override
    public Set<Object> getSingletons() {
        return Stream.of(new PublicAPI(),new DefaultExceptionMapper())
                .collect(Collectors.toSet());
    }
    
}
