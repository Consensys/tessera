package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.recovery.resend.ResendRequest;
import com.quorum.tessera.recovery.resend.ResendResponse;
import com.quorum.tessera.transaction.exception.EnhancedPrivacyNotSupportedException;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LegacyResendManagerTest {

    private Enclave enclave;

    private Discovery discovery;

    private PayloadEncoder encoder;

    private PayloadPublisher publisher;

    private EncryptedTransactionDAO dao;

    private LegacyResendManager resendManager;

    @Before
    public void init() {
        this.enclave = mock(Enclave.class);
        this.discovery = mock(Discovery.class);
        this.encoder = mock(PayloadEncoder.class);
        this.publisher = mock(PayloadPublisher.class);
        this.dao = mock(EncryptedTransactionDAO.class);

        this.resendManager = new LegacyResendManagerImpl(enclave, dao, 1, encoder, publisher, discovery);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(enclave, discovery, encoder, publisher, dao);
    }

    @Test
    public void individualMissingTxFails() {
        when(dao.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());

        final MessageHash txHash = new MessageHash("sample-hash".getBytes());
        final PublicKey targetResendKey = PublicKey.from("target".getBytes());
        final ResendRequest request = ResendRequest.Builder.create()
            .withType(ResendRequest.ResendRequestType.INDIVIDUAL)
            .withHash(txHash)
            .withRecipient(targetResendKey)
            .build();

        final Throwable throwable = catchThrowable(() -> resendManager.resend(request));

        assertThat(throwable)
            .isInstanceOf(TransactionNotFoundException.class)
            .hasMessage("Message with hash c2FtcGxlLWhhc2g= was not found");

        verify(dao).retrieveByHash(txHash);
    }

    @Test
    public void individualNonStandardPrivateTxFails() {
        final EncodedPayload nonSPPayload = EncodedPayload.Builder.create()
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
            .build();
        final EncryptedTransaction databaseTx = new EncryptedTransaction();
        databaseTx.setEncodedPayload(new byte[0]);

        when(dao.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(databaseTx));
        when(encoder.decode(any(byte[].class))).thenReturn(nonSPPayload);

        final MessageHash txHash = new MessageHash("sample-hash".getBytes());
        final PublicKey targetResendKey = PublicKey.from("target".getBytes());
        final ResendRequest request = ResendRequest.Builder.create()
            .withType(ResendRequest.ResendRequestType.INDIVIDUAL)
            .withHash(txHash)
            .withRecipient(targetResendKey)
            .build();

        final Throwable throwable = catchThrowable(() -> resendManager.resend(request));

        assertThat(throwable)
            .isInstanceOf(EnhancedPrivacyNotSupportedException.class)
            .hasMessage("Cannot resend enhanced privy transaction in legacy resend");

        verify(dao).retrieveByHash(txHash);
        verify(encoder).decode(any(byte[].class));
    }

    @Test
    public void targetKeyIsNotSenderOfTransaction() {
        final MessageHash txHash = new MessageHash("sample-hash".getBytes());
        final PublicKey targetResendKey = PublicKey.from("target".getBytes());

        final EncodedPayload nonSPPayload = EncodedPayload.Builder.create()
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .build();
        final EncryptedTransaction databaseTx = new EncryptedTransaction();
        databaseTx.setEncodedPayload(new byte[0]);

        final ResendRequest request = ResendRequest.Builder.create()
            .withType(ResendRequest.ResendRequestType.INDIVIDUAL)
            .withHash(txHash)
            .withRecipient(targetResendKey)
            .build();

        when(dao.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(databaseTx));
        when(encoder.decode(any(byte[].class))).thenReturn(nonSPPayload);
        when(encoder.forRecipient(nonSPPayload, targetResendKey)).thenReturn(nonSPPayload);

        final ResendResponse response = resendManager.resend(request);

        assertThat(response).isNotNull();
        assertThat(response.getPayload()).isEqualTo(nonSPPayload);

        verify(dao).retrieveByHash(txHash);
        verify(encoder).decode(any(byte[].class));
        verify(encoder).forRecipient(nonSPPayload, targetResendKey);
    }

    @Test
    public void targetIsSenderOfTransaction() {
        final MessageHash txHash = new MessageHash("sample-hash".getBytes());
        final PublicKey targetResendKey = PublicKey.from("target".getBytes());
        final PublicKey localRecipientKey = PublicKey.from("local-recipient".getBytes());

        final EncodedPayload nonSPPayload = EncodedPayload.Builder.create()
            .withSenderKey(targetResendKey)
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .build();
        final EncryptedTransaction databaseTx = new EncryptedTransaction();
        databaseTx.setEncodedPayload(new byte[0]);

        final ResendRequest request = ResendRequest.Builder.create()
            .withType(ResendRequest.ResendRequestType.INDIVIDUAL)
            .withHash(txHash)
            .withRecipient(targetResendKey)
            .build();

        when(dao.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(databaseTx));
        when(encoder.decode(any(byte[].class))).thenReturn(nonSPPayload);
        when(enclave.getPublicKeys()).thenReturn(Set.of(localRecipientKey));
        when(enclave.unencryptTransaction(any(), eq(localRecipientKey))).thenReturn(new byte[0]);

        final ResendResponse response = resendManager.resend(request);

        final EncodedPayload expected = EncodedPayload.Builder.from(nonSPPayload)
            .withRecipientKey(localRecipientKey).build();
        assertThat(response).isNotNull();
        assertThat(response.getPayload()).isEqualToComparingFieldByFieldRecursively(expected);

        verify(dao).retrieveByHash(txHash);
        verify(encoder).decode(any(byte[].class));
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(any(), eq(localRecipientKey));
    }

    @Test
    public void performResendAll() {
        final PublicKey targetResendKey = PublicKey.from("target".getBytes());
        final ResendRequest request = ResendRequest.Builder.create()
            .withType(ResendRequest.ResendRequestType.ALL)
            .withRecipient(targetResendKey)
            .build();

        //Not bothered about going through the process, just make sure they are all loaded from the database
        //We are not testing the workflow itself, only that the workflow gets the right amount of transactions

        when(dao.transactionCount()).thenReturn(2L);
        when(dao.retrieveTransactions(0, 1)).thenReturn(List.of(new EncryptedTransaction()));
        when(dao.retrieveTransactions(1, 1)).thenReturn(List.of(new EncryptedTransaction()));

        final ResendResponse response = resendManager.resend(request);
        assertThat(response).isNotNull();
        assertThat(response.getPayload()).isNull();

        verify(enclave, times(2)).status();
        verify(encoder, times(2)).decode(any());
        verify(dao).transactionCount();
        verify(dao).retrieveTransactions(0, 1);
        verify(dao).retrieveTransactions(1, 1);
    }

    @Test
    public void createReturnsInstance() {
        final Config config = mock(Config.class);

        final JdbcConfig jdbcConfig = new JdbcConfig("junit", "", "jdbc:h2:mem:test");
        when(config.getJdbcConfig()).thenReturn(jdbcConfig);

        final ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        final LegacyResendManager result = LegacyResendManager.create(config);

        assertThat(result).isNotNull();
    }
}
