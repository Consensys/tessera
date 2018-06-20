package com.github.nexus.config.jaxb;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class PathAdapter extends XmlAdapter<String,Path> {

    @Override
    public Path unmarshal(String v) throws Exception {
        return Paths.get(v);
    }

    @Override
    public String marshal(Path v) throws Exception {
        return v.toAbsolutePath().toString();
    }



}
