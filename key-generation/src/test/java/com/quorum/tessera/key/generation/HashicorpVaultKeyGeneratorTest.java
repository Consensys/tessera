package com.quorum.tessera.key.generation;

import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.nacl.NaclFacade;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class HashicorpVaultKeyGeneratorTest {

    private final String pubStr = "public";
    private final String privStr = "private";
    private final PublicKey pub = PublicKey.from(pubStr.getBytes());
    private final PrivateKey priv = PrivateKey.from(privStr.getBytes());

    private NaclFacade naclFacade;
    private KeyVaultService keyVaultService;
    private HashicorpVaultKeyGenerator hashicorpVaultKeyGenerator;

    @Before
    public void setUp() {
        this.naclFacade = mock(NaclFacade.class);
        this.keyVaultService = mock(KeyVaultService.class);

        final KeyPair keyPair = new KeyPair(pub, priv);
        when(naclFacade.generateNewKeys()).thenReturn(keyPair);

        this.hashicorpVaultKeyGenerator = new HashicorpVaultKeyGenerator(naclFacade, keyVaultService);

    }

    @Test(expected = NullPointerException.class)
    public void nullFilenameThrowsException() {
        hashicorpVaultKeyGenerator.generate(null, null);
    }

    @Test
    public void generatedKeyPairIsSavedToSpecifiedPathInVaultWithIds() {
        String filename = "secret/path";

        hashicorpVaultKeyGenerator.generate(filename, null);

        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("publicKey", pub.encodeToBase64());
        expectedData.put("privateKey", priv.encodeToBase64());

        verify(keyVaultService).setSecretAtPath(filename, expectedData);
    }

}
