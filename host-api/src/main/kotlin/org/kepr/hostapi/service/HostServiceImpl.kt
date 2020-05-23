package org.kepr.hostapi.service

import org.apache.commons.validator.routines.InetAddressValidator
import org.kepr.hostapi.data.Alias
import org.kepr.hostapi.data.Host
import org.kepr.hostapi.exception.*
import org.kepr.hostapi.model.HostModel
import org.kepr.hostapi.repository.AliasRepository
import org.kepr.hostapi.repository.HostRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class HostServiceImpl(@Autowired private val hostRepository: HostRepository, @Autowired private val aliasRepository: AliasRepository) : HostService {

    override fun findAll(): List<Host> = hostRepository.findAll()
    override fun findById(id: Long): Host = hostRepository.findById(id)
            .orElseThrow { NotFoundException(NO_HOST_FOUND_WITH_ID.plus(id)) }

    override fun findByName(name: String): Host = hostRepository.findHostByName(name)
            .orElseThrow { NotFoundException(NO_HOST_FOUND_WITH_NAME.plus(name)) }

    override fun findByAddress(address: String): Host = hostRepository.findHostByAddress(address)
            .orElseThrow { NotFoundException(NO_HOST_FOUND_WITH_ADDRESS.plus(address)) }

    override fun findByNameAndAddress(name: String, address: String): Host {
        return hostRepository
                .findHostByNameAndAddress(name, address)
                .orElseThrow { NotFoundException(NO_HOST_FOUND_WITH_NAME_AND_ADDRESS.plus(name).plus(", ").plus(address)) }
    }

    override fun save(hostModel: HostModel): Host {
        checkForBlanks(hostModel)
        validateAddress(hostModel)
        val foundAlias = aliasRepository.findAliasByName(hostModel.alias)
        val foundHost = hostRepository.findHostByNameOrAddress(hostModel.name, hostModel.address)
        validateForSave(hostModel, foundHost.orElse(null), foundAlias.orElse(null))
        val hostToSave = Host(hostModel.address, hostModel.name, foundAlias.get())
        foundAlias.get().hosts.add(hostToSave)
        aliasRepository.save(foundAlias.get())
        return hostRepository.findHostByName(hostModel.name).orElseThrow { NotFoundException(HOST_NOT_FOUND_AFTER_SAVE) }
    }

    private fun validateAddress(hostModel: HostModel) {
        if (!InetAddressValidator.getInstance().isValidInet4Address(hostModel.address))
            throw BadRequestException(NOT_VALID_IPV4_ADDRESS.plus(hostModel.address))
    }

    override fun update(hostModel: HostModel, id: Long): Host {
        val foundAlias = aliasRepository.findAliasByName(hostModel.alias)
        val foundHost = hostRepository.findById(id)
        validateForUpdate(hostModel, foundHost.orElse(null), foundAlias.orElse(null))
        return hostRepository.findById(id).map {
                    hostRepository
                            .save(it.copy(address = hostModel.address, name = hostModel.name, alias = foundAlias.get()))
                }
                .orElseThrow { NotFoundException(HOST_NOT_FOUND_AFTER_UPDATE) }
    }

    override fun delete(id: Long) = hostRepository.delete(findById(id))

    private fun validateForSave(hostModel: HostModel, foundHost: Host?, foundAlias: Alias?) {
        if (foundHost != null) {
            var errorMessage = ""
            if (foundHost.address == hostModel.address)
                errorMessage = errorMessage.plus(HOST_ADDRESS_ALREADY_EXISTS.plus(foundHost.address))
            if (foundHost.name == hostModel.name)
                errorMessage = errorMessage.plus(HOST_NAME_ALREADY_EXISTS.plus(foundHost.name))
            throw ConflictException(errorMessage.trim())
        }
        if (foundAlias == null) throw NotFoundException(NO_ALIAS_FOUND_WITH_NAME.plus(hostModel.alias))

    }

    private fun checkForBlanks(hostModel: HostModel) {
        var errorMessage = ""
        if (hostModel.name.isBlank()) errorMessage = EMPTY_NAME_NOT_ALLOWED
        if (hostModel.alias.isBlank()) errorMessage = errorMessage.plus(EMPTY_ALIAS_NOT_ALLOWED)

        if (errorMessage.isNotEmpty())
            throw BadRequestException(errorMessage)
    }

    private fun validateForUpdate(hostModel: HostModel, foundHost: Host?, foundAlias: Alias?) {
        var notFoundErrorMessage = ""
        if (foundHost == null) notFoundErrorMessage = notFoundErrorMessage.plus(NO_HOST_FOUND_WITH_ID.plus(hostModel.id))
        if (foundAlias == null) notFoundErrorMessage = notFoundErrorMessage.plus(NO_ALIAS_FOUND_WITH_NAME.plus(hostModel.alias))
        if (notFoundErrorMessage.isNotEmpty()) throw NotFoundException(notFoundErrorMessage.trim())
        var conflictErrorMessage = ""
        if (hostRepository.existsByName(hostModel.name)) conflictErrorMessage = conflictErrorMessage.plus(HOST_NAME_ALREADY_EXISTS.plus(hostModel.name))
        if (hostRepository.existsByAddress(hostModel.address)) conflictErrorMessage = conflictErrorMessage.plus(HOST_ADDRESS_ALREADY_EXISTS.plus(hostModel.address))
        if (conflictErrorMessage.isNotEmpty()) throw ConflictException(conflictErrorMessage.trim())
    }

}