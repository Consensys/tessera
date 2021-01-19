package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;

public class JdbcPrivacyGroupPayloadLookup implements PrivacyGroupPayloadLookup {

    private final DataSource dataSource;

    private ObjectMapper objectMapper;

    public JdbcPrivacyGroupPayloadLookup(DataSource dataSource, ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<PrivacyGroupPayload> findRecipients(EncryptedPayload encryptedPayload) {
        byte[] privacyGroupId = encryptedPayload.privacyGroupId();
        try {
            byte[] privacyGroupPayloadData = getPrivacyGroupPayloadData(privacyGroupId);
            if (privacyGroupPayloadData != null) {
                return Optional.ofNullable(privacyGroupPayloadData)
                        .map(
                                data -> {
                                    try {
                                        return objectMapper.readValue(data, PrivacyGroupPayload.class);
                                    } catch (IOException e) {
                                        throw new UncheckedIOException(e);
                                    }
                                });
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        ;

        return Optional.empty();
    }

    byte[] getPrivacyGroupPayloadData(byte[] privacyGroupId) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT VALUE FROM STORE WHERE KEY = ?");

        byte[] encodedPrivacyGroupId = Base64.getEncoder().encode(privacyGroupId);
        statement.setBytes(1, encodedPrivacyGroupId);

        ResultSet resultSet = statement.executeQuery();
        try (connection;
                statement;
                resultSet) {
            if (resultSet.next()) {
                return resultSet.getBytes(1);
            }
        }
        return null;
    }
}
