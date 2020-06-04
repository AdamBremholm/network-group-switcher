package org.kepr.userapi.controller

import io.swagger.annotations.Api
import org.kepr.userapi.data.User
import org.kepr.userapi.model.UserModelIn
import org.kepr.userapi.model.UserModelOut
import org.kepr.userapi.model.UserModelOut.Companion.toModel
import org.kepr.userapi.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/")
@Api(value = "user", description = "Rest API for users", tags = ["User API"])
@Validated
class UserController(@Autowired private val userService: UserService) {

    @GetMapping("users")
    fun findWithQueryParams(@RequestParam allParams: MutableMap<String, String>): Any {
        return userService.findByParams(allParams)
    }

    @GetMapping("users/loadforauth/{userName}")
    fun findWithQueryParams(@PathVariable userName : String): User {
        return userService.findByUserName(userName)
    }

    @GetMapping("users/{id}")
    fun findById(@PathVariable id : Long) : UserModelOut = toModel(userService.findById(id))

    @PostMapping("users")
    fun save(@Valid @RequestBody userModelIn : UserModelIn) : UserModelOut = toModel(userService.save(userModelIn))

    @PutMapping("users/{id}")
    fun update(@PathVariable id: Long, @RequestBody userModelIn : UserModelIn) : UserModelOut = toModel(userService.update(userModelIn, id))

    @DeleteMapping("users/{id}")
    fun delete(@PathVariable id : Long) = userService.delete(id)
}

