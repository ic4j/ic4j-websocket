package org.ic4j.websocket;

import java.lang.Byte;
import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public enum CanisterIncomingMessage {
  IcWebSocketEstablished,

  DirectlyFromClient,

  RelayedByGateway;

  @Name("IcWebSocketEstablished")
  @Field(Type.NAT8)
  public Byte[] icWebSocketEstablishedValue;

  @Name("DirectlyFromClient")
  @Field(Type.RECORD)
  public DirectClientMessage directlyFromClientValue;

  @Name("RelayedByGateway")
  @Field(Type.RECORD)
  public RelayedClientMessage relayedByGatewayValue;
}
