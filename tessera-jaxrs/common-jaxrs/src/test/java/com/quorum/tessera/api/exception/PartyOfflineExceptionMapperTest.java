package com.quorum.tessera.api.exception;

import com.quorum.tessera.partyinfo.PartyOfflineException;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class PartyOfflineExceptionMapperTest {

    private PartyOfflineExceptionMapper partyOfflineExceptionMapper;

    @Before
    public void onSetUp() {
        partyOfflineExceptionMapper = new PartyOfflineExceptionMapper();
    }

    @Test
    public void toResponse() {
        PartyOfflineException exception = new PartyOfflineException("Node not avialable",new Exception("OUCH"));

        Response response = partyOfflineExceptionMapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getStatusInfo().getReasonPhrase()).isEqualTo("Node not avialable");
    }

}
