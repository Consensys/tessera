
package com.quorum.tessera.data.migration;

import java.nio.file.Path;

public interface DataExporterFactory {

    static DataExporter create(ExportType exportType,Path outputFile) {
        switch(exportType) {
            case H2 : return new H2DataExporter();
            case SQLITE : return new SqliteDataExporter();
            default: return new DefaultDataExporter();
        }
    }
    
    
}
