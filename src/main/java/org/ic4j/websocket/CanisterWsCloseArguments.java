package org.ic4j.websocket;

import java.lang.Byte;
import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class CanisterWsCloseArguments {
  @Name("client_key")
  @Field(Type.NAT8)
  public Byte[] fieldclient_key;
}
