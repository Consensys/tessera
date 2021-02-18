package net.consensys.tessera.migration.data;

public interface MigrationInfoFactory<T> {

    void init() throws Exception;

    static  MigrationInfoFactory create(InboundDbHelper inboundDbHelper) {
        if(inboundDbHelper.getInputType() == InputType.LEVELDB) {
            return new LeveldbMigrationInfoFactory(inboundDbHelper.getLevelDb().get());
        }

        if(inboundDbHelper.getInputType() == InputType.JDBC) {
            return new JdbcMigrationInfoFactory(inboundDbHelper.getJdbcDataSource().get());
        }
        throw new UnsupportedOperationException();

    }

}
