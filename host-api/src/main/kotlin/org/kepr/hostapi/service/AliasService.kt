package org.kepr.hostapi.service

import org.kepr.hostapi.data.Alias
import org.kepr.hostapi.model.AliasModel


interface AliasService {

    fun findAll() : List<Alias>
    fun findById(id : Long) : Alias
    fun findByName(name : String) : Alias
    fun save(aliasModel : AliasModel) : Alias
    fun update(aliasModel: AliasModel, id: Long) : Alias
    fun delete(id: Long)
}