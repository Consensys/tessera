package com.quorum.tessera.q2t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.api.*;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import jakarta.ws.rs.core.Response;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class PrivacyGroupResourceTest {

  private PrivacyGroupManager privacyGroupManager;

  private PrivacyGroup mockResult;

  private PrivacyGroupResource privacyGroupResource;

  @Before
  public void onSetup() throws Exception {

    privacyGroupManager = mock(PrivacyGroupManager.class);
    privacyGroupResource = new PrivacyGroupResource(privacyGroupManager);

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
  }

  @Test
  public void testCreatePrivacyGroup() {

    String member1 = Base64.getEncoder().encodeToString("member1".getBytes());
    String member2 = Base64.getEncoder().encodeToString("member2".getBytes());
    PrivacyGroupRequest request = new PrivacyGroupRequest();
    request.setAddresses(new String[] {member1, member2});
    request.setFrom(member1);
    request.setName("name");
    request.setDescription("description");

    when(privacyGroupManager.createPrivacyGroup(any(), any(), any(), anyList(), any(byte[].class)))
        .thenReturn(mockResult);

    final Response response = privacyGroupResource.createPrivacyGroup(request);
    // jersey.target("createPrivacyGroup").request().post(Entity.entity(request,
    // MediaType.APPLICATION_JSON));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    final PrivacyGroupResponse entity = (PrivacyGroupResponse) response.getEntity();
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
  public void testFindPrivacyGroup() {

    PrivacyGroupSearchRequest req = new PrivacyGroupSearchRequest();

    final List<PublicKey> members =
        List.of(PublicKey.from("member1".getBytes()), PublicKey.from("member2".getBytes()));
    req.setAddresses(members.stream().map(PublicKey::encodeToBase64).toArray(String[]::new));

    when(privacyGroupManager.findPrivacyGroup(members)).thenReturn(List.of(mockResult));

    final Response response = privacyGroupResource.findPrivacyGroup(req);
    // jersey.target("findPrivacyGroup").request().post(Entity.entity(req,
    // MediaType.APPLICATION_JSON));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    PrivacyGroupResponse[] privacyGroupResponses = (PrivacyGroupResponse[]) response.getEntity();

    PrivacyGroupResponse result = privacyGroupResponses[0];
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

    final Response response = privacyGroupResource.retrievePrivacyGroup(req);
    //   jersey.target("retrievePrivacyGroup").request().post(Entity.entity(req,
    // MediaType.APPLICATION_JSON));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    final PrivacyGroupResponse res = (PrivacyGroupResponse) response.getEntity();
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

    final Response response = privacyGroupResource.deletePrivacyGroup(req);
    //   jersey.target("deletePrivacyGroup").request().post(Entity.entity(req,
    // MediaType.APPLICATION_JSON));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    final String out = Objects.toString(response.getEntity());

    assertThat(out).isEqualTo("\"aWQ=\"");

    verify(privacyGroupManager)
        .deletePrivacyGroup(PublicKey.from("member1".getBytes()), mockResult.getId());
  }

  @Test
  public void testGetGroups() {

    final RuntimeContext runtimeContext = mock(RuntimeContext.class);
    when(runtimeContext.isMultiplePrivateStates()).thenReturn(true);

    when(privacyGroupManager.findPrivacyGroupByType(PrivacyGroup.Type.RESIDENT))
        .thenReturn(List.of(mockResult));

    try (var runtimeContextMockedStatic = mockStatic(RuntimeContext.class)) {
      runtimeContextMockedStatic.when(RuntimeContext::getInstance).thenReturn(runtimeContext);

      final Response response = privacyGroupResource.getPrivacyGroups("resident");

      assertThat(response).isNotNull();
      assertThat(response.getStatus()).isEqualTo(200);

      PrivacyGroupResponse result = ((PrivacyGroupResponse[]) response.getEntity())[0];
      assertThat(result.getName()).isEqualTo(mockResult.getName());
      assertThat(result.getDescription()).isEqualTo(mockResult.getDescription());
      assertThat(result.getPrivacyGroupId()).isEqualTo(mockResult.getId().getBase64());
      assertThat(result.getType()).isEqualTo(mockResult.getType().name());

      runtimeContextMockedStatic.verify(RuntimeContext::getInstance);
      runtimeContextMockedStatic.verifyNoMoreInteractions();
    }
    verify(runtimeContext).isMultiplePrivateStates();
    verifyNoMoreInteractions(runtimeContext);
    verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
  }

  @Test
  public void testGetGroupsMPSDisabled() {

    RuntimeContext runtimeContext = mock(RuntimeContext.class);
    when(runtimeContext.isMultiplePrivateStates()).thenReturn(false);

    try (var runtimeContextMockedStatic = mockStatic(RuntimeContext.class)) {
      runtimeContextMockedStatic.when(RuntimeContext::getInstance).thenReturn(runtimeContext);
      final Response response = privacyGroupResource.getPrivacyGroups("resident");

      assertThat(response).isNotNull();
      assertThat(response.getStatus()).isEqualTo(503);
      assertThat(response.getEntity())
          .isEqualTo("Multiple private state feature is not available on this privacy manager");

      runtimeContextMockedStatic.verify(RuntimeContext::getInstance);
      runtimeContextMockedStatic.verifyNoMoreInteractions();
    }

    verify(runtimeContext).isMultiplePrivateStates();
    verifyNoMoreInteractions(runtimeContext);
  }

  @Test
  public void testGetNoGroupsFound() {

    RuntimeContext runtimeContext = mock(RuntimeContext.class);

    try (var runtimeContextMockedStatic = mockStatic(RuntimeContext.class)) {
      runtimeContextMockedStatic.when(RuntimeContext::getInstance).thenReturn(runtimeContext);

      final Response response = privacyGroupResource.getPrivacyGroups("legacy");

      assertThat(response).isNotNull();
      assertThat(response.getStatus()).isEqualTo(200);
      PrivacyGroupResponse[] responses = (PrivacyGroupResponse[]) response.getEntity();

      assertThat(responses.length).isEqualTo(0);
      runtimeContextMockedStatic.verify(RuntimeContext::getInstance);
      runtimeContextMockedStatic.verifyNoMoreInteractions();
    }
    verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.LEGACY));
    verifyNoMoreInteractions(runtimeContext);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetGroupNotValid() {
    privacyGroupResource.getPrivacyGroups("bogus");
  }
}
