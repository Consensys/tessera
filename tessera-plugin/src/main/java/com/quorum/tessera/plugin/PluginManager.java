package com.quorum.tessera.plugin;

import com.quorum.tessera.service.Service;
import org.pf4j.DefaultPluginManager;

public class PluginManager implements Service {

  @Override public void start() {
    final DefaultPluginManager pluginManager = new DefaultPluginManager();
    pluginManager.loadPlugins();
    pluginManager.startPlugins();
  }

  @Override public void stop() {}

  @Override public Status status() {
    return Status.STARTED;
  }
}
