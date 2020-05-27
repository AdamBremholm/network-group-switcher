package org.kepr.userapi.repository

import org.kepr.userapi.data.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository: JpaRepository<User, Long> {
    fun findUserByUserName(userName: String) : Optional<User>
    fun findUserByEmail(email: String) : Optional<User>
}