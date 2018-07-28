package com.quorum.tessera.config.util;

/**
 * Provides mapping from jdbc url input to the appropriate jdbc driver class name
 */
public enum JdbcDriverClassName {

    H2 {
        @Override
        public String toString() {
            return "org.h2.Driver";
        }
    },

    HSQL {
        @Override
        public String toString() {
            return "org.hsqldb.jdbc.JDBCDriver";
        }
    },

    SQLITE {
        @Override
        public String toString() {
            return "org.sqlite.JDBC";
        }
    };

    public static String fromUrl(final String url) {
        if (url.contains("hsql")) {
            return HSQL.toString();
        }
        if (url.contains("sqlite")) {
            return SQLITE.toString();
        }
        return H2.toString();
    }
}
