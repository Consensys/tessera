package com.quorum.tessera.enclave;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import com.quorum.tessera.encryption.PublicKey;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class CBOREncoder implements PayloadEncoder {

  final CBORFactory cborFactory = new CBORFactory();

  @Override
  public byte[] encode(EncodedPayload payload) {

    ByteArrayOutputStream output = new ByteArrayOutputStream();

    try (CBORGenerator generator = cborFactory.createGenerator(output)) {

      generator.writeBinary(payload.getSenderKey().getKeyBytes());
      generator.writeBinary(payload.getCipherText());
      generator.writeBinary(payload.getCipherTextNonce().getNonceBytes());
      generator.writeBinary(payload.getRecipientNonce().getNonceBytes());

      generator.writeStartArray();
      for (RecipientBox box : payload.getRecipientBoxes()) {
        generator.writeBinary(box.getData());
      }
      generator.writeEndArray();

      generator.writeStartArray();
      for (PublicKey recipient : payload.getRecipientKeys()) {
        generator.writeBinary(recipient.getKeyBytes());
      }
      generator.writeEndArray();

      generator.writeNumber(payload.getPrivacyMode().getPrivacyFlag());

      generator.writeStartArray();
      for (Map.Entry<TxHash, SecurityHash> entry :
          payload.getAffectedContractTransactions().entrySet()) {
        generator.writeBinary(entry.getKey().getBytes());
        generator.writeBinary(entry.getValue().getData());
      }
      generator.writeEndArray();

      generator.writeBinary(payload.getExecHash());

      generator.writeStartArray();
      for (PublicKey mr : payload.getMandatoryRecipients()) {
        generator.writeBinary(mr.getKeyBytes());
      }
      generator.writeEndArray();

      final byte[] privacyGroupId =
          payload.getPrivacyGroupId().map(PrivacyGroup.Id::getBytes).orElse(new byte[0]);

      generator.writeBinary(privacyGroupId);

      generator.flush();

    } catch (Exception ex) {
      throw new RuntimeException("Unable to encode payload. ", ex);
    }

    final byte[] encoded = output.toByteArray();

    System.out.println("Encode AAAAA : " + Base64.getEncoder().encodeToString(encoded));

    return encoded;
  }

  @Override
  public EncodedPayload decode(byte[] input) {

    EncodedPayload.Builder builder = EncodedPayload.Builder.create();

    try (final CBORParser parser = cborFactory.createParser(input)) {

      parser.nextToken();
      builder.withSenderKey(PublicKey.from(parser.getBinaryValue()));

      parser.nextToken();
      builder.withCipherText(parser.getBinaryValue());

      parser.nextToken();
      builder.withCipherTextNonce(parser.getBinaryValue());

      parser.nextToken();
      builder.withRecipientNonce(parser.getBinaryValue());

      parser.nextToken();
      while (JsonToken.END_ARRAY != parser.nextToken()) {
        final byte[] box = parser.getBinaryValue();
        builder.withRecipientBox(box);
      }

      parser.nextToken();
      while (JsonToken.END_ARRAY != parser.nextToken()) {
        final PublicKey recipient = PublicKey.from(parser.getBinaryValue());
        builder.withRecipientKey(recipient);
      }

      parser.nextToken();
      final PrivacyMode privacyMode = PrivacyMode.fromFlag(parser.getIntValue());
      builder.withPrivacyMode(privacyMode);

      parser.nextToken();
      Map<TxHash, byte[]> affectedTxs = new HashMap<>();
      while (JsonToken.END_ARRAY != parser.nextToken()) {
        TxHash txHash = TxHash.from(parser.getBinaryValue());
        parser.nextToken();
        byte[] securityHash = parser.getBinaryValue();
        affectedTxs.put(txHash, securityHash);
      }
      builder.withAffectedContractTransactions(affectedTxs);

      parser.nextToken();
      builder.withExecHash(parser.getBinaryValue());

      parser.nextToken();
      final Set<PublicKey> mandatoryRecipients = new HashSet<>();
      while (JsonToken.END_ARRAY != parser.nextToken()) {
        final PublicKey recipient = PublicKey.from(parser.getBinaryValue());
        mandatoryRecipients.add(recipient);
      }
      builder.withMandatoryRecipients(mandatoryRecipients);

      parser.nextToken();
      final byte[] privacyGroupId = parser.getBinaryValue();

      if (privacyGroupId.length > 0) {
        builder.withPrivacyGroupId(PrivacyGroup.Id.fromBytes(privacyGroupId));
      }

    } catch (Exception ex) {
      throw new RuntimeException("Unable to decode payload data. ", ex);
    }

    return builder.build();
  }

  @Override
  public EncodedPayloadCodec encodedPayloadCodec() {
    return EncodedPayloadCodec.CBOR;
  }
}
