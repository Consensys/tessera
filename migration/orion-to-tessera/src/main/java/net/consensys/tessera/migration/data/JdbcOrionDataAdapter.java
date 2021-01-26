package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.dsl.Disruptor;
import com.quorum.tessera.enclave.RecipientBox;
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

public class JdbcOrionDataAdapter implements OrionDataAdapter {

    private final ObjectMapper cborObjectMapper;

    private final Disruptor<OrionEvent> disruptor;

    private final DataSource dataSource;

    private final OrionKeyHelper orionKeyHelper;

    private volatile Long totalRecords;

    public JdbcOrionDataAdapter(DataSource dataSource,
                                ObjectMapper cborObjectMapper,
                                Disruptor<OrionEvent> disruptor,
                                OrionKeyHelper orionKeyHelper) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.cborObjectMapper = Objects.requireNonNull(cborObjectMapper);
        this.disruptor = Objects.requireNonNull(disruptor);
        this.orionKeyHelper = Objects.requireNonNull(orionKeyHelper);
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
                totalRecords = countRs.getLong(1);
            }
        }

        try (connection;
                statement;
                resultSet) {

            for (long i = 1;resultSet.next();i++) {
                byte[] key = resultSet.getBytes("KEY");
                byte[] value = resultSet.getBytes("VALUE");

                JsonObject jsonObject = cborObjectMapper.readValue(value, JsonObject.class);
                PayloadType payloadType = PayloadType.get(jsonObject);
                final OrionEvent.Builder orionEvent = OrionEvent.Builder.create()
                    .withTotalEventCount(totalRecords)
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
                                orionEvent.withRecipientBoxMap(recipientBoxMap);
                            }
                        }
                    }
                }

                disruptor.publishEvent(orionEvent.build());
            }
        }
    }



}
