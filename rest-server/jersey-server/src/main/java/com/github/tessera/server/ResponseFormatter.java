package com.github.tessera.server;

import java.util.Map;

public interface ResponseFormatter {
    String createResponse(Map<String, String> metrics);
}
