package org.kepr.userapi.service

import org.kepr.userapi.data.User

interface UserService {
    fun findAll() : List<User>
    fun findById(id: Long) : User
    fun findByUserName(name : String) : User
    fun save (user: User) : User
    fun update (user: User) : User
    fun delete(id: Long)
}