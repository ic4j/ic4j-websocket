package org.ic4j.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientIncomingMessage {
  @JsonProperty("key")
  public String key;	
  
  @JsonProperty("cert")
  public byte[] cert;

  @JsonProperty("tree")
  public byte[] tree;
  
  @JsonProperty("val")
  public byte[] val;  
}
