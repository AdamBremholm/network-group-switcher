package org.kepr.userapi.controller

import io.swagger.annotations.Api
import org.kepr.userapi.config.NON_SUPPORTED_QUERY_PARAM
import org.kepr.userapi.data.User
import org.kepr.userapi.model.UserModel
import org.kepr.userapi.model.UserModel.Companion.toModel
import org.kepr.userapi.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid

@RestController
@RequestMapping("/api/")
@Api(value = "user", description = "Rest API for users", tags = ["User API"])
@Validated
class UserController(@Autowired private val userService: UserService) {

    @GetMapping("users")
    fun findWithQueryParams(@RequestParam allParams: MutableMap<String, String>): Any {
        return findWithQueryParams(allParams)
    }

    @GetMapping("users/{id}")
    fun findById(@PathVariable id : Long) : UserModel = toModel(userService.findById(id))

    @PostMapping("users")
    fun save(@Valid @RequestBody user : User) : UserModel = toModel(userService.save(user))

    @PutMapping("users/{id}")
    fun update(@PathVariable id: Long, @RequestBody user : User) : UserModel = toModel(userService.update(user, id))

    @DeleteMapping("users/{id}")
    fun delete(@PathVariable id : Long) = userService.delete(id)
}

