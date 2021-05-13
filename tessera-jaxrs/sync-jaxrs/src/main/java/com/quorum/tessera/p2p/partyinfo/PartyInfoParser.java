package com.quorum.tessera.p2p.partyinfo;

import static java.lang.Math.toIntExact;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.quorum.tessera.enclave.BinaryEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;

/** A parser for PartyInfo node discovery information */
public interface PartyInfoParser extends BinaryEncoder {

  /**
   * Decodes a set of PartyInfo to the format that is shared between nodes
   *
   * @param encoded the encoded information that needs to be read
   * @return the decoded {@link PartyInfo} which contains the other nodes information
   */
  default PartyInfo from(final byte[] encoded) {

    final ByteBuffer byteBuffer = ByteBuffer.wrap(encoded);

    final int urlLength = toIntExact(byteBuffer.getLong());
    checkLength(urlLength);

    final byte[] urlBytes = new byte[urlLength];
    byteBuffer.get(urlBytes);
    final String url = new String(urlBytes, UTF_8);

    final int numberOfRecipients = toIntExact(byteBuffer.getLong());
    checkLength(numberOfRecipients);

    final Set<Recipient> recipients = new HashSet<>();
    for (int i = 0; i < numberOfRecipients; i++) {

      final int recipientKeyLength = toIntExact(byteBuffer.getLong());
      checkLength(recipientKeyLength);

      final byte[] recipientKeyBytes = new byte[recipientKeyLength];
      byteBuffer.get(recipientKeyBytes);

      final int recipientUrlValueLength = toIntExact(byteBuffer.getLong());
      checkLength(recipientUrlValueLength);

      final byte[] urlValueData = new byte[recipientUrlValueLength];

      byteBuffer.get(urlValueData);
      final String recipientUrl = new String(urlValueData, UTF_8);

      recipients.add(Recipient.of(PublicKey.from(recipientKeyBytes), recipientUrl));
    }

    final int partyCount = toIntExact(byteBuffer.getLong());
    checkLength(partyCount);

    final Set<Party> parties = new HashSet<>();
    for (int i = 0; i < partyCount; i++) {
      long partyElementLength = byteBuffer.getLong();
      checkLength(partyElementLength);

      byte[] ptyData = new byte[toIntExact(partyElementLength)];
      byteBuffer.get(ptyData);
      parties.add(new Party(new String(ptyData, UTF_8)));
    }

    return new PartyInfo(url, recipients, parties);
  }

  /**
   * Encodes a {@link PartyInfo} object to the defined structure that is shared between nodes
   *
   * <p>The result can be feed into {@link PartyInfoParser#from(byte[])} to produce the input to
   * this function.
   *
   * @param partyInfo the information to encode
   * @return the encoded result that should be shared with other nodes
   */
  default byte[] to(final PartyInfo partyInfo) {

    // prefix and url bytes
    final byte[] url = encodeField(partyInfo.getUrl().getBytes(UTF_8));

    // each element in the list is one encoded element from the map
    // so the prefix is always 2 (2 elements) and
    final byte[] recipients =
        partyInfo.getRecipients().stream()
            .map(
                r -> {
                  final byte[] encodedKey = encodeField(r.getKey().getKeyBytes());
                  final byte[] encodedUrl = encodeField(r.getUrl().getBytes(UTF_8));
                  return ArrayUtils.addAll(encodedKey, encodedUrl);
                })
            .reduce(new byte[0], ArrayUtils::addAll);

    final List<byte[]> parties =
        partyInfo.getParties().stream()
            .map(p -> p.getUrl().getBytes(UTF_8))
            .collect(Collectors.toList());

    final byte[] partiesBytes = encodeArray(parties);

    return ByteBuffer.allocate(url.length + Long.BYTES + recipients.length + partiesBytes.length)
        .put(url)
        .putLong(partyInfo.getRecipients().size())
        .put(recipients)
        .put(partiesBytes)
        .array();
  }

  /**
   * Creates a new parser with default settings
   *
   * @return a default parser
   */
  static PartyInfoParser create() {
    return new PartyInfoParser() {};
  }

  static void checkLength(long value) {
    Optional.of(value)
        .filter(v -> v >= 0)
        .filter(v -> v < Long.MAX_VALUE - 1)
        .orElseThrow(() -> new PartyInfoParserException("Invalid length " + value));
  }

  static void checkLength(int value) {
    Optional.of(value)
        .filter(v -> v >= 0)
        .filter(v -> v < Integer.MAX_VALUE - 1)
        .orElseThrow(() -> new PartyInfoParserException("Invalid length " + value));
  }
}
