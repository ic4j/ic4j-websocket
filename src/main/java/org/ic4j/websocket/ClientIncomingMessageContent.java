package org.ic4j.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientIncomingMessageContent {
  @JsonProperty("client_key")
  public byte[] clientKey;
  
  @JsonProperty("sequence_num")
  public Integer sequenceNum;  

  @JsonProperty("timestamp")
  public Long timestamp;  
  
  @JsonProperty("message")
  public byte[] message;  
}
