package net.consensys.tessera.migration;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class TeeOutputStream extends OutputStream {

    private List<OutputStream> outputStreams;

    public TeeOutputStream(OutputStream... outputStream) {
        this.outputStreams = List.of(outputStream);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        for (OutputStream outputStream : outputStreams) {
            outputStream.write(buf, off, len);
        }
    }

    public void write(int b) throws IOException {
        for (OutputStream outputStream : outputStreams) {
            outputStream.write(b);
        }
    }

    public void flush() throws IOException {
        for (OutputStream outputStream : outputStreams) {
            outputStream.flush();
        }
    }

    public void close() throws IOException {
        for (OutputStream outputStream : outputStreams) {
            outputStream.close();
        }
    }
}
