package org.ic4j.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CanisterFirstMessageContent {
	  @JsonProperty("client_key")
	  public byte[] clientKey;

	  @JsonProperty("canister_id")
	  public String canisterId;	

}
