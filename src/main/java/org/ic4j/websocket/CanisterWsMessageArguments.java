package org.ic4j.websocket;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class CanisterWsMessageArguments {
  @Name("msg")
  @Field(Type.VARIANT)
  public CanisterIncomingMessage msg;
}
