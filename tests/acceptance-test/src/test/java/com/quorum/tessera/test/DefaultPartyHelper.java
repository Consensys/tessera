package com.quorum.tessera.test;

import com.quorum.tessera.config.CommunicationType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import suite.ExecutionContext;
import suite.SocketType;


public class DefaultPartyHelper implements PartyHelper {

    private final List<Party> parties = new ArrayList<>();

    public DefaultPartyHelper() {
        
        ExecutionContext executionContext = ExecutionContext.currentContext();
        
        CommunicationType ct = executionContext.getCommunicationType();
        DBType dbType = executionContext.getDbType();
        SocketType socketType = executionContext.getSocketType();
        
        String prefix = "/"+ ct.name().toLowerCase() +"/"+ socketType.name().toLowerCase() +"/"+ dbType.name().toLowerCase() + "/";

        parties.add(new Party("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", getClass().getResource(prefix + "config1.json"), "A"));
        parties.add(new Party("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=", getClass().getResource(prefix + "config2.json"), "B"));
        parties.add(new Party("giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=", getClass().getResource(prefix + "config3.json"), "C"));
        parties.add(new Party("Tj8xg/HpsYmh7Te3UerzlLx1HgpWVOGq25ZgbwaPNVM=", getClass().getResource(prefix + "config4.json"), "D"));
    }
    
    @Override
    public Stream<Party> getParties() {
        return parties.stream();
    }
    
}
