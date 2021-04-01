package net.consensys.tessera.migration;

import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OrionKeyHelperConvertor implements CommandLine.ITypeConverter<OrionKeyHelper> {

    @Override
    public OrionKeyHelper convert(String value) throws Exception {
        Path path = Paths.get(value);
        return OrionKeyHelper.from(path);
    }
}
