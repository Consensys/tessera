package com.quorum.tessera.config.cli;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyUpdateCommandFactoryTest {

    private KeyUpdateCommandFactory keyUpdateCommandFactory = new KeyUpdateCommandFactory();

    @Test
    public void createKeyUpdateCommand() throws Exception {
        KeyUpdateCommand command = keyUpdateCommandFactory.create(KeyUpdateCommand.class);
        assertThat(command).isNotNull();
    }

    @Test
    public void createOther() throws Exception {
        TesseraCommand command = keyUpdateCommandFactory.create(TesseraCommand.class);
        assertThat(command).isNotNull();
    }

}
