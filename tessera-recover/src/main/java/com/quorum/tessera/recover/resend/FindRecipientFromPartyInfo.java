package com.quorum.tessera.recover.resend;

import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.Recipient;

public class FindRecipientFromPartyInfo implements BatchWorkflowAction {

    private PartyInfoService partyInfoService;

    public FindRecipientFromPartyInfo(PartyInfoService partyInfoService) {
        this.partyInfoService = partyInfoService;
    }

    @Override
    public boolean execute(BatchWorkflowContext event) {
        PublicKey recipientKey = event.getRecipientKey();
        final Recipient retrievedRecipientFromStore =
            partyInfoService.getPartyInfo().getRecipients().stream()
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
