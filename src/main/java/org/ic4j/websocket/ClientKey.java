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
import java.math.BigInteger;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;
import org.ic4j.types.Principal;
import org.ic4j.types.PrincipalError;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

public class ClientKey {
  @JsonProperty("client_principal")
  @Name("client_principal")
  @Field(Type.PRINCIPAL)
  public Principal clientPrincipal;

  @JsonProperty("client_nonce")
  @Name("client_nonce")
  @Field(Type.NAT64)
  public BigInteger clientNonce;
  
  @JsonSetter("client_principal")
  void setClientPrincipal(JsonNode clientPrincipaldNode) {
		if (clientPrincipaldNode != null && clientPrincipaldNode.isBinary())
			try {
				this.clientPrincipal = Principal.from(clientPrincipaldNode.binaryValue());
			} catch (PrincipalError | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
  }


@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientKey other = (ClientKey) obj;
		if (clientNonce == null) {
			if (other.clientNonce != null)
				return false;
		} else if (!clientNonce.equals(other.clientNonce))
			return false;
		if (clientPrincipal == null) {
			if (other.clientPrincipal != null)
				return false;
		} else if (!clientPrincipal.equals(other.clientPrincipal))
			return false;
		return true;
	}
  
}
