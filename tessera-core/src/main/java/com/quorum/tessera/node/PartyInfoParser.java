package com.quorum.tessera.node;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.node.model.Recipient;
import com.quorum.tessera.util.BinaryEncoder;
import static java.lang.Math.toIntExact;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Optional;

/**
 * A parser for PartyInfo node discovery information
 */
public interface PartyInfoParser extends BinaryEncoder {

    /**
     * Decodes a set of PartyInfo to the format that is shared between nodes
     *
     * @param encoded the encoded information that needs to be read
     * @return the decoded {@link PartyInfo} which contains the other nodes
     * information
     */
    default PartyInfo from(final byte[] encoded) {

        final ByteBuffer byteBuffer = ByteBuffer.wrap(encoded);

        final int urlLength = (int) toIntExact(byteBuffer.getLong());
        checkLength(urlLength);


        final byte[] urlBytes = new byte[urlLength];
        byteBuffer.get(urlBytes);
        final String url = new String(urlBytes, UTF_8);

        final int numberOfRecipients = (int) toIntExact(byteBuffer.getLong());
        checkLength(urlLength);

        final Set<Recipient> recipients = new HashSet<>();

        for (int i = 0; i < numberOfRecipients; i++) {

            final int recipientKeyLength = (int) toIntExact(byteBuffer.getLong());
            checkLength(recipientKeyLength);
            
            final byte[] recipientKeyBytes = new byte[recipientKeyLength];
            byteBuffer.get(recipientKeyBytes);

            final int recipientUrlValueLength = (int) toIntExact(byteBuffer.getLong());
            checkLength(recipientUrlValueLength);
            
            final byte[] urlValueData = new byte[recipientUrlValueLength];
            
            
            byteBuffer.get(urlValueData);
            final String recipientUrl = new String(urlValueData, UTF_8);

            recipients.add(new Recipient(PublicKey.from(recipientKeyBytes), recipientUrl));

        }

        final int partyCount = (int) toIntExact(byteBuffer.getLong());
        checkLength(partyCount);
        
        final Party[] parties = new Party[partyCount];
        for (int i = 0; i < partyCount; i++) {
            long partyElementLength = byteBuffer.getLong();
            checkLength(partyElementLength);
            
            byte[] ptyData = new byte[(int) toIntExact(partyElementLength)];
            byteBuffer.get(ptyData);
            String ptyURL = new String(ptyData, UTF_8);
            parties[i] = new Party(ptyURL);
        }

        return new PartyInfo(url, recipients, new HashSet<>(Arrays.asList(parties)));
    }

    /**
     * Encodes a {@link PartyInfo} object to the defined structure that is
     * shared between nodes
     *
     * The result can be feed into {@link PartyInfoParser#from(byte[])} to
     * produce the input to this function.
     *
     * @param partyInfo the information to encode
     * @return the encoded result that should be shared with other nodes
     */
    default byte[] to(final PartyInfo partyInfo) {

        //prefix and url bytes
        final byte[] url = encodeField(partyInfo.getUrl().getBytes(UTF_8));

        //each element in the list is one encoded element from the map
        //so the prefix is always 2 (2 elements) and
        final List<byte[]> recipients = partyInfo.getRecipients()
                .stream()
                .map(r -> {
                    final byte[] encodedKey = encodeField(r.getKey().getKeyBytes());
                    final byte[] encodedUrl = encodeField(r.getUrl().getBytes(UTF_8));

                    //using Apache Commons array utils since it is already available
                    //other concat the two arrays manually
                    return ArrayUtils.addAll(encodedKey, encodedUrl);
                }).collect(Collectors.toList());
        final int recipientLength = recipients.stream().mapToInt(r -> r.length).sum();

        final List<byte[]> parties = partyInfo.getParties()
                .stream()
                .map(p -> p.getUrl().getBytes(UTF_8))
                .collect(Collectors.toList());

        final byte[] partiesBytes = encodeArray(parties);

        final ByteBuffer byteBuffer = ByteBuffer
                .allocate(url.length + Long.BYTES + recipientLength + partiesBytes.length)
                .put(url)
                .putLong(partyInfo.getRecipients().size());

        recipients.forEach(byteBuffer::put);
        byteBuffer.put(partiesBytes);

        return byteBuffer.array();

    }

    /**
     * Creates a new parser with default settings
     *
     * @return a default parser
     */
    static PartyInfoParser create() {
        return new PartyInfoParser() {
        };
    }

    
    static boolean checkLength(long value) {
      return Optional.of(value)
                .filter(v -> v >= 0)
                .filter(v -> v < Long.MAX_VALUE - 1)
               .isPresent();
    }

    static boolean checkLength(int value) {
        return Optional.of(value)
                .filter(v -> v >= 0)
                .filter(v -> v < Integer.MAX_VALUE - 1)
                .isPresent();
    }

}
