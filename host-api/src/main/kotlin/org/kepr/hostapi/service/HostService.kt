package org.kepr.hostapi.service

import org.kepr.hostapi.data.Host
import org.kepr.hostapi.model.HostModel
import org.springframework.stereotype.Service

@Service
interface HostService{

    fun findAll() : List<Host>
    fun findById(id : Long) : Host
    fun findByName(name: String) : Host
    fun findByAddress(address: String) : Host
    fun findByNameAndAddress(name : String, address: String) : Host
    fun save(hostModel : HostModel) : Host
    fun update(hostModel: HostModel, id: Long) : Host
    fun delete(id: Long)
}