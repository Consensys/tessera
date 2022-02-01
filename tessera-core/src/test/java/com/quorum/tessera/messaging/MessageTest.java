package com.quorum.tessera.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.base64.Base64Codec;
import com.quorum.tessera.base64.DecodingException;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Base64;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MessageTest {
  private PublicKey sender;
  private PublicKey recipient;
  private Message message;

  @Before
  public void setUp() {
    sender = mock(PublicKey.class);
    recipient = mock(PublicKey.class);
    message = mock(Message.class);
  }

  @Test
  public void testGetSender() {

    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getSenderKey()).thenReturn(sender);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(sender, recipient));

    PublicKey key = sender;
    when(message.getSender()).thenReturn(key);
    Assert.assertNotNull(message.getSender());
    Assert.assertNotNull(key);

    String senderText = "This is for sender";
    byte sender[] = senderText.getBytes();
    String receiverText = "This is for receiver";
    byte receiver[] = receiverText.getBytes();

    String data = "this is message";

    final PublicKey senderKey = PublicKey.from(sender);
    final PublicKey receiverKey = PublicKey.from(receiver);

    assertThat(senderKey).isNotNull();
    assertThat(receiverKey).isNotNull();
    Message messageObj = new Message(senderKey, receiverKey, data.getBytes());
    assertThat(messageObj).isNotNull();
    assertThat(messageObj.getSender()).isNotNull();
    assertThat(messageObj.getSender()).isSameAs(senderKey);
    assertThat(messageObj.getRecipient()).isSameAs(receiverKey);
    assertThat(messageObj.getData()).isEqualTo(data.getBytes());

    assertThat(messageObj.getSender()).doesNotHaveSameClassAs(new String());
    assertThat(messageObj.getSender()).hasNoNullFieldsOrProperties();
    assertThat(messageObj.toString()).isNotEmpty();
  }

  @Test
  public void testGetRecipient() {
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getSenderKey()).thenReturn(sender);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(sender, recipient));

    PublicKey key = recipient;
    when(message.getRecipient()).thenReturn(key);
    Message messageObj = new Message(sender, recipient, "test".getBytes());
    assertThat(messageObj).isNotNull();
    assertThat(messageObj.getRecipient()).isNotNull();

    String senderText = "This is for sender";
    byte sender[] = senderText.getBytes();
    String receiverText = "This is for receiver";
    byte receiver[] = receiverText.getBytes();

    String data = "this is message";

    final PublicKey senderKey = PublicKey.from(sender);
    final PublicKey receiverKey = PublicKey.from(receiver);

    assertThat(senderKey).isNotNull();
    assertThat(receiverKey).isNotNull();
    Message messageObject = new Message(senderKey, receiverKey, data.getBytes());
    assertThat(messageObject).isNotNull();
    assertThat(messageObject.getSender()).isNotNull();
    assertThat(messageObject.getSender()).isSameAs(senderKey);
    assertThat(messageObject.getRecipient()).isSameAs(receiverKey);
    assertThat(messageObject.getData()).isEqualTo(data.getBytes());
  }

  @Test
  public void testGetData() {
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getSenderKey()).thenReturn(sender);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(sender, recipient));

    Message messageObj = new Message(sender, recipient, "test".getBytes());
    assertThat(messageObj).isNotNull();
    assertThat(messageObj.getData()).isNotNull();

    String senderText = "This is for sender";
    byte sender[] = senderText.getBytes();
    String receiverText = "This is for receiver";
    byte receiver[] = receiverText.getBytes();
    String data = "this is message";

    final PublicKey senderKey = PublicKey.from(sender);
    final PublicKey receiverKey = PublicKey.from(receiver);

    Base64Codec base64Codec = new Base64Codec() {};

    assertThat(base64Codec).isNotNull();
    Message message = new Message(senderKey, receiverKey, data.getBytes());
    String encodedData = base64Codec.encodeToString(message.getData());
    byte output[] = base64Codec.decode(encodedData);
    assertThat(encodedData.equals(new String(output)));
  }

  @Test
  public void testConstructorWithArgs() {

    Message message = new Message(sender, recipient, "test".getBytes());
    Assert.assertNotNull(message);
    String senderText = "This is for sender";
    byte sender[] = senderText.getBytes();
    String receiverText = "This is for receiver";
    byte receiver[] = receiverText.getBytes();

    String data = "this is message";

    final PublicKey senderKey = PublicKey.from(sender);
    final PublicKey receiverKey = PublicKey.from(receiver);

    assertThat(senderKey).isNotNull();
    assertThat(receiverKey).isNotNull();
    Message messageObject = new Message(senderKey, receiverKey, data.getBytes());
    assertThat(messageObject).isNotNull();
    assertThat(messageObject.getSender()).isNotNull();
    assertThat(messageObject.getSender()).isSameAs(senderKey);
    assertThat(messageObject.getRecipient()).isSameAs(receiverKey);
    assertThat(messageObject.getData()).isEqualTo(data.getBytes());
  }

  @Test(expected = DecodingException.class)
  public void invalidBase64DataThrowsDecodeException() {
    Base64Codec.create().decode("1");
  }

  @Test
  public void decode() {
    byte[] result = Base64Codec.create().decode("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void encodeToString() {
    byte[] data = "Read".getBytes();
    String expected = Base64.getEncoder().encodeToString(data);
    String result = Base64Codec.create().encodeToString(data);
    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void testToString() {
    String senderText = "This is for sender";
    byte sender[] = senderText.getBytes();
    String receiverText = "This is for receiver";
    byte receiver[] = receiverText.getBytes();

    String data = "this is message";

    final PublicKey senderKey = PublicKey.from(sender);
    final PublicKey receiverKey = PublicKey.from(receiver);

    assertThat(senderKey).isNotNull();
    assertThat(receiverKey).isNotNull();
    Message messageObject = new Message(senderKey, receiverKey, data.getBytes());

    String encoded = messageObject.getSender().encodeToBase64();
    assertThat(encoded).isNotEmpty();
    assertThat(encoded).isBase64();

    String encodedReceiver = messageObject.getRecipient().encodeToBase64();
    assertThat(encodedReceiver).isNotEmpty();
    assertThat(encodedReceiver).isBase64();
  }
}
