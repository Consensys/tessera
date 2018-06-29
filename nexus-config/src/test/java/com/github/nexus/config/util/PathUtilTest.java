package com.github.nexus.config.util;

import com.github.nexus.config.ConfigException;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PathUtilTest {

    @Test
    public void readDataValueAndPathAreNotNull() {

        Path path = mock(Path.class);

        String value = "HELLOW";

        assertThat(PathUtil.readData(path, value)).isEqualTo(value);

    }

    @Test
    public void readDataValueIsNotNullAndPathIsNull() {

        String value = "HELLOW";

        assertThat(PathUtil.readData(null, value)).isEqualTo(value);

    }

    @Test
    public void readDataValueIsNullAndPathIsValid() throws URISyntaxException {

        Path path = Paths.get(getClass().getResource("/keyfile.txt").toURI());

        assertThat(PathUtil.readData(path, null)).isEqualTo("SOMEDATA");

    }

    @Test(expected = ConfigException.class)
    public void readPathThrowsIOException() {

        Path path = Paths.get("bogus");
        

        PathUtil.readData(path, null);

    }
}
