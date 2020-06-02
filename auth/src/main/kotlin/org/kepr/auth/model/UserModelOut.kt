package org.kepr.auth.model

import org.kepr.auth.data.User
import kotlin.IllegalStateException

data class UserModelOut(val id: Long?, val userName: String, val email: String, val role: String) {

    companion object {
        fun toModel(user: User): UserModelOut {
            return UserModelOut(user.id ?: throw IllegalStateException(), user.userName, user.email, user.role)
        }
        fun toModel(userList: List<User>): List<UserModelOut> {
            return userList.map { toModel(it) }
        }
        fun toModel(any: Any): Any {
            return when (any) {
                is User -> {
                    toModel(any)
                }
                is List<*> -> toModel(any)
                else -> throw IllegalStateException("any is".plus(any))
            }
        }
    }
}