
package com.github.nexus.nacl.kalium;

import com.github.nexus.nacl.NaclFacade;
import com.github.nexus.nacl.NaclFacadeFactory;
import org.abstractj.kalium.NaCl;


public class KaliumFactory implements NaclFacadeFactory {

    @Override
    public NaclFacade create() {
        NaCl.Sodium sodium = NaCl.sodium();
        return new Kalium(sodium);
    }
    
}
