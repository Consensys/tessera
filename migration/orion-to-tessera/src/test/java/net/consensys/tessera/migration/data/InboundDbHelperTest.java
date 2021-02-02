package net.consensys.tessera.migration.data;

import net.consensys.orion.config.Config;
import org.junit.Ignore;
import org.junit.Test;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class InboundDbHelperTest {

    @Test
    public void leveldb() {
        Config leveldbConfig = mock(Config.class);
        when(leveldbConfig.storage()).thenReturn("leveldb:junitdb");
        when(leveldbConfig.workDir()).thenReturn(Paths.get("build"));

        InboundDbHelper inboundDbHelper = InboundDbHelper.from(leveldbConfig);
        assertThat(inboundDbHelper.getInputType()).isEqualTo(InputType.LEVELDB);
        assertThat(inboundDbHelper.getLevelDb()).isPresent();
        assertThat(inboundDbHelper.getJdbcDataSource()).isNotPresent();
    }

    @Test
    public void jdbc() {
        Config jdbcConfig = mock(Config.class);
        when(jdbcConfig.storage()).thenReturn("sql:jdbc:h2:mem:junit");
        when(jdbcConfig.workDir()).thenReturn(Paths.get("build"));

        InboundDbHelper inboundDbHelper = InboundDbHelper.from(jdbcConfig);
        assertThat(inboundDbHelper.getInputType()).isEqualTo(InputType.JDBC);
        assertThat(inboundDbHelper.getLevelDb()).isNotPresent();
        assertThat(inboundDbHelper.getJdbcDataSource()).isPresent();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unsupported() {
        Config unsupportedConfig = mock(Config.class);
        when(unsupportedConfig.storage()).thenReturn("unsupported");
        when(unsupportedConfig.workDir()).thenReturn(Paths.get("build"));

        InboundDbHelper.from(unsupportedConfig);
    }


}
