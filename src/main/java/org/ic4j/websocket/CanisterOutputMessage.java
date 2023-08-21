package org.ic4j.websocket;

import java.lang.Byte;
import java.lang.String;
import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class CanisterOutputMessage {
  @Name("key")
  @Field(Type.TEXT)
  public String key;

  @Name("val")
  @Field(Type.NAT8)
  public Byte[] val;

  @Name("client_key")
  @Field(Type.NAT8)
  public Byte[] fieldclient_key;
}
