package com.quorum.tessera.discovery;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;

import java.util.Objects;

public class OnCreateHelperFactory implements OnCreateHelper {

    public static OnCreateHelper provider() {
        Enclave enclave = EnclaveFactory.create().enclave().get();
        NetworkStore networkStore = NetworkStore.getInstance();
        return new OnCreateHelperImpl(enclave,networkStore);
    }

    private OnCreateHelper delegate;

    protected OnCreateHelperFactory(OnCreateHelper delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    public OnCreateHelperFactory() {
        this(provider());
    }

    @Override
    public void onCreate() {
        delegate.onCreate();
    }
}
