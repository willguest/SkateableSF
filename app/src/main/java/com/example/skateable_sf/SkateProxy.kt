package com.example.skateable_sf

import android.provider.ContactsContract.Data
import org.ic4j.agent.annotations.Argument
import org.ic4j.agent.annotations.QUERY
import org.ic4j.agent.annotations.UPDATE
import org.ic4j.agent.annotations.Waiter
import org.ic4j.candid.annotations.Modes
import org.ic4j.candid.annotations.Name
import org.ic4j.candid.types.Mode
import org.ic4j.candid.types.Type
import org.ic4j.types.Principal
import java.util.concurrent.CompletableFuture


public interface SkateProxy {
    @QUERY
    @Name("idQuick")
    @Modes(Mode.QUERY)
    @Waiter(timeout = 30)
    fun idQuick(): Principal

    @UPDATE
    @Name("canisterStatus")
    @Waiter(timeout = 10)
    fun checkStatus(@Argument(Type.PRINCIPAL) canisterId: Principal): CompletableFuture<CanisterStatusResponse>

    @UPDATE
    @Name("greet")
    @Waiter(timeout = 10)
    fun greet(@Argument(Type.TEXT) name : String): CompletableFuture<String>

    @UPDATE
    @Name("storeData")
    @Waiter(timeout = 10)
    fun storeData(@Argument(Type.TEXT) entry : String): CompletableFuture<String>

    @QUERY
    @Name("getAllData")
    @Modes(Mode.QUERY)
    @Waiter(timeout = 10)
    fun getAllData(): Array<Note>
}