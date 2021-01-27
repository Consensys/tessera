package net.consensys.tessera.migration.data;

import com.quorum.tessera.data.PrivacyGroupEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

public class PersistPrivacyGroupEventHandlerTest {

    private PersistPrivacyGroupEventHandler persistPrivacyGroupEventHandler;

    private EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    @Before
    public void beforeTest() {

        entityManagerFactory = mock(EntityManagerFactory.class);
        entityManager = mock(EntityManager.class);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);

        persistPrivacyGroupEventHandler = new PersistPrivacyGroupEventHandler(entityManagerFactory);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(entityManagerFactory);
        verifyNoMoreInteractions(entityManager);
    }

    @Test
    public void handle() throws Exception {

        OrionEvent orionEvent = mock(OrionEvent.class);
        when(orionEvent.getPayloadType()).thenReturn(PayloadType.PRIVACY_GROUP_PAYLOAD);
        when(orionEvent.getKey()).thenReturn("privacyGroupId".getBytes());

        List<String> addresses = Stream.of("ONE","TWO","THREE")
            .map(String::getBytes)
            .map(Base64.getEncoder()::encodeToString)
            .collect(Collectors.toList());

        JsonObject jsonObject = Json.createObjectBuilder()
            .add("addresses",Json.createArrayBuilder(addresses))
            .add("description","A very private group")
            .add("name","Give me lard")
            .add("state", "ACTIVE")
            .add("type","LEGACY")
            .build();

        when(orionEvent.getJsonObject()).thenReturn(jsonObject);

        persistPrivacyGroupEventHandler.onEvent(orionEvent);

        verify(entityManagerFactory).createEntityManager();
        verify(entityManager).persist(any(PrivacyGroupEntity.class));

    }

    @Test
    public void ignoreNonEncryptedPayloadTypes() throws Exception {
        Stream<PayloadType> payloadTypes = Stream.of(PayloadType.ENCRYPTED_PAYLOAD,PayloadType.QUERY_PRIVACY_GROUP_PAYLOAD);
        List<OrionEvent> events = payloadTypes.map(payloadType -> {
            OrionEvent orionEvent = mock(OrionEvent.class);
            when(orionEvent.getPayloadType()).thenReturn(payloadType);
            return orionEvent;
        }).collect(Collectors.toList());

        for(OrionEvent orionEvent : events) {
            persistPrivacyGroupEventHandler.onEvent(orionEvent);
        }

        verifyNoInteractions(entityManagerFactory);
    }


}
