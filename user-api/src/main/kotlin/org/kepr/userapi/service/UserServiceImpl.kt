package org.kepr.userapi.service

import org.kepr.userapi.config.*
import org.kepr.userapi.data.User
import org.kepr.userapi.model.UserModel.Companion.toModel
import org.kepr.userapi.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class UserServiceImpl(@Autowired private val userRepository: UserRepository) : UserService {

    override fun findAll(): List<User> {
       return userRepository.findAll();
    }

    override fun findById(id: Long): User {
        return userRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, NO_USER_FOUND_WITH_ID.plus(id) ) }
    }

    override fun findByUserName(userName: String): User {
        return userRepository.findUserByUserName(userName).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, NO_USER_FOUND_WITH_USERNAME.plus(userName) ) }
    }
    override fun findByParams(queryParams : MutableMap<String, String>) : Any{
        return if (queryParams.isEmpty())
            toModel(findAll())
        else {
            checkForNotAllowedKeysInQuery(queryParams)
            fixQueryParams(queryParams)
                if (queryParams.containsKey("username") || queryParams.containsKey("email"))
                    toModel(userRepository.findUserByUserNameOrEmail(queryParams["username"]?:"", queryParams["email"]?:""))

                else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "could not parse query params, please check the docs")
            }
        }
    private fun checkForNotAllowedKeysInQuery(queryParams: MutableMap<String, String>) {
        val allowedKeys = setOf("email", "username, userName")
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

    override fun save(user: User): User {
        val optFoundUser = userRepository.findUserByUserNameOrEmail(user.userName, user.email)
        validateUserForSave(user, optFoundUser)
        validatePassword(user)
        return userRepository.save(user)
    }

    private fun validatePassword(user: User) {
        TODO("Not yet implemented")
    }

    private fun validateUserForSave(user: User, optFoundUser: Optional<User>) {
        if (optFoundUser.isPresent) {
            val foundUser = optFoundUser.get()
            var errorMessage = ""
            if (foundUser.userName == user.userName)
                errorMessage = errorMessage.plus(USERNAME_ALREADY_EXISTS.plus(foundUser.userName))
            if (foundUser.email == user.email)
                errorMessage = errorMessage.plus(USER_EMAIL_ALREADY_EXISTS.plus(foundUser.email))
            throw ResponseStatusException(HttpStatus.CONFLICT, errorMessage.trim())
        }
    }
    override fun update(user: User, id : Long): User {
        val foundUser = findById(id)
        validateUserForUpdate(user, foundUser)
        if(user.userName.isNotBlank())
            foundUser.userName = foundUser.userName
        if(user.email.isNotBlank())
            foundUser.email = foundUser.email
        if(user.password.isNotBlank()) {
            validatePassword(user)
        }

        return userRepository.save(user)
    }

    private fun validateUserForUpdate(user: User, foundUser: User) {
        val errorMessage = ""
        if (user.userName != foundUser.userName && userRepository.existsByUserName(user.userName))
            errorMessage.plus(USERNAME_ALREADY_EXISTS.plus(foundUser.userName))
        if (user.email != foundUser.email && userRepository.existsByEmail(user.email))
            errorMessage.plus(USER_EMAIL_ALREADY_EXISTS).plus(foundUser.email)
        throw ResponseStatusException(HttpStatus.CONFLICT, errorMessage.trim())
    }

    override fun delete(id: Long) {
      val userToDelete = findById(id)
      userRepository.delete(userToDelete)
    }

}