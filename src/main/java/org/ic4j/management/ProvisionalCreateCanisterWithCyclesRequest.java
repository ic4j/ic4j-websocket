package org.ic4j.management;

import java.math.BigInteger;
import java.util.Optional;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;
import org.ic4j.types.Principal;

public class ProvisionalCreateCanisterWithCyclesRequest {
    @Name("amount")
    @Field(Type.NAT)
    public Optional<BigInteger> amount;	
    
    @Name("settings")
    @Field(Type.RECORD)
    public Optional<CanisterSettings> settings;	   
    
    @Name("specified_id")
    @Field(Type.PRINCIPAL)
    public Optional<Principal> specifiedId;	    
}
