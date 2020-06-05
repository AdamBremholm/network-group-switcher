package org.kepr.hostapi.model

import org.kepr.hostapi.config.EMPTY_NAME_NOT_ALLOWED
import org.kepr.hostapi.data.Alias
import java.lang.IllegalStateException
import javax.validation.constraints.NotBlank

data class AliasModelIn(val id: Long? = null, @field:NotBlank(message = EMPTY_NAME_NOT_ALLOWED) val name: String = "", val hosts: List<String> = mutableListOf()) {



}
