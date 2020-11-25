package net.consensys.tessera.b2t;

import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.service.locator.ServiceLocator;

import javax.ws.rs.ApplicationPath;
import java.util.Set;

@ApplicationPath("/")
public class B2TRestApp extends TesseraRestApplication {

    private final ServiceLocator serviceLocator;

    public B2TRestApp() {
        this(ServiceLocator.create());
    }

    public B2TRestApp(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    public Set<Object> getSingletons() {

        final Config config =
            serviceLocator.getServices().stream()
                .filter(Config.class::isInstance)
                .map(Config.class::cast)
                .findAny()
                .get();

        final PrivacyGroupManager privacyGroupManager = PrivacyGroupManager.create(config);
        final PrivacyGroupResource privacyGroupResource = new PrivacyGroupResource(privacyGroupManager);

        return Set.of(privacyGroupResource);
    }

    @Override
    public AppType getAppType() {
        return AppType.B2T;
    }
}
