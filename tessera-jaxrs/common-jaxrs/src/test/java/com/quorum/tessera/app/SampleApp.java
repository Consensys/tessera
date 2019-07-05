
package com.quorum.tessera.app;

import com.quorum.tessera.config.AppType;
import java.util.Arrays;


public class SampleApp extends TesseraRestApplication {

    @Override
    public AppType getAppType() {
        return Arrays.stream(AppType.values()).findAny().get();
    }
     
}
