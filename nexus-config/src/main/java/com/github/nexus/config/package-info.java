@javax.xml.bind.annotation.XmlSchema(namespace = "http://nexus.github.com/config",
        elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED, attributeFormDefault = javax.xml.bind.annotation.XmlNsForm.UNQUALIFIED)
@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(PathAdapter.class)}
)
package com.github.nexus.config;

import com.github.nexus.config.adapters.PathAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
