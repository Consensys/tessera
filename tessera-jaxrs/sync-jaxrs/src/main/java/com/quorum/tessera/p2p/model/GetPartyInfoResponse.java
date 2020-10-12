package com.quorum.tessera.p2p.model;


import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

// TODO(cjh) use this in the actual jaxrs method response
public class GetPartyInfoResponse {

    @Schema(description = "server's url")
    @XmlElement
    public String url;

    @Schema()
    @XmlElement
    public List<Key> keys;

    @XmlElement
    public List<Peer> peers;
}
