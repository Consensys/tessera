package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.Recipient;

public class FindRecipientFromPartyInfo implements BatchWorkflowAction {

  private Discovery discovery;

  public FindRecipientFromPartyInfo(Discovery discovery) {
    this.discovery = discovery;
  }

  @Override
  public boolean execute(BatchWorkflowContext event) {
    PublicKey recipientKey = event.getRecipientKey();
    final Recipient retrievedRecipientFromStore =
        discovery.getCurrent().getRecipients().stream()
            .filter(recipient -> recipientKey.equals(recipient.getKey()))
            .findAny()
            .orElseThrow(
                () ->
                    new KeyNotFoundException(
                        "Recipient not found for key: " + recipientKey.encodeToBase64()));

    event.setRecipient(retrievedRecipientFromStore);

    return true;
  }
}
