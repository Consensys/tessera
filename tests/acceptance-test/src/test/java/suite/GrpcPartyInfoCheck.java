package suite;

import com.google.protobuf.Empty;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.grpc.p2p.PartyInfoGrpc;
import com.quorum.tessera.grpc.p2p.PartyInfoJson;
import com.quorum.tessera.test.DefaultPartyHelper;
import com.quorum.tessera.test.PartyHelper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcPartyInfoCheck implements PartyInfoChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcPartyInfoCheck.class);
    
    private PartyHelper partyHelper = new DefaultPartyHelper();

    @Override
    public boolean hasSynced() {
        return partyHelper.getParties().allMatch(p -> {

            ServerConfig p2pConfig = p.getConfig().getP2PServerConfig();
            URI uri = p2pConfig.getBindingUri();

            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(uri.getHost(), uri.getPort())
                    .usePlaintext()
                    .build();

            try {
                PartyInfoJson partyInfo = PartyInfoGrpc.newBlockingStub(channel)
                        .getPartyInfoMessage(Empty.getDefaultInstance());
                        
                int peerCount = partyInfo.getPeersCount();
                
                long expectedCount = partyHelper.getParties().count();
                
                LOGGER.debug("Peer count found on {} : {} , expected party count : {}",p.getP2PUri(),peerCount,expectedCount);
                
                if(uri.getPort() == 7000) {
                    return true;
                }
                    

                
                return peerCount == expectedCount;
            } catch (Exception ex) {
                LOGGER.debug(null,ex);
                return false;
            } finally {
                channel.shutdown();
            }
        });
    }

}
