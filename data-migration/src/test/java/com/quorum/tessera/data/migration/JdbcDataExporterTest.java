package com.quorum.tessera.data.migration;

import com.mockrunner.jdbc.BasicJDBCTestCaseAdapter;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class JdbcDataExporterTest extends BasicJDBCTestCaseAdapter {

    @Test
    public void runAnExportAndVerifyRunStatements() throws Exception {
        final String createStatement = "CREATE TEST TABLE";
        final String insertStatement = "INSERT INTO TABLE(?, ?)";

        final JdbcDataExporter exporter =
                new JdbcDataExporter("jdbc:bogus", insertStatement, singletonList(createStatement));

        final StoreLoader mockLoader = new MockDataLoader(singletonMap("HASH", "VALUE"));

        final Path output = mock(Path.class);

        exporter.export(mockLoader, output, "username", "password");

        final List<String> executedSQLStatements = super.getExecutedSQLStatements();
        assertThat(executedSQLStatements).hasSize(2).containsExactly(createStatement, insertStatement);

        final List<MockPreparedStatement> preparedStatements = super.getPreparedStatements();
        assertThat(preparedStatements).hasSize(1);
        assertThat(preparedStatements.get(0).getSQL()).isEqualTo("INSERT INTO TABLE(?, ?)");

        final byte[] key = (byte[]) super.getPreparedStatementParameter(preparedStatements.get(0), 1);
        final InputStream value = (InputStream) super.getPreparedStatementParameter(preparedStatements.get(0), 2);
        assertThat(new String(key)).isEqualTo("HASH");
        assertThat(new String(IOUtils.toByteArray(value))).isEqualTo("VALUE");

        verifyAllStatementsClosed();
    }
}
