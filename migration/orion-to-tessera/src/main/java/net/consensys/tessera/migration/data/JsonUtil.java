package net.consensys.tessera.migration.data;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Map;

public class JsonUtil {

    static synchronized void prettyPrint(JsonObject jsonObject, OutputStream outputStream) {
        JsonWriterFactory writerFactory = Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true));
        try (JsonWriter jsonWriter = writerFactory.createWriter(outputStream)) {
            jsonWriter.writeObject(jsonObject);
            outputStream.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static synchronized String format(JsonObject jsonObject) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            prettyPrint(jsonObject, outputStream);
            return outputStream.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
