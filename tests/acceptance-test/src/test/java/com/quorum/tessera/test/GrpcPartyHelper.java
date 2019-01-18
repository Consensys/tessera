package com.quorum.tessera.test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class GrpcPartyHelper implements PartyHelper {

    private final List<Party> parties = new ArrayList<>();

    public GrpcPartyHelper() {
        parties.add(new Party("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", getClass().getResource("/grpc/config1.json"), "A"));
        parties.add(new Party("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=", getClass().getResource("/grpc/config2.json"), "B"));
        parties.add(new Party("giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=", getClass().getResource("/grpc/config3.json"), "C"));
        parties.add(new Party("Tj8xg/HpsYmh7Te3UerzlLx1HgpWVOGq25ZgbwaPNVM=", getClass().getResource("/grpc/config4.json"), "D"));
    }

    @Override
    public Stream<Party> getParties() {
        return parties.stream();
    }

}
