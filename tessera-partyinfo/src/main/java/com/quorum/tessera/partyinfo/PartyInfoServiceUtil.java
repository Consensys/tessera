package com.quorum.tessera.partyinfo;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import java.util.Map;
import java.util.stream.Collectors;

public interface PartyInfoServiceUtil {

  static boolean validateKeysToUrls(final NodeInfo existingPartyInfo, final NodeInfo newPartyInfo) {

    final Map<PublicKey, String> existingRecipientKeyUrlMap =
        existingPartyInfo.getRecipients().stream()
            .collect(Collectors.toMap(Recipient::getKey, Recipient::getUrl));

    final Map<PublicKey, String> newRecipientKeyUrlMap =
        newPartyInfo.getRecipients().stream()
            .collect(Collectors.toMap(Recipient::getKey, Recipient::getUrl));

    for (final Map.Entry<PublicKey, String> entry : newRecipientKeyUrlMap.entrySet()) {
      final PublicKey key = entry.getKey();

      if (existingRecipientKeyUrlMap.containsKey(key)) {
        String existingUrl = existingRecipientKeyUrlMap.get(key);
        String newUrl = entry.getValue();
        if (!existingUrl.equalsIgnoreCase(newUrl)) {
          return false;
        }
      }
    }

    return true;
  }
}
