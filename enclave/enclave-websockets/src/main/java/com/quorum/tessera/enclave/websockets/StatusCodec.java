package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.service.Service.Status;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;


public class StatusCodec extends JsonCodec<Status> {

    @Override
    protected JsonObjectBuilder doEncode(Status status) throws Exception {
        return Json.createObjectBuilder()
                .add("status", status.name());
    }

    @Override
    protected Status doDecode(JsonObject s) throws Exception {
        return Status.valueOf(s.getString("status"));
    }
    
}
