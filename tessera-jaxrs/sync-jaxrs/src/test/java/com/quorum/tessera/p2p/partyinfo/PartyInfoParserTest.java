package com.quorum.tessera.p2p.partyinfo;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import java.nio.ByteBuffer;
import java.util.Base64;
import org.junit.Before;
import org.junit.Test;

public class PartyInfoParserTest {

  private final int[] sampleDataTwo =
      new int[] {
        0, 0, 0, 0, 0, 0, 0, 23, 104, 116, 116, 112, 115, 58, 47, 47, 49, 50, 55, 46, 48, 46, 48,
        46, 52, 58, 57, 48, 48, 52, 47, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 32, 214, 36,
        217, 117, 239, 231, 116, 17, 239, 206, 23, 37, 237, 94, 188, 199, 142, 21, 199, 186, 101,
        242, 124, 49, 244, 186, 167, 51, 240, 31, 37, 72, 0, 0, 0, 0, 0, 0, 0, 23, 104, 116, 116,
        112, 115, 58, 47, 47, 49, 50, 55, 46, 48, 46, 48, 46, 51, 58, 57, 48, 48, 51, 47, 0, 0, 0,
        0, 0, 0, 0, 32, 81, 243, 82, 121, 33, 178, 73, 226, 160, 215, 80, 213, 52, 73, 234, 173, 75,
        113, 97, 21, 104, 175, 143, 130, 190, 91, 136, 242, 213, 111, 235, 102, 0, 0, 0, 0, 0, 0, 0,
        23, 104, 116, 116, 112, 115, 58, 47, 47, 49, 50, 55, 46, 48, 46, 48, 46, 54, 58, 57, 48, 48,
        54, 47, 0, 0, 0, 0, 0, 0, 0, 32, 160, 219, 41, 60, 248, 44, 205, 85, 5, 195, 74, 166, 24,
        87, 214, 194, 29, 110, 197, 85, 23, 130, 240, 113, 149, 229, 206, 68, 120, 244, 238, 15, 0,
        0, 0, 0, 0, 0, 0, 23, 104, 116, 116, 112, 115, 58, 47, 47, 49, 50, 55, 46, 48, 46, 48, 46,
        52, 58, 57, 48, 48, 52, 47, 0, 0, 0, 0, 0, 0, 0, 32, 71, 158, 160, 203, 135, 103, 219, 134,
        14, 143, 12, 158, 177, 55, 51, 97, 175, 38, 231, 24, 79, 234, 17, 118, 185, 51, 2, 142, 239,
        245, 198, 70, 0, 0, 0, 0, 0, 0, 0, 23, 104, 116, 116, 112, 115, 58, 47, 47, 49, 50, 55, 46,
        48, 46, 48, 46, 53, 58, 57, 48, 48, 53, 47, 0, 0, 0, 0, 0, 0, 0, 32, 68, 224, 25, 5, 107,
        82, 105, 204, 87, 66, 179, 158, 220, 81, 128, 168, 144, 242, 38, 49, 94, 61, 30, 92, 123,
        132, 210, 35, 57, 137, 208, 23, 0, 0, 0, 0, 0, 0, 0, 23, 104, 116, 116, 112, 115, 58, 47,
        47, 49, 50, 55, 46, 48, 46, 48, 46, 55, 58, 57, 48, 48, 55, 47, 0, 0, 0, 0, 0, 0, 0, 32, 5,
        66, 222, 71, 194, 114, 81, 104, 98, 186, 224, 140, 83, 241, 203, 3, 68, 57, 167, 57, 24, 79,
        231, 7, 32, 141, 217, 40, 23, 178, 220, 26, 0, 0, 0, 0, 0, 0, 0, 23, 104, 116, 116, 112,
        115, 58, 47, 47, 49, 50, 55, 46, 48, 46, 48, 46, 49, 58, 57, 48, 48, 49, 47, 0, 0, 0, 0, 0,
        0, 0, 32, 65, 247, 131, 3, 43, 61, 48, 240, 236, 217, 113, 196, 198, 215, 60, 226, 50, 134,
        31, 22, 96, 253, 168, 249, 216, 52, 225, 210, 251, 64, 221, 119, 0, 0, 0, 0, 0, 0, 0, 23,
        104, 116, 116, 112, 115, 58, 47, 47, 49, 50, 55, 46, 48, 46, 48, 46, 50, 58, 57, 48, 48, 50,
        47, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 23, 104, 116, 116, 112, 115, 58, 47, 47,
        49, 50, 55, 46, 48, 46, 48, 46, 49, 58, 57, 48, 48, 49, 47, 0, 0, 0, 0, 0, 0, 0, 23, 104,
        116, 116, 112, 115, 58, 47, 47, 49, 50, 55, 46, 48, 46, 48, 46, 55, 58, 57, 48, 48, 55, 47,
        0, 0, 0, 0, 0, 0, 0, 23, 104, 116, 116, 112, 115, 58, 47, 47, 49, 50, 55, 46, 48, 46, 48,
        46, 54, 58, 57, 48, 48, 54, 47, 0, 0, 0, 0, 0, 0, 0, 23, 104, 116, 116, 112, 115, 58, 47,
        47, 49, 50, 55, 46, 48, 46, 48, 46, 52, 58, 57, 48, 48, 52, 47, 0, 0, 0, 0, 0, 0, 0, 23,
        104, 116, 116, 112, 115, 58, 47, 47, 49, 50, 55, 46, 48, 46, 48, 46, 50, 58, 57, 48, 48, 50,
        47, 0, 0, 0, 0, 0, 0, 0, 23, 104, 116, 116, 112, 115, 58, 47, 47, 49, 50, 55, 46, 48, 46,
        48, 46, 53, 58, 57, 48, 48, 53, 47, 0, 0, 0, 0, 0, 0, 0, 23, 104, 116, 116, 112, 115, 58,
        47, 47, 49, 50, 55, 46, 48, 46, 48, 46, 51, 58, 57, 48, 48, 51, 47, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
      };

