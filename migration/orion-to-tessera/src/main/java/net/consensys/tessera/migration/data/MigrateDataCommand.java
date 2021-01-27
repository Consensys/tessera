package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.EncryptorFactory;
import net.consensys.tessera.migration.OrionKeyHelper;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.stream.IntStream;

public class MigrateDataCommand implements Callable<Boolean> {

    private TesseraJdbcOptions tesseraJdbcOptions;

    private OrionKeyHelper orionKeyHelper;

    private Encryptor tesseraEncryptor = EncryptorFactory.newFactory("NACL").create();

    private InboundDbHelper inboundDbHelper;

    private ObjectMapper cborObjectMapper = JsonMapper.builder(new CBORFactory())
        .addModule(new Jdk8Module())
        .addModule(new JSR353Module())
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .build();

    static EntityManagerFactory createEntityManagerFactory(TesseraJdbcOptions jdbcOptions) {
        Map jdbcProperties = new HashMap<>();
        jdbcProperties.put("javax.persistence.jdbc.user", jdbcOptions.getUsername());
        jdbcProperties.put("javax.persistence.jdbc.password", jdbcOptions.getPassword());
        jdbcProperties.put("javax.persistence.jdbc.url", jdbcOptions.getUrl());
        jdbcProperties.put("eclipselink.logging.level", "FINE");
        jdbcProperties.put("eclipselink.logging.parameters", "true");
        jdbcProperties.put("eclipselink.logging.level.sql", "FINE");

        jdbcProperties.put("javax.persistence.schema-generation.database.action", jdbcOptions.getAction());

        return Persistence.createEntityManagerFactory("tessera-em", jdbcProperties);
    }

    public MigrateDataCommand(
            InboundDbHelper inboundDbHelper, TesseraJdbcOptions tesseraJdbcOptions, OrionKeyHelper orionKeyHelper) {
        this.inboundDbHelper = inboundDbHelper;
        this.tesseraJdbcOptions = tesseraJdbcOptions;
        this.orionKeyHelper = orionKeyHelper;
    }

    @Override
    public Boolean call() throws Exception {

        Disruptor<OrionEvent> disruptor =
                new Disruptor<>(
                    OrionEvent.FACTORY,
                        128,
                        (ThreadFactory) Thread::new,
                        ProducerType.SINGLE,
                        new BlockingWaitStrategy());

        InputType inputType = inboundDbHelper.getInputType();
        final OrionDataAdapter inboundAdapter;
        switch (inputType) {
            case LEVELDB:
                inboundAdapter =
                        new LevelDbOrionDataAdapter(inboundDbHelper.getLevelDb().get(), cborObjectMapper, disruptor,orionKeyHelper,tesseraEncryptor);
                break;
            case JDBC:
                DataSource dataSource = inboundDbHelper.getJdbcDataSource().get();
                inboundAdapter = new JdbcOrionDataAdapter(dataSource, cborObjectMapper, disruptor,orionKeyHelper,tesseraEncryptor);
                break;
            default:
                throw new UnsupportedOperationException("");
        }

        EntityManagerFactory entityManagerFactory = createEntityManagerFactory(tesseraJdbcOptions);

        List<EventHandler<OrionEvent>> handlers = new ArrayList<>();
        for(int i = 0;i < 10;i++) {
            handlers.add(new PersistPrivacyGroupEventHandler(entityManagerFactory));
            handlers.add(new PersistTransactionEventHandler(entityManagerFactory));
        }

        CompletionHandler completionHandler = new CompletionHandler();

        disruptor
                .handleEventsWith(handlers.toArray(EventHandler[]::new))
                    .then(completionHandler);

        disruptor.start();

        inboundAdapter.start();

        completionHandler.await();

        disruptor.shutdown();

        return Boolean.TRUE;
    }
}
