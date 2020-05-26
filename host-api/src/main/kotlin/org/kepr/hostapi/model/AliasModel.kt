package org.kepr.hostapi.model

import org.kepr.hostapi.data.Alias
import java.lang.IllegalStateException

data class AliasModel(val id: Long?, val name: String, val hosts: List<String>) {

    companion object {
        fun toModel(alias: Alias): AliasModel {
            return AliasModel(alias.id ?: throw IllegalStateException(), alias.name, alias.hosts.map { it.name })
        }

        fun toModel(aliasList: List<Alias>): List<AliasModel> {
            return aliasList.map { toModel(it) }
        }
    }



}