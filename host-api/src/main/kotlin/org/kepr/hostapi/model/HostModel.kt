package org.kepr.hostapi.model

import org.kepr.hostapi.data.Host
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class HostModel(val id: Long?, @NotBlank val address: String, @NotBlank val name: String, val alias: String) {

    companion object {
        fun toModel(host: Host): HostModel {
            return HostModel(host.id?: throw IllegalStateException(), host.address, host.name,
                    host.alias?.name?: throw IllegalStateException("alias is null"))
        }

        fun toModel(hostList: List<Host>): List<HostModel> {
           return hostList.map { it -> toModel(it) }
        }


    }

}
