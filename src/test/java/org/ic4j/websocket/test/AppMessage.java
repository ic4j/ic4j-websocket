package org.ic4j.websocket.test;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class AppMessage {
	  @Name("message")
	  @Field(Type.TEXT)
	  public String message;
	  
}
