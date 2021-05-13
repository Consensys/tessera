package net.consensys.tessera.migration.data;

public interface MigrationInfoFactory<T> {

  MigrationInfo init() throws Exception;

  static MigrationInfo create(InboundDbHelper inboundDbHelper) throws Exception {

    if (inboundDbHelper.getInputType() == InputType.LEVELDB) {
      return new LeveldbMigrationInfoFactory(inboundDbHelper.getLevelDb().get()).init();
    }

    if (inboundDbHelper.getInputType() == InputType.JDBC) {
      return new JdbcMigrationInfoFactory(inboundDbHelper.getJdbcDataSource().get()).init();
    }
    throw new UnsupportedOperationException();
  }
}
