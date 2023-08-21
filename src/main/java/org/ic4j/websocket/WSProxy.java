package org.ic4j.websocket;

import java.util.concurrent.CompletableFuture;
import org.ic4j.agent.annotations.Argument;
import org.ic4j.candid.annotations.Modes;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Mode;
import org.ic4j.candid.types.Type;


public interface WSProxy {
  @Name("ws_register")
  CompletableFuture<CanisterWsRegisterResult> wsRegister(
      @Argument(Type.RECORD) CanisterWsRegisterArguments arg);

  @Name("ws_message")
  CompletableFuture<CanisterWsMessageResult> wsMessage(
      @Argument(Type.RECORD) CanisterWsMessageArguments arg);

  @Name("ws_open")
  CompletableFuture<CanisterWsOpenResult> wsOpen(
      @Argument(Type.RECORD) CanisterWsOpenArguments arg);

  @Name("ws_get_messages")
  @Modes(Mode.QUERY)
  CanisterWsGetMessagesResult wsGetMessages(
      @Argument(Type.RECORD) CanisterWsGetMessagesArguments arg);

  @Name("ws_close")
  CompletableFuture<CanisterWsCloseResult> wsClose(
      @Argument(Type.RECORD) CanisterWsCloseArguments arg);
}
