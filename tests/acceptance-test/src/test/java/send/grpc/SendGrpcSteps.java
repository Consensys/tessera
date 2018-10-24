package send.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.quorum.tessera.grpc.api.APITransactionGrpc;
import com.quorum.tessera.grpc.api.ReceiveRequest;
import com.quorum.tessera.grpc.api.ReceiveResponse;
import com.quorum.tessera.grpc.api.SendRequest;
import com.quorum.tessera.grpc.api.SendResponse;
import com.quorum.tessera.grpc.p2p.TesseraGrpc;
import com.quorum.tessera.test.ClientFacade;
import com.quorum.tessera.test.GrpcPartyFactory;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyFactory;
import cucumber.api.java8.En;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;

public class SendGrpcSteps implements En {


    
    private PartyFactory partyFactory = new GrpcPartyFactory();

    public SendGrpcSteps() {


        
        Collection<Party> senderHolder = new ArrayList<>();

        Set<Party> recipients = new HashSet<>();

        Set<String> storedHashes = new TreeSet<>();

        byte[] txnData = ClientFacade.generateTransactionData();

        Given("^Sender party (.+)$", (String pty) -> {
            partyFactory.getParties()
                .filter(p -> p.getAlias().equals(pty))
                .findAny()
                .ifPresent(senderHolder::add);

        });
        
        And("^Recipient part(?:y|ies) (.+)$", (String alias) -> {
            parseAliases(alias).stream()
                .map(partyFactory::findByAlias)
                .forEach(recipients::add);

            assertThat(recipients).isNotEmpty();
        });
        
        And("^all parties are running$", () -> {

            partyFactory.getParties()
                .map(p -> createChannel(p))
                .map(TesseraGrpc::newBlockingStub)
                .map(stub -> stub.getUpCheck(Empty.getDefaultInstance()))
                .forEach(mesage -> {
                    assertThat(mesage.getUpCheck()).isNotNull().isEqualTo("I'm up!");
                });

        });

        When("sender party receives transaction from Quorum peer", () -> {

            Party sender = senderHolder.stream().findAny().get();

            List<String> recipientKeys = recipients.stream()
                .map(Party::getPublicKey)
                .collect(Collectors.toList());

            SendRequest sendRequest = SendRequest.newBuilder()
                .setFrom(sender.getPublicKey())
                .addAllTo(recipientKeys)
                .setPayload(ByteString.copyFrom(txnData))
                .build();

            SendResponse sendResponse = senderHolder.stream()
                .map(p -> createChannel(p))
                .map(APITransactionGrpc::newBlockingStub)
                .map(stub -> stub.send(sendRequest))
                .findAny().get();

            storedHashes.add(sendResponse.getKey());

        });

        

        Then("^sender party stores the transaction$", () -> {
            Party sender = senderHolder.iterator().next();
            try (PreparedStatement statement
                = sender.getDatabaseConnection().prepareStatement("SELECT COUNT(*) FROM ENCRYPTED_TRANSACTION WHERE HASH = ?")) {
                statement.setBytes(1, Base64.getDecoder().decode(storedHashes.iterator().next()));

                try (ResultSet results = statement.executeQuery()) {
                    assertThat(results.next()).isTrue();
                    assertThat(results.getLong(1)).isEqualTo(1);
                }

            }
        });

        Then("^forwards the transaction to recipient part(?:y|ies)$", () -> {

            String hash = storedHashes.stream().findAny().get();

            recipients.forEach(recipient -> {

                ReceiveRequest receiveRequest = ReceiveRequest.newBuilder()
                    .setTo(recipient.getPublicKey())
                    .setKey(hash)
                    .build();

                ManagedChannel channel = createChannel(recipient);

                ReceiveResponse receiveResponse = APITransactionGrpc.newBlockingStub(channel).receive(receiveRequest);
                assertThat(receiveResponse.getPayload().toByteArray()).isNotNull().isEqualTo(txnData);

                channel.shutdownNow();
            });

        });

        After(() -> {
            channels.forEach(ManagedChannel::shutdownNow);
            channels.clear();

        });

    }

    static List<String> parseAliases(String alias) {
        return Arrays.asList(alias.split(",| and "));
    }

    static List<ManagedChannel> channels = new ArrayList<>();

    static ManagedChannel createChannel(Party party) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", party.getGrpcPort())
            .usePlaintext()
            .build();

        channels.add(channel);

        return channel;
    }

}
