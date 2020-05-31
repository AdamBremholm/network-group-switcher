package org.kepr.userapi.model

import org.kepr.userapi.data.User
import javax.validation.constraints.NotBlank
import kotlin.IllegalStateException

data class UserModelOut(val id: Long?, @NotBlank val userName: String, @NotBlank val email: String) {

    companion object {
        fun toModel(user: User): UserModelOut {
            return UserModelOut(user.id ?: throw IllegalStateException(), user.userName, user.email)
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
                else -> throw IllegalStateException("any is not user or List")
            }
        }
    }
}