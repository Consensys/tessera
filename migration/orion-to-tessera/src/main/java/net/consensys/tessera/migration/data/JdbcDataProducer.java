package net.consensys.tessera.migration.data;

import com.lmax.disruptor.dsl.Disruptor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcDataProducer implements DataProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(JdbcDataProducer.class);

  private final DataSource dataSource;

  private final Disruptor<OrionDataEvent> disruptor;

  public JdbcDataProducer(DataSource dataSource, Disruptor<OrionDataEvent> disruptor) {
    this.dataSource = dataSource;
    this.disruptor = disruptor;
  }

  @Override
  public void start() throws Exception {

    final MigrationInfo migrationInfo = MigrationInfo.getInstance();
    AtomicLong eventCounter = new AtomicLong(0);

    try (Connection connection = dataSource.getConnection();
        ResultSet resultSet =
            connection.createStatement().executeQuery("SELECT KEY,VALUE FROM STORE")) {

      while (resultSet.next()) {

        String key = resultSet.getString(1);
        byte[] value = resultSet.getBytes(2);

        PayloadType payloadType = PayloadType.parsePayloadType(value);

        final OrionDataEvent.Builder orionDataEventBuilder =
            OrionDataEvent.Builder.create()
                .withEventNumber(eventCounter.incrementAndGet())
                .withTotalEventCount((long) migrationInfo.getRowCount())
                .withPayloadData(value)
                .withPayloadType(payloadType)
                .withKey(Base64.getDecoder().decode(key));

        if (payloadType == PayloadType.ENCRYPTED_PAYLOAD) {
          byte[] privacyGroupId = findPrivacyGroupId(value).get();
          byte[] privacyGroupIdToFind = Base64.getEncoder().encode(privacyGroupId);

          try (PreparedStatement preparedStatement =
              connection.prepareStatement("SELECT VALUE FROM STORE WHERE KEY = ?")) {
            preparedStatement.setString(1, new String(privacyGroupIdToFind));
            try (ResultSet rs = preparedStatement.executeQuery()) {
              if (rs.next()) {
                byte[] privacyGroupData = rs.getBytes(1);
                if (Objects.nonNull(privacyGroupData)) {
                  orionDataEventBuilder.withPrivacyGroupData(privacyGroupData);
                }
              } else {
                LOGGER.warn("No privacy group data found for {}", new String(privacyGroupId));
              }
            }
          }
        }

        final OrionDataEvent orionDataEvent = orionDataEventBuilder.build();
        disruptor.publishEvent(orionDataEvent);
      }

      LOGGER.info("All {} records published.", migrationInfo.getRowCount());
    }
  }
}
