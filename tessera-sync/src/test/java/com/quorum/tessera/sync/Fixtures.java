
package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import java.util.Base64;
import java.util.Collections;

public interface Fixtures {
    
    static PartyInfo samplePartyInfo() {
        return new PartyInfo(
                "http://bogus.com:9999",
                Collections.singleton(new Recipient(sampleKey(), "http://bogus.com:9998")),
                Collections.singleton(new Party("http://bogus.com:9997")));
    }

    static EncodedPayload samplePayload() {
        return new EncodedPayload(sampleKey(), "cipherText".getBytes(),
                new Nonce("cipherTextNonce".getBytes()),
                Collections.singletonList("recipientBoxes".getBytes()),
                new Nonce("recipientNonce".getBytes()), Collections.singletonList(sampleKey()));
    }

    static PublicKey sampleKey() {
        return PublicKey.from(Base64.getDecoder().decode("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc="));
    }
}
