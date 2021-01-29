package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventHandler;
import com.quorum.tessera.data.PrivacyGroupEntity;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyGroupUtil;
import com.quorum.tessera.encryption.PublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class PersistPrivacyGroupEventHandler implements EventHandler<OrionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistPrivacyGroupEventHandler.class);

    private final EntityManagerFactory entityManagerFactory;

    public PersistPrivacyGroupEventHandler(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory);
    }

    @Override
    public void onEvent(OrionEvent event,long sequence,boolean endOfBatch) throws Exception {

        if(event.getPayloadType() != PayloadType.PRIVACY_GROUP_PAYLOAD) {
            LOGGER.debug("Ignoring event {}",event);
            return;
        }

        PublicKey privacyGroupId = PublicKey.from(event.getKey());

        JsonObject jsonObject = event.getJsonObject();

        List<PublicKey> members = jsonObject.getJsonArray("addresses")
            .stream()
            .map(JsonString.class::cast)
            .map(JsonString::getString)
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .collect(Collectors.toList());

        String description = jsonObject.getString("description");
        String name = jsonObject.getString("name");
        PrivacyGroup.State state = PrivacyGroup.State.valueOf(jsonObject.getString("state"));
        PrivacyGroup.Type type = PrivacyGroup.Type.valueOf(jsonObject.getString("type"));

        if(type == PrivacyGroup.Type.PANTHEON && !jsonObject.containsKey("randomSeed")) {
            throw new UnsupportedOperationException("No randomSeed elemet defined for PANTHEON group type");
        }

        byte[] seed = Optional.of(jsonObject)
            .filter(j -> j.containsKey("randomSeed"))
            .map(j -> j.getString("randomSeed"))
            .map(Base64.getDecoder()::decode)
            .orElse(new byte[0]);

        PrivacyGroup privacyGroup = PrivacyGroup.Builder.create()
            .withPrivacyGroupId(privacyGroupId)
            .withDescription(description)
            .withName(name)
            .withMembers(members)
            .withType(type)
            .withState(state)
            .withSeed(seed)
            .build();

        PrivacyGroupUtil privacyGroupUtil = PrivacyGroupUtil.create();
        byte[] privacyGroupData = privacyGroupUtil.encode(privacyGroup);
        byte[] lookupId = privacyGroupUtil.generateLookupId(privacyGroup.getMembers());

        PrivacyGroupEntity privacyGroupEntity = new PrivacyGroupEntity();
        privacyGroupEntity.setData(privacyGroupData);
        privacyGroupEntity.setLookupId(lookupId);
        privacyGroupEntity.setId(privacyGroup.getPrivacyGroupId().getKeyBytes());

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
        entityManager.persist(privacyGroupEntity);
        entityTransaction.commit();
        LOGGER.info("Persisted {}", event);

    }
}
