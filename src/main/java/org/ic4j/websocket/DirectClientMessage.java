package org.ic4j.websocket;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class DirectClientMessage {
  @Name("client_key")
  @Field(Type.NAT8)
  public byte[] clientKey;

  @Name("message")
  @Field(Type.NAT8)
  public byte[] message;
}
