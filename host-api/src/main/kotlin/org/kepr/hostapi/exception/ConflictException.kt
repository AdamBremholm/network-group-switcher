package org.kepr.hostapi.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException(override val message: String? = "undefined") : RuntimeException(){


}