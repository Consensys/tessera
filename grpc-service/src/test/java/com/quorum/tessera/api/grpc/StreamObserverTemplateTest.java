package com.quorum.tessera.api.grpc;

import com.quorum.tessera.node.AutoDiscoveryDisabledException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolationException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class StreamObserverTemplateTest {

    private StreamObserver observer;

    private StreamObserverTemplate template;

    @Before
    public void onSetup() {
        observer = mock(StreamObserver.class);
        template = new StreamObserverTemplate(observer);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void executeNoError() {

        Object outcome = "HAPPY";

        template.handle(() -> {
            return outcome;
        });

        verify(observer).onNext(outcome);
        verify(observer).onCompleted();

    }

    @Test
    public void executeValidationError() {

        List<StatusRuntimeException> results = new ArrayList<>();
        doAnswer((iom) -> {
            results.add(iom.getArgument(0));
            return null;
        }).when(observer)
                .onError(any(StatusRuntimeException.class));

        ConstraintViolationException exception = mock(ConstraintViolationException.class);

        template.handle(() -> {
            throw exception;
        });

        StatusRuntimeException result = results.stream().findAny().get();

        assertThat(result.getStatus().getCode()).isEqualTo(Status.INVALID_ARGUMENT.getCode());

        assertThat(result.getStatus().getCause()).isSameAs(exception);

        verify(observer).onError(result);

    }

    @Test
    public void executeOtherError() {

        Throwable exception = new Throwable("OUCH");

        template.handle(() -> {
            throw exception;
        });


        verify(observer).onError(exception);

    }
    
    
    @Test
    public void executeAutoDiscoveryDisabled() {
    
        List<StatusRuntimeException> results = new ArrayList<>();
        doAnswer((iom) -> {
            results.add(iom.getArgument(0));
            return null;
        }).when(observer)
                .onError(any(StatusRuntimeException.class));

        final String exceptionMessage = "Sorry Dave I cant let you do that";
        
        AutoDiscoveryDisabledException exception = mock(AutoDiscoveryDisabledException.class);
        when(exception.getMessage()).thenReturn(exceptionMessage);
        

        template.handle(() -> {
            throw exception;
        });

        StatusRuntimeException result = results.stream().findAny().get();

        assertThat(result.getStatus().getCode()).isEqualTo(Status.PERMISSION_DENIED.getCode());

        assertThat(result.getStatus().getDescription()).isEqualTo(exceptionMessage);

        verify(observer).onError(result);
    }
}
