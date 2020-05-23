package org.kepr.hostapi.repository

import org.kepr.hostapi.data.Alias
import org.kepr.hostapi.data.Host
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AliasRepository : JpaRepository<Alias, Long> {

    @Query(value = "SELECT a FROM alias a LEFT JOIN FETCH a.hosts WHERE a.name = :name")
    fun findAliasByName(@Param("name") name : String) : Optional<Alias>


    fun existsByName(name: String) : Boolean
}