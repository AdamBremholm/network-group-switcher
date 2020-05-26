package org.kepr.userapi.service

import org.kepr.userapi.data.User
import org.kepr.userapi.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserServiceImpl(@Autowired private val userRepository: UserRepository) : UserService {

    override fun findAll(): List<User> {
       return userRepository.findAll();
    }

    override fun findById(id: Long): User {
        return userRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, ) }
    }

    override fun findByName(name: String): User {
        TODO("Not yet implemented")
    }

    override fun save(user: User): User {
        TODO("Not yet implemented")
    }

    override fun update(user: User): User {
        TODO("Not yet implemented")
    }

    override fun delete(id: Long) {
        TODO("Not yet implemented")
    }

}