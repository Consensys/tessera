package net.consensys.tessera.migration.data;

import java.util.Optional;

public class MigrationInfo {

    private final int rowCount;

    private final int transactionCount;

    private final int privacyGroupCount;

    private final int queryPrivacyGroupCount;

    private enum Holder {
        INSTANCE;

        private MigrationInfo migrationInfo;

        private Optional<MigrationInfo> getMigrationInfo() {
            return Optional.ofNullable(migrationInfo);
        }

        private void setMigrationInfo(MigrationInfo migrationInfo) {
            this.migrationInfo = migrationInfo;
        }
    }

    private MigrationInfo(int rowCount, int transactionCount, int privacyGroupCount,int queryPrivacyGroupCount) {
        this.rowCount = rowCount;
        this.transactionCount = transactionCount;
        this.privacyGroupCount = privacyGroupCount;
        this.queryPrivacyGroupCount = queryPrivacyGroupCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public int getPrivacyGroupCount() {
        return privacyGroupCount;
    }

    public static MigrationInfo from(int rowCount, int transactionCount, int privacyGroupCount,int queryPrivacyGroupCount) {

        if (Holder.INSTANCE.getMigrationInfo().isEmpty()) {
            Holder.INSTANCE
                .setMigrationInfo(new MigrationInfo(rowCount, transactionCount, privacyGroupCount,queryPrivacyGroupCount));

        }
        return Holder.INSTANCE.getMigrationInfo().get();
    }

    @Override
    public String toString() {
        return "MigrationInfo{" +
            "rowCount=" + rowCount +
            ", transactionCount=" + transactionCount +
            ", privacyGroupCount=" + privacyGroupCount +
            ", queryPrivacyGroupCount=" + queryPrivacyGroupCount +
            '}';
    }

    public static MigrationInfo getInstance() {
        return Holder.INSTANCE.getMigrationInfo().get();
    }

    static void clear() {
        Holder.INSTANCE.setMigrationInfo(null);
    }
}
