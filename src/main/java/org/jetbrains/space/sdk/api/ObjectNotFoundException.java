package org.jetbrains.space.sdk.api;

import java.io.IOException;

public class ObjectNotFoundException extends IOException {

  public ObjectNotFoundException() {
    super("object not found");
  }
}
