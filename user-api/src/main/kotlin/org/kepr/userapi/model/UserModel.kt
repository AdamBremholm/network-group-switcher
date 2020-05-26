package org.kepr.userapi.model

import org.kepr.userapi.data.User
import java.lang.IllegalStateException
import javax.validation.constraints.NotBlank

data class UserModel(val id: Long?, @NotBlank val userName: String, @NotBlank val email: String) {

    companion object {
        fun toModel(user: User): UserModel {
            return UserModel(user.id ?: throw IllegalStateException(), user.userName, user.email)
        }
        fun toModel(userList: List<User>): List<UserModel> {
            return userList.map { toModel(it) }
        }
    }
}