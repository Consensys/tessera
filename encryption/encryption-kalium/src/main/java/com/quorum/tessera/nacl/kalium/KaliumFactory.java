package com.quorum.tessera.nacl.kalium;

import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.NaclFacadeFactory;
import org.abstractj.kalium.NaCl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KaliumFactory implements NaclFacadeFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(KaliumFactory.class);

    @Override
    public NaclFacade create() {
        LOGGER.debug("Creating a Kalium implementation of NaclFacadeFactory");

        final NaCl.Sodium sodium = NaCl.sodium();

        return new Kalium(sodium);
    }

}
