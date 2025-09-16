package com.quantumsoft.tia.scanner.dto.files

import jakarta.validation.constraints.Min

data class CleanupRequest(
    @field:Min(1, message = "Days to keep must be at least 1")
    val daysToKeep: Int = 30
)