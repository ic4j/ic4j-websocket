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

import java.io.IOException;

import org.ic4j.types.Principal;
import org.ic4j.types.PrincipalError;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

public class GatewayHandshakeMessage {
	@JsonProperty("gateway_principal")
	public Principal gatewayPrincipal;

	@JsonSetter("gateway_principal")
	void setGatewayPrincipal(JsonNode gatewayPrincipalNode) {
		if (gatewayPrincipalNode != null && gatewayPrincipalNode.isBinary())
			try {
				this.gatewayPrincipal = Principal.from(gatewayPrincipalNode.binaryValue());
			} catch (PrincipalError | IOException e) {
				throw new WebsocketError(e);
			}
	}

}
