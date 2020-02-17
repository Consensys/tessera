package com.quorum.tessera.app;

import com.quorum.tessera.api.common.ApiResource;
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
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.apps.TesseraApp;
import io.swagger.annotations.Api;

import java.util.Set;
import javax.ws.rs.core.Application;

@Api
public abstract class TesseraRestApplication extends Application implements TesseraApp {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
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
                VersionResource.class,
                ApiResource.class
        );

    }

    @Override
    public CommunicationType getCommunicationType() {
        return CommunicationType.REST;
    }
}
