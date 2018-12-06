
package com.quorum.tessera.data.migration;

import com.mockrunner.jdbc.BasicJDBCTestCaseAdapter;
import com.mockrunner.mock.jdbc.JDBCMockObjectFactory;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
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
        

        JdbcDataExporter exporter = new JdbcDataExporter("jdbc:bogus","insert stuff","create stuff");
        
        Map<byte[],byte[]> data = new HashMap<byte[],byte[]>() {{
            put("ONE".getBytes(),"TWO".getBytes());
        }};
        
        
        
        Path output = mock(Path.class);
        
        exporter.export(data, output, "someone", "pw");

        verifyAllStatementsClosed();
        verifyAllStatementsClosed();

        
    }
    
}
