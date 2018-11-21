package com.quorum.tessera.test.grpc;

import com.google.protobuf.ByteString;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.grpc.p2p.PartyInfoGrpc;
import com.quorum.tessera.grpc.p2p.PartyInfoMessage;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.node.model.Recipient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * After a few iterations of updatePartyInfo (performed by the PartyInfoPoller) all nodes should be aware of eachother.
 */
public class PartyInfoGrpcIT {

    private ManagedChannel channel1;
    private ManagedChannel channel2;
    private ManagedChannel channel3;
    private ManagedChannel channel4;

    private PartyInfoGrpc.PartyInfoBlockingStub blockingStub1;
    private PartyInfoGrpc.PartyInfoBlockingStub blockingStub2;
    private PartyInfoGrpc.PartyInfoBlockingStub blockingStub3;
    private PartyInfoGrpc.PartyInfoBlockingStub blockingStub4;

    private final PartyInfoParser partyInfoParser = PartyInfoParser.create();

    private PartyInfo partyInfo = new PartyInfo("http://localhost:50520", Collections.emptySet(), Collections.emptySet());
    private PartyInfoMessage request = PartyInfoMessage.newBuilder()
        .setPartyInfo(ByteString.copyFrom(partyInfoParser.to(partyInfo)))
        .build();

    @Before
    public void onSetUp() {
        channel1 = ManagedChannelBuilder.forAddress("127.0.0.1", 50520)
            .usePlaintext()
            .build();
        channel2 = ManagedChannelBuilder.forAddress("127.0.0.1", 50521)
            .usePlaintext()
            .build();
        channel3 = ManagedChannelBuilder.forAddress("127.0.0.1", 50522)
            .usePlaintext()
            .build();
        channel4 = ManagedChannelBuilder.forAddress("127.0.0.1", 50523)
            .usePlaintext()
            .build();

        blockingStub1 = PartyInfoGrpc.newBlockingStub(channel1);
        blockingStub2 = PartyInfoGrpc.newBlockingStub(channel2);
        blockingStub3 = PartyInfoGrpc.newBlockingStub(channel3);
        blockingStub4 = PartyInfoGrpc.newBlockingStub(channel4);
    }

    @After
    public void onTearDown() {
        channel1.shutdown();
        channel2.shutdown();
        channel3.shutdown();
        channel4.shutdown();
    }

    @Test
    public void checkNode1() throws MalformedURLException{
        PartyInfoMessage response = blockingStub1.getPartyInfo(request);
        assertThat(response).isNotNull();
        PartyInfo responsePartyInfo = partyInfoParser.from(response.getPartyInfo().toByteArray());

        checkPartyInfoContents(responsePartyInfo);
    }

    @Test
    public void checkNode2() throws MalformedURLException{
        PartyInfoMessage response = blockingStub2.getPartyInfo(request);
        assertThat(response).isNotNull();
        PartyInfo responsePartyInfo = partyInfoParser.from(response.getPartyInfo().toByteArray());

        checkPartyInfoContents(responsePartyInfo);
    }

    @Test
    public void checkNode3() throws MalformedURLException{
        PartyInfoMessage response = blockingStub3.getPartyInfo(request);
        assertThat(response).isNotNull();
        PartyInfo responsePartyInfo = partyInfoParser.from(response.getPartyInfo().toByteArray());

        checkPartyInfoContents(responsePartyInfo);
    }

    @Test
    public void checkNode4() throws MalformedURLException{
        PartyInfoMessage response = blockingStub4.getPartyInfo(request);
        assertThat(response).isNotNull();
        PartyInfo responsePartyInfo = partyInfoParser.from(response.getPartyInfo().toByteArray());

        checkPartyInfoContents(responsePartyInfo);
    }

    private void checkPartyInfoContents(PartyInfo partyInfo) {
        assertThat(partyInfo.getRecipients()).containsExactlyInAnyOrder(
            new Recipient(PublicKey.from(Base64.decode("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")),"http://localhost:50520/"),
            new Recipient(PublicKey.from(Base64.decode("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=")),"http://localhost:50521/"),
            new Recipient(PublicKey.from(Base64.decode("giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=")),"http://localhost:50522/"),
            new Recipient(PublicKey.from(Base64.decode("jP4f+k/IbJvGyh0LklWoea2jQfmLwV53m9XoHVS4NSU=")),"http://localhost:50522/"),
            new Recipient(PublicKey.from(Base64.decode("Tj8xg/HpsYmh7Te3UerzlLx1HgpWVOGq25ZgbwaPNVM=")),"http://localhost:50523/")
            );
        assertThat(partyInfo.getParties()).containsExactlyInAnyOrder(
            new Party("http://localhost:50520/"),
            new Party("http://localhost:50521/"),
            new Party("http://localhost:50522/"),
            new Party("http://localhost:50523/")
            );
    }

}
