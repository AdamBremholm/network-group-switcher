package org.kepr.auth.service

import org.kepr.auth.data.User
import org.kepr.auth.model.UserModelIn

interface UserService {
    fun findAll() : List<User>
    fun findById(id: Long) : User
    fun findByUserName(userName : String) : User
    fun save (userModelIn: UserModelIn) : User
    fun delete(id: Long)
    fun update(userModelIn: UserModelIn, id: Long): User
    fun findByParams(queryParams: MutableMap<String, String>): Any
}