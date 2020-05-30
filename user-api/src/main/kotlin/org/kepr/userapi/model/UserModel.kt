package org.kepr.userapi.model

import org.kepr.userapi.data.User
import javax.validation.constraints.NotBlank
import kotlin.IllegalStateException

data class UserModel(val id: Long?, @NotBlank val userName: String, @NotBlank val email: String) {

    companion object {
        fun toModel(user: User): UserModel {
            return UserModel(user.id ?: throw IllegalStateException(), user.userName, user.email)
        }
        fun toModel(userList: List<User>): List<UserModel> {
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