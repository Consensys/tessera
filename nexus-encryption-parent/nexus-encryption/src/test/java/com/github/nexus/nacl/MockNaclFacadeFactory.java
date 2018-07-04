package com.github.nexus.nacl;

public class MockNaclFacadeFactory implements NaclFacadeFactory {

    @Override
    public NaclFacade create() {
        return MockNaclFacade.INSTANCE;
    }
    
}
