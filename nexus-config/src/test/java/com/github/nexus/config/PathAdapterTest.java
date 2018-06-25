package com.github.nexus.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PathAdapterTest {

    private PathAdapter pathAdapter;

    public PathAdapterTest() {
    }

    @Before
    public void setUp() {
        pathAdapter = new PathAdapter();
    }

    @After
    public void tearDown() {
        pathAdapter = null;
    }

    @Test
    public void marshal() throws Exception {

        String value = "/somepath/to";

        Path path = Paths.get(value, "someplace");
        String result = pathAdapter.marshal(path);

        assertThat(result).isEqualTo("/somepath/to/someplace");
    }
    
    @Test
    public void marshalNull() throws Exception {

        String result = pathAdapter.marshal(null);
        assertThat(result).isNull();
    }
    
    @Test
    public void unmarshal() throws Exception {

        String value = "/somepath/to/someplace";

        Path result = pathAdapter.unmarshal(value);

        assertThat(result).isEqualTo(Paths.get(value));
    }
    
    @Test
    public void unmarshalNull() throws Exception {

        Path result = pathAdapter.unmarshal(null);

        assertThat(result).isNull();
    }
}
