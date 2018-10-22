package com.quorum.tessera;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class AddCustomFunctionsToH2DB {
    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.
            getConnection("jdbc:h2:/home/nicolae/Develop/java/IJWorkspaces/tessera/tests/acceptance-test/target/h2/rest1", "sa", "");
        Statement stmt = conn.createStatement();
        String[] functions = {
            "CREATE or replace ALIAS bytesToHex FOR \"javax.xml.bind.DatatypeConverter.printHexBinary\"",
            "CREATE or replace ALIAS bytesToB64 FOR \"javax.xml.bind.DatatypeConverter.printBase64Binary\"",
            "CREATE or replace ALIAS hexToBytes FOR \"javax.xml.bind.DatatypeConverter.parseHexBinary\"",
            "CREATE or replace ALIAS b64ToBytes FOR \"javax.xml.bind.DatatypeConverter.parseBase64Binary\""
        };
        for (String func : functions){
            try{
                stmt.execute(func);
            } catch(SQLException e){
                e.printStackTrace();
            }
        }

        conn.close();
    }
}
