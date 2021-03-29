package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.quorum.tessera.data.PrivacyGroupEntity;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyGroupUtil;
import com.quorum.tessera.encryption.PublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConvertToPrivacyGroupEntity implements EventHandler<OrionDataEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertToPrivacyGroupEntity.class);

    private Disruptor<TesseraDataEvent> tesseraDataEventDisruptor;

    private ObjectMapper cborObjectMapper = JacksonObjectMapperFactory.create();

    public ConvertToPrivacyGroupEntity(Disruptor<TesseraDataEvent> tesseraDataEventDisruptor) {
        this.tesseraDataEventDisruptor = Objects.requireNonNull(tesseraDataEventDisruptor);
    }

    @Override
    public void onEvent(OrionDataEvent event,long sequence,boolean endOfBatch) throws Exception {

        if(event.getPayloadType() != PayloadType.PRIVACY_GROUP_PAYLOAD) {
            LOGGER.debug("Ignoring event {}",event);
            return;
        }

        PrivacyGroup.Id privacyGroupId = PrivacyGroup.Id.fromBase64String(new String(event.getKey()));

        JsonObject jsonObject = cborObjectMapper.readValue(event.getPayloadData(),JsonObject.class);

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
            throw new UnsupportedOperationException("No randomSeed element defined for PANTHEON group type");
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
        privacyGroupEntity.setId(privacyGroup.getId().getBytes());

        tesseraDataEventDisruptor.publishEvent(new TesseraDataEvent<>(privacyGroupEntity));

        LOGGER.debug("Published {}", privacyGroupEntity);

    }
}
