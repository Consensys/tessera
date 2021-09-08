package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.ConfigProperties;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MapAdapter extends XmlAdapter<ConfigProperties, Map<String, String>> {

  @Override
  public Map<String, String> unmarshal(ConfigProperties configProperties) throws Exception {
    if (configProperties == null) return null;

    Map<String, String> outcome = new LinkedHashMap<>();
    // TODO : Find out why we have a org.w3c.dom.Element rarher than jakarta.xml.bind.JAXBElement
    for (Object element : configProperties.getProperties()) {

      //  outcome.put(element.getName(), element.getValue());
      if (Element.class.isInstance(element)) {
        String localname = Element.class.cast(element).getLocalName();
        String value =
            Optional.ofNullable(element)
                .map(Element.class::cast)
                .map(Element::getFirstChild)
                .map(Node::getNodeValue)
                .orElse(null);

        outcome.put(localname, value);
      }

      if (JAXBElement.class.isInstance(element)) {
        String localname = JAXBElement.class.cast(element).getName().getLocalPart();
        String value = Objects.toString(JAXBElement.class.cast(element).getValue());
        outcome.put(localname, value);
      }
    }

    return outcome;
  }

  @Override
  public ConfigProperties marshal(Map<String, String> map) throws Exception {
    if (map == null) return null;

    ConfigProperties configProperties = new ConfigProperties();

    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      JAXBElement<String> element = new JAXBElement<>(new QName(key), String.class, value);

      configProperties.getProperties().add(element);
    }

    return configProperties;
  }
}
