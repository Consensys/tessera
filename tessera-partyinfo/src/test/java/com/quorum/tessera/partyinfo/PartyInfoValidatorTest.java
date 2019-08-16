package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import java.util.Collections;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class PartyInfoValidatorTest {

    private PartyInfoValidator partyInfoValidator;

    private Enclave enclave;

    private PayloadEncoder payloadEncoder;

    private RandomDataGenerator randomDataGenerator;

    @Before
    public void onSetUp() {
        enclave = mock(Enclave.class);
        payloadEncoder = mock(PayloadEncoder.class);
        randomDataGenerator = mock(RandomDataGenerator.class);
        partyInfoValidator = new PartyInfoValidatorImpl(enclave, payloadEncoder, randomDataGenerator);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(enclave, payloadEncoder, randomDataGenerator);
    }

    @Test
    public void validateAndFetchValidRecipients() {

        PublicKey someKey = PublicKey.from("SOMEKEYDATA".getBytes());

        String url = "http://foo.bar";

        String generatedValue = "Sucess";

        when(randomDataGenerator.generate()).thenReturn(generatedValue);

        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(someKey);
        when(recipient.getUrl()).thenReturn(url);

        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfo.getUrl()).thenReturn(url);
        when(partyInfo.getRecipients()).thenReturn(Collections.singleton(recipient));

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(enclave.encryptPayload(
                any(byte[].class),
                any(PublicKey.class),
                anyList(),
                any(PrivacyMode.class),
                anyMap(),
                any(byte[].class))).thenReturn(encodedPayload);

        when(payloadEncoder.encode(encodedPayload))
                .thenReturn(generatedValue.getBytes());

        when(enclave.defaultPublicKey()).thenReturn(someKey);

        Set<Recipient> results = partyInfoValidator.validateAndFetchValidRecipients(partyInfo, (r, d) -> {
            return generatedValue;
        });

        assertThat(results).hasSize(1).containsExactly(recipient);

        verify(enclave).defaultPublicKey();
        verify(enclave).encryptPayload(
                any(byte[].class),
                any(PublicKey.class),
                anyList(),
                any(PrivacyMode.class),
                anyMap(),
                any(byte[].class));
        verify(payloadEncoder).encode(encodedPayload);
        verify(randomDataGenerator).generate();

    }

    @Test
    public void validateAndFetchValidRecipientsNoResults() {

        PublicKey someKey = PublicKey.from("SOMEKEYDATA".getBytes());

        String url = "http://foo.bar";

        String generatedValue = "Sucess";

        when(randomDataGenerator.generate()).thenReturn(generatedValue);

        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(someKey);
        when(recipient.getUrl()).thenReturn(url);

        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfo.getUrl()).thenReturn(url);
        when(partyInfo.getRecipients()).thenReturn(Collections.singleton(recipient));

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(enclave.encryptPayload(
                any(byte[].class),
                any(PublicKey.class),
                anyList(),
                any(PrivacyMode.class),
                anyMap(),
                any(byte[].class))).thenReturn(encodedPayload);

        when(payloadEncoder.encode(encodedPayload))
                .thenReturn(generatedValue.getBytes());

        when(enclave.defaultPublicKey()).thenReturn(someKey);

        Set<Recipient> results = partyInfoValidator.validateAndFetchValidRecipients(partyInfo, (r, d) -> {
            return "Ouch";
        });

        assertThat(results).isEmpty();

        verify(enclave).defaultPublicKey();
        verify(enclave).encryptPayload(
                any(byte[].class),
                any(PublicKey.class),
                anyList(),
                any(PrivacyMode.class),
                anyMap(),
                any(byte[].class));
        verify(payloadEncoder).encode(encodedPayload);
        verify(randomDataGenerator).generate();

    }

    
     @Test
    public void validateAndFetchValidRecipientsException() {

        PublicKey someKey = PublicKey.from("SOMEKEYDATA".getBytes());

        String url = "http://foo.bar";

        String generatedValue = "Sucess";

        when(randomDataGenerator.generate()).thenReturn(generatedValue);

        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(someKey);
        when(recipient.getUrl()).thenReturn(url);

        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfo.getUrl()).thenReturn(url);
        when(partyInfo.getRecipients()).thenReturn(Collections.singleton(recipient));

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(enclave.encryptPayload(
                any(byte[].class),
                any(PublicKey.class),
                anyList(),
                any(PrivacyMode.class),
                anyMap(),
                any(byte[].class))).thenReturn(encodedPayload);

        when(payloadEncoder.encode(encodedPayload))
                .thenReturn(generatedValue.getBytes());

        when(enclave.defaultPublicKey()).thenReturn(someKey);

        Set<Recipient> results = partyInfoValidator.validateAndFetchValidRecipients(partyInfo, (r, d) -> {
           throw new RuntimeException("");
        });

        assertThat(results).isEmpty();

        verify(enclave).defaultPublicKey();
        verify(enclave).encryptPayload(
                any(byte[].class),
                any(PublicKey.class),
                anyList(),
                any(PrivacyMode.class),
                anyMap(),
                any(byte[].class));
        verify(payloadEncoder).encode(encodedPayload);
        verify(randomDataGenerator).generate();

    }
    
    @Test
    public void create() {
        assertThat(PartyInfoValidator.create(enclave)).isNotNull();
    }

}
