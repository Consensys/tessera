package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.rlp.RLP;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public interface PrivacyGroupUtil extends BinaryEncoder {

    /*
    This bytes is added to the list of addresses to generate id for querying purpose.
    The matches the same value being used in Orion to maintain backward compatibility
     */
    byte[] BYTES = Bytes.fromHexString("5375ba871e5c3d0f1d055b5da0ac02ea035bed38").toArrayUnsafe();

    default byte[] generateId(final List<PublicKey> addresses, final byte[] seed) {

        final List<byte[]> sortedKeys =
                addresses.stream()
                        .distinct()
                        .map(PublicKey::getKeyBytes)
                        .sorted(Comparator.comparing(Arrays::hashCode))
                        .collect(Collectors.toList());

        Optional.ofNullable(seed).ifPresent(sortedKeys::add);

        final byte[] rlpEncoded =
                RLP.encodeList(listWriter -> sortedKeys.forEach(listWriter::writeByteArray)).toArray();

        return new Keccak.Digest256().digest(rlpEncoded);
    }

    default byte[] generateId(final List<PublicKey> addresses) {
        return generateId(addresses, null);
    }

    default byte[] generateLookupId(final List<PublicKey> addresses) {
        return generateId(addresses, BYTES);
    }

    default byte[] encode(final PrivacyGroup privacyGroup) {

        final byte[] name = encodeField(privacyGroup.getName().getBytes());
        final byte[] description = encodeField(privacyGroup.getDescription().getBytes());
        final byte[] members =
                encodeArray(
                        privacyGroup.getMembers().stream()
                                .map(PublicKey::getKeyBytes)
                                .collect(Collectors.toUnmodifiableList()));
        final byte[] seed = encodeField(privacyGroup.getSeed());
        final byte[] type = encodeField(privacyGroup.getType().name().getBytes());
        final byte[] state = encodeField(privacyGroup.getState().name().getBytes());

        return ByteBuffer.allocate(
                        name.length + description.length + members.length + seed.length + type.length + state.length)
                .put(name)
                .put(description)
                .put(members)
                .put(seed)
                .put(type)
                .put(state)
                .array();
    }

    default PrivacyGroup decode(final byte[] encoded) {

        final ByteBuffer buffer = ByteBuffer.wrap(encoded);

        final long nameSize = buffer.getLong();
        final byte[] name = new byte[Math.toIntExact(nameSize)];
        buffer.get(name);

        final long descriptionSize = buffer.getLong();
        final byte[] description = new byte[Math.toIntExact(descriptionSize)];
        buffer.get(description);

        final long numberOfMembers = buffer.getLong();
        final List<byte[]> members = new ArrayList<>();
        for (long i = 0; i < numberOfMembers; i++) {
            final long boxSize = buffer.getLong();
            final byte[] box = new byte[Math.toIntExact(boxSize)];
            buffer.get(box);
            members.add(box);
        }
        final List<PublicKey> memberKeys = members.stream().map(PublicKey::from).collect(Collectors.toList());

        final long seedSize = buffer.getLong();
        final byte[] seed = new byte[Math.toIntExact(seedSize)];
        buffer.get(seed);

        final long typeSize = buffer.getLong();
        final byte[] type = new byte[Math.toIntExact(typeSize)];
        buffer.get(type);
        final PrivacyGroup.Type pgType = PrivacyGroup.Type.valueOf(new String(type));

        final long stateSize = buffer.getLong();
        final byte[] state = new byte[Math.toIntExact(stateSize)];
        buffer.get(state);

        final byte[] groupId;
        if (pgType == PrivacyGroup.Type.LEGACY) groupId = generateId(memberKeys);
        else groupId = generateId(memberKeys, seed);

        return PrivacyGroup.Builder.create()
                .withPrivacyGroupId(PrivacyGroup.Id.fromBytes(groupId))
                .withName(new String(name))
                .withDescription(new String(description))
                .withMembers(memberKeys)
                .withSeed(seed)
                .withType(PrivacyGroup.Type.valueOf(new String(type)))
                .withState(PrivacyGroup.State.valueOf(new String(state)))
                .build();
    }

    static PrivacyGroupUtil create() {
        return new PrivacyGroupUtil() {};
    }
}
