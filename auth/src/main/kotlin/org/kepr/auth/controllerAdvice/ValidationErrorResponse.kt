package org.kepr.auth.controllerAdvice


class ValidationErrorResponse {
    val violations: MutableList<Violation> = mutableListOf()
}

