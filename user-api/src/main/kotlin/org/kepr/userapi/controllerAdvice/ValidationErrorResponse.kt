package org.kepr.userapi.controllerAdvice


class ValidationErrorResponse {
    val violations: MutableList<Violation> = mutableListOf()
}

