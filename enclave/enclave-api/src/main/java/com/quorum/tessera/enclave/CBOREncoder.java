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

      generator.writeStartObject(11);
      generator.writeBinaryField("sender", payload.getSenderKey().getKeyBytes());
      generator.writeBinaryField("cipherText", payload.getCipherText());
      generator.writeBinaryField("nonce", payload.getCipherTextNonce().getNonceBytes());
      generator.writeBinaryField("recipientNonce", payload.getRecipientNonce().getNonceBytes());

      generator.writeFieldName("recipientBoxes");
      generator.writeStartArray(payload.getRecipientBoxes().size());
      for (RecipientBox box : payload.getRecipientBoxes()) {
        generator.writeBinary(box.getData());
      }
      generator.writeEndArray();

      generator.writeFieldName("recipients");
      generator.writeStartArray(payload.getRecipientKeys().size());
      for (PublicKey key : payload.getRecipientKeys()) {
        generator.writeBinary(key.getKeyBytes());
      }
      generator.writeEndArray();

      generator.writeNumberField("privacyFlag", payload.getPrivacyMode().getPrivacyFlag());

      generator.writeFieldName("affected");
      generator.writeStartObject(payload.getAffectedContractTransactions().size());
      for (Map.Entry<TxHash, SecurityHash> entry :
          payload.getAffectedContractTransactions().entrySet()) {
        generator.writeFieldName(entry.getKey().encodeToBase64());
        generator.writeBinary(entry.getValue().getData());
      }
      generator.writeEndObject();

      generator.writeBinaryField("execHash", payload.getExecHash());

      generator.writeFieldName("mandatoryFor");
      generator.writeStartArray(payload.getMandatoryRecipients().size());
      for (PublicKey recipient : payload.getMandatoryRecipients()) {
        generator.writeBinary(recipient.getKeyBytes());
      }
      generator.writeEndArray();

      final byte[] privacyGroupId =
          payload.getPrivacyGroupId().map(PrivacyGroup.Id::getBytes).orElse(new byte[0]);

      generator.writeBinaryField("privacyGroupId", privacyGroupId);

      generator.writeEndObject();

      generator.flush();

    } catch (Exception ex) {
      throw new RuntimeException("Unable to encode payload. ", ex);
    }

    return output.toByteArray();
  }

  @Override
  public EncodedPayload decode(byte[] input) {

    EncodedPayload.Builder payloadBuilder = EncodedPayload.Builder.create();

    try (final CBORParser parser = cborFactory.createParser(input)) {

      validateToken(JsonToken.START_OBJECT, parser.nextToken());

      while (parser.nextFieldName() != null) {

        if (parser.getCurrentName().equals("sender")) {
          validateToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
          final byte[] senderKey = parser.getBinaryValue();
          payloadBuilder.withSenderKey(PublicKey.from(senderKey));
          continue;
        }

        if (parser.getCurrentName().equals("cipherText")) {
          validateToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
          final byte[] cipherText = parser.getBinaryValue();
          payloadBuilder.withCipherText(cipherText);
          continue;
        }

        if (parser.getCurrentName().equals("nonce")) {
          validateToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
          final byte[] nonceBytes = parser.getBinaryValue();
          payloadBuilder.withCipherTextNonce(nonceBytes);
          continue;
        }

        if (parser.getCurrentName().equals("recipientNonce")) {
          validateToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
          final byte[] recipientNonceBytes = parser.getBinaryValue();
          payloadBuilder.withRecipientNonce(recipientNonceBytes);
          continue;
        }

        if (parser.getCurrentName().equals("recipients")) {
          validateToken(JsonToken.START_ARRAY, parser.nextToken());
          while (parser.nextToken() != JsonToken.END_ARRAY) {
            final byte[] recipientBytes = parser.getBinaryValue();
            payloadBuilder.withRecipientKey(PublicKey.from(recipientBytes));
          }
          continue;
        }

        if (parser.getCurrentName().equals("recipientBoxes")) {
          validateToken(JsonToken.START_ARRAY, parser.nextToken());
          while (parser.nextToken() != JsonToken.END_ARRAY) {
            final byte[] box = parser.getBinaryValue();
            payloadBuilder.withRecipientBox(box);
          }
          continue;
        }

        if (parser.getCurrentName().equals("privacyFlag")) {
          final int flag = parser.nextIntValue(0);
          payloadBuilder.withPrivacyFlag(flag);
          continue;
        }

        if (parser.getCurrentName().equals("affected")) {
          validateToken(JsonToken.START_OBJECT, parser.nextToken());
          final Map<TxHash, byte[]> affectedTxs = new HashMap<>();
          while (parser.nextToken() != JsonToken.END_OBJECT) {
            final TxHash txHash = new TxHash(parser.currentName());
            validateToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
            final byte[] securityHashBytes = parser.getBinaryValue();
            affectedTxs.put(txHash, securityHashBytes);
          }
          payloadBuilder.withAffectedContractTransactions(affectedTxs);
          continue;
        }

        if (parser.getCurrentName().equals("execHash")) {
          validateToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
          final byte[] execHash = parser.getBinaryValue();
          payloadBuilder.withExecHash(execHash);
          continue;
        }

        if (parser.getCurrentName().equals("mandatoryFor")) {
          validateToken(JsonToken.START_ARRAY, parser.nextToken());
          final Set<PublicKey> mandatoryRecipients = new HashSet<>();
          while (parser.nextToken() != JsonToken.END_ARRAY) {
            final byte[] recipient = parser.getBinaryValue();
            mandatoryRecipients.add(PublicKey.from(recipient));
          }
          payloadBuilder.withMandatoryRecipients(mandatoryRecipients);
          continue;
        }

        if (parser.getCurrentName().equals("privacyGroupId")) {
          validateToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
          final byte[] groupId = parser.getBinaryValue();
          if (groupId.length > 0)
            payloadBuilder.withPrivacyGroupId(PrivacyGroup.Id.fromBytes(groupId));
        }
      }
    } catch (Exception ex) {
      throw new RuntimeException("Unable to decode payload data. ", ex);
    }

    return payloadBuilder.build();
  }

  @Override
  public EncodedPayloadCodec encodedPayloadCodec() {
    return EncodedPayloadCodec.CBOR;
  }

  private void validateToken(JsonToken expected, JsonToken current) {
    if (current != expected) {
      throw new IllegalArgumentException("Invalid payload data");
    }
  }
}
