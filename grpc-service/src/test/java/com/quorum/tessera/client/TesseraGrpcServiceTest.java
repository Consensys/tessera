package com.quorum.tessera.client;

import com.google.protobuf.Empty;
import com.quorum.tessera.api.grpc.TesseraGrpcService;
import com.quorum.tessera.api.grpc.model.UpCheckMessage;
import com.quorum.tessera.api.grpc.model.VersionMessage;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class TesseraGrpcServiceTest {

    private TesseraGrpcService service = new TesseraGrpcService();

    @Mock
    private StreamObserver<VersionMessage> versionResponseObserver;

    @Mock
    private StreamObserver<UpCheckMessage> upCheckMessageStreamObserver;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(versionResponseObserver, upCheckMessageStreamObserver);
    }

    @Test
    public void testVersion() {
        service.getVersion(Empty.getDefaultInstance(), versionResponseObserver);
        ArgumentCaptor<VersionMessage> responseCaptor = ArgumentCaptor.forClass(VersionMessage.class);
        verify(versionResponseObserver).onNext(responseCaptor.capture());
        VersionMessage message = responseCaptor.getValue();
        assertThat(message).isNotNull();
        assertThat(message.getVersion()).isEqualTo("0.6");
        verify(versionResponseObserver).onCompleted();
    }

    @Test
    public void testUpCheck() {
        service.getUpCheck(Empty.getDefaultInstance(), upCheckMessageStreamObserver);
        ArgumentCaptor<UpCheckMessage> responseCaptor = ArgumentCaptor.forClass(UpCheckMessage.class);
        verify(upCheckMessageStreamObserver).onNext(responseCaptor.capture());
        UpCheckMessage message = responseCaptor.getValue();
        assertThat(message).isNotNull();
        assertThat(message.getUpCheck()).isEqualTo("I'm up!");
        verify(upCheckMessageStreamObserver).onCompleted();

    }

}
