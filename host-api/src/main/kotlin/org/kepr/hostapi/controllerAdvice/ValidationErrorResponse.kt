package org.kepr.hostapi.controllerAdvice



class ValidationErrorResponse {
    val violations: MutableList<Violation> = mutableListOf()
}

