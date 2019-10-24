package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.ConfigProperties;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;

public class MapAdapter extends XmlAdapter<ConfigProperties, Map<String, String>> {

    @Override
    public Map<String, String> unmarshal(ConfigProperties configProperties) throws Exception {
        if (configProperties == null) return null;
        return configProperties.getProperties().stream()
                .collect(Collectors.toMap(p -> p.getName().getLocalPart(), p -> p.getValue()));
    }

    @Override
    public ConfigProperties marshal(Map<String, String> map) throws Exception {
        if (map == null) return null;

        ConfigProperties configProperties = new ConfigProperties();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            configProperties
                    .getProperties()
                    .add(new JAXBElement<>(new QName(entry.getKey()), String.class, entry.getValue()));
        }

        return configProperties;
    }
}
