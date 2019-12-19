package com.quorum.tessera.cli.admin.subcommands;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.admin.subcommands.AddPeerCommand;
import com.quorum.tessera.cli.parsers.ConfigurationMixin;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.io.SystemAdapter;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.test.util.ElUtil;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AddPeerCommandTest {

    private AddPeerCommand command;

    private Invocation.Builder invocationBuilder;

    @Before
    public void onSetUp() {
        invocationBuilder = mock(Invocation.Builder.class);

        final ClientFactory clientFactory = mock(ClientFactory.class);

        Client client = mock(Client.class);
        WebTarget webTarget = mock(WebTarget.class);

        when(client.target(any(URI.class))).thenReturn(webTarget);
        when(webTarget.path(anyString())).thenReturn(webTarget, webTarget);

        when(webTarget.request(APPLICATION_JSON)).thenReturn(invocationBuilder);

        when(invocationBuilder.accept(APPLICATION_JSON)).thenReturn(invocationBuilder);

        when(clientFactory.buildFrom(any(ServerConfig.class))).thenReturn(client);

        command = new AddPeerCommand(clientFactory, SystemAdapter.INSTANCE);
    }

    @Test
    public void defaultConstructor() {
        AddPeerCommand def = new AddPeerCommand();
        AddPeerCommand arg = new AddPeerCommand(new ClientFactory(), SystemAdapter.INSTANCE);

        assertThat(def).isEqualToComparingFieldByFieldRecursively(arg);
    }

    @Test
    public void nullParametersReturnsFalse() {
        // First go
        command.setConfigMixin(null);
        final CliResult resultOne = command.call();
        assertThat(resultOne).isEqualToComparingFieldByField(new CliResult(1, true, null));

        // Second go
        final ConfigurationMixin configurationMixin = new ConfigurationMixin();
        command.setConfigMixin(configurationMixin);
        final CliResult resultTwo = command.call();
        assertThat(resultTwo).isEqualToComparingFieldByField(new CliResult(1, true, null));

        // Third go
        final Config config = new Config();
        configurationMixin.setConfig(config);
        final CliResult resultThree = command.call();
        assertThat(resultThree).isEqualToComparingFieldByField(new CliResult(1, true, null));
    }

    @Test
    public void addPeer() throws Exception {
        final Entity entity = Entity.entity(new Peer("http://junit.com:8989"), APPLICATION_JSON);

        final URI uri = UriBuilder.fromPath("/result").build();
        when(invocationBuilder.put(entity)).thenReturn(Response.created(uri).build());

        final Path configFile = ElUtil.createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        final ConfigurationMixin mixin = new ConfigurationMixin();
        try (InputStream in = Files.newInputStream(configFile)) {
            final Config config = ConfigFactory.create().create(in);
            mixin.setConfig(config);
        }

        command.setConfigMixin(mixin);
        command.setPeerUrl("http://junit.com:8989");

        final CliResult result = command.call();
        assertThat(result).isEqualToComparingFieldByField(new CliResult(0, true, null));

        verify(invocationBuilder).put(entity);
    }

    @Test
    public void failToAddPeerReturnsFalse() throws Exception {

        Peer peer = new Peer("http://junit.com:8989");
        Entity entity = Entity.entity(peer, APPLICATION_JSON);

        when(invocationBuilder.put(entity)).thenReturn(Response.serverError().build());

        final Path configFile = ElUtil.createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        final ConfigurationMixin mixin = new ConfigurationMixin();
        try (InputStream in = Files.newInputStream(configFile)) {
            final Config config = ConfigFactory.create().create(in);
            mixin.setConfig(config);
        }

        command.setConfigMixin(mixin);
        command.setPeerUrl("http://junit.com:8989");

        final CliResult result = command.call();
        assertThat(result).isEqualToComparingFieldByField(new CliResult(1, true, null));

        verify(invocationBuilder).put(entity);
    }
}
