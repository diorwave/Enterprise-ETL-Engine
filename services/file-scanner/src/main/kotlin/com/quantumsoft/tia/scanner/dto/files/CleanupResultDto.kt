package com.quantumsoft.tia.scanner.dto.files

data class CleanupResultDto(
    val deletedCount: Int,
    val beforeCount: Long,
    val afterCount: Long,
    val message: String
)