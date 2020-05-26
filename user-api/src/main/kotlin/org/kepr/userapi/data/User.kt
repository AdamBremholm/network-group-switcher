package org.kepr.userapi.data

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Transient
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size


@Entity(name="user")
data class User(@Column(unique=true) @NotBlank val userName: String,
                @NotBlank @Min(4) val password: String,
                @NotBlank @Transient val passwordConfirm : String,
                @Column(unique=true) @NotBlank val email: String)
{

    @Id
    @GeneratedValue
    val id: Long? = null

}
