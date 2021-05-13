package com.quorum.tessera.config.apps;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;

public interface TesseraApp {

  CommunicationType getCommunicationType();

  AppType getAppType();
}
