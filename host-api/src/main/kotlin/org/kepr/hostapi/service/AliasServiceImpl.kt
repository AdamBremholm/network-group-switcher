package org.kepr.hostapi.service

import org.kepr.hostapi.data.Alias
import org.kepr.hostapi.data.Host
import org.kepr.hostapi.exception.*
import org.kepr.hostapi.model.AliasModel
import org.kepr.hostapi.repository.AliasRepository
import org.kepr.hostapi.repository.HostRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AliasServiceImpl(@Autowired private val aliasRepository: AliasRepository, @Autowired private val hostRepository: HostRepository) : AliasService {
    override fun findAll(): List<Alias> {
        return aliasRepository.findAll()
    }

    override fun findById(id: Long): Alias {
        return aliasRepository.findById(id).orElseThrow { NotFoundException(NO_ALIAS_FOUND_WITH_ID.plus(id)) }
    }

    override fun findByName(name: String): Alias {
        return aliasRepository.findAliasByName(name).orElseThrow { NotFoundException(NO_ALIAS_FOUND_WITH_NAME.plus(name)) }
    }

    override fun save(aliasModel: AliasModel): Alias {
        val dbHosts = hostRepository.findHostsByNameIn(aliasModel.hosts)
        validateForSave(aliasModel, dbHosts)
        return aliasRepository.save(Alias(aliasModel.name, dbHosts))
    }

    override fun update(aliasModel: AliasModel, id: Long): Alias {
        TODO("Not yet implemented")
    }

    override fun delete(id: Long) {
        TODO("Not yet implemented")
    }

    private fun validateForSave(aliasModel: AliasModel, dbHosts: List<Host>) {
        if(aliasModel.name.isBlank())
            throw BadRequestException(EMPTY_NAME_NOT_ALLOWED)
        if (aliasRepository.existsByName(aliasModel.name))
            throw ConflictException(ALIAS_NAME_ALREADY_EXISTS.plus(aliasModel.name))
        if (dbHosts.size != aliasModel.hosts.size) {
            val diffList = aliasModel.hosts.minus(dbHosts.map { it.name })
            throw NotFoundException(THESE_HOSTS_WERE_NOT_FOUND.plus(diffList))
        }

    }

}