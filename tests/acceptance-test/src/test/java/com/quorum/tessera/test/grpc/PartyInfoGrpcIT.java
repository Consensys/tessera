package com.quorum.tessera.test.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.grpc.p2p.PartyInfoGrpc;
import com.quorum.tessera.grpc.p2p.PartyInfoJson;
import com.quorum.tessera.grpc.p2p.PartyInfoMessage;
import com.quorum.tessera.grpc.p2p.Peer;
import com.quorum.tessera.test.PartyHelper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.net.URI;
import org.bouncycastle.util.encoders.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.UriBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * After a few iterations of updatePartyInfo (performed by the PartyInfoPoller) all nodes should be aware of each other.
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

    private PartyInfo partyInfo;
    private PartyInfoMessage request;

    
    private com.quorum.tessera.test.Party partyOne;
    
    private com.quorum.tessera.test.Party partyTwo;
    
    private com.quorum.tessera.test.Party partyThree;
    
    private com.quorum.tessera.test.Party partyFour;
    
    @Before
    public void onSetUp() {
        PartyHelper partyHelper = PartyHelper.create();
        partyOne = partyHelper.findByAlias("A");
        partyTwo = partyHelper.findByAlias("B");
        partyThree = partyHelper.findByAlias("C");
        partyFour = partyHelper.findByAlias("D");
        
        
        channel1 = ManagedChannelBuilder.forAddress("127.0.0.1", partyOne.getP2PUri().getPort())
            .usePlaintext()
            .build();
        channel2 = ManagedChannelBuilder.forAddress("127.0.0.1", partyTwo.getP2PUri().getPort())
            .usePlaintext()
            .build();
        channel3 = ManagedChannelBuilder.forAddress("127.0.0.1", partyThree.getP2PUri().getPort())
            .usePlaintext()
            .build();
        channel4 = ManagedChannelBuilder.forAddress("127.0.0.1", partyFour.getP2PUri().getPort())
            .usePlaintext()
            .build();

        partyInfo = new PartyInfo(partyOne.getP2PUri().toString(), Collections.emptySet(), Collections.emptySet());
        
        
        request = PartyInfoMessage.newBuilder()
        .setPartyInfo(ByteString.copyFrom(partyInfoParser.to(partyInfo)))
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
    public void checkNode1() {
        PartyInfoMessage response = blockingStub1.getPartyInfo(request);
        assertThat(response).isNotNull();
        PartyInfo responsePartyInfo = partyInfoParser.from(response.getPartyInfo().toByteArray());

        checkPartyInfoContents(responsePartyInfo);
    }

    @Test
    public void checkNode2() {
        PartyInfoMessage response = blockingStub2.getPartyInfo(request);
        assertThat(response).isNotNull();
        PartyInfo responsePartyInfo = partyInfoParser.from(response.getPartyInfo().toByteArray());

        checkPartyInfoContents(responsePartyInfo);
    }

    @Test
    public void checkNode3() {
        PartyInfoMessage response = blockingStub3.getPartyInfo(request);
        assertThat(response).isNotNull();
        PartyInfo responsePartyInfo = partyInfoParser.from(response.getPartyInfo().toByteArray());

        checkPartyInfoContents(responsePartyInfo);
    }

    @Test
    public void checkNode4() {
        PartyInfoMessage response = blockingStub4.getPartyInfo(request);
        assertThat(response).isNotNull();
        PartyInfo responsePartyInfo = partyInfoParser.from(response.getPartyInfo().toByteArray());

        checkPartyInfoContents(responsePartyInfo);
    }

    private void checkPartyInfoContents(PartyInfo partyInfo) {
        
        String description = partyInfo.getRecipients().stream()
                .map(PartyInfoGrpcIT::printRecipient)
                .collect(Collectors.joining(System.lineSeparator()));
        
        Set<String> recipientKeys = partyInfo.getRecipients().stream()
                .map(r -> r.getKey().encodeToBase64()).collect(Collectors.toSet());
        
        Set<String> expectedKeys = Stream.of(
                partyOne,partyTwo,partyThree,partyFour)
                .map(com.quorum.tessera.test.Party::getConfig)
                .map(Config::getKeys)
                .map(KeyConfiguration::getKeyData)
                .flatMap(List::stream)
                .map(ConfigKeyPair::getPublicKey)
                .collect(Collectors.toSet());
                
        
        assertThat(recipientKeys)
                .describedAs("Recipients: "+ description)
                .containsExactlyInAnyOrderElementsOf(expectedKeys);
        
        
        List<URI> uriList = Stream.of(partyOne,partyTwo,partyThree,partyFour)
                .map(com.quorum.tessera.test.Party::getP2PUri)
                .map(u -> UriBuilder.fromPath("")
                        .host(u.getHost())
                        .scheme(u.getScheme())
                        .port(u.getPort())
                        .build())        
                .collect(Collectors.toList());
        
        assertThat(uriList).containsExactly(
            partyOne.getP2PUri(),
            partyTwo.getP2PUri(),
            partyThree.getP2PUri(),
            partyFour.getP2PUri()
        );
    }

    
    private static String printRecipient(Recipient recipient) {
        return "Recipient[key: "+ recipient.getKey().encodeToBase64() +",url: "+ recipient.getUrl() +"]";
    }
    
    @Test
    public void partyInfoGetNode1() {

        final PartyInfoJson response = blockingStub1.getPartyInfoMessage(Empty.getDefaultInstance());

        assertThat(response).isNotNull();

        final Set<Party> peers = response.getPeersList()
            .stream()
            .map(Peer::getUrl)
            .map(Party::new)
            .collect(Collectors.toSet());
        final Set<Recipient> recipients = response.getKeysMap().entrySet()
            .stream()
            .map(kv -> new Recipient(PublicKey.from(Base64.decode(kv.getKey())), kv.getValue()))
            .collect(Collectors.toSet());

        checkPartyInfoContents(new PartyInfo("", recipients, peers));
    }

    @Test
    public void partyInfoGetNode2() {

        final PartyInfoJson response = blockingStub2.getPartyInfoMessage(Empty.getDefaultInstance());

        assertThat(response).isNotNull();

        final Set<Party> peers = response.getPeersList()
            .stream()
            .map(Peer::getUrl)
            .map(Party::new)
            .collect(Collectors.toSet());
        final Set<Recipient> recipients = response.getKeysMap().entrySet()
            .stream()
            .map(kv -> new Recipient(PublicKey.from(Base64.decode(kv.getKey())), kv.getValue()))
            .collect(Collectors.toSet());

        checkPartyInfoContents(new PartyInfo("", recipients, peers));
    }

    @Test
    public void partyInfoGetNode3() {

        final PartyInfoJson response = blockingStub3.getPartyInfoMessage(Empty.getDefaultInstance());

        assertThat(response).isNotNull();

        final Set<Party> peers = response.getPeersList()
            .stream()
            .map(Peer::getUrl)
            .map(Party::new)
            .collect(Collectors.toSet());
        final Set<Recipient> recipients = response.getKeysMap().entrySet()
            .stream()
            .map(kv -> new Recipient(PublicKey.from(Base64.decode(kv.getKey())), kv.getValue()))
            .collect(Collectors.toSet());

        checkPartyInfoContents(new PartyInfo("", recipients, peers));
    }

    @Test
    public void partyInfoGetNode4() {

        final PartyInfoJson response = blockingStub4.getPartyInfoMessage(Empty.getDefaultInstance());

        assertThat(response).isNotNull();

        final Set<Party> peers = response.getPeersList()
            .stream()
            .map(Peer::getUrl)
            .map(Party::new)
            .collect(Collectors.toSet());
        
        final Set<Recipient> recipients = response.getKeysMap().entrySet()
            .stream()
            .map(kv -> new Recipient(PublicKey.from(Base64.decode(kv.getKey())), kv.getValue()))
            .collect(Collectors.toSet());

        checkPartyInfoContents(new PartyInfo("", recipients, peers));
    }

}
