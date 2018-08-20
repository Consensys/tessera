
package com.quorum.tessera.config.util;

import com.quorum.tessera.config.ConfigException;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

@FunctionalInterface
public interface XmlProcessingCallback<T> {
    
    T doExecute() throws IOException, JAXBException, TransformerException;
    
    static <T> T execute(XmlProcessingCallback<T> callback) {
        try {
            return callback.doExecute();
        } catch (IOException | JAXBException | TransformerException ex) {
           throw new ConfigException(ex);
        }
    }
    
    
}
