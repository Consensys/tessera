package com.quorum.tessera.jaxb;

import java.io.StringReader;
import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import org.junit.Test;

public class JaxbCallbackTest {

    @Test
    public void execute() {
        String sample = "<someObject><someValue>HELLOW</someValue></someObject>";

        SomeObject result = JaxbCallback.execute(() -> JAXB.unmarshal(new StringReader(sample), SomeObject.class));

        assertThat(result.getSomeValue()).isEqualTo("HELLOW");
    }

    @Test
    public void executeThrowsJAXException() {

        JAXBException exception = new JAXBException("GURU Meditation 22");

        try {
        JaxbCallback.execute(() -> {
           throw exception;
        });
            failBecauseExceptionWasNotThrown(DataBindingException.class);
        } catch(DataBindingException ex) {
            assertThat(ex).hasCause(exception);
        }

    }
}
