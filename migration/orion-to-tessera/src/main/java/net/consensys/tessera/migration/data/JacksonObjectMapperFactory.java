package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;

public interface JacksonObjectMapperFactory {

    static ObjectMapper create() {
        return JsonMapper.builder(new CBORFactory())
            .addModule(new Jdk8Module())
            .addModule(new JSR353Module())
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();
    }

}
