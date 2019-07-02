package com.quorum.tessera.app;

import com.quorum.tessera.api.common.UpCheckResource;
import com.quorum.tessera.api.common.VersionResource;
import com.quorum.tessera.api.exception.AutoDiscoveryDisabledExceptionMapper;
import com.quorum.tessera.api.exception.DecodingExceptionMapper;
import com.quorum.tessera.api.exception.DefaultExceptionMapper;
import com.quorum.tessera.api.exception.EnclaveNotAvailableExceptionMapper;
import com.quorum.tessera.api.exception.EntityNotFoundExceptionMapper;
import com.quorum.tessera.api.exception.KeyNotFoundExceptionMapper;
import com.quorum.tessera.api.exception.NotFoundExceptionMapper;
import com.quorum.tessera.api.exception.SecurityExceptionMapper;
import com.quorum.tessera.api.exception.TransactionNotFoundExceptionMapper;
import com.quorum.tessera.api.exception.WebApplicationExceptionMapper;
import com.quorum.tessera.config.apps.TesseraApp;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.Application;

public abstract class TesseraRestApplication extends Application implements TesseraApp {

    @Override
    public Set<Class<?>> getClasses() {
        // TODO: use new java 9+ api Sets once we move from java 8.
        return Stream.of(
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
                        UpCheckResource.class,
                        VersionResource.class)
                .collect(Collectors.toSet());
    }
}
