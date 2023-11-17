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

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebsocketMessage {
	
  @JsonProperty("sequence_num")
  @Name("sequence_num")
  @Field(Type.NAT64)  
  public Long sequenceNum;

  @JsonProperty("content")
  @Name("content")
  @Field(Type.NAT8)
  public byte[] content;

  @JsonProperty("client_key")
  @Name("client_key")
  @Field(Type.RECORD)
  public ClientKey clientKey;

  @JsonProperty("timestamp")
  @Name("timestamp")
  @Field(Type.NAT64)
  public Long timestamp;

  @JsonProperty("is_service_message")
  @Name("is_service_message")
  @Field(Type.BOOL)
  public Boolean isServiceMessage;
}
