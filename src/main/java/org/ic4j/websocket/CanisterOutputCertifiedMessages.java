package org.ic4j.websocket;

import java.lang.Byte;
import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class CanisterOutputCertifiedMessages {
  @Name("messages")
  @Field(Type.RECORD)
  public CanisterOutputMessage[] messages;

  @Name("cert")
  @Field(Type.NAT8)
  public Byte[] cert;

  @Name("tree")
  @Field(Type.NAT8)
  public Byte[] tree;
}
