package com.quorum.tessera.serviceloader;

public interface SampleService {

  static SampleService create() {
    return ServiceLoaderExt.load(SampleService.class).findFirst().get();
  }
}
