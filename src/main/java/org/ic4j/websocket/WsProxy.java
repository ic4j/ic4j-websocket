/*
 * Copyright 2023 Exilor Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.ic4j.websocket;

import java.util.concurrent.CompletableFuture;
import org.ic4j.agent.annotations.Argument;
import org.ic4j.candid.annotations.Modes;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Mode;
import org.ic4j.candid.types.Type;


public interface WsProxy {

  @Name("ws_message")
  @Modes(Mode.ONEWAY)
  CompletableFuture<Void> wsMessage(
      @Argument(Type.RECORD) CanisterWsMessageArguments arg);

  @Name("ws_open")
  @Modes(Mode.ONEWAY)
  CompletableFuture<Void> wsOpen(
      @Argument(Type.RECORD) CanisterWsOpenArguments arg);
  
}
