package org.kepr.auth.repository

import org.kepr.auth.data.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository: JpaRepository<User, Long> {
    @Query(value = "SELECT u FROM user u LEFT JOIN FETCH u.roles WHERE u.userName = :userName")
    fun findUserByUserName(userName: String) : Optional<User>
    fun findUserByEmail(email: String) : Optional<User>
    fun findUserByUserNameOrEmail(username:String, email:String) : Optional<User>
    fun existsByUserName(userName: String): Boolean
    fun existsByEmail(email: String): Boolean
}