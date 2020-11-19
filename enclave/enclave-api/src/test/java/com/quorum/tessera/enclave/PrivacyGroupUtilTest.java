package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivacyGroupUtilTest {

    private PrivacyGroupUtil privacyGroupUtil = PrivacyGroupUtil.create();

    private PublicKey recipient1 =
            PublicKey.from(Base64.getDecoder().decode("arhIcNa+MuYXZabmzJD5B33F3dZgqb0hEbM3FZsylSg="));
    private PublicKey recipient2 =
            PublicKey.from(Base64.getDecoder().decode("B687sgdtqsem2qEXO8h8UqvW1Mb3yKo7id5hPFLwCmY="));
    private PublicKey recipient3 =
        PublicKey.from(Base64.getDecoder().decode("HEkOUBXbgGCQ5+WDFUAhucXm/n5zUrfGkgdJY/5lfCs="));

    private byte[] seed = Base64.getDecoder().decode("Zm9v");

    @Test
    public void testGenerateLegacyGroupId() {

        final byte[] id = privacyGroupUtil.generatePrivacyGroupId(List.of(recipient1, recipient2), null);

        final String base64Id = PublicKey.from(id).encodeToBase64();

        assertThat(base64Id).isEqualTo("f+2n2ScSwQj/MCQyGkIWWzukT90w51ouYlOPm80BMTE=");
    }

    @Test
    public void testGeneratePrivacyGroupId() {

        final List<PublicKey> members = List.of(recipient1, recipient2, recipient3);

        final byte[] id = privacyGroupUtil.generatePrivacyGroupId(members, seed);

        final String base64Id = PublicKey.from(id).encodeToBase64();

        assertThat(base64Id).isEqualTo("cOHh0dgkVV4lodSNuHu31ipEtUN/AFqviYekZDu4gEc=");

    }

    @Test
    public void testGenerateLookupId() {

        final List<PublicKey> members = List.of(recipient1, recipient2, recipient3);

        final byte[] id = privacyGroupUtil.generateLookupId(members);

        final String base64Id = PublicKey.from(id).encodeToBase64();

        assertThat(base64Id).isEqualTo("y2HKqFmRmb1EAwpVanIzCDN3v2ZRKcQGdIoQ6XsFRmk=");
    }

    @Test
    public void testEncodeDecode() {

        final List<PublicKey> members = List.of(recipient1, recipient2, recipient3);

        final PrivacyGroup privacyGroup = PrivacyGroup.Builder.create()
            .withPrivacyGroupId(PublicKey.from(Base64.getDecoder().decode("cOHh0dgkVV4lodSNuHu31ipEtUN/AFqviYekZDu4gEc=")))
            .withName("Organisation A")
            .withDescription("Privacy group contains recipient 1,2, and 3")
            .withSeed("foo".getBytes())
            .withType(PrivacyGroup.Type.PANTHEON)
            .withState(PrivacyGroup.State.ACTIVE)
            .withMembers(members)
            .build();

        final byte[] encoded = privacyGroupUtil.encode(privacyGroup);

        final PrivacyGroup decoded = privacyGroupUtil.decode(encoded);

        assertThat(decoded.getPrivacyGroupId()).isEqualTo(PublicKey.from(Base64.getDecoder().decode("cOHh0dgkVV4lodSNuHu31ipEtUN/AFqviYekZDu4gEc=")));
        assertThat(decoded.getName()).isEqualTo("Organisation A");
        assertThat(decoded.getDescription()).isEqualTo("Privacy group contains recipient 1,2, and 3");
        assertThat(decoded.getSeed()).isEqualTo("foo".getBytes());
        assertThat(decoded.getType()).isEqualTo(PrivacyGroup.Type.PANTHEON);
        assertThat(decoded.getState()).isEqualTo(PrivacyGroup.State.ACTIVE);
        assertThat(decoded.getMembers()).containsExactly(recipient1, recipient2, recipient3);

    }

    @Test
    public void testEncodeDecodePartialData() {

        final PublicKey groupId = PublicKey.from(privacyGroupUtil.generatePrivacyGroupId(List.of(),new byte[0]));

        final PrivacyGroup privacyGroup = PrivacyGroup.Builder.create()
            .withPrivacyGroupId(groupId)
            .withMembers(List.of())
            .withType(PrivacyGroup.Type.LEGACY)
            .withState(PrivacyGroup.State.DELETED)
            .build();

        final byte[] encoded = privacyGroupUtil.encode(privacyGroup);

        final PrivacyGroup decoded = privacyGroupUtil.decode(encoded);

        assertThat(decoded.getPrivacyGroupId()).isEqualTo(groupId);
        assertThat(decoded.getName()).isEmpty();
        assertThat(decoded.getMembers()).isEmpty();
        assertThat(decoded.getType()).isEqualTo(PrivacyGroup.Type.LEGACY);
        assertThat(decoded.getState()).isEqualTo(PrivacyGroup.State.DELETED);

    }

}
