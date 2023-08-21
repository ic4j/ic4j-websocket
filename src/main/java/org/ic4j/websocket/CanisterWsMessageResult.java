package org.ic4j.websocket;

import java.lang.String;
import javax.lang.model.type.NullType;
import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public enum CanisterWsMessageResult {
  Ok,

  Err;

  @Name("Ok")
  @Field(Type.NULL)
  public NullType okValue;

  @Name("Err")
  @Field(Type.TEXT)
  public String errValue;
}
