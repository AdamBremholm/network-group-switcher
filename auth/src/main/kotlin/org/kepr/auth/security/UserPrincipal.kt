package org.kepr.auth.security

import org.kepr.auth.data.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

class UserPrincipal(private val user: User) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        val authorities: MutableList<GrantedAuthority> = ArrayList()

        // Extract list of roles (ROLE_name)
          val authority: GrantedAuthority = SimpleGrantedAuthority(user.role)
          authorities.add(authority)
          return authorities
        }

    override fun isEnabled(): Boolean = true

    override fun getUsername(): String = user.userName

    override fun isCredentialsNonExpired(): Boolean  = true

    override fun getPassword(): String = user.password

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true


}
