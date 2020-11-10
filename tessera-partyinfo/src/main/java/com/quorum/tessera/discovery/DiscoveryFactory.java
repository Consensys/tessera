package com.quorum.tessera.discovery;

import com.quorum.tessera.context.RuntimeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class DiscoveryFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryFactory.class);

    private static final AtomicReference<Discovery> HOLDER = new AtomicReference<>();

    /**
     * @see java.util.ServiceLoader.Provider
     */
    public static Discovery provider() {

//        if(HOLDER.get() != null) {
//            return HOLDER.get();
//        }

        final NetworkStore networkStore = NetworkStore.getInstance();
        final RuntimeContext runtimeContext = RuntimeContext.getInstance();
        final Discovery discovery;
        if(runtimeContext.isDisablePeerDiscovery()) {
            final Set<NodeUri> knownNodes = runtimeContext.getPeers().stream()
                                        .map(NodeUri::create)
                                        .collect(Collectors.toUnmodifiableSet());
            discovery = new DisabledAutoDiscovery(networkStore,knownNodes);
        } else {
            discovery = new AutoDiscovery(networkStore);
        }

       // HOLDER.set(discovery);

        return discovery;
    }



}
