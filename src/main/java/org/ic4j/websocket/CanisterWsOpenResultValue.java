package org.ic4j.websocket;

import java.lang.Byte;
import java.lang.Long;
import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;
import org.ic4j.types.Principal;

public class CanisterWsOpenResultValue {
  @Name("client_key")
  @Field(Type.NAT8)
  public Byte[] fieldclient_key;

  @Name("canister_id")
  @Field(Type.PRINCIPAL)
  public Principal fieldcanister_id;

  @Name("nonce")
  @Field(Type.NAT64)
  public Long nonce;
}
