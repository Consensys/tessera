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

public class GrpcPartyInfoCheck implements PartyInfoChecker {

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

                return partyInfo.getPeersCount() == partyHelper.getParties().count();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            } finally {
                channel.shutdown();
            }
        });
    }

}
