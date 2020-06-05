package org.kepr.hostapi.service

import org.kepr.hostapi.data.Alias
import org.kepr.hostapi.model.AliasModel
import org.kepr.hostapi.model.AliasModelIn


interface AliasService {

    fun findAll() : List<Alias>
    fun findById(id : Long) : Alias
    fun findByName(name : String) : Alias
    fun save(aliasModelIn: AliasModelIn) : Alias
    fun update(aliasModelIn: AliasModelIn, id: Long) : Alias
    fun delete(id: Long)
    fun findByQueryParams(allParams: MutableMap<String, String>): Any
}