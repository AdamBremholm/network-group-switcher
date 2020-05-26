package org.kepr.hostapi.controller

import io.swagger.annotations.Api
import org.kepr.hostapi.exception.NON_SUPPORTED_QUERY_PARAM
import org.kepr.hostapi.model.HostModel
import org.kepr.hostapi.model.HostModel.Companion.toModel
import org.kepr.hostapi.service.HostService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid

@RestController
@RequestMapping("/api/")
@Api(value = "host", description = "Rest API for hosts", tags = ["Host API"])
class HostController(@Autowired private val hostService: HostService) {


    @GetMapping("hosts")
    fun findWithQueryParams(@RequestParam allParams: Map<String, String>): Any {

        return if (allParams.isEmpty())
            toModel(hostService.findAll())
        else {
            checkForNotAllowedKeysInQuery(allParams)
            if (allParams.containsKey("name") && allParams.containsKey("address"))
                toModel(hostService.findByNameAndAddress(allParams["name"] ?: "", allParams["address"] ?: ""))
            else if (allParams.containsKey("name")) toModel(hostService.findByName(allParams["name"] ?: ""))
            else if (allParams.containsKey("address")) toModel(hostService.findByAddress(allParams["address"] ?: ""))
            else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "could not parse query params, please check the docs")
        }

    }

    private fun checkForNotAllowedKeysInQuery(allParams: Map<String, String>) {
        val allowedKeys = setOf("name", "address")
        allParams.keys.forEach {
            if (!allowedKeys.contains(it))
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, NON_SUPPORTED_QUERY_PARAM.plus(it))
        }
    }


    @GetMapping("hosts/{id}")
    fun findById(@PathVariable id: Long): HostModel = toModel(hostService.findById(id))

    @PostMapping("hosts")
    fun save(@Valid @RequestBody hostModel: HostModel): HostModel = toModel(hostService.save(hostModel))

    @PutMapping("hosts/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody hostModel: HostModel): HostModel = toModel(hostService.update(hostModel, id))

    @DeleteMapping("hosts/{id}")
    fun delete(@PathVariable id: Long) = hostService.delete(id)


}