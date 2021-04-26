package net.consensys.tessera.migration.data;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicInteger;

public class JdbcMigrationInfoFactory implements MigrationInfoFactory<DataSource> {

    private final DataSource dataSource;

    JdbcMigrationInfoFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public MigrationInfo init() throws Exception {

        AtomicInteger totalRecords = new AtomicInteger(0);
        AtomicInteger transactionRecords = new AtomicInteger(0);
        AtomicInteger privacyGroupRecords = new AtomicInteger(0);
        AtomicInteger queryPrivacyGroupRecords = new AtomicInteger(0);

        try(
            Connection connection = dataSource.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT VALUE FROM STORE")
            ) {

            while(resultSet.next()) {

                byte[] value = resultSet.getBytes(1);
                PayloadType payloadType = PayloadType.parsePayloadType(value);

                switch (payloadType) {
                    case ENCRYPTED_PAYLOAD:
                        transactionRecords.incrementAndGet();
                        break;
                    case PRIVACY_GROUP_PAYLOAD:
                        privacyGroupRecords.incrementAndGet();
                        break;
                    case QUERY_PRIVACY_GROUP_PAYLOAD:
                        queryPrivacyGroupRecords.incrementAndGet();
                        break;
                    default:throw new UnsupportedOperationException();
                }
                totalRecords.incrementAndGet();

            }

            return MigrationInfo.from(
                totalRecords.get(),
                transactionRecords.get(),
                privacyGroupRecords.get(),
                queryPrivacyGroupRecords.get());


        }


    }
}
