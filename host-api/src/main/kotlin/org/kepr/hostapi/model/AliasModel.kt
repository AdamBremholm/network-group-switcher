package org.kepr.hostapi.model

import org.kepr.hostapi.config.EMPTY_NAME_NOT_ALLOWED
import org.kepr.hostapi.data.Alias
import java.lang.IllegalStateException
import javax.validation.constraints.NotBlank

data class AliasModel(val id: Long? = null, @field:NotBlank(message = EMPTY_NAME_NOT_ALLOWED) val name: String = "", val hosts: List<String> = mutableListOf()) {

    companion object {
        fun toModel(alias: Alias): AliasModel {
            return AliasModel(alias.id ?: throw IllegalStateException(), alias.name, alias.hosts?.map { it.name })
        }

        fun toModel(aliasList: List<Alias>): List<AliasModel> {
            return aliasList.map { toModel(it) }
        }
    }



}