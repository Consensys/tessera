package com.quorum.tessera.plugin.p2p;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.CBOREncoder;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveProvider;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.pluginAPI.p2p.P2PPluginService;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class P2PPluginServiceImpl implements P2PPluginService {

  public static P2PPluginService INSTANCE = new P2PPluginServiceImpl();

  private final Enclave enclave;
  private final CBOREncoder cborEncoder;
  private final Discovery discovery;

  private P2PPluginServiceImpl() {
    enclave = EnclaveProvider.provider();
    cborEncoder = new CBOREncoder();
    discovery = Discovery.create();
  }

  @Override
  //
  public byte[] encryptAndEncodeData(final byte[] message, final String senderPublicKey, final List<String> recipientPublicKeys) {
    final PublicKey senderPubKey = PublicKey.from(Base64.getDecoder().decode(senderPublicKey.trim()));
    final List<PublicKey> recipientsPubKeys = recipientPublicKeys.stream().map(string -> PublicKey.from(Base64.getDecoder().decode(string.trim()))).collect(Collectors.toList());
    final EncodedPayload encodedPayload = enclave.encryptPayload(message, senderPubKey, recipientsPubKeys);
    final byte[] encodedMessage = cborEncoder.encode(encodedPayload);
    return encodedMessage;
  }

  @Override
  public String getURLForRecipient(final String recipientPublicKey) {
    final NodeInfo remoteNodeInfo = discovery.getRemoteNodeInfo(PublicKey.from(Base64.getDecoder().decode(recipientPublicKey.trim())));
    if (remoteNodeInfo == null) {
      return null;
    }
    return remoteNodeInfo.getUrl();
  }
}
