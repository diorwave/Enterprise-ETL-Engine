package com.quantumsoft.tia.scanner.dto.metrics

data class CleanupResult(
    val cleanedLocks: Int,
    val message: String
)