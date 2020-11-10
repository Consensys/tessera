package com.quorum.tessera.config.util;

import com.quorum.tessera.config.util.jaxb.JaxbCallback;
import org.junit.Test;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class JaxbCallbackTest {

    @Test
    public void execute() {
        final String sample = "<someObject><someValue>Test Value</someValue></someObject>";

        final SomeObject result = JaxbCallback.execute(() -> JAXB.unmarshal(new StringReader(sample), SomeObject.class));

        assertThat(result.getSomeValue()).isEqualTo("Test Value");
    }

    @Test
    public void executeThrowsJAXException() {

        final JAXBException exception = new JAXBException("GURU Meditation 22");

        final Throwable throwable = catchThrowable(() ->
            JaxbCallback.execute(() -> {
                throw exception;
            })
        );

        assertThat(throwable).isNotNull().isInstanceOf(DataBindingException.class).hasCause(exception);
    }
}
