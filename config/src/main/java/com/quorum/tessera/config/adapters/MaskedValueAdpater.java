
package com.quorum.tessera.config.adapters;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.bind.annotation.adapters.XmlAdapter;


public class MaskedValueAdpater  extends XmlAdapter<String, String> {

    @Override
    public String unmarshal(String value) throws Exception {
        return value;
    }

    @Override
    public String marshal(String value) throws Exception {
        if(Objects.isNull(value)) {
            return null;
        }
        return IntStream.range(0, value.length())
                .mapToObj(i -> "*").collect(Collectors.joining());
            
    }
    
}
