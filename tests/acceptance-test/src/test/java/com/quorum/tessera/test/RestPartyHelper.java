package com.quorum.tessera.test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import suite.ExecutionContext;

public class RestPartyHelper implements PartyHelper {
    
    private final ExecutionContext executionContext;
    
    private final List<Party> parties = new ArrayList<>();
    
    private final String acceptanceTestsDBType;

    public RestPartyHelper(ExecutionContext executionContext) {
        this.executionContext = executionContext;
        this.acceptanceTestsDBType = executionContext.getDbType().name().toLowerCase();
    }

    public RestPartyHelper() {
        this(ExecutionContext.currentContext());
    }
    

    
    
    private void initParties(){
        parties.clear();

        String prefix = "/rest/" + this.acceptanceTestsDBType + "/";
        parties.add(new Party("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", getClass().getResource(prefix + "config1.json"), "A"));
        parties.add(new Party("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=", getClass().getResource(prefix + "config2.json"), "B"));
        parties.add(new Party("giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=", getClass().getResource(prefix + "config3.json"), "C"));
        parties.add(new Party("Tj8xg/HpsYmh7Te3UerzlLx1HgpWVOGq25ZgbwaPNVM=", getClass().getResource(prefix + "config4.json"), "D"));
    }

    @Override
    public Stream<Party> getParties() {
        if(parties.isEmpty()) {
            initParties();
        }
        return parties.stream();
    }

}
