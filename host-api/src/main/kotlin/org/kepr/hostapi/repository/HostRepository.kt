package org.kepr.hostapi.repository

import org.kepr.hostapi.data.Host
import org.kepr.hostapi.service.HostService
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import java.util.*

@Repository
interface HostRepository : JpaRepository<Host, Long> {

    fun findHostByName(name : String) : Optional<Host>

    @Query(value = "SELECT h FROM host h LEFT JOIN FETCH h.alias WHERE h.id = :id")
    fun findHostById(id : Long) : Optional<Host>

    fun findHostByAddress(address: String) : Optional<Host>
    fun findHostsByAliasName(aliasName: String) : MutableList<Host>
    fun findHostsByNameIn(names : List<String>) : MutableList<Host>
    fun findHostByNameOrAddress(name : String, address : String) : Optional<Host>
    fun findHostByNameAndAddress(name : String, address : String) : Optional<Host>
    fun existsByNameOrAddress(name : String, address : String) : Boolean
    fun existsByName(name: String) : Boolean
    fun existsByAddress(address: String) : Boolean
}