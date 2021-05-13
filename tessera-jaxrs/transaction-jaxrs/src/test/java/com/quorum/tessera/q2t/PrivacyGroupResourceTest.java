package com.quorum.tessera.q2t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.api.*;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.util.Base64Codec;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class PrivacyGroupResourceTest {

  private JerseyTest jersey;

  private PrivacyGroupManager privacyGroupManager;

  private PrivacyGroupResource resource;

  private PrivacyGroup mockResult;

  RuntimeContext runtimeContext = RuntimeContext.getInstance();

  @BeforeClass
  public static void setUpLoggers() {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  @Before
  public void onSetup() throws Exception {

    when(runtimeContext.isMultiplePrivateStates()).thenReturn(false);

    privacyGroupManager = mock(PrivacyGroupManager.class);
    resource = new PrivacyGroupResource(privacyGroupManager);

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
    when(mockResult.getId()).thenReturn(PrivacyGroup.Id.fromBytes("id".getBytes()));
    when(mockResult.getMembers())
        .thenReturn(
            List.of(PublicKey.from("member1".getBytes()), PublicKey.from("member2".getBytes())));
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
    request.setAddresses(new String[] {member1, member2});
    request.setFrom(member1);
    request.setName("name");
    request.setDescription("description");

    when(privacyGroupManager.createPrivacyGroup(any(), any(), any(), anyList(), any(byte[].class)))
        .thenReturn(mockResult);

    final Response response =
        jersey
            .target("createPrivacyGroup")
            .request()
            .post(Entity.entity(request, MediaType.APPLICATION_JSON));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    final PrivacyGroupResponse entity = response.readEntity(PrivacyGroupResponse.class);
    assertThat(entity.getName()).isEqualTo("name");
    assertThat(entity.getDescription()).isEqualTo("description");

    ArgumentCaptor<String> strArgCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<List<PublicKey>> memberCaptor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<PublicKey> fromArgCaptor = ArgumentCaptor.forClass(PublicKey.class);

    verify(privacyGroupManager)
        .createPrivacyGroup(
            strArgCaptor.capture(),
            strArgCaptor.capture(),
            fromArgCaptor.capture(),
            memberCaptor.capture(),
            any(byte[].class));

    assertThat(strArgCaptor.getAllValues()).containsExactly("name", "description");
    assertThat(memberCaptor.getValue())
        .containsExactly(
            PublicKey.from("member1".getBytes()), PublicKey.from("member2".getBytes()));
    assertThat(fromArgCaptor.getValue().encodeToBase64()).isEqualTo(member1);
  }

  @Test
  public void testCreatePrivacyGroupMinimal() {
    String member1 = Base64Codec.create().encodeToString("member1".getBytes());
    String member2 = Base64Codec.create().encodeToString("member2".getBytes());
    PrivacyGroupRequest request = new PrivacyGroupRequest();
    request.setAddresses(new String[] {member1, member2});
    request.setFrom(member1);
    when(privacyGroupManager.createPrivacyGroup(any(), any(), any(), anyList(), any(byte[].class)))
        .thenReturn(mockResult);

    final Response response =
        jersey
            .target("createPrivacyGroup")
            .request()
            .post(Entity.entity(request, MediaType.APPLICATION_JSON));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    ArgumentCaptor<String> strArgCaptor = ArgumentCaptor.forClass(String.class);

    verify(privacyGroupManager)
        .createPrivacyGroup(
            strArgCaptor.capture(), strArgCaptor.capture(), any(), any(), any(byte[].class));

    assertThat(strArgCaptor.getAllValues()).contains("");
  }

  @Test
  public void testFindPrivacyGroup() {

    PrivacyGroupSearchRequest req = new PrivacyGroupSearchRequest();

    final List<PublicKey> members =
        List.of(PublicKey.from("member1".getBytes()), PublicKey.from("member2".getBytes()));
    req.setAddresses(members.stream().map(PublicKey::encodeToBase64).toArray(String[]::new));

    when(privacyGroupManager.findPrivacyGroup(members)).thenReturn(List.of(mockResult));

    final Response response =
        jersey
            .target("findPrivacyGroup")
            .request()
            .post(Entity.entity(req, MediaType.APPLICATION_JSON));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);
    PrivacyGroupResponse result =
        Arrays.stream(response.readEntity(PrivacyGroupResponse[].class)).iterator().next();
    assertThat(result.getName()).isEqualTo(mockResult.getName());
    assertThat(result.getDescription()).isEqualTo(mockResult.getDescription());
    assertThat(result.getMembers()).isEqualTo(req.getAddresses());
    assertThat(result.getPrivacyGroupId()).isEqualTo(mockResult.getId().getBase64());
    assertThat(result.getType()).isEqualTo(mockResult.getType().name());

    verify(privacyGroupManager).findPrivacyGroup(members);
  }

  @Test
  public void testRetrievePrivacyGroup() {

    PrivacyGroupRetrieveRequest req = new PrivacyGroupRetrieveRequest();
    req.setPrivacyGroupId("aWQ=");

    when(privacyGroupManager.retrievePrivacyGroup(mockResult.getId())).thenReturn(mockResult);

    final Response response =
        jersey
            .target("retrievePrivacyGroup")
            .request()
            .post(Entity.entity(req, MediaType.APPLICATION_JSON));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    final PrivacyGroupResponse res = response.readEntity(PrivacyGroupResponse.class);
    assertThat(res.getName()).isEqualTo(mockResult.getName());
    assertThat(res.getDescription()).isEqualTo(mockResult.getDescription());
    assertThat(res.getMembers())
        .isEqualTo(mockResult.getMembers().stream().map(PublicKey::encodeToBase64).toArray());
    assertThat(res.getPrivacyGroupId()).isEqualTo(mockResult.getId().getBase64());
    assertThat(res.getType()).isEqualTo(mockResult.getType().name());

    verify(privacyGroupManager).retrievePrivacyGroup(PrivacyGroup.Id.fromBytes("id".getBytes()));
  }

  @Test
  public void testDeletePrivacyGroup() {

    PrivacyGroupDeleteRequest req = new PrivacyGroupDeleteRequest();
    req.setPrivacyGroupId("aWQ=");
    req.setFrom(PublicKey.from("member1".getBytes()).encodeToBase64());

    when(privacyGroupManager.deletePrivacyGroup(
            PublicKey.from("member1".getBytes()), mockResult.getId()))
        .thenReturn(mockResult);

    final Response response =
        jersey
            .target("deletePrivacyGroup")
            .request()
            .post(Entity.entity(req, MediaType.APPLICATION_JSON));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    final String out = response.readEntity(String.class);

    assertThat(out).isEqualTo("\"aWQ=\"");

    verify(privacyGroupManager)
        .deletePrivacyGroup(PublicKey.from("member1".getBytes()), mockResult.getId());
  }

  @Test
  public void testGetGroups() {

    RuntimeContext context = RuntimeContext.getInstance();
    when(context.isMultiplePrivateStates()).thenReturn(true);

    when(privacyGroupManager.findPrivacyGroupByType(PrivacyGroup.Type.RESIDENT))
        .thenReturn(List.of(mockResult));

    final Response response = resource.getPrivacyGroups("resident");

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    PrivacyGroupResponse result = ((PrivacyGroupResponse[]) response.getEntity())[0];
    assertThat(result.getName()).isEqualTo(mockResult.getName());
    assertThat(result.getDescription()).isEqualTo(mockResult.getDescription());
    assertThat(result.getPrivacyGroupId()).isEqualTo(mockResult.getId().getBase64());
    assertThat(result.getType()).isEqualTo(mockResult.getType().name());

    verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
  }

  @Test
  public void testGetGroupsMPSDisabled() {

    RuntimeContext context = RuntimeContext.getInstance();
    when(context.isMultiplePrivateStates()).thenReturn(false);

    final Response response = resource.getPrivacyGroups("resident");

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(503);
    assertThat(response.getEntity())
        .isEqualTo("Multiple private state feature is not available on this privacy manager");
  }

  @Test
  public void testGetNoGroupsFound() {
    final Response response = jersey.target("groups/legacy").request().get();

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);
    PrivacyGroupResponse[] responses = response.readEntity(PrivacyGroupResponse[].class);

    assertThat(responses.length).isEqualTo(0);

    verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.LEGACY));
  }

  @Test
  public void testGetGroupNotValid() {
    final Response response = jersey.target("groups/bogus").request().get();
    assertThat(response.getStatus()).isNotEqualTo(200);
  }
}
