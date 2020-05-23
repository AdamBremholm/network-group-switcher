package org.kepr.hostapi.controller


import io.swagger.annotations.Api;
import org.kepr.hostapi.model.AliasModel;
import org.kepr.hostapi.model.AliasModel.Companion.toModel
import org.kepr.hostapi.service.AliasService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid

@RestController
@RequestMapping("/api/")
@Api(value = "alias", description = "Rest API for aliases", tags = ["Alias API"])
class AliasController(@Autowired private val aliasService : AliasService) {

    @GetMapping("aliases")
    fun findAll(): List<AliasModel> = toModel(aliasService.findAll())

    @GetMapping("aliases/{id}")
    fun findById(@PathVariable id : Long) : AliasModel = toModel(aliasService.findById(id))

    @PostMapping("aliases")
    fun save(@Valid @RequestBody hostModel : AliasModel) : AliasModel = toModel(aliasService.save(hostModel))

    @PutMapping("aliases/{id}")
    fun update(@PathVariable id: Long, hostModel : AliasModel) : AliasModel = toModel(aliasService.update(hostModel, id))

    @DeleteMapping("aliases/{id}")
    fun delete(@PathVariable id : Long) = aliasService.delete(id)
}
