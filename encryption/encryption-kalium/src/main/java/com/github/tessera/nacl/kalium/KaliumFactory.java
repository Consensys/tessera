package com.github.tessera.nacl.kalium;

import com.github.tessera.nacl.NaclFacade;
import com.github.tessera.nacl.NaclFacadeFactory;
import org.abstractj.kalium.NaCl;

public class KaliumFactory implements NaclFacadeFactory {

    @Override
    public NaclFacade create() {
        final NaCl.Sodium sodium = NaCl.sodium();

        return new Kalium(sodium);
    }

}
