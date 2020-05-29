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
import org.springframework.validation.annotation.Validated
import org.springframework.web.server.ResponseStatusException

@Service
@Validated
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
        val foundAliasOptional = aliasRepository.findById(id)
        if (foundAliasOptional.isEmpty) throw IllegalArgumentException(NO_ALIAS_FOUND_WITH_ID.plus(id))
        foundAliasOptional.ifPresent{alias ->
            run {
                if (aliasModel.name.isNotBlank())
                    alias.name = aliasModel.name
                if (aliasModel.hosts.isNotEmpty())
                    alias.hosts = hostRepository.findHostsByNameIn(aliasModel.hosts)

                aliasRepository.save(alias)
            }
        }
        return foundAliasOptional.get()
    }

    override fun delete(id: Long) {
        val foundAlias = findById(id)
        aliasRepository.delete(foundAlias)
    }

    private fun validateForSave(aliasModel: AliasModel, dbHosts: List<Host>) {
        if (aliasRepository.existsByName(aliasModel.name))
            throw ResponseStatusException(HttpStatus.CONFLICT, ALIAS_NAME_ALREADY_EXISTS.plus(aliasModel.name))
        if (dbHosts.size != aliasModel.hosts.size) {
            val diffList = aliasModel.hosts.minus(dbHosts.map { it.name })
            throw ResponseStatusException(HttpStatus.NOT_FOUND, THESE_HOSTS_WERE_NOT_FOUND.plus(diffList))
        }

    }

}