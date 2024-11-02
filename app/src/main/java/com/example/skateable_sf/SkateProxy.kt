package com.example.skateable_sf

import org.ic4j.agent.annotations.Waiter
import org.ic4j.candid.annotations.Modes
import org.ic4j.candid.annotations.Name
import org.ic4j.candid.types.Mode
import org.ic4j.types.Principal
import java.math.BigInteger
import java.util.concurrent.CompletableFuture


public interface  SkateProxy {
    @Name("cycles")
    @Modes(Mode.QUERY)
    fun cycles(): BigInteger?

    @Name("idQuick")
    @Modes(Mode.QUERY)
    @Waiter(timeout = 30)
    fun idQuick(): CompletableFuture<Principal>?

    @Name("get_halt")
    @Modes(Mode.QUERY)
    fun get_halt(): Boolean?
}