package org.ic4j.websocket;

import java.lang.Long;
import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class CanisterWsGetMessagesArguments {
  @Name("nonce")
  @Field(Type.NAT64)
  public Long nonce;
}
