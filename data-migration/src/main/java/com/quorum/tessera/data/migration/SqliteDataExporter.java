
package com.quorum.tessera.data.migration;

import java.nio.file.Path;
import java.sql.*;
import java.util.Map;


public class SqliteDataExporter implements DataExporter {

    @Override
    public void export(Map<byte[], byte[]> data,Path output) throws SQLException {

        final String connectionString = "jdbc:sqlite:"+ output.toString();
        
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


    
}
