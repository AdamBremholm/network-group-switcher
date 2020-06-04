package org.kepr.gateway.data



data class User(var userName: String,
                val password: String,
                var email: String = "",
                 var roles: MutableSet<String> =  mutableSetOf())
{

    val id: Long? = null

}
