package org.kepr.hostapi.service

import org.kepr.hostapi.config.*
import org.kepr.hostapi.data.Alias
import org.kepr.hostapi.data.Host
import org.kepr.hostapi.model.AliasModel
import org.kepr.hostapi.repository.AliasRepository
import org.kepr.hostapi.repository.HostRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AliasServiceImpl(@Autowired private val aliasRepository: AliasRepository, @Autowired private val hostRepository: HostRepository) : AliasService {
    override fun findAll(): List<Alias> {
        return aliasRepository.findAll()
    }

    override fun findById(id: Long): Alias {
        return aliasRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, NO_ALIAS_FOUND_WITH_ID.plus(id)) }
    }

    override fun findByName(name: String): Alias {
        return aliasRepository.findAliasByName(name).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, NO_ALIAS_FOUND_WITH_NAME.plus(name)) }
    }

    override fun save(aliasModel: AliasModel): Alias {
        val dbHosts = hostRepository.findHostsByNameIn(aliasModel.hosts)
        validateForSave(aliasModel, dbHosts)
        return aliasRepository.save(Alias(aliasModel.name, dbHosts))
    }

    override fun update(aliasModel: AliasModel, id: Long): Alias {
        val foundAlias = aliasRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, NO_ALIAS_FOUND_WITH_ID.plus(id)) }
        val dbHosts = hostRepository.findHostsByNameIn(aliasModel.hosts)
        validateForUpdate(aliasModel, foundAlias, dbHosts)
        if (aliasModel.name.isNotBlank())
            foundAlias.name = aliasModel.name
        if (aliasModel.hosts.isNotEmpty())
            foundAlias.hosts = dbHosts

        for (host in dbHosts)
            host.alias=foundAlias

        return aliasRepository.save(foundAlias)
    }

    override fun delete(id: Long) {
        val foundAliasOpt = aliasRepository.findAliasById(id)
        val foundAlias = foundAliasOpt.orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, NO_ALIAS_FOUND_WITH_ID.plus(id)) }
        val hosts = mutableSetOf<Host>()
        for (host in foundAlias.hosts)
            hosts.add(host)
        dismissHosts(foundAlias)
        aliasRepository.delete(foundAlias)
        hostRepository.deleteAll(hosts)
    }

    override fun findByQueryParams(allParams: MutableMap<String, String>): Any {
        return if (allParams.isEmpty())
            AliasModel.toModel(findAll())
        else {
            checkForNotAllowedKeysInQuery(allParams)
            if (allParams.containsKey("name")) AliasModel.toModel(findByName(allParams["name"]
                    ?: ""))
            else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "could not parse query params, please check the docs")
        }
    }

    private fun checkForNotAllowedKeysInQuery(allParams: Map<String, String>) {
        val allowedKeys = setOf("name")
        allParams.keys.forEach {
            if (!allowedKeys.contains(it))
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, NON_SUPPORTED_QUERY_PARAM.plus(it))
        }
    }

    private fun dismissParent(host: Host) {
        if (host.alias != null)
            dismissHost(host.alias!!, host)
        host.alias = null
    }

    private fun dismissHost(alias: Alias, host: Host) {
        alias.hosts.remove(host)
    }

    private fun dismissHosts(alias: Alias) {
        alias.hosts.forEach { dismissParent(it) }
        alias.hosts.clear()
    }

    private fun validateForSave(aliasModel: AliasModel, dbHosts: MutableSet<Host>) {
        if (aliasRepository.existsByName(aliasModel.name))
            throw ResponseStatusException(HttpStatus.CONFLICT, ALIAS_NAME_ALREADY_EXISTS.plus(aliasModel.name))
        if (dbHosts.size != aliasModel.hosts.size) {
            val diffList = aliasModel.hosts.minus(dbHosts.map { it.name })
            throw ResponseStatusException(HttpStatus.NOT_FOUND, THESE_HOSTS_WERE_NOT_FOUND.plus(diffList))
        }

    }

    private fun validateForUpdate(aliasModel: AliasModel, foundAlias: Alias, dbHosts: MutableSet<Host>) {
        if (aliasModel.name != foundAlias.name && aliasRepository.existsByName(aliasModel.name))
            throw ResponseStatusException(HttpStatus.CONFLICT, ALIAS_NAME_ALREADY_EXISTS.plus(aliasModel.name))
        if (allRequestedHostsAreNotInDb(dbHosts, aliasModel.hosts)) {
            val diffList = aliasModel.hosts.minus(dbHosts.map { it.name })
            throw ResponseStatusException(HttpStatus.NOT_FOUND, THESE_HOSTS_WERE_NOT_FOUND.plus(diffList))
        }

    }

    private fun allRequestedHostsAreNotInDb(dbHosts: MutableSet<Host>, requestedHost: List<String>): Boolean = dbHosts.size != requestedHost.size
}