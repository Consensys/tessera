package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ResendRequestTest {

    @Test
    public void buildWithAllIndividual() {
        final MessageHash emptyHash = new MessageHash(new byte[0]);
        final PublicKey emptyKey = PublicKey.from(new byte[0]);

        final ResendRequest request = ResendRequest.Builder.create()
            .withType(ResendRequest.ResendRequestType.INDIVIDUAL)
            .withHash(emptyHash)
            .withRecipient(emptyKey)
            .build();

        assertThat(request).isNotNull();
        assertThat(request.getHash()).isSameAs(emptyHash);
        assertThat(request.getRecipient()).isSameAs(emptyKey);
        assertThat(request.getType()).isEqualTo(ResendRequest.ResendRequestType.INDIVIDUAL);
    }

    @Test
    public void buildIndividualMissingHash() {
        final PublicKey emptyKey = PublicKey.from(new byte[0]);

        final ResendRequest.Builder builder = ResendRequest.Builder.create()
            .withType(ResendRequest.ResendRequestType.INDIVIDUAL)
            .withRecipient(emptyKey);

        final Throwable throwable = catchThrowable(builder::build);

        assertThat(throwable)
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Transaction hash is required for individual resends");
    }

    @Test
    public void buildAllRequest() {
        final PublicKey emptyKey = PublicKey.from(new byte[0]);

        final ResendRequest request = ResendRequest.Builder.create()
            .withType(ResendRequest.ResendRequestType.ALL)
            .withRecipient(emptyKey)
            .build();

        assertThat(request).isNotNull();
        assertThat(request.getRecipient()).isSameAs(emptyKey);
        assertThat(request.getType()).isEqualTo(ResendRequest.ResendRequestType.ALL);
    }

    @Test
    public void buildAllMissingKey() {
        final ResendRequest.Builder builder = ResendRequest.Builder.create()
            .withType(ResendRequest.ResendRequestType.ALL);

        final Throwable throwable = catchThrowable(builder::build);

        assertThat(throwable)
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Recipient is required");
    }

    @Test
    public void buildWithMissingRequestType() {
        final PublicKey emptyKey = PublicKey.from(new byte[0]);

        final ResendRequest.Builder builder = ResendRequest.Builder.create().withRecipient(emptyKey);

        final Throwable throwable = catchThrowable(builder::build);

        assertThat(throwable)
            .isInstanceOf(NullPointerException.class)
            .hasMessage("ResendRequestType is required");
    }
}
