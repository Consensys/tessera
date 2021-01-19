package net.consensys.tessera.migration.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.consensys.orion.config.Config;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

public class InboundDbHelper {

    private final DataSource jdbcDataSource;

    private final DB leveldb;

    private InboundDbHelper(DataSource jdbcDataSource, DB leveldb) {
        this.jdbcDataSource = jdbcDataSource;
        this.leveldb = leveldb;
    }

    Optional<DB> getLevelDb() {
        return Optional.ofNullable(leveldb);
    }

    Optional<DataSource> getJdbcDataSource() {
        return Optional.ofNullable(jdbcDataSource);
    }

    InputType getInputType() {
        return Objects.nonNull(leveldb) ? InputType.LEVELDB : InputType.JDBC;
    }

    public static InboundDbHelper from(Config config) {

        String connectionString = config.storage();
        Path storageDir = config.workDir();

        if (connectionString.startsWith("sql")) {
            final String[] storageOptions = connectionString.split(":", 2);

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(storageOptions[1]);
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);

            return new InboundDbHelper(dataSource, null);
        }

        if (connectionString.startsWith("leveldb")) {
            Options options = new Options();
            options.logger(s -> System.out.println(s));
            options.createIfMissing(true);
            String dbname = connectionString.split(":")[1];
            try {
                DB leveldb = factory.open(storageDir.resolve(dbname).toAbsolutePath().toFile(), options);
                return new InboundDbHelper(null, leveldb);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        throw new UnsupportedOperationException();
    }
}
