package com.quorum.tessera.grpc;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.grpc.api.Q2TGrpcApp;
import com.quorum.tessera.grpc.p2p.P2PGrpcApp;
import com.quorum.tessera.grpc.p2p.TesseraGrpcService;
import com.quorum.tessera.service.locator.ServiceLocator;
import io.grpc.BindableService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class GrpcAppTest {

    private ServiceLocator serviceLocator;

    private GrpcApp grpcApp;

    @Before
    public void setUp() {
        serviceLocator = mock(ServiceLocator.class);
        grpcApp =
                new GrpcApp(serviceLocator) {
                    @Override
                    public AppType getAppType() {
                        return AppType.THIRD_PARTY;
                    }
                };
        assertThat(grpcApp.getCommunicationType()).isEqualTo(CommunicationType.GRPC);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(serviceLocator);
    }

    @Test
    public void getBindableServices() {
        grpcApp.getBindableServices();
        grpcApp = new P2PGrpcApp(serviceLocator);
        grpcApp.getBindableServices();
        grpcApp = new Q2TGrpcApp(serviceLocator);
        grpcApp.getBindableServices();
        verify(serviceLocator, times(3)).getServices();
    }

    @Test
    public void createWithNoServiceLocator() {

        final Throwable throwable =
                catchThrowable(
                        () ->
                                new GrpcApp(null) {
                                    @Override
                                    public AppType getAppType() {
                                        return AppType.ADMIN;
                                    }
                                });
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void onCreateApiObjects() {
        TesseraGrpcService apiObject = new TesseraGrpcService();
        Object nonApiObject = new HashMap<>();

        when(serviceLocator.getServices()).thenReturn(Stream.of(apiObject, nonApiObject).collect(Collectors.toSet()));

        Set<BindableService> result = grpcApp.getBindableServices();
        assertThat(result).containsOnly(apiObject);
        verify(serviceLocator).getServices();
    }
}
