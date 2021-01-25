package net.consensys.tessera.migration;

import net.consensys.tessera.migration.data.InboundDbHelper;
import net.consensys.tessera.migration.data.InputType;
import net.consensys.tessera.migration.data.MigrateDataCommand;
import net.consensys.tessera.migration.data.TesseraJdbcOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MigrateDataCommandTest {

    private MigrateDataCommand migrateDataCommand;

    private InboundDbHelper inboundDbHelper;

    private TesseraJdbcOptions tesseraJdbcOptions;

    private OrionKeyHelper orionKeyHelper;

    @Before
    public void beforeTest() {
        orionKeyHelper = mock(OrionKeyHelper.class);
        tesseraJdbcOptions = mock(TesseraJdbcOptions.class);
        inboundDbHelper = mock(InboundDbHelper.class);
        migrateDataCommand = new MigrateDataCommand(inboundDbHelper,tesseraJdbcOptions,orionKeyHelper);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(orionKeyHelper);
        verifyNoMoreInteractions(tesseraJdbcOptions);
        verifyNoMoreInteractions(inboundDbHelper);
    }

    @Test
    public void doStuff() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(inboundDbHelper.getJdbcDataSource()).thenReturn(Optional.of(dataSource));
        when(inboundDbHelper.getInputType()).thenReturn(InputType.JDBC);

        boolean result = migrateDataCommand.call();

        assertThat(result).isTrue();


    }

}
