package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.nio.ByteBuffer;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class PartyInfoCodecTest {

    private PartyInfoCodec codec;

    private PartyInfoParser parser;

    @Before
    public void onSetUp() {
        parser = mock(PartyInfoParser.class);
        codec = new PartyInfoCodec(parser);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(parser);
    }

    @Test
    public void decode() throws DecodeException {

        byte[] data = "GIVEMELARD".getBytes();

        ByteBuffer buffer = ByteBuffer.wrap(data);

        PartyInfo partyInfo = mock(PartyInfo.class);

        when(parser.from(data)).thenReturn(partyInfo);

        PartyInfo result = codec.decode(buffer);

        assertThat(result).isSameAs(result);

        verify(parser).from(data);
    }

    @Test
    public void encode() throws EncodeException {

        byte[] data = "ILOVESPARROWS".getBytes();

        PartyInfo partyInfo = mock(PartyInfo.class);

        when(parser.to(partyInfo)).thenReturn(data);

        ByteBuffer result = codec.encode(partyInfo);

        assertThat(result.array()).isEqualTo(data);

        verify(parser).to(partyInfo);
    }

    @Test
    public void createDefaultInstance() {
        assertThat(new PartyInfoCodec()).isNotNull();
    }

    @Test
    public void destroyDoesNothing() {
        codec.destroy();
        verifyZeroInteractions(parser);
    }

    @Test
    public void initDoesNothing() {
        EndpointConfig config = mock(EndpointConfig.class);
        codec.init(config);
        verifyZeroInteractions(config);
        verifyZeroInteractions(parser);
    }

    @Test
    public void willDecodeAlwaysReturnsTrue() {
        assertThat(codec.willDecode(ByteBuffer.allocate(1))).isTrue();
        assertThat(codec.willDecode((ByteBuffer) null)).isTrue();
    }
}
