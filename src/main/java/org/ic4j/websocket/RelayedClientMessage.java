package org.ic4j.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RelayedClientMessage<T> {
  @JsonProperty("sig")
  public byte[] sig;

  @JsonProperty("content")
  public byte[] content;
}
