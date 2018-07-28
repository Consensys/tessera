package com.quorum.tessera.nacl;

public class MockNaclFacadeFactory implements NaclFacadeFactory {

    @Override
    public NaclFacade create() {
        return MockNaclFacade.INSTANCE;
    }
    
}
