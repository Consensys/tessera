package net.consensys.tessera.migration.data;

public interface MigrationInfoFactory<T> {
    void init(T store);
}
