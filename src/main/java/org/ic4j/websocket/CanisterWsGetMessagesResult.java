package org.ic4j.websocket;

import java.lang.String;
import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public enum CanisterWsGetMessagesResult {
  Ok,

  Err;

  @Name("Ok")
  @Field(Type.RECORD)
  public CanisterOutputCertifiedMessages okValue;

  @Name("Err")
  @Field(Type.TEXT)
  public String errValue;
}
