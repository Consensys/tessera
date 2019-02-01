package com.jpmorgan.quorum.enclave.websockets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;


public class EnclaveResponseCodec extends CodecAdapter<EnclaveResponse>  {

    @Override
    public boolean willDecode(ByteBuffer bytes) {
        return true;
    }

    @Override
    public ByteBuffer encode(EnclaveResponse object) throws EncodeException {

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos)){
            oos.writeObject(object);
            oos.flush();

            return ByteBuffer.wrap(bos.toByteArray());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public EnclaveResponse decode(ByteBuffer bytes) throws DecodeException {
        byte[] data = bytes.array();
        try(ObjectInput objectIn = new ObjectInputStream(new ByteArrayInputStream(data))) {
            
            return (EnclaveResponse) objectIn.readObject();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (ClassNotFoundException ex) {
            throw new WebSocketException(ex);
        }
    }
    
}
