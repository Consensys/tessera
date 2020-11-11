import com.quorum.tessera.discovery.DiscoveryHelperProvider;
import com.quorum.tessera.discovery.DiscoveryProvider;
import com.quorum.tessera.discovery.NetworkStoreProvider;

module tessera.tessera.partyinfo.main {
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires tessera.config.main;
    requires tessera.enclave.enclave.api.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.shared.main;
    requires tessera.tessera.context.main;

    exports com.quorum.tessera.discovery;
    exports com.quorum.tessera.partyinfo;
    exports com.quorum.tessera.partyinfo.node;

    uses com.quorum.tessera.partyinfo.P2pClientFactory;
    uses com.quorum.tessera.discovery.NetworkStore;
    uses com.quorum.tessera.enclave.EnclaveFactory;

    uses com.quorum.tessera.discovery.DiscoveryHelper;
    uses com.quorum.tessera.discovery.Discovery;

    provides com.quorum.tessera.discovery.Discovery with
        DiscoveryProvider;

    provides com.quorum.tessera.discovery.DiscoveryHelper
        with DiscoveryHelperProvider;

    provides com.quorum.tessera.discovery.EnclaveKeySynchroniser with
        com.quorum.tessera.discovery.EnclaveKeySynchroniserProvider;

    provides com.quorum.tessera.discovery.NetworkStore with
        NetworkStoreProvider;


}
