package net.consensys.tessera.migration.data;

import com.quorum.tessera.io.IOCallback;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.consensys.orion.config.Config;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

public class InboundDbHelper implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InboundDbHelper.class);

    private final DataSource jdbcDataSource;

    private final DB leveldb;

    private final String storageInfo;

    private InboundDbHelper(DataSource jdbcDataSource, DB leveldb,String storageInfo) {
        this.jdbcDataSource = jdbcDataSource;
        this.leveldb = leveldb;
        this.storageInfo = storageInfo;
    }

    public String getStorageInfo() {
        return storageInfo;
    }

    public Optional<DB> getLevelDb() {
        return Optional.ofNullable(leveldb);
    }

    public Optional<DataSource> getJdbcDataSource() {
        return Optional.ofNullable(jdbcDataSource);
    }

    public InputType getInputType() {
        return Objects.nonNull(jdbcDataSource) ? InputType.JDBC : InputType.LEVELDB;
    }

    public static InboundDbHelper from(Config config) {

        String connectionString = config.storage();
        Path storageDir = config.workDir();

        if (connectionString.startsWith("sql")) {
            final String[] storageOptions = connectionString.split(":", 2);

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(storageOptions[1]);
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);

            return new InboundDbHelper(dataSource, null,hikariConfig.toString());
        }

        if (connectionString.startsWith("leveldb")) {

            Options options = new Options();
           // options.logger(s -> System.out.println(s));
            options.createIfMissing(true);
            String[] tokens = connectionString.split(":");
            String dbname = tokens.length == 2 ? connectionString.split(":")[1] : "routerdb";

            File dbpath = storageDir.resolve(dbname).toAbsolutePath().toFile();
            LOGGER.info("Opening leveldb {}",dbpath);
            DB leveldb = IOCallback.execute(
                () -> factory.open(dbpath, options)
            );

            String description = new StringBuilder("LevelDB{")
                .append("path = ")
                .append(dbpath)
                .append("}")
                .toString();

            return new InboundDbHelper(null, leveldb,description);

        }

        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        leveldb.close();
    }
}
