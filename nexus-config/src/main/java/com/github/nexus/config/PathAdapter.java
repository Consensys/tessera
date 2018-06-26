package com.github.nexus.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class PathAdapter extends XmlAdapter<String,Path> {

    @Override
    public Path unmarshal(String v) throws Exception {
        if(Objects.isNull(v)) {
            return null;
        }
        return Paths.get(v);
    }

    @Override
    public String marshal(Path v) throws Exception {
        if(Objects.isNull(v)) {
            return null;
        }
        return v.toAbsolutePath().toString();
    }



}
