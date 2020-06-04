package org.kepr.hostapi.controller

import io.swagger.annotations.Api
import org.kepr.hostapi.model.HostModel
import org.kepr.hostapi.model.HostModel.Companion.toModel
import org.kepr.hostapi.service.HostService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/network/")
@Api(value = "host", description = "Rest API for hosts", tags = ["Host API"])
@Validated
class HostController(@Autowired private val hostService: HostService) {


    @GetMapping("hosts")
    fun findWithQueryParams(@RequestParam allParams: MutableMap<String, String>): Any = hostService.findByQueryParams(allParams)

    @GetMapping("hosts/{id}")
    fun findById(@PathVariable id: Long): HostModel = toModel(hostService.findById(id))

    @PostMapping("hosts")
    fun save(@Valid @RequestBody hostModel: HostModel): HostModel = toModel(hostService.save(hostModel))

    @PutMapping("hosts/{id}")
    fun update(@PathVariable id: Long, @RequestBody hostModel: HostModel): HostModel = toModel(hostService.update(hostModel, id))

    @DeleteMapping("hosts/{id}")
    fun delete(@PathVariable id: Long) = hostService.delete(id)


}