package org.kepr.hostapi.service

import org.apache.commons.validator.routines.InetAddressValidator
import org.kepr.hostapi.config.*
import org.kepr.hostapi.data.Alias
import org.kepr.hostapi.data.Host
import org.kepr.hostapi.model.HostModel
import org.kepr.hostapi.repository.AliasRepository
import org.kepr.hostapi.repository.HostRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException


@Service
class HostServiceImpl(@Autowired private val hostRepository: HostRepository, @Autowired private val aliasRepository: AliasRepository) : HostService {

    override fun findAll(): List<Host> = hostRepository.findAll()
    override fun findById(id: Long): Host = hostRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, NO_HOST_FOUND_WITH_ID.plus(id)) }

    override fun findByName(name: String): Host = hostRepository.findHostByName(name)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, NO_HOST_FOUND_WITH_NAME.plus(name)) }

    override fun findByAddress(address: String): Host = hostRepository.findHostByAddress(address)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, NO_HOST_FOUND_WITH_ADDRESS.plus(address)) }

    override fun findByNameAndAddress(name: String, address: String): Host {
        return hostRepository
                .findHostByNameAndAddress(name, address)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, NO_HOST_FOUND_WITH_NAME_AND_ADDRESS.plus(name).plus(", ").plus(address)) }
    }

    override fun save(hostModel: HostModel): Host {
        validateAddress(hostModel)
        val foundAlias = aliasRepository.findAliasByName(hostModel.alias)
        val foundHost = hostRepository.findHostByNameOrAddress(hostModel.name, hostModel.address)
        validateForSave(hostModel, foundHost.orElse(null), foundAlias.orElse(null))
        val hostToSave = Host(hostModel.address, hostModel.name, foundAlias.get())
        foundAlias.get().hosts.add(hostToSave)
        aliasRepository.save(foundAlias.get())
        return hostRepository.findHostByName(hostModel.name).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, HOST_NOT_FOUND_AFTER_SAVE) }
    }

    private fun validateAddress(hostModel: HostModel) {
        if (!InetAddressValidator.getInstance().isValidInet4Address(hostModel.address))
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, NOT_VALID_IPV4_ADDRESS.plus(hostModel.address))
    }

    override fun update(hostModel: HostModel, id: Long): Host {
        val foundAlias = aliasRepository.findAliasByName(hostModel.alias)
        val foundHostOptional = hostRepository.findById(id)
        validateForUpdate(hostModel, foundHostOptional.orElse(null), foundAlias.orElse(null))
        validateAddress(hostModel)
        val hostToUpdate = foundHostOptional.get()
        if (hostModel.address.isNotBlank())
            hostToUpdate.address = hostModel.address
        if (hostModel.name.isNotBlank())
            hostToUpdate.name = hostModel.name
        if (foundAlias.isPresent)
            hostToUpdate.alias = foundAlias.get()

        return hostRepository.save(hostToUpdate)
    }

    override fun delete(id: Long) {
        val foundHostOpt = hostRepository.findById(id)
        val foundHost = foundHostOpt.orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, NO_HOST_FOUND_WITH_ID.plus(id)) }
        hostRepository.delete(foundHost)
    }

    private fun validateForSave(hostModel: HostModel, foundHost: Host?, foundAlias: Alias?) {
        if (foundHost != null) {
            var errorMessage = ""
            if (foundHost.address == hostModel.address)
                errorMessage = errorMessage.plus(HOST_ADDRESS_ALREADY_EXISTS.plus(foundHost.address))
            if (foundHost.name == hostModel.name)
                errorMessage = errorMessage.plus(HOST_NAME_ALREADY_EXISTS.plus(foundHost.name))
            throw ResponseStatusException(HttpStatus.CONFLICT, errorMessage.trim())
        }
        if (foundAlias == null) throw ResponseStatusException(HttpStatus.NOT_FOUND, NO_ALIAS_FOUND_WITH_NAME.plus(hostModel.alias))

    }


    private fun validateForUpdate(hostModel: HostModel, foundHost: Host?, foundAlias: Alias?) {
        var notFoundErrorMessage = ""
        if (foundHost == null) notFoundErrorMessage = notFoundErrorMessage.plus(NO_HOST_FOUND_WITH_ID.plus(hostModel.id))
        if (foundAlias == null) notFoundErrorMessage = notFoundErrorMessage.plus(NO_ALIAS_FOUND_WITH_NAME.plus(hostModel.alias))
        if (notFoundErrorMessage.isNotEmpty()) throw ResponseStatusException(HttpStatus.NOT_FOUND, notFoundErrorMessage.trim())
        var conflictErrorMessage = ""
        if (hostModel.name != foundHost?.name)
            if (hostRepository.existsByName(hostModel.name)) conflictErrorMessage = conflictErrorMessage.plus(HOST_NAME_ALREADY_EXISTS.plus(hostModel.name))
        if (hostModel.address != foundHost?.address)
            if (hostRepository.existsByAddress(hostModel.address)) conflictErrorMessage = conflictErrorMessage.plus(HOST_ADDRESS_ALREADY_EXISTS.plus(hostModel.address))
        if (conflictErrorMessage.isNotEmpty()) throw ResponseStatusException(HttpStatus.CONFLICT, conflictErrorMessage.trim())
    }

}