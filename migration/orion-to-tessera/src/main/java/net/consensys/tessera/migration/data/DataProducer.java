package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.lmax.disruptor.dsl.Disruptor;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
* Reads orion data store and publishs records to ringbuffer
 * */
public interface DataProducer {
    void start() throws Exception;

    static DataProducer create(InboundDbHelper inboundDbHelper, Disruptor<OrionDataEvent> outboundPubisher) {
        final InputType inputType = inboundDbHelper.getInputType();
        switch (inputType) {
            case LEVELDB:
                return new LevelDbDataProducer(inboundDbHelper.getLevelDb().get(), outboundPubisher);
            case JDBC:
                return new JdbcDataProducer(inboundDbHelper.getJdbcDataSource().get(),outboundPubisher);
            default:
                throw new UnsupportedOperationException("");
        }
    }

    default Optional<byte[]> findPrivacyGroupId(byte[] data) throws IOException {

        final JsonFactory jacksonJsonFactory = JacksonObjectMapperFactory.createFactory();

        try(JsonParser jacksonJsonParser = jacksonJsonFactory.createParser(data)) {

            while (!jacksonJsonParser.isClosed()) {

                JsonToken jsonToken = jacksonJsonParser.nextToken();
                if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                    String fieldname = jacksonJsonParser.getCurrentName();
                    if(Objects.equals("privacyGroupId",fieldname)) {
                        jacksonJsonParser.nextToken();
                        byte[] d = Base64.getEncoder().encode(jacksonJsonParser.getBinaryValue());
                        return Optional.of(d);
                    }
                }
            }

        }
        return Optional.empty();
    }

}
