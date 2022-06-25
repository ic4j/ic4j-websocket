package org.ic4j.management;

import java.math.BigInteger;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;
import org.ic4j.types.Principal;

public class ProvisionalTopUpCanisterRequest {
    @Name("canister_id")
    @Field(Type.PRINCIPAL)
    public Principal canisterId;	
    
    @Name("amount")
    @Field(Type.NAT)
    public BigInteger amount;	
}
