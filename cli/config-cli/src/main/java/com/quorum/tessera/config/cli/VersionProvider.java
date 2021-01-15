package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Version;
import picocli.CommandLine;

class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() {
        return new String[]{Version.getVersion()};
    }
}
