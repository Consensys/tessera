package com.quorum.tessera.data.migration;

public interface DataExporterFactory {

    static DataExporter create(ExportType exportType) {
        if (exportType == ExportType.H2) {
            return new H2DataExporter();
        } else if (exportType == ExportType.SQLITE) {
            return new SqliteDataExporter();
        } 
        throw new UnsupportedOperationException(""+ exportType);
    }

}
