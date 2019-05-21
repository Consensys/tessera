package com.quorum.tessera.admin.cli;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.test.util.ElUtil;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AdminCliAdapterTest {

    private AdminCliAdapter adminCliAdapter;

    private ClientFactory clientFactory;

    private Invocation.Builder invocationBuilder;

    @Before
    public void onSetUp() {

        invocationBuilder = mock(Invocation.Builder.class);

        clientFactory = mock(ClientFactory.class);

        Client client = mock(Client.class);
        WebTarget webTarget = mock(WebTarget.class);

        when(client.target(any(URI.class))).thenReturn(webTarget);
        when(webTarget.path(anyString())).thenReturn(webTarget,webTarget);

        when(webTarget.request(MediaType.APPLICATION_JSON)).thenReturn(invocationBuilder);

        when(invocationBuilder.accept(MediaType.APPLICATION_JSON)).thenReturn(invocationBuilder);

        when(clientFactory.buildFrom(any(ServerConfig.class))).thenReturn(client);

        adminCliAdapter = new AdminCliAdapter(clientFactory);
    }

    @Test
    public void getType() {
        assertThat(adminCliAdapter.getType()).isEqualTo(CliType.ADMIN);
    }

    @Test
    public void defaultConstructor() {
        AdminCliAdapter def = new AdminCliAdapter();
        AdminCliAdapter arg = new AdminCliAdapter(new ClientFactory());

        assertThat(def).isEqualToComparingFieldByFieldRecursively(arg);
    }

    @Test
    public void help() throws Exception {
        //new CliResult(0, true, false, null);
        CliResult result = adminCliAdapter.execute("help");
        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.isSuppressStartup()).isTrue();
    }

    @Test
    public void noArgsSameAsHelp() throws Exception {
        CliResult result = adminCliAdapter.execute();
        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.isSuppressStartup()).isTrue();
    }

    @Test
    public void addPeer() throws Exception {

        Peer peer = new Peer("http://junit.com:8989");
        Entity entity = Entity.entity(peer, MediaType.APPLICATION_JSON);

        URI uri = UriBuilder.fromPath("/result").build();
        when(invocationBuilder.put(entity)).thenReturn(Response.created(uri).build());

        Path configFile = ElUtil.createAndPopulatePaths(getClass().getResource("/sample-config.json"));

        CliResult result = adminCliAdapter.execute("-addpeer",peer.getUrl(),"-configfile",configFile.toString());
        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.isSuppressStartup()).isTrue();

        assertThat(result.getStatus()).isEqualTo(0);

        verify(invocationBuilder).put(entity);
    }

    @Test
    public void addPeerSomethingBadWentDown() throws Exception {

        Peer peer = new Peer("http://junit.com:8989");
        Entity entity = Entity.entity(peer, MediaType.APPLICATION_JSON);

        URI uri = UriBuilder.fromPath("/result").build();
        when(invocationBuilder.put(entity)).thenReturn(Response.serverError().build());

        Path configFile = ElUtil.createAndPopulatePaths(getClass().getResource("/sample-config.json"));

        CliResult result = adminCliAdapter.execute("-addpeer",peer.getUrl(),"-configfile",configFile.toString());
        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.isSuppressStartup()).isTrue();

        assertThat(result.getStatus()).isEqualTo(1);

        verify(invocationBuilder).put(entity);
    }

    @Test
    public void noPeerProvidedReturnsNonZeroStatus() throws Exception {
        CliResult result = adminCliAdapter.execute("-configfile", "/path/to/file");
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(1);
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.isSuppressStartup()).isTrue();
    }
}
