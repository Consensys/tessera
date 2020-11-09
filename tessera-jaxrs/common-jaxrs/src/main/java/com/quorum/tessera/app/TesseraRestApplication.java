package com.quorum.tessera.app;

import com.quorum.tessera.api.common.BaseResource;
import com.quorum.tessera.api.common.VersionResource;
import com.quorum.tessera.api.exception.*;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.apps.TesseraApp;

import javax.ws.rs.core.Application;
import java.util.Set;

// @Api
public abstract class TesseraRestApplication extends Application implements TesseraApp {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                EnhancedPrivacyNotSupportedExceptionMapper.class,
                PrivacyViolationExceptionMapper.class,
                AutoDiscoveryDisabledExceptionMapper.class,
                DecodingExceptionMapper.class,
                DefaultExceptionMapper.class,
                EnclaveNotAvailableExceptionMapper.class,
                EntityNotFoundExceptionMapper.class,
                KeyNotFoundExceptionMapper.class,
                NotFoundExceptionMapper.class,
                SecurityExceptionMapper.class,
                TransactionNotFoundExceptionMapper.class,
                WebApplicationExceptionMapper.class,
                NodeOfflineExceptionMapper.class,
                VersionResource.class,
                BaseResource.class);
    }

    // TODO(cjh) return full swagger doc by default in case specific ApiResource method for that server hasn't been added

    @Override
    public CommunicationType getCommunicationType() {
        return CommunicationType.REST;
    }
}
