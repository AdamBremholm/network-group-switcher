package org.kepr.auth.data

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Transient
import javax.validation.constraints.Size


@Entity(name="user")
data class User(@Column(unique=true) @Size(min=1)val userName: String,
                @Size(min=1)val password: String,
                @Transient val passwordConfirm : String,
                @Column(unique=true) val email: String)
 {

    @Id
    @GeneratedValue
    val id: Long? = null

}