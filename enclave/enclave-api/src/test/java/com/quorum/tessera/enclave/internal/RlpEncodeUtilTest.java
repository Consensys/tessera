package com.quorum.tessera.enclave.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.encryption.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.rlp.RLP;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

public class RlpEncodeUtilTest {

  private final PublicKey recipient1 =
      PublicKey.from(Base64.getDecoder().decode("arhIcNa+MuYXZabmzJD5B33F3dZgqb0hEbM3FZsylSg="));
  private final PublicKey recipient2 =
      PublicKey.from(Base64.getDecoder().decode("B687sgdtqsem2qEXO8h8UqvW1Mb3yKo7id5hPFLwCmY="));
  private final PublicKey recipient3 =
      PublicKey.from(Base64.getDecoder().decode("HEkOUBXbgGCQ5+WDFUAhucXm/n5zUrfGkgdJY/5lfCs="));

  @Test
  public void bouncyCastleHexDecodeDoesSameAsTuweniFromHexString() {
    byte[] tuweniVal =
        Bytes.fromHexString("5375ba871e5c3d0f1d055b5da0ac02ea035bed38").toArrayUnsafe();

    byte[] myval = Hex.decode("5375ba871e5c3d0f1d055b5da0ac02ea035bed38");

    assertThat(tuweniVal).isEqualTo(myval);
    assertThat(Arrays.equals(tuweniVal, myval)).isTrue();
  }

  @Test
  public void encoodeListDoesSameAsTuweni() {

    final List<PublicKey> members = List.of(recipient1, recipient2, recipient3);

    List<byte[]> sortedKeys =
        members.stream()
            .distinct()
            .map(PublicKey::getKeyBytes)
            .sorted(Comparator.comparing(Arrays::hashCode))
            .collect(Collectors.toList());

    final byte[] rlpEncoded =
        RLP.encodeList(listWriter -> sortedKeys.forEach(listWriter::writeByteArray)).toArray();

    assertThat(rlpEncoded).isEqualTo(RlpEncodeUtil.encodeList(sortedKeys));
  }

  @Test
  public void encoodeListDoesSameAsTuweni55Length() {

    final byte[] data = new byte[54];
    Arrays.fill(data, (byte) 1);

    final List<PublicKey> members = List.of(PublicKey.from(data));

    List<byte[]> sortedKeys =
        members.stream()
            .distinct()
            .map(PublicKey::getKeyBytes)
            .sorted(Comparator.comparing(Arrays::hashCode))
            .collect(Collectors.toList());

    final byte[] rlpEncoded =
        RLP.encodeList(listWriter -> sortedKeys.forEach(listWriter::writeByteArray)).toArray();

    assertThat(rlpEncoded).isEqualTo(RlpEncodeUtil.encodeList(sortedKeys));
  }

  @Test
  public void encodeRLPElementNull() {
    byte[] result = RlpEncodeUtil.encodeRLPElement(null);
    assertThat(result).isEqualTo(new byte[] {(byte) 0x80});
  }

  @Test
  public void encodeRLPElementEmpty() {
    byte[] result = RlpEncodeUtil.encodeRLPElement(new byte[0]);
    assertThat(result).isEqualTo(new byte[] {(byte) 0x80});
  }

  @Test
  public void encodeRLPElementLength55OrUnder() {
    final byte[] data = new byte[54];
    Arrays.fill(data, (byte) 1);
    byte[] result = RlpEncodeUtil.encodeRLPElement(data);
    assertThat(result)
        .isEqualTo(
            new byte[] {
              -74, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
              1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
            });
  }

  @Test
  public void encodeRLPElementLengthGreaterThan55() {
    final byte[] data = new byte[300];
    Arrays.fill(data, (byte) 1);

    final byte[] result = RlpEncodeUtil.encodeRLPElement(data);

    final byte[] expected = new byte[303];
    expected[0] = -71;
    expected[1] = 44;
    Arrays.fill(expected, 2, 303, (byte) 1);

    assertThat(result.length).isEqualTo(expected.length);
    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void encodeRLPElementLengthOnlyOne() {
    final byte[] data = new byte[] {9};
    byte[] result = RlpEncodeUtil.encodeRLPElement(data);
    assertThat(result).isEqualTo(new byte[] {9});
  }
}
