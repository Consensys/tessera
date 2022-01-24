package com.quorum.tessera.messaging;

import com.quorum.tessera.base64.Base64Codec;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageTest{
  private PublicKey sender;
  private PublicKey recipient;
  private byte[] data;
  private Message message;
  private Base64Codec base64Codec;

  @Before
  public void setUp()  {

  }

  @Test
  public void testGetSender() {
   // PublicKey key = sender;
   // when(message.getSender()).thenReturn(key);
   // Assert.assertNotNull(message.getSender());

    String senderText = "This is for sender";
    byte sender[] = senderText.getBytes();
    String receiverText = "This is for receiver";
    byte receiver[] = receiverText.getBytes();

    String data = "this is message";

    PublicKey senderKey = PublicKey.from(sender);
    PublicKey receiverKey = PublicKey.from(receiver);

    assertThat(senderKey).isNotNull();
    assertThat(receiverKey).isNotNull();
    Message messageObj = new Message(senderKey,receiverKey,data.getBytes());
    assertThat(messageObj).isNotNull();
    assertThat(messageObj.getSender()).isNotNull();
    assertThat(messageObj.getSender()).isSameAs(senderKey);
    assertThat(messageObj.getRecipient()).isSameAs(receiverKey);
    assertThat(messageObj.getData()).isEqualTo(data.getBytes());

  }

  @Test
  public void testGetRecipient() {
    //PublicKey key = recipient;
    //when(message.getRecipient()).thenReturn(key);
    //Message messageObj = new Message(sender,recipient,data);
    //assertThat(messageObj).isNotNull();
    //assertThat(messageObj.getRecipient()).isNotNull();

    String senderText = "This is for sender";
    byte sender[] = senderText.getBytes();
    String receiverText = "This is for receiver";
    byte receiver[] = receiverText.getBytes();

    String data = "this is message";

    PublicKey senderKey = PublicKey.from(sender);
    PublicKey receiverKey = PublicKey.from(receiver);

    assertThat(senderKey).isNotNull();
    assertThat(receiverKey).isNotNull();
    Message messageObject = new Message(senderKey,receiverKey,data.getBytes());
    assertThat(messageObject).isNotNull();
    assertThat(messageObject.getSender()).isNotNull();
    assertThat(messageObject.getSender()).isSameAs(senderKey);
    assertThat(messageObject.getRecipient()).isSameAs(receiverKey);
    assertThat(messageObject.getData()).isEqualTo(data.getBytes());

  }

  @Test
  public void testGetData() {
    //Message messageObj = new Message(sender,recipient,data);
    //assertThat(messageObj).isNotNull();
    //assertThat(messageObj.getData()).isNotNull();

    String senderText = "This is for sender";
    byte sender[] = senderText.getBytes();
    String receiverText = "This is for receiver";
    byte receiver[] = receiverText.getBytes();

    String data = "this is message";

    PublicKey senderKey = PublicKey.from(sender);
    PublicKey receiverKey = PublicKey.from(receiver);

    base64Codec = new Base64Codec() {
    };

    assertThat(base64Codec).isNotNull();


    Message message = new Message(senderKey,receiverKey,data.getBytes());

    String encodedData = base64Codec.encodeToString(message.getData());

    byte output[] = base64Codec.decode(encodedData);

    assertThat(encodedData.equals(new String(output)));

  }


  @Test
  public void testConstructorWithArgs() {

   Message message = new Message(sender,recipient,data);
    Assert.assertNotNull(message);


    String senderText = "This is for sender";
    byte sender[] = senderText.getBytes();
    String receiverText = "This is for receiver";
    byte receiver[] = receiverText.getBytes();

    String data = "this is message";

    PublicKey senderKey = PublicKey.from(sender);
    PublicKey receiverKey = PublicKey.from(receiver);

    assertThat(senderKey).isNotNull();
    assertThat(receiverKey).isNotNull();
    Message messageObject = new Message(senderKey,receiverKey,data.getBytes());
    assertThat(messageObject).isNotNull();
    assertThat(messageObject.getSender()).isNotNull();
    assertThat(messageObject.getSender()).isSameAs(senderKey);
    assertThat(messageObject.getRecipient()).isSameAs(receiverKey);
    assertThat(messageObject.getData()).isEqualTo(data.getBytes());
    }
  }

