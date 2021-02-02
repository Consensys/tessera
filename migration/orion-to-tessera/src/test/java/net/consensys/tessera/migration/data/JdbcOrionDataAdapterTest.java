package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.dsl.Disruptor;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.io.IOCallback;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fusesource.leveldbjni.JniDBFactory.factory;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class JdbcOrionDataAdapterTest {

    private JdbcOrionDataAdapter jdbcOrionDataAdapter;

    private DataSource dataSource;

    private ObjectMapper objectMapper;

    private Disruptor<OrionEvent> disruptor;

    private EncryptedKeyMatcher encryptedKeyMatcher;

    private RecipientBoxHelper recipientBoxHelper;

    @Before
    public void beforeTest() throws Exception {
        Path storageDir = Paths.get("build","resources","test");
        Options options = new Options();
        options.logger(s -> System.out.println(s));
        options.createIfMissing(false);
        String dbname = "routerdb";

        DB leveldb = IOCallback.execute(
            () -> factory.open(storageDir.resolve(dbname).toAbsolutePath().toFile(), options)
        );

        objectMapper = JacksonObjectMapperFactory.create();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername("junit");
        hikariConfig.setPassword("junit");
        //ACCESS_MODE_DATA=r
        hikariConfig.setJdbcUrl("jdbc:h2:./build/resources/test/sqldb");
        dataSource = new HikariDataSource(hikariConfig);

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE STORE IF EXISTS");
                statement.execute("CREATE TABLE STORE (KEY CHAR(60) PRIMARY KEY,VALUE BLOB)");
            }
        }

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO STORE (KEY,VALUE) VALUES (?,?) ")) {
                DBIterator iterator = leveldb.iterator();
                for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    System.out.println("HERE");

                    Map.Entry<byte[], byte[]> entry = iterator.peekNext();
                    insertStatement.setBytes(1, entry.getKey());
                    insertStatement.setBytes(2, entry.getValue());
                    System.out.println("INSERTed " + insertStatement.executeUpdate());
                }
                connection.commit();
            }
        }

        disruptor = mock(Disruptor.class);
        encryptedKeyMatcher = mock(EncryptedKeyMatcher.class);
        recipientBoxHelper = mock(RecipientBoxHelper.class);

        jdbcOrionDataAdapter = new JdbcOrionDataAdapter(dataSource, objectMapper, disruptor, encryptedKeyMatcher,recipientBoxHelper);
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

        jdbcOrionDataAdapter.start();
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
