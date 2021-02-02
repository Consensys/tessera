package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.dsl.Disruptor;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.io.IOCallback;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fusesource.leveldbjni.JniDBFactory.factory;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class LeveldbOrionDataAdapterTest {

    private LevelDbOrionDataAdapter orionDataAdapter;

    private ObjectMapper objectMapper;

    private Disruptor<OrionEvent> disruptor;

    private EncryptedKeyMatcher encryptedKeyMatcher;

    private RecipientBoxHelper recipientBoxHelper;

    @Before
    public void beforeTest() throws Exception {

        objectMapper = JacksonObjectMapperFactory.create();
        disruptor = mock(Disruptor.class);
        encryptedKeyMatcher = mock(EncryptedKeyMatcher.class);
        recipientBoxHelper = mock(RecipientBoxHelper.class);

        Path storageDir = Paths.get("build","resources","test");
        Options options = new Options();
        options.logger(s -> System.out.println(s));
        options.createIfMissing(true);
        String dbname = "routerdb";

        DB leveldb = IOCallback.execute(
            () -> factory.open(storageDir.resolve(dbname).toAbsolutePath().toFile(), options)
        );


        orionDataAdapter = new LevelDbOrionDataAdapter(leveldb, objectMapper, disruptor, encryptedKeyMatcher,recipientBoxHelper);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(disruptor);
        verifyNoMoreInteractions(encryptedKeyMatcher);
        verifyNoMoreInteractions(recipientBoxHelper);
    }

    @Test
    public void doStuff() throws Exception {
        final ArgumentCaptor<OrionEvent> publishArg = ArgumentCaptor.forClass(OrionEvent.class);

        PublicKey publicKey = mock(PublicKey.class);
        when(encryptedKeyMatcher.findRecipientKeyWhenNotSenderAndPrivacyGroupNotFound(any(EncryptedPayload.class)))
            .thenReturn(Optional.of(publicKey));

        when(recipientBoxHelper.getRecipientMapping(any(EncryptedPayload.class),any(PrivacyGroupPayload.class)))
            .thenReturn(Map.of(
                mock(PublicKey.class),mock(RecipientBox.class)
            ));

        orionDataAdapter.start();
        verify(disruptor,times(32)).publishEvent(publishArg.capture());
        assertThat(publishArg.getAllValues()).hasSize(32);


        long encodedPayloadCount = publishArg.getAllValues().stream()
            .filter(v -> v.getPayloadType() == PayloadType.ENCRYPTED_PAYLOAD)
            .count();
        assertThat(encodedPayloadCount).isEqualTo(22);

        verify(recipientBoxHelper,times(20))
            .getRecipientMapping(any(EncryptedPayload.class),any(PrivacyGroupPayload.class));
        verify(encryptedKeyMatcher,times(2))
            .findRecipientKeyWhenNotSenderAndPrivacyGroupNotFound(any(EncryptedPayload.class));

    }


}
