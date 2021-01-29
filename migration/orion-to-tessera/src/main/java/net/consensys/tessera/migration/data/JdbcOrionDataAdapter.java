package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.dsl.Disruptor;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.PublicKey;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;
import net.consensys.tessera.migration.OrionKeyHelper;

import javax.json.JsonObject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class JdbcOrionDataAdapter implements OrionDataAdapter {

    private final ObjectMapper cborObjectMapper;

    private final Disruptor<OrionEvent> disruptor;

    private final DataSource dataSource;

    private final OrionKeyHelper orionKeyHelper;

    private final AtomicLong totalRecords = new AtomicLong(0);

    private final EncryptedKeyMatcher encryptedKeyMatcher;

    public JdbcOrionDataAdapter(DataSource dataSource,
                                ObjectMapper cborObjectMapper,
                                Disruptor<OrionEvent> disruptor,
                                OrionKeyHelper orionKeyHelper,
                                Encryptor encryptor) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.cborObjectMapper = Objects.requireNonNull(cborObjectMapper);
        this.disruptor = Objects.requireNonNull(disruptor);
        this.orionKeyHelper = Objects.requireNonNull(orionKeyHelper);
        this.encryptedKeyMatcher = new EncryptedKeyMatcher(orionKeyHelper,new EncryptorHelper(encryptor));
    }

    @Override
    public void start() throws Exception {

        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();

        PreparedStatement findRowByIdStatement = connection.prepareStatement("SELECT VALUE FROM STORE WHERE KEY = ?");
        ResultSet resultSet = statement.executeQuery("SELECT * FROM STORE");

        if(Objects.isNull(totalRecords)) {
            try(ResultSet countRs = statement.executeQuery("SELECT COUNT(*) FROM STORE")) {
                countRs.next();
                totalRecords.set(countRs.getLong(1));
            }
        }

        try (connection;
                statement;
                resultSet) {

            for (long eventNumber = 1;resultSet.next();eventNumber++) {
                byte[] key = resultSet.getBytes("KEY");
                byte[] value = resultSet.getBytes("VALUE");

                JsonObject jsonObject = cborObjectMapper.readValue(value, JsonObject.class);
                PayloadType payloadType = PayloadType.get(jsonObject);
                final OrionEvent.Builder orionEventBuilder = OrionEvent.Builder.create()
                    .withTotalEventCount(totalRecords.get())
                    .withEventNumber(eventNumber)
                    .withJsonObject(jsonObject)
                    .withKey(key)
                    .withPayloadType(payloadType);

                if(payloadType == PayloadType.ENCRYPTED_PAYLOAD) {

                    EncryptedPayload encryptedPayload = cborObjectMapper.readValue(value,EncryptedPayload.class);
                    try(findRowByIdStatement) {

                        byte[] privacyGroupId = encryptedPayload.privacyGroupId();

                        findRowByIdStatement.setBytes(1,privacyGroupId);

                        try(ResultSet rs = findRowByIdStatement.executeQuery()) {
                            if(rs.next()) {
                                byte[] privacyGroupData = rs.getBytes(1);
                                PrivacyGroupPayload privacyGroup = cborObjectMapper.readValue(privacyGroupData, PrivacyGroupPayload.class);
                                final Map<PublicKey, RecipientBox> recipientBoxMap = new RecipientBoxHelper(orionKeyHelper,encryptedPayload,privacyGroup).getRecipientMapping();
                                orionEventBuilder.withRecipientBoxMap(recipientBoxMap);
                            } else {

                                assert encryptedPayload.encryptedKeys().length == 1 : "There must only be one encryptedKey";

                                final PublicKey recipientKey = encryptedKeyMatcher.findRecipientKeyWhenNotSenderAndPrivacyGroupNotFound(encryptedPayload).get();
                                final RecipientBox recipientBox = RecipientBox.from(encryptedPayload.encryptedKeys()[0].getEncoded());

                                final Map<PublicKey,RecipientBox> recipientBoxMap = Map.of(recipientKey,recipientBox);
                                orionEventBuilder
                                    .withRecipientBoxMap(recipientBoxMap);
                            }
                        }
                    }
                }
                disruptor.publishEvent(orionEventBuilder.build());
            }
        }
    }



}
