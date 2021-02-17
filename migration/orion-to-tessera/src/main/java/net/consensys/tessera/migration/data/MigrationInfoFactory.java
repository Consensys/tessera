package net.consensys.tessera.migration.data;

public interface MigrationInfoFactory<T> {
    MigrationInfo from(T store);

    static <T> MigrationInfoFactory<T> create(InputType inputType) {
        switch (inputType) {
            case LEVELDB:
                return (MigrationInfoFactory<T>) new LeveldbMigrationInfoFactory();
            default:
                throw new UnsupportedOperationException();
        }
    }
}
