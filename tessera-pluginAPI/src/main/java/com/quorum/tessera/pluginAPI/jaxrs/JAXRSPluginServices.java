package com.quorum.tessera.pluginAPI.jaxrs;

import com.quorum.tessera.pluginAPI.TesseraPluginService;

public interface JAXRSPluginServices extends TesseraPluginService {

  // Registers a endpoint handler with Tessera
  void registerEndpoint ();
}
