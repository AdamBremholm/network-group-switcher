package org.kepr.userapi.model

import org.kepr.userapi.config.EMAIL_INCORRECTLY_FORMATTED
import org.kepr.userapi.config.EMPTY_USERNAME_NOT_ALLOWED
import org.kepr.userapi.config.PASSWORD_LENGTH_WARNING
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class UserModelIn(@Column(unique=true) @field:NotBlank(message = EMPTY_USERNAME_NOT_ALLOWED) var userName: String,
                       @field:Size(min = 4, message = PASSWORD_LENGTH_WARNING) val password: String,
                       @field:Size(min = 4, message = PASSWORD_LENGTH_WARNING) @Transient val passwordConfirm : String,
                       @Column(unique=true) @field:Email(message = EMAIL_INCORRECTLY_FORMATTED) var email: String)
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

}
