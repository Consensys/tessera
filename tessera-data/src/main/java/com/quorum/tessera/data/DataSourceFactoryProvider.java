package com.quorum.tessera.data;

public class DataSourceFactoryProvider {

    public static DataSourceFactory provider() {
        return HikariDataSourceFactory.INSTANCE;
    }

}


