package org.openeel.demolaunchableapp.ext

import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import kotlin.uuid.Uuid

fun DataLoadState<List<Uuid>>.prettyResultString(): String {
    return when(this) {
        is DataReadyState -> {
           "Statement sent successfully: ${data.joinToString()}"
        }

        is DataErrorResult -> {
            "ERROR: ${error.message}"
        }

        else -> {
            "Something went wrong: $this"
        }
    }
}