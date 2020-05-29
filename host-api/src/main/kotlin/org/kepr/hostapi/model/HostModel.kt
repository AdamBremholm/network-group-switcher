package org.kepr.hostapi.model

import org.kepr.hostapi.config.EMPTY_ADDRESS_NOT_ALLOWED
import org.kepr.hostapi.config.EMPTY_ALIAS_NOT_ALLOWED
import org.kepr.hostapi.config.EMPTY_NAME_NOT_ALLOWED
import org.kepr.hostapi.data.Host
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class HostModel(val id: Long? = null, @field:NotBlank(message = EMPTY_ADDRESS_NOT_ALLOWED) val address: String, @field:NotBlank(message = EMPTY_NAME_NOT_ALLOWED) val name: String = "", @field:NotBlank(message = EMPTY_ALIAS_NOT_ALLOWED) val alias: String) {

    companion object {
        fun toModel(host: Host): HostModel {
            return HostModel(host.id?: throw IllegalStateException(), host.address, host.name,
                    host.alias?.name ?: throw IllegalStateException("alias is null"))
        }

        fun toModel(hostList: List<Host>): List<HostModel> {
           return hostList.map { toModel(it) }
        }


    }

}
