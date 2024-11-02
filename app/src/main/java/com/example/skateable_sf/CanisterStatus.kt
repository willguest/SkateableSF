package com.example.skateable_sf

import org.ic4j.types.Principal

data class CanisterStatusResponse(
    val status: String,
    val memorySize: Long,
    val cycles: Long,
    val settings: DefiniteCanisterSettings,
    val moduleHash: String?
)

data class DefiniteCanisterSettings(
    val controllers: List<String>?,
    val freezingThreshold: Long,
    val memoryAllocation: Long,
    val computeAllocation: Long
)

