package org.kepr.auth.data

import javax.persistence.*


@Entity(name="user")
data class User(@Column(unique=true) var userName: String,
                val password: String,
                @Column(unique=true) var email: String = "",
                @Column @ElementCollection(targetClass = String::class) var roles: MutableSet<String> =  mutableSetOf())
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

}
