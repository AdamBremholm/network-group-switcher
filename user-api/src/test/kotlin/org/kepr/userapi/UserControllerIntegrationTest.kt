package org.kepr.userapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kepr.userapi.config.*
import org.kepr.userapi.data.User
import org.kepr.userapi.model.UserModelIn
import org.kepr.userapi.model.UserModelOut
import org.kepr.userapi.repository.UserRepository
import org.kepr.userapi.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerIntegrationTest {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var userModelInGlobal: UserModelIn
    lateinit var user: User

    @BeforeAll
    fun setup() {
        userModelInGlobal = UserModelIn("adam", "12345", "12345", "adam@gmail.com")
        user = User("bengt", BCryptPasswordEncoder().encode("12345"), " bengt@gmail.com", mutableSetOf(USER_ROLE))
    }

    @Test
   fun findAll() {
       val userModelIn = UserModelIn("bengt", "1234", "1234", "bengt@gmail.com")
       val savedUser = userService.save(userModelIn)
       val result = testRestTemplate.getForEntity(USER_API_PATH, String::class.java)
       assertNotNull(result)
       assertEquals(result.statusCode, HttpStatus.OK)
       val resultModels: List<UserModelOut> = objectMapper.readValue(result.body ?: throw IllegalStateException())
       assertEquals(resultModels[0].userName, userModelIn.userName)
       savedUser.id?.let { userService.delete(it) }
   }

    @Test
    fun findByUserName(){
        val userModelIn = UserModelIn("bengt", "1234", "1234", "bengt@gmail.com")
        val savedUser = userService.save(userModelIn)
        val result = testRestTemplate.getForEntity(USER_API_PATH.plus("?username=bengt"), String::class.java)
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.OK)
        assertTrue(result.body.toString().contains(userModelIn.userName))
        savedUser.id?.let { userService.delete(it) }
    }

    @Test
    fun findByUserNameForAuth(){
        val userModelIn = UserModelIn("bengt", "1234", "1234", "bengt@gmail.com")
        val savedUser = userService.save(userModelIn)
        val result = testRestTemplate.getForEntity(USER_API_PATH.plus("/loadforauth/bengt"), String::class.java)
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.OK)
        assertTrue(result.body.toString().contains(userModelIn.userName))
        assertTrue(result.body.toString().contains("password"))
        savedUser.id?.let { userService.delete(it) }
    }

    @Test
    fun findOne_By_Id_Throws_NotFoundException_When_None_Exists() {
        val id = 99L
        val result = testRestTemplate.getForEntity(USER_API_PATH.plus("/$id"), String::class.java)
        assertNotNull(result)
        assertTrue(result.body.toString().contains(NO_USER_FOUND_WITH_ID))
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)
    }

    @Test
    fun saveOne_Throws_ConflictException_When_Same_UserName_Exists() {
        val userModelIn = UserModelIn("bengt", "1234", "1234", "bengt@gmail.com")
        val savedUser = userService.save(userModelIn)
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<UserModelIn> = HttpEntity<UserModelIn>(userModelIn, headers)
        val result = testRestTemplate.postForEntity(USER_API_PATH, request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.CONFLICT)
        savedUser.id?.let { userService.delete(it) }

    }
    @Test
    fun saveOne_Throws_ConflictException_When_Same_Email_Exists() {
        val savedUser = userService.save(userModelInGlobal)
        val userModelIn = UserModelIn("annan", "12345", "12345", "adam@gmail.com")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<UserModelIn> = HttpEntity<UserModelIn>(userModelIn, headers)
        val result = testRestTemplate.postForEntity(USER_API_PATH, request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.CONFLICT)
        savedUser.id?.let { userService.delete(it) }
    }

    @Test
    fun saveOne_Throws_BadRequestException_On_Blank_String_In_Name() {
        val headers = HttpHeaders()
        val userModelIn = UserModelIn(" ", "12345", "12345", "bengt@gmail.com")
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<UserModelIn> = HttpEntity<UserModelIn>(userModelIn, headers)
        val result = testRestTemplate.postForEntity(USER_API_PATH, request, String::class.java)
        println(result)
        assertTrue(result.body.toString().contains(EMPTY_USERNAME_NOT_ALLOWED))
        assertEquals(result.statusCode, HttpStatus.BAD_REQUEST)
    }

    @Test
    fun saveOne_Throws_BadRequestException_On_Non_Matching_Passwords() {
        val headers = HttpHeaders()
        val userModelIn = UserModelIn("ronny", "12345", "nonmatch", "bengt@gmail.com")
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<UserModelIn> = HttpEntity<UserModelIn>(userModelIn, headers)
        val result = testRestTemplate.postForEntity(USER_API_PATH, request, String::class.java)
        println(result.toString())
        assertTrue(result.body.toString().contains(PASSWORDS_NOT_MATCHING))
        assertEquals(result.statusCode, HttpStatus.BAD_REQUEST)
    }

    @Test
    fun saveOne_Throws_BadRequestException_On_Too_Short_Password() {
        val headers = HttpHeaders()
        val userModelIn = UserModelIn(" ", "123", "123", "bengt@gmail.com")
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<UserModelIn> = HttpEntity<UserModelIn>(userModelIn, headers)
        val result = testRestTemplate.postForEntity(USER_API_PATH, request, String::class.java)
        println(result.toString())
        assertTrue(result.body.toString().contains(PASSWORD_LENGTH_WARNING))
        assertEquals(result.statusCode, HttpStatus.BAD_REQUEST)
    }

    @Test
    fun saveOneNormalOperations() {
        val userModelIn = UserModelIn("bengt", "1234", "1234", "bengt@gmail.com")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<UserModelIn> = HttpEntity<UserModelIn>(userModelIn, headers)
        val result = testRestTemplate.postForEntity(USER_API_PATH, request, String::class.java)
        val resultUserModelIn: UserModelOut = objectMapper.readValue(result.body ?: throw IllegalStateException())
        assertEquals(result.statusCode, HttpStatus.OK)
        assertEquals(resultUserModelIn.userName, userModelIn.userName)
        assertEquals(resultUserModelIn.email, userModelIn.email)
        resultUserModelIn.id?.let { userService.delete(it) }
    }


    @Test
    fun update_Id_Not_Found(){
        val userModelInDb = UserModelIn("bengt", "1234", "1234", "bengt@gmail.com")
        val userModelIn = UserModelIn("ronny", "5432", "5432", "ronny@gmail.com")
        val savedUser = userService.save(userModelInDb)
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<UserModelIn> = HttpEntity<UserModelIn>(userModelIn, headers)
        val result = testRestTemplate.exchange(USER_API_PATH.plus("/").plus(99), HttpMethod.PUT, request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)
        savedUser.id?.let { userService.delete(it) }
    }

    @Test
    fun update_Normal_Operations(){
        val userModelInDb = UserModelIn("bengt", "1234", "1234", "bengt@gmail.com")
        val userModelIn = UserModelIn("ronny", "5432", "5432", "ronny@gmail.com")
        val savedUser = userService.save(userModelInDb)
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<UserModelIn> = HttpEntity<UserModelIn>(userModelIn, headers)
        val result = testRestTemplate.exchange(USER_API_PATH.plus("/").plus(savedUser.id), HttpMethod.PUT, request, String::class.java)
        val resultUserModelIn: UserModelOut = objectMapper.readValue(result.body ?: throw IllegalStateException())
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.OK)
        assertEquals(userModelIn.userName, resultUserModelIn.userName)
        assertEquals(userModelIn.email, userModelIn.email)
        savedUser.id?.let { userService.delete(it) }
    }

    @Test
    fun update_UserName_Already_Exists_In_Db(){
        val userModelInDb = UserModelIn("bengt", "1234", "1234", "bengt@gmail.com")
        val userModelInDb2 = UserModelIn("bengt2", "1234", "1234", "bengt2@gmail.com")
        val savedUser = userService.save(userModelInDb)
        val savedUser2 = userService.save(userModelInDb2)
        val userModelIn = UserModelIn("bengt2", "5432", "5432", "ronny@gmail.com")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<UserModelIn> = HttpEntity<UserModelIn>(userModelIn, headers)
        val result = testRestTemplate.exchange(USER_API_PATH.plus("/").plus(savedUser.id), HttpMethod.PUT, request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.CONFLICT)
        savedUser2.id?.let { userService.delete(it) }
        savedUser.id?.let { userService.delete(it) }
    }

    @Test
    fun update_Email_Already_Exists_In_Db(){
        val userModelInDb = UserModelIn("bengt", "1234", "1234", "bengt@gmail.com")
        val userModelInDb2 = UserModelIn("bengt2", "1234", "1234", "bengt2@gmail.com")
        val savedUser = userService.save(userModelInDb)
        val savedUser2 = userService.save(userModelInDb2)
        val userModelIn = UserModelIn("ronny", "5432", "5432", "bengt2@gmail.com")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<UserModelIn> = HttpEntity<UserModelIn>(userModelIn, headers)
        val result = testRestTemplate.exchange(USER_API_PATH.plus("/").plus(savedUser.id), HttpMethod.PUT, request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.CONFLICT)
        savedUser2.id?.let { userService.delete(it) }
        savedUser.id?.let { userService.delete(it) }
    }


    @Test
    fun deleteNormalOps() {
        val userModelIn = UserModelIn("bengt2", "5432", "5432", "ronny@gmail.com")
        val savedUser = userService.save(userModelIn)
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<UserModelIn> = HttpEntity<UserModelIn>(headers)
        testRestTemplate.exchange(USER_API_PATH.plus("/").plus(savedUser.id), HttpMethod.DELETE, request, String::class.java)
        assertFalse(userRepository.existsByUserName("bengt2"))
    }

    @Test
    fun deleteNoneFound() {
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<UserModelIn> = HttpEntity<UserModelIn>(headers)
        val result = testRestTemplate.exchange(USER_API_PATH.plus("/").plus(99), HttpMethod.DELETE, request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)
        println(result.body)
        result.body?.contains(NO_USER_FOUND_WITH_ID)?.let { assertTrue(it) }
    }
}
