package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.encryption.PublicKey;
import java.util.Base64;
import java.util.List;
import org.junit.Test;

public class PrivacyGroupUtilTest {

  private final PrivacyGroupUtil privacyGroupUtil = PrivacyGroupUtil.create();

  private final PublicKey recipient1 =
      PublicKey.from(Base64.getDecoder().decode("arhIcNa+MuYXZabmzJD5B33F3dZgqb0hEbM3FZsylSg="));
  private final PublicKey recipient2 =
      PublicKey.from(Base64.getDecoder().decode("B687sgdtqsem2qEXO8h8UqvW1Mb3yKo7id5hPFLwCmY="));
  private final PublicKey recipient3 =
      PublicKey.from(Base64.getDecoder().decode("HEkOUBXbgGCQ5+WDFUAhucXm/n5zUrfGkgdJY/5lfCs="));

  private final byte[] seed = Base64.getDecoder().decode("Zm9v");

  @Test
  public void testGenerateLegacyGroupId() {

    final byte[] id = privacyGroupUtil.generateId(List.of(recipient1, recipient2));

    final String base64Id = PrivacyGroup.Id.fromBytes(id).getBase64();

    assertThat(base64Id).isEqualTo("f+2n2ScSwQj/MCQyGkIWWzukT90w51ouYlOPm80BMTE=");
  }

  @Test
  public void testGeneratePrivacyGroupId() {

    final List<PublicKey> members = List.of(recipient1, recipient2, recipient3);

    final byte[] id = privacyGroupUtil.generateId(members, seed);

    final String base64Id = PrivacyGroup.Id.fromBytes(id).getBase64();

    assertThat(base64Id).isEqualTo("cOHh0dgkVV4lodSNuHu31ipEtUN/AFqviYekZDu4gEc=");
  }

  @Test
  public void testGenerateLookupId() {

    final List<PublicKey> members = List.of(recipient1, recipient2, recipient3);

    final byte[] id = privacyGroupUtil.generateLookupId(members);

    final String base64Id = PrivacyGroup.Id.fromBytes(id).getBase64();

    assertThat(base64Id).isEqualTo("y2HKqFmRmb1EAwpVanIzCDN3v2ZRKcQGdIoQ6XsFRmk=");
  }

  @Test
  public void testEncodeDecode() {

    final List<PublicKey> members = List.of(recipient1, recipient2, recipient3);

    final PrivacyGroup privacyGroup =
        PrivacyGroup.Builder.create()
            .withPrivacyGroupId("cOHh0dgkVV4lodSNuHu31ipEtUN/AFqviYekZDu4gEc=")
            .withName("Organisation A")
            .withDescription("Privacy group contains recipient 1,2, and 3")
            .withSeed("foo".getBytes())
            .withType(PrivacyGroup.Type.PANTHEON)
            .withState(PrivacyGroup.State.ACTIVE)
            .withMembers(members)
            .build();

    final byte[] encoded = privacyGroupUtil.encode(privacyGroup);

    final PrivacyGroup decoded = privacyGroupUtil.decode(encoded);

    assertThat(decoded.getId())
        .isEqualTo(
            PrivacyGroup.Id.fromBase64String("cOHh0dgkVV4lodSNuHu31ipEtUN/AFqviYekZDu4gEc="));
    assertThat(decoded.getName()).isEqualTo("Organisation A");
    assertThat(decoded.getDescription()).isEqualTo("Privacy group contains recipient 1,2, and 3");
    assertThat(decoded.getSeed()).isEqualTo("foo".getBytes());
    assertThat(decoded.getType()).isEqualTo(PrivacyGroup.Type.PANTHEON);
    assertThat(decoded.getState()).isEqualTo(PrivacyGroup.State.ACTIVE);
    assertThat(decoded.getMembers()).containsExactly(recipient1, recipient2, recipient3);
  }

  @Test
  public void testEncodeDecodePartialData() {

    final PrivacyGroup.Id groupId =
        PrivacyGroup.Id.fromBytes(privacyGroupUtil.generateId(List.of()));

    final PrivacyGroup privacyGroup =
        PrivacyGroup.Builder.create()
            .withPrivacyGroupId(groupId)
            .withMembers(List.of())
            .withType(PrivacyGroup.Type.LEGACY)
            .withState(PrivacyGroup.State.DELETED)
            .build();

    final byte[] encoded = privacyGroupUtil.encode(privacyGroup);

    final PrivacyGroup decoded = privacyGroupUtil.decode(encoded);

    assertThat(decoded.getId()).isEqualTo(groupId);
    assertThat(decoded.getName()).isEmpty();
    assertThat(decoded.getMembers()).isEmpty();
    assertThat(decoded.getType()).isEqualTo(PrivacyGroup.Type.LEGACY);
    assertThat(decoded.getState()).isEqualTo(PrivacyGroup.State.DELETED);
  }

  @Test
  public void testEncodeDecodeResidentGroup() {

    final PrivacyGroup.Id groupId = mock(PrivacyGroup.Id.class);

    final PrivacyGroup privacyGroup =
        PrivacyGroup.Builder.create()
            .withPrivacyGroupId(groupId)
            .withMembers(List.of())
            .withName("ResidentGroup")
            .withType(PrivacyGroup.Type.RESIDENT)
            .withState(PrivacyGroup.State.ACTIVE)
            .build();

    final byte[] encoded = privacyGroupUtil.encode(privacyGroup);

    final PrivacyGroup decoded = privacyGroupUtil.decode(encoded);

    assertThat(decoded.getId()).isEqualTo(PrivacyGroup.Id.fromBytes("ResidentGroup".getBytes()));
    assertThat(decoded.getName()).isEqualTo("ResidentGroup");
    assertThat(decoded.getMembers()).isEmpty();
    assertThat(decoded.getType()).isEqualTo(PrivacyGroup.Type.RESIDENT);
    assertThat(decoded.getState()).isEqualTo(PrivacyGroup.State.ACTIVE);
  }
}
