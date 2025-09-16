package com.quantumsoft.tia.scanner.exceptions

class ThresholdExceededException(
    message: String,
    val currentSize: Int,
    val threshold: Int
) : RuntimeException(message)