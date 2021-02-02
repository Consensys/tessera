package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.dsl.Disruptor;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;
import javax.json.JsonObject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class JdbcOrionDataAdapter implements OrionDataAdapter {

    private final ObjectMapper cborObjectMapper;

    private final Disruptor<OrionEvent> disruptor;

    private final DataSource dataSource;

    private final AtomicLong totalRecords = new AtomicLong(0);

    private final EncryptedKeyMatcher encryptedKeyMatcher;

    private final RecipientBoxHelper recipientBoxHelper;

    public JdbcOrionDataAdapter(DataSource dataSource,
                                ObjectMapper cborObjectMapper,
                                Disruptor<OrionEvent> disruptor,
                                EncryptedKeyMatcher encryptedKeyMatcher,
                                RecipientBoxHelper recipientBoxHelper) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.cborObjectMapper = Objects.requireNonNull(cborObjectMapper);
        this.disruptor = Objects.requireNonNull(disruptor);
        this.encryptedKeyMatcher = Objects.requireNonNull(encryptedKeyMatcher);
        this.recipientBoxHelper = Objects.requireNonNull(recipientBoxHelper);
    }

    @Override
    public void start() throws Exception {

        Connection connection = dataSource.getConnection();

        if(totalRecords.get() == 0) {
            try(
                Statement statement = connection.createStatement();
                ResultSet countRs = statement.executeQuery("SELECT COUNT(*) FROM STORE")) {
                assert countRs.next();
                long count = countRs.getLong(1);
                assert count > 0;
                totalRecords.set(count);
            }
        }

        try (connection;
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM STORE")) {

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
                    try(PreparedStatement findRowByIdStatement = connection.prepareStatement("SELECT VALUE FROM STORE WHERE KEY = ?");
                    ) {

                        byte[] privacyGroupId = Base64.getEncoder().encode(encryptedPayload.privacyGroupId());

                        findRowByIdStatement.setBytes(1,privacyGroupId);

                        try(ResultSet rs = findRowByIdStatement.executeQuery()) {
                            if(rs.next()) {
                                byte[] privacyGroupData = rs.getBytes(1);
                                PrivacyGroupPayload privacyGroup = cborObjectMapper.readValue(privacyGroupData, PrivacyGroupPayload.class);
                                final Map<PublicKey, RecipientBox> recipientBoxMap = recipientBoxHelper.getRecipientMapping(encryptedPayload,privacyGroup);
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