  private byte[] dataTwo;

  private final int[] sampleDataOne =
      new int[] {
        0, 0, 0, 0, 0, 0, 0, 21, // URL index
        104, 116, 116, 112, 58, 47, 47, 108, 111, 99, 97, 108, 104, 111, 115, 116, 58, 56, 48, 48,
        48, // URL data 8-21
        0, 0, 0, 0, 0, 0, 0, 1, // Number of recipients
        0, 0, 0, 0, 0, 0, 0, 32, // recipient Key length
        216, 17, 154, 12, 190, 199, 22, 18, 28, 2, 208, 62, 196, 51, 102, 28, 204, 27, 44, 163, 139,
        255, 186, 192, 111, 73, 209, 61, 101, 17, 101, 32, // Recipient key
        0, 0, 0, 0, 0, 0, 0, 21, // recipient value length/ URL
        104, 116, 116, 112, 58, 47, 47, 108, 111, 99, 97, 108, 104, 111, 115, 116, 58, 56, 48, 48,
        49, 0, 0, 0, 0, 0, 0, 0, 1, // Number of parties
        0, 0, 0, 0, 0, 0, 0, 21, // Length of party url
        104, 116, 116, 112, 58, 47, 47, 108, 111, 99, 97, 108, 104, 111, 115, 116, 58, 56, 48, 48,
        49, // party URL data
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0
      };

  private byte[] dataOne;

  private PartyInfoParser partyInfoParser = PartyInfoParser.create();

  @Before
  public void setUp() {

    this.dataOne = new byte[sampleDataOne.length];
    for (int i = 0; i < sampleDataOne.length; i++) {
      this.dataOne[i] = Integer.valueOf(sampleDataOne[i]).byteValue();
    }

    this.dataTwo = new byte[sampleDataTwo.length];
    for (int i = 0; i < sampleDataTwo.length; i++) {
      this.dataTwo[i] = Integer.valueOf(sampleDataTwo[i]).byteValue();
    }
  }

