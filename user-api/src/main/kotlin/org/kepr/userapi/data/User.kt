package org.kepr.userapi.data

import org.kepr.userapi.config.EMAIL_INCORRECTLY_FORMATTED
import org.kepr.userapi.config.EMPTY_USERNAME_NOT_ALLOWED
import org.kepr.userapi.config.PASSWORD_LENGTH_WARNING
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank


@Entity(name="user")
data class User(@Column(unique=true) var userName: String,
                val password: String,
                @Column(unique=true) var email: String)
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

}
