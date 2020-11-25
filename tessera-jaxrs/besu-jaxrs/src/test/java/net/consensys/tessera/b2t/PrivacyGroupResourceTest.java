package net.consensys.tessera.b2t;

import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.util.Base64Codec;
import net.consensys.tessera.b2t.model.PrivacyGroupRequest;
import net.consensys.tessera.b2t.model.PrivacyGroupResponse;
import net.consensys.tessera.b2t.model.PrivacyGroupRetrieveRequest;
import net.consensys.tessera.b2t.model.PrivacyGroupSearchRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PrivacyGroupResourceTest {

    private JerseyTest jersey;

    private PrivacyGroupManager privacyGroupManager;

    private PrivacyGroup mockResult;

    @BeforeClass
    public static void setUpLoggers() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Before
    public void onSetup() throws Exception {

        privacyGroupManager = mock(PrivacyGroupManager.class);
        PrivacyGroupResource resource = new PrivacyGroupResource(privacyGroupManager);

        jersey =
            new JerseyTest() {
                @Override
                protected Application configure() {
                    forceSet(TestProperties.CONTAINER_PORT, "0");
                    enable(TestProperties.LOG_TRAFFIC);
                    enable(TestProperties.DUMP_ENTITY);
                    return new ResourceConfig().register(resource);
                }
            };

        jersey.setUp();

        mockResult = mock(PrivacyGroup.class);
        when(mockResult.getName()).thenReturn("name");
        when(mockResult.getDescription()).thenReturn("description");
        when(mockResult.getPrivacyGroupId()).thenReturn(PublicKey.from("id".getBytes()));
        when(mockResult.getMembers()).thenReturn(List.of(PublicKey.from("member1".getBytes()), PublicKey.from("member2".getBytes())));
        when(mockResult.getType()).thenReturn(PrivacyGroup.Type.PANTHEON);
        when(mockResult.getState()).thenReturn(PrivacyGroup.State.ACTIVE);
    }

    @After
    public void onTearDown() throws Exception {
        verifyNoMoreInteractions(privacyGroupManager);
        jersey.tearDown();
    }

    @Test
    public void testCreatePrivacyGroup() {

        String member1 = Base64Codec.create().encodeToString("member1".getBytes());
        String member2 = Base64Codec.create().encodeToString("member2".getBytes());
        PrivacyGroupRequest request = new PrivacyGroupRequest();
        request.setAddresses(new String[]{member1,member2});
        request.setFrom(member1);
        request.setName("name");
        request.setDescription("description");

        when(privacyGroupManager.createPrivacyGroup(any(), any(), anyList(), any(byte[].class)))
            .thenReturn(mockResult);

        final Response response = jersey.target("createPrivacyGroup")
            .request()
            .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);

        final PrivacyGroupResponse entity = response.readEntity(PrivacyGroupResponse.class);
        assertThat(entity.getName()).isEqualTo("name");
        assertThat(entity.getDescription()).isEqualTo("description");

        ArgumentCaptor<String> strArgCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<PublicKey>> memberCaptor = ArgumentCaptor.forClass(List.class);

        verify(privacyGroupManager).createPrivacyGroup(
            strArgCaptor.capture(),
            strArgCaptor.capture(),
            memberCaptor.capture(),
            any(byte[].class));

        assertThat(strArgCaptor.getAllValues()).containsExactly("name", "description");
        assertThat(memberCaptor.getValue())
            .containsExactly(PublicKey.from("member1".getBytes()), PublicKey.from("member2".getBytes()));

    }

    @Test
    public void testFindPrivacyGroup() {

        PrivacyGroupSearchRequest req = new PrivacyGroupSearchRequest();

        final List<PublicKey> members = List.of(PublicKey.from("member1".getBytes()), PublicKey.from("member2".getBytes()));
        req.setAddresses(members.stream().map(PublicKey::encodeToBase64).toArray(String[]::new));


        when(privacyGroupManager.findPrivacyGroup(members)).thenReturn(List.of(mockResult));

        final Response response = jersey.target("findPrivacyGroup")
            .request()
            .post(Entity.entity(req, MediaType.APPLICATION_JSON));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        PrivacyGroupResponse result = Arrays.stream(response.readEntity(PrivacyGroupResponse[].class)).iterator().next();
        assertThat(result.getName()).isEqualTo(mockResult.getName());
        assertThat(result.getDescription()).isEqualTo(mockResult.getDescription());
        assertThat(result.getAddresses()).isEqualTo(req.getAddresses());
        assertThat(result.getPrivacyGroupId()).isEqualTo(mockResult.getPrivacyGroupId().encodeToBase64());
        assertThat(result.getType()).isEqualTo(mockResult.getType().name());

        verify(privacyGroupManager).findPrivacyGroup(members);
    }

    @Test
    public void testRetrievePrivacyGroup() {

        PrivacyGroupRetrieveRequest req = new PrivacyGroupRetrieveRequest();
        req.setPrivacyGroupId("aWQ=");

        when(privacyGroupManager.retrievePrivacyGroup(mockResult.getPrivacyGroupId())).thenReturn(mockResult);

        final Response response = jersey.target("retrievePrivacyGroup")
            .request()
            .post(Entity.entity(req, MediaType.APPLICATION_JSON));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final PrivacyGroupResponse res = response.readEntity(PrivacyGroupResponse.class);
        assertThat(res.getName()).isEqualTo(mockResult.getName());
        assertThat(res.getDescription()).isEqualTo(mockResult.getDescription());
        assertThat(res.getAddresses()).isEqualTo(mockResult.getMembers().stream().map(PublicKey::encodeToBase64).toArray());
        assertThat(res.getPrivacyGroupId()).isEqualTo(mockResult.getPrivacyGroupId().encodeToBase64());
        assertThat(res.getType()).isEqualTo(mockResult.getType().name());

        verify(privacyGroupManager).retrievePrivacyGroup(PublicKey.from("id".getBytes()));

    }
}
