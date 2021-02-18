package net.consensys.tessera.migration.data;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LeveldbMigrationInfoFactory implements MigrationInfoFactory<DB> {

    private final DB leveldb;

    public LeveldbMigrationInfoFactory(DB leveldb) {
        this.leveldb = leveldb;
    }

    @Override
    public MigrationInfo init() throws Exception {

        AtomicInteger totalRecords = new AtomicInteger(0);
        AtomicInteger transactionRecords = new AtomicInteger(0);
        AtomicInteger privacyGroupRecords = new AtomicInteger(0);
        AtomicInteger queryPrivacyGroupRecords = new AtomicInteger(0);

        DBIterator iterator = leveldb.iterator();
        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            Map.Entry<byte[], byte[]> entry = iterator.peekNext();
            byte[] value = entry.getValue();
            PayloadType payloadType = PayloadType.parsePayloadType(value);
            switch (payloadType) {
                case ENCRYPTED_PAYLOAD:
                    transactionRecords.incrementAndGet();
                    break;
                case PRIVACY_GROUP_PAYLOAD:
                    privacyGroupRecords.incrementAndGet();
                    break;
                case QUERY_PRIVACY_GROUP_PAYLOAD:
                    queryPrivacyGroupRecords.incrementAndGet();
                    break;
                default:throw new UnsupportedOperationException();
            }
            totalRecords.incrementAndGet();
        }

       return MigrationInfo.from(
                                totalRecords.get(),
                                transactionRecords.get(),
                                privacyGroupRecords.get(),
                                queryPrivacyGroupRecords.get());

    }

}
