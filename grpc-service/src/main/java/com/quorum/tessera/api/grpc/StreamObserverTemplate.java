package com.quorum.tessera.api.grpc;

import com.quorum.tessera.node.AutoDiscoveryDisabledException;
import io.grpc.stub.StreamObserver;
import java.util.Objects;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamObserverTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamObserverTemplate.class);

    private final StreamObserver observer;

    public StreamObserverTemplate(StreamObserver observer) {
        this.observer = Objects.requireNonNull(observer);
    }

    public void handle(StreamObserverCallback callback) {

        try {
            Object r = callback.execute();
            observer.onNext(r);
            observer.onCompleted();

        } catch(AutoDiscoveryDisabledException ex) {
            observer.onError(io.grpc.Status.PERMISSION_DENIED
                    .withDescription(ex.getMessage()).asRuntimeException());
        } catch (ConstraintViolationException validationError) {
            observer.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withCause(validationError)
                    .asRuntimeException());
        } catch (Throwable ex) {
            LOGGER.error(null, ex);
            observer.onError(ex);
        }
    }

}
