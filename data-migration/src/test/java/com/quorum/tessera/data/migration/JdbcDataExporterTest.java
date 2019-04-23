package com.quorum.tessera.data.migration;

import com.mockrunner.jdbc.BasicJDBCTestCaseAdapter;
import com.mockrunner.mock.jdbc.JDBCMockObjectFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.After;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;

public class JdbcDataExporterTest extends BasicJDBCTestCaseAdapter{

    private final JDBCMockObjectFactory mockObjectFactory = new JDBCMockObjectFactory();
    
    @Test
    public void onSetUp() {
        mockObjectFactory.registerMockDriver();
    }
    
    @After
    public void onTearDown(){
        mockObjectFactory.restoreDrivers();
    }
    
    @Test
    public void doStuff() throws Exception {
        Path sqlFile = Files.createTempFile(UUID.randomUUID().toString(),".txt");

        Files.write(sqlFile, Arrays.asList("create stuff"));

        JdbcDataExporter exporter = new JdbcDataExporter("jdbc:bogus","insert stuff",sqlFile.toUri().toURL());

        Map<byte[],InputStream> data = new HashMap<byte[],InputStream>() {{
            put("ONE".getBytes(),new ByteArrayInputStream("TWO".getBytes()));
        }};

        Path output = mock(Path.class);
        
        exporter.export(data, output, "someone", "pw");

        verifyAllStatementsClosed();
        verifyAllStatementsClosed();

    }

}
