package org.kepr.hostapi.controller


import io.swagger.annotations.Api;
import org.kepr.hostapi.config.NON_SUPPORTED_QUERY_PARAM
import org.kepr.hostapi.model.AliasModel;
import org.kepr.hostapi.model.AliasModel.Companion.toModel
import org.kepr.hostapi.service.AliasService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid

@RestController
@RequestMapping("/api/")
@Api(value = "alias", description = "Rest API for aliases", tags = ["Alias API"])
@Validated
class AliasController(@Autowired private val aliasService : AliasService) {

    @GetMapping("aliases")
    fun findWithQueryParams(@RequestParam allParams: MutableMap<String, String>): Any = aliasService.findByQueryParams(allParams)

    @GetMapping("aliases/{id}")
    fun findById(@PathVariable id : Long) : AliasModel = toModel(aliasService.findById(id))

    @PostMapping("aliases")
    fun save(@Valid @RequestBody aliasModel : AliasModel) : AliasModel = toModel(aliasService.save(aliasModel))

    @PutMapping("aliases/{id}")
    fun update(@PathVariable id: Long, @RequestBody aliasModel : AliasModel) : AliasModel = toModel(aliasService.update(aliasModel, id))

    @DeleteMapping("aliases/{id}")
    fun delete(@PathVariable id : Long) = aliasService.delete(id)
}
