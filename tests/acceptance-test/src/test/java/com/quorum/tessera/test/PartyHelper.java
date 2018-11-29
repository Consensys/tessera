package com.quorum.tessera.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface PartyHelper {

    Stream<Party> getParties();

    default Party findByAlias(String alias) {
        return getParties()
            .filter(p -> p.getAlias().equals(alias))
            .findAny()
            .orElseThrow(() -> new RuntimeException("No party found with alias " + alias));
    }

    default Party findByPublicKey(String publicKey) {
        return getParties()
            .filter(p -> p.getPublicKey().equals(publicKey))
            .findAny()
            .orElseThrow(() -> new RuntimeException("No party found with publicKey " + publicKey));

    }
    
    static void initialiseData(List<Party> parties) {
        
       String createTableSql = Stream.of(PartyHelper.class.getResourceAsStream("/ddls/h2-ddl.sql"))
               .map(InputStreamReader::new)
               .map(BufferedReader::new)
               .flatMap(BufferedReader::lines)
               .collect(Collectors.joining(System.lineSeparator()));

         parties.forEach(party -> {
        
            try (Connection conn = party.getDatabaseConnection()) {
                try(Statement statement = conn.createStatement()) {
                    statement.execute(createTableSql);
                }
            } catch(SQLException ex) {
                //IGNORE Assume table already exists
          //      throw new UncheckedSQLException(ex);
            }
        });
        
                
        
        
    }
    

}
