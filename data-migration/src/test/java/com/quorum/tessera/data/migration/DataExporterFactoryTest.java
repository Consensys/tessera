
package com.quorum.tessera.data.migration;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class DataExporterFactoryTest {
 
    @Test
    public void createForH2() {
       DataExporter result =  DataExporterFactory.create(ExportType.H2);
       assertThat(result).isExactlyInstanceOf(H2DataExporter.class);
    }
    
    @Test
    public void createForSqlite() {
       DataExporter result =  DataExporterFactory.create(ExportType.SQLITE);
       assertThat(result).isExactlyInstanceOf(SqliteDataExporter.class);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void createForUndefined() {
       DataExporterFactory.create(null);
    }
}
