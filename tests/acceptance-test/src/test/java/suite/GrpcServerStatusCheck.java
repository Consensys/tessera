package suite;

import com.google.protobuf.Empty;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.grpc.api.APITransactionGrpc;
import com.quorum.tessera.grpc.p2p.P2PTransactionGrpc;
import com.quorum.tessera.grpc.p2p.TesseraGrpc;
import com.quorum.tessera.grpc.p2p.UpCheckMessage;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcServerStatusCheck implements ServerStatusCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServerStatusCheck.class);

    private final AppType appType;

    private final URL url;

    public GrpcServerStatusCheck(URL url, AppType appType) {
        this.url = url;
        this.appType = appType;
    }

    @Override
    public boolean checkStatus() {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(url.getHost(), url.getPort())
                .usePlaintext()
                .build();

        try{


            if (appType == AppType.Q2T) {

                CallOptions callOptions = APITransactionGrpc.newBlockingStub(channel)
                        .getCallOptions();
                
                LOGGER.info("{} callOptions result {} ", appType, callOptions);
               return true;
            }


            if (appType == AppType.P2P) {

                UpCheckMessage result = TesseraGrpc.newBlockingStub(channel)
                        .getUpCheck(Empty.getDefaultInstance());
                LOGGER.info("{} Upcheck result {} ", appType, result.getUpCheck());

                CallOptions callOptions = P2PTransactionGrpc.newBlockingStub(channel)
                        .getCallOptions();
                
                LOGGER.info("{} callOptions result {} ", appType, callOptions);

                return true;
            }

            throw new UnsupportedOperationException(appType + " not supported");
        } catch (Exception ex) {
            LOGGER.info("cannot connect to {}. {}", this, ex.getMessage());
            return false;
        } finally {
            channel.shutdown();
        }

    }

    @Override
    public String toString() {
        return "GrpcServerStatusCheck{" + "appType=" + appType + ", url=" + url + '}';
    }

}
