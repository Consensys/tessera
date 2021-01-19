package net.consensys.tessera.migration.data;

public class NoOpEventHandler extends AbstractEventHandler {

    @Override
    public void onEvent(OrionRecordEvent event) throws Exception {}
}
