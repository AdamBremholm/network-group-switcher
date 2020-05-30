package org.kepr.userapi.data

import org.kepr.userapi.config.EMAIL_INCORRECTLY_FORMATTED
import org.kepr.userapi.config.EMPTY_USERNAME_NOT_ALLOWED
import org.kepr.userapi.config.PASSWORD_LENGTH_WARNING
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank


@Entity(name="user")
data class User(@Column(unique=true) @NotBlank(message = EMPTY_USERNAME_NOT_ALLOWED) val userName: String,
                @NotBlank @Min(4, message = PASSWORD_LENGTH_WARNING) val password: String,
                @Min(4, message = PASSWORD_LENGTH_WARNING) @Transient val passwordConfirm : String,
                @Column(unique=true) @Email(message = EMAIL_INCORRECTLY_FORMATTED) val email: String)
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

}