  @Test
  public void multiplePartiesParses() {

    final PartyInfo result = partyInfoParser.from(dataTwo);

    assertThat(result.getParties())
        .containsExactlyInAnyOrder(
            new Party("https://127.0.0.5:9005/"),
            new Party("https://127.0.0.3:9003/"),
            new Party("https://127.0.0.1:9001/"),
            new Party("https://127.0.0.7:9007/"),
            new Party("https://127.0.0.6:9006/"),
            new Party("https://127.0.0.4:9004/"),
            new Party("https://127.0.0.2:9002/"));

    assertThat(result.getRecipients())
        .containsExactlyInAnyOrder(
            Recipient.of(
                toKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc="), "https://127.0.0.7:9007/"),
            Recipient.of(
                toKey("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo="), "https://127.0.0.1:9001/"),
            Recipient.of(
                toKey("QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc="), "https://127.0.0.2:9002/"),
            Recipient.of(
                toKey("1iTZde/ndBHvzhcl7V68x44Vx7pl8nwx9LqnM/AfJUg="), "https://127.0.0.3:9003/"),
            Recipient.of(
                toKey("UfNSeSGySeKg11DVNEnqrUtxYRVor4+CvluI8tVv62Y="), "https://127.0.0.6:9006/"),
            Recipient.of(
                toKey("oNspPPgszVUFw0qmGFfWwh1uxVUXgvBxleXORHj07g8="), "https://127.0.0.4:9004/"),
            Recipient.of(
                toKey("R56gy4dn24YOjwyesTczYa8m5xhP6hF2uTMCju/1xkY="), "https://127.0.0.5:9005/"));
  }

  private PublicKey toKey(final String b64) {
    return PublicKey.from(Base64.getDecoder().decode(b64));
  }

  @Test
  public void from() {

    PartyInfo result = partyInfoParser.from(dataOne);

    assertThat(result).isNotNull();

    assertThat(result.getUrl()).isEqualTo("http://localhost:8000");

    assertThat(result.getRecipients()).hasSize(1);
    final Recipient recipient = result.getRecipients().iterator().next();
    assertThat(recipient.getUrl()).isEqualTo("http://localhost:8001");
    assertThat(recipient.getKey()).isNotNull();

    assertThat(result.getParties()).hasSize(1);
    assertThat(result.getParties()).containsExactly(new Party("http://localhost:8001"));
  }

  @Test
  public void toUsingSameInfoFromFixture() {

    final PartyInfo partyInfo = partyInfoParser.from(dataOne);
    final byte[] result = partyInfoParser.to(partyInfo);

    final ByteBuffer byteBuffer = ByteBuffer.wrap(result);

    assertThat(result).isNotEmpty();
    assertThat(byteBuffer.getLong()).isEqualTo(21L);

    byte[] urlData = new byte[21];
    byteBuffer.get(urlData);

    final String url = new String(urlData);
    assertThat(url).isEqualTo(partyInfo.getUrl());

    long numberOfRecipients = byteBuffer.getLong();
    assertThat(numberOfRecipients).isEqualTo(1L);

    long keyByteLength = byteBuffer.getLong();
    assertThat(keyByteLength).isEqualTo(32L);

    final byte[] keyData = new byte[32];
    byteBuffer.get(keyData);
    assertThat(keyData)
        .hasSize(32)
        .isEqualTo(partyInfo.getRecipients().iterator().next().getKey().getKeyBytes());

    long recipientUrlLength = byteBuffer.getLong();
    assertThat(recipientUrlLength).isEqualTo(21L);

    byte[] recipientUrlData = new byte[21];
    byteBuffer.get(recipientUrlData);

    String recipientUrl = new String(recipientUrlData);
    assertThat(recipientUrl).isEqualTo(partyInfo.getRecipients().iterator().next().getUrl());

    long partyCount = byteBuffer.getLong();
    assertThat(partyCount).isEqualTo(1L);

    long partyUrlLength = byteBuffer.getLong();
    assertThat(partyUrlLength).isEqualTo(22L);

    byte[] partyUrlData = new byte[22];
    byteBuffer.get(partyUrlData);

    String partyUrl = new String(partyUrlData);
    assertThat(partyUrl).isEqualTo(partyInfo.getParties().iterator().next().getUrl());
  }

  @Test
  public void checkLengthZero() {
    PartyInfoParser.checkLength(0);
    // NO ERROR
  }

  @Test(expected = PartyInfoParserException.class)
  public void checkLengthMinusValue() {
    PartyInfoParser.checkLength(-1);
  }

  @Test
  public void checkLengthZeroLong() {
    PartyInfoParser.checkLength((long) 0);
  }

  @Test(expected = PartyInfoParserException.class)
  public void checkLengthMaxValue() {
    PartyInfoParser.checkLength(Integer.MAX_VALUE);
  }

  @Test(expected = PartyInfoParserException.class)
  public void checkLengthMinusValueLong() {
    PartyInfoParser.checkLength((long) -1);
  }

  @Test(expected = PartyInfoParserException.class)
  public void checkLengthMaxValueLong() {
    PartyInfoParser.checkLength(Long.MAX_VALUE);
  }
}
