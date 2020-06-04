package org.kepr.auth.service

import org.kepr.auth.config.*
import org.kepr.auth.data.User
import org.kepr.auth.model.UserModelIn
import org.kepr.auth.model.UserModelOut.Companion.toModel
import org.kepr.auth.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class UserServiceImpl(@Autowired private val userRepository: UserRepository) : UserService, UserDetailsService {

    override fun findAll(): List<User> {
        return userRepository.findAll()
    }

    override fun findById(id: Long): User {
        return userRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, NO_USER_FOUND_WITH_ID.plus(id)) }
    }

    override fun findByUserName(userName: String): User {
        return userRepository.findUserByUserName(userName).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, NO_USER_FOUND_WITH_USERNAME.plus(userName)) }
    }

    override fun findByParams(queryParams: MutableMap<String, String>): Any {
        return if (queryParams.isEmpty())
            toModel(findAll())
        else {
            checkForNotAllowedKeysInQuery(queryParams)
            fixQueryParams(queryParams)
            if (queryParams.containsKey("username") || queryParams.containsKey("email"))
                toModel(userRepository.findUserByUserNameOrEmail(queryParams["username"] ?: "", queryParams["email"]
                        ?: "").orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, NO_USER_FOUND_WITH_USERNAME_OR_EMAIL.plus(queryParams["username"]).plus(queryParams["email"]))})
            else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "could not parse query params, please check the docs")
        }
    }

    private fun checkForNotAllowedKeysInQuery(queryParams: MutableMap<String, String>) {
        val allowedKeys = setOf("email", "username", "userName")
        queryParams.keys.forEach {
            if (!allowedKeys.contains(it))
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, NON_SUPPORTED_QUERY_PARAM.plus(it))
        }
    }

    private fun fixQueryParams(queryParams: MutableMap<String, String>) {
        if (queryParams.containsKey("userName")) {
            queryParams["username"] = queryParams["userName"]!!
            queryParams.remove("userName")
        }
    }

    override fun save(userModelIn: UserModelIn): User {
        val optFoundUser = userRepository.findUserByUserNameOrEmail(userModelIn.userName, userModelIn.email)
        validateUserForSave(userModelIn, optFoundUser)
        return userRepository.save(User(userModelIn.userName, BCryptPasswordEncoder().encode(userModelIn.password), userModelIn.email, mutableSetOf(USER_ROLE)))
    }


    private fun validateUserForSave(userModelIn: UserModelIn, optFoundUser: Optional<User>) {
        if (optFoundUser.isPresent) {
            val foundUser = optFoundUser.get()
            var errorMessage = ""
            if (foundUser.userName == userModelIn.userName)
                errorMessage = errorMessage.plus(USERNAME_ALREADY_EXISTS.plus(foundUser.userName))
            if (foundUser.email == userModelIn.email)
                errorMessage = errorMessage.plus(USER_EMAIL_ALREADY_EXISTS.plus(foundUser.email))
            throw ResponseStatusException(HttpStatus.CONFLICT, errorMessage.trim())
        }
        validatePassword(userModelIn)
    }

    private fun validatePassword(user: UserModelIn) {
        if (user.password != user.passwordConfirm) throw ResponseStatusException(HttpStatus.BAD_REQUEST, PASSWORDS_NOT_MATCHING)
    }

    override fun update(userModelIn: UserModelIn, id: Long): User {
        val foundUser = findById(id)
        validateUserForUpdate(userModelIn, foundUser)
        if (userModelIn.userName.isNotBlank())
            foundUser.userName = userModelIn.userName
        if (userModelIn.email.isNotBlank())
            foundUser.email = userModelIn.email
        if (userModelIn.password.isNotBlank()) {
            validatePassword(userModelIn)
        }

        return userRepository.save(foundUser)
    }

    private fun validateUserForUpdate(user: UserModelIn, foundUser: User) {
        var errorMessage = ""
        if (user.userName != foundUser.userName && userRepository.existsByUserName(user.userName))
            errorMessage = errorMessage.plus(USERNAME_ALREADY_EXISTS.plus(foundUser.userName))
        if (user.email != foundUser.email && userRepository.existsByEmail(user.email))
            errorMessage = errorMessage.plus(USER_EMAIL_ALREADY_EXISTS).plus(foundUser.email)
        if(errorMessage.isNotBlank())
            throw ResponseStatusException(HttpStatus.CONFLICT, errorMessage.trim())
        else
            validatePassword(user)

    }

    override fun delete(id: Long) {
        val userToDelete = findById(id)
        userRepository.delete(userToDelete)
    }

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String?): UserDetails {
       val foundUser = RestTemplate().getForObject("http://user-api-service/users/loadforauth/".plus(username), User::class.java)
        val authorities = ArrayList<GrantedAuthority>()
        foundUser?.roles?.forEach { authorities.add(SimpleGrantedAuthority(it)) }

        return org.springframework.security.core.userdetails.User(foundUser?.userName, foundUser?.password, authorities)
    }

}