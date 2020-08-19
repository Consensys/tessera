package com.quorum.tessera.discovery;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.version.ApiVersion;

import java.util.Objects;
import java.util.Optional;

public class OnCreateHelperImpl implements OnCreateHelper {

    private final Enclave enclave;

    private final NetworkStore networkStore;

    protected OnCreateHelperImpl(Enclave enclave, NetworkStore networkStore) {
        this.enclave = Objects.requireNonNull(enclave);
        this.networkStore = Objects.requireNonNull(networkStore);
    }

    @Override
    public void onCreate() {

        RuntimeContext runtimeContext = RuntimeContext.getInstance();
        runtimeContext.getPeers().stream()
            .map(NodeUri::create)
            .map(ActiveNode.Builder.create()::withUri)
            .map(ActiveNode.Builder::build)
            .forEach(networkStore::store);

        final NodeUri nodeUri = Optional.of(runtimeContext)
            .map(RuntimeContext::getP2pServerUri)
            .map(NodeUri::create)
            .get();

        ActiveNode thisNode = ActiveNode.Builder.create()
            .withUri(nodeUri)
            .withKeys(enclave.getPublicKeys())
            .withUri(nodeUri)
            .withSupportedVersions(ApiVersion.versions())
            .build();

        networkStore.store(thisNode);

    }


}
