package com.quorum.tessera.data.migration;

import com.quorum.tessera.io.IOCallback;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public interface Utils {

    static byte[] toByteArray(InputStream in) {
        return IOCallback.execute(() -> {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }

            return os.toByteArray();
        });
    }
}
