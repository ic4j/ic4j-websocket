package org.ic4j.websocket.test;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class AppMessage {
	  @Name("text")
	  @Field(Type.TEXT)
	  public String text;
	  
	  
	  @Name("timestamp")
	  @Field(Type.NAT64)
	  public Long timestamp;	  
}
