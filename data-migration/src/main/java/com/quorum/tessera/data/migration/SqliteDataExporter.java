
package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;


public class SqliteDataExporter implements DataExporter {

    @Override
    public void export(Map<byte[], byte[]> data) throws SQLException, IOException {

        final String connectionString = "jdbc:sqlite:"+ calculateExportPath();
        
        try (Connection conn = DriverManager.getConnection(connectionString)) {


            try (Statement stmt = conn.createStatement()) {
                
               int tableCreated =  stmt.executeUpdate("CREATE TABLE ENCRYPTED_TRANSACTION "
                       + "(ID NUMBER(19) NOT NULL, "
                       + "ENCODED_PAYLOAD BLOB NOT NULL, "
                       + "HASH BLOB NOT NULL UNIQUE, "
                       + "PRIMARY KEY (ID))");

               if(tableCreated != 0) throw new IllegalStateException("UNABLE to Create table");
               
                stmt.execute("CREATE TABLE SEQUENCE (SEQ_NAME VARCHAR(50) NOT NULL, SEQ_COUNT NUMBER(19), PRIMARY KEY (SEQ_NAME))");
                stmt.execute("INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('ENC_TX_SEQ', 0)");
                

            }

            try (PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO ENCRYPTED_TRANSACTION "
                    + "(ID,HASH,ENCODED_PAYLOAD) "
                    + "VALUES ((SELECT MAX(SEQ_COUNT) FROM SEQUENCE WHERE SEQ_NAME = 'ENC_TX_SEQ')  ,?,?)")) {
                for (Map.Entry<byte[], byte[]> values : data.entrySet()) {
                    insertStatement.setBytes(1, values.getKey());
                    insertStatement.setBytes(2, values.getValue());
                    insertStatement.execute();
                }
            }

        }
    }

    @Override
    public String dbId() {
        return "sqlite";
    }
    
}
