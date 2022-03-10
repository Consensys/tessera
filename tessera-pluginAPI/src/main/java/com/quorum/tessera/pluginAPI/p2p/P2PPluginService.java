package com.quorum.tessera.pluginAPI.p2p;

import com.quorum.tessera.pluginAPI.TesseraPluginService;

import java.util.List;

public interface P2PPluginService extends TesseraPluginService {
  byte[] encryptAndEncodeData(    final byte[] message,
                                  final String senderPublicKey,
                                  final List<String> recipientPublicKeys);

  String getURLForRecipient(String recipientPublicKey);

}
