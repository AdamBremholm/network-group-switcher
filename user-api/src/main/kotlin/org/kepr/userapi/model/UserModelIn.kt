package org.kepr.userapi.model

import org.kepr.userapi.config.EMAIL_INCORRECTLY_FORMATTED
import org.kepr.userapi.config.EMPTY_USERNAME_NOT_ALLOWED
import org.kepr.userapi.config.PASSWORD_LENGTH_WARNING
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

data class UserModelIn(@Column(unique=true) @NotBlank(message = EMPTY_USERNAME_NOT_ALLOWED) var userName: String,
                @NotBlank @Min(4, message = PASSWORD_LENGTH_WARNING) val password: String,
                @Min(4, message = PASSWORD_LENGTH_WARNING) @Transient val passwordConfirm : String,
                @Column(unique=true) @Email(message = EMAIL_INCORRECTLY_FORMATTED) var email: String)
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

}
