package com.quorum.tessera.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.KeyDataUtil;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.p2p.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import suite.NodeAlias;

public class PeerToPeerIT {

  private PartyHelper partyHelper = PartyHelper.create();

  private ClientFactory clientFactory = new ClientFactory();

  private Party partyA;

  @Before
  public void onSetUp() {
    partyA = partyHelper.findByAlias(NodeAlias.A);
    validatePartyInfoContentsOnNodeA();
  }

  @After
  public void onTearDown() {
    validatePartyInfoContentsOnNodeA();
  }

  /*
   * Send a valid party info from B to A
   */
  @Test
  public void happyCase() {

    Party partyB = partyHelper.findByAlias(NodeAlias.B);

    ServerConfig serverContext =
        Optional.of(partyB.getConfig()).map(Config::getP2PServerConfig).get();

    Client client = clientFactory.buildFrom(serverContext);

    PublicKey partyBKey =
        Optional.of(partyB)
            .map(Party::getPublicKey)
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .get();

    String partyBServerAddress = partyB.getConfig().getP2PServerConfig().getServerAddress();

    Recipient recipient = Recipient.of(partyBKey, partyBServerAddress);

    PartyInfo partyInfo =
        new PartyInfo(
            partyBServerAddress, Collections.singleton(recipient), Collections.emptySet());

    PartyInfoParser partyInfoParser = PartyInfoParser.create();

    byte[] data = partyInfoParser.to(partyInfo);

    StreamingOutput output = out -> out.write(data);

    Response response =
        client
            .target(partyA.getP2PUri())
            .path("partyinfo")
            .request()
            .post(Entity.entity(output, MediaType.APPLICATION_OCTET_STREAM));

    assertThat(response.getStatus()).isEqualTo(200);
  }

  /*
  If the sending node has an invalid key we 200 as the secondary key
  should no be validated.

   */
  @Test
  public void malicousNodeHasInvalidKey() throws Exception {

    Party hihjackedParty = partyHelper.findByAlias(NodeAlias.B);

    PublicKey bogusKey = PublicKey.from("BADKEY".getBytes());

    ServerConfig serverConfig = hihjackedParty.getConfig().getP2PServerConfig();

    Recipient recipient = Recipient.of(bogusKey, serverConfig.getServerUri().toString());

    PartyInfo partyInfo =
        new PartyInfo(
            serverConfig.getServerUri().toString(),
            Collections.singleton(recipient),
            Collections.emptySet());

    Client client = clientFactory.buildFrom(serverConfig);

    PartyInfoParser partyInfoParser = PartyInfoParser.create();

    byte[] data = partyInfoParser.to(partyInfo);

    StreamingOutput output = out -> out.write(data);

    Response response =
        client
            .target(partyA.getP2PUri())
            .path("partyinfo")
            .request()
            .post(Entity.entity(output, MediaType.APPLICATION_OCTET_STREAM));

    assertThat(response.getStatus()).isEqualTo(500);
  }

  /*
  A good node with valid key has a bad recipient in its party info
   */
  @Test
  public void benevolentNodeBecomesPosessedAndSendsInvalidKeyInRecipientList() throws Exception {

    Party partyB = partyHelper.findByAlias(NodeAlias.B);

    ServerConfig serverConfig = partyB.getConfig().getP2PServerConfig();

    PublicKey publicKey =
        Optional.of(partyB)
            .map(Party::getPublicKey)
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .get();

    Recipient itself = Recipient.of(publicKey, serverConfig.getServerUri().toString());

    String validButIncorrectUrl =
        partyHelper.findByAlias(NodeAlias.C).getConfig().getP2PServerConfig().getServerAddress();

    Recipient badRecipient = Recipient.of(PublicKey.from("OUCH".getBytes()), validButIncorrectUrl);

    Set<Recipient> recipients = Stream.of(itself, badRecipient).collect(Collectors.toSet());

    assertThat(recipients).containsExactlyInAnyOrder(itself, badRecipient);

    PartyInfo partyInfo =
        new PartyInfo(serverConfig.getServerUri().toString(), recipients, Collections.emptySet());

    Client client = new ClientFactory().buildFrom(serverConfig);

    PartyInfoParser partyInfoParser = PartyInfoParser.create();

    byte[] data = partyInfoParser.to(partyInfo);

    StreamingOutput output = out -> out.write(data);

    Response response =
        client
            .target(partyA.getP2PUri())
            .path("partyinfo")
            .request()
            .post(Entity.entity(output, MediaType.APPLICATION_OCTET_STREAM));

    assertThat(response.getStatus()).isEqualTo(200);
  }

  /*
  A good node with valid key has a bad recipient in its party info.
  The key is valid (node C's key) but there is a validation failure as
  the url cannot be called.
   */
  @Test
  public void benevolentNodeBecomesPosessedAndSendsInvalidUrlInRecipientList() throws Exception {

    Party partyB = partyHelper.findByAlias(NodeAlias.B);

    ServerConfig serverConfig =
        Optional.of(partyB.getConfig()).map(Config::getP2PServerConfig).get();

    PublicKey publicKey =
        Optional.of(partyB)
            .map(Party::getPublicKey)
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .get();

    Recipient itself = Recipient.of(publicKey, serverConfig.getServerUri().toString());

    String validKeyFromOtherNode = partyHelper.findByAlias(NodeAlias.C).getPublicKey();

    PublicKey validButIncorrectKey =
        Optional.of(validKeyFromOtherNode)
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .get();

    Recipient badRecipient = Recipient.of(validButIncorrectKey, "http://bogus.supersnide.com:8829");

    Set<Recipient> recipients = Stream.of(itself, badRecipient).collect(Collectors.toSet());

    assertThat(recipients).containsExactlyInAnyOrder(itself, badRecipient);

    PartyInfo partyInfo =
        new PartyInfo(serverConfig.getServerUri().toString(), recipients, Collections.emptySet());

    Client client = new ClientFactory().buildFrom(serverConfig);

    PartyInfoParser partyInfoParser = PartyInfoParser.create();

    byte[] data = partyInfoParser.to(partyInfo);

    StreamingOutput output = out -> out.write(data);

    Response response =
        client
            .target(partyA.getP2PUri())
            .path("partyinfo")
            .request()
            .post(Entity.entity(output, MediaType.APPLICATION_OCTET_STREAM));

    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  public void benevolentNodeBecomesPosessedAndTriesToSendInavludUrlAndKeyCombo() throws Exception {

    Party partyB = partyHelper.findByAlias(NodeAlias.B);

    ServerConfig serverConfig = partyB.getConfig().getP2PServerConfig();

    PublicKey publicKey =
        Optional.of(partyB)
            .map(Party::getPublicKey)
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .get();

    Recipient itself = Recipient.of(publicKey, serverConfig.getServerUri().toString());

    String validKeyFromOtherNode = partyHelper.findByAlias(NodeAlias.C).getPublicKey();

    PublicKey validButIncorrectKey =
        Optional.of(validKeyFromOtherNode)
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .get();

    String workingUrlFromSomeOtherNode =
        partyHelper.findByAlias(NodeAlias.D).getConfig().getP2PServerConfig().getServerAddress();

    Recipient badRecipient = Recipient.of(validButIncorrectKey, workingUrlFromSomeOtherNode);

    Set<Recipient> recipients = Stream.of(itself, badRecipient).collect(Collectors.toSet());

    assertThat(recipients).containsExactlyInAnyOrder(itself, badRecipient);

    PartyInfo partyInfo =
        new PartyInfo(serverConfig.getServerUri().toString(), recipients, Collections.emptySet());

    Client client = new ClientFactory().buildFrom(serverConfig);

    PartyInfoParser partyInfoParser = PartyInfoParser.create();

    byte[] data = partyInfoParser.to(partyInfo);

    StreamingOutput output = out -> out.write(data);

    Response response =
        client
            .target(partyA.getP2PUri())
            .path("partyinfo")
            .request()
            .post(Entity.entity(output, MediaType.APPLICATION_OCTET_STREAM));

    assertThat(response.getStatus()).isEqualTo(200);
  }

  /*
  Assume that not of the tests should have managed to change the initial party info
   */
  private void validatePartyInfoContentsOnNodeA() {

    Party someParty =
        partyHelper.getParties().filter(p -> !p.getAlias().equals("A")).findAny().get();
    ServerConfig serverContext = someParty.getConfig().getP2PServerConfig();
    Client client = clientFactory.buildFrom(serverContext);

    Response response = client.target(partyA.getP2PUri()).path("partyinfo").request().get();

    assertThat(response.getStatus()).isEqualTo(200);

    JsonObject result = response.readEntity(JsonObject.class);

    Map<String, String> actual =
        result.getJsonArray("keys").stream()
            .map(o -> o.asJsonObject())
            .collect(
                Collectors.toMap(
                    o -> o.getString("key"), o -> removeTrailingSlash(o.getString("url"))));

    EncryptorConfig encryptorConfig =
        partyHelper.getParties().findFirst().map(Party::getConfig).map(Config::getEncryptor).get();
    KeyEncryptor keyEncryptor = KeyEncryptorFactory.newFactory().create(encryptorConfig);

    List<String> keyz =
        partyHelper
            .getParties()
            .map(Party::getConfig)
            .map(Config::getKeys)
            .flatMap(k -> k.getKeyData().stream())
            .map(kd -> KeyDataUtil.unmarshal(kd, keyEncryptor))
            .map(ConfigKeyPair::getPublicKey)
            .collect(Collectors.toList());

    List<String> urls =
        partyHelper
            .getParties()
            .map(Party::getConfig)
            .map(Config::getP2PServerConfig)
            .map(ServerConfig::getServerAddress)
            .map(s -> removeTrailingSlash(s))
            .collect(Collectors.toList());

    assertThat(actual).containsKeys(keyz.toArray(new String[0]));
    assertThat(actual).containsValues(urls.toArray(new String[0]));
  }

  private static String removeTrailingSlash(String s) {
    if (s.endsWith("/")) {
      return s.substring(0, s.length() - 1);
    }
    return s;
  }
}
