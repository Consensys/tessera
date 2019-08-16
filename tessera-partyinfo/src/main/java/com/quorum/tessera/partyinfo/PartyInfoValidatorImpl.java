package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartyInfoValidatorImpl implements PartyInfoValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoValidatorImpl.class);

    private final PayloadEncoder payloadEncoder;

    private final Enclave enclave;

    private final RandomDataGenerator randomDataGenerator;

    public PartyInfoValidatorImpl(Enclave enclave) {
        this(enclave, PayloadEncoder.create(), new RandomDataGenerator() {
        });
    }

    public PartyInfoValidatorImpl(Enclave enclave, PayloadEncoder payloadEncoder, RandomDataGenerator randomDataGenerator) {
        this.enclave = enclave;
        this.payloadEncoder = payloadEncoder;
        this.randomDataGenerator = randomDataGenerator;
    }

    @Override
    public Set<Recipient> validateAndFetchValidRecipients(PartyInfo partyInfo, PartyInfoValidatorCallback partyInfoValidatorCallback) {

        final PublicKey sender = enclave.defaultPublicKey();

        final String url = partyInfo.getUrl();

        final String dataToEncrypt = randomDataGenerator.generate();

        final Predicate<Recipient> isValidRecipientKey
                = r -> {
                    try {

                        final PublicKey key = r.getKey();
                        final EncodedPayload encodedPayload = enclave.encryptPayload(
                                dataToEncrypt.getBytes(),
                                sender,
                                Arrays.asList(key),
                                PrivacyMode.STANDARD_PRIVATE,
                                Collections.emptyMap(),
                                new byte[0]);

                        final byte[] encodedPayloadData = payloadEncoder.encode(encodedPayload);

                        String unencodedValidationData = partyInfoValidatorCallback.requestDecode(r, encodedPayloadData);

                        boolean isValid = Objects.equals(unencodedValidationData, dataToEncrypt);
                        if (!isValid) {
                            LOGGER.warn("Invalid key found {} recipient will be ignored.", r.getUrl());
                        } else {
                            LOGGER.info("{} has a valid key", r.getUrl());
                        }

                        return isValid;
                        // Assume any all exceptions to mean invalid. enclave bubbles up nacl array out of
                        // bounds when calculating shared key from invalid data
                    } catch (Exception ex) {
                        LOGGER.debug(null, ex);
                        return false;
                    }
                };

        final Predicate<Recipient> isSendingUrl = r -> r.getUrl().equalsIgnoreCase(url);

        // Validate caller and treat no valid certs as security issue.
        return partyInfo.getRecipients().stream()
                .filter(isSendingUrl.and(isValidRecipientKey))
                .collect(Collectors.toSet());

    }

}
