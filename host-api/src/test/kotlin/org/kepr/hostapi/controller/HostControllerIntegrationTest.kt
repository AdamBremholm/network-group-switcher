package org.kepr.hostapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kepr.hostapi.data.Alias
import org.kepr.hostapi.exception.*
import org.kepr.hostapi.model.HostModel
import org.kepr.hostapi.repository.AliasRepository
import org.kepr.hostapi.repository.HostRepository
import org.kepr.hostapi.service.AliasService
import org.kepr.hostapi.service.HostService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HostControllerIntegrationTest {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    lateinit var hostRepository: HostRepository

    @Autowired
    lateinit var aliasRepository: AliasRepository

    @Autowired
    lateinit var hostService: HostService

    @Autowired
    lateinit var aliasService: AliasService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    val nycAlias = Alias("nyc", mutableListOf())
    val stockholmAlias = Alias("stockholm", mutableListOf())
    val desktopHostModel = HostModel(null, "192.168.1.102", "desktop", "nyc")


    @BeforeAll
    fun populateDB() {
        aliasRepository.save(nycAlias)
        aliasRepository.save(stockholmAlias)
        hostService.save(desktopHostModel)
    }


    @Test
    fun findAll() {
        val result = testRestTemplate.getForEntity("/api/hosts", String::class.java)
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.OK)
        val resultModels: List<HostModel> = objectMapper.readValue(result.body ?: throw IllegalStateException())
        assertEquals(resultModels[0].name, desktopHostModel.name)

    }

    @Test
    fun findByName_QueryParams_Non_Allowed_Param_Throws_BadRequestException() {
        val result = testRestTemplate.getForEntity("/api/hosts?name=hej&notAllowedParam=2", String::class.java)
        assertNotNull(result)
        assertTrue(result.body.toString().contains(NON_SUPPORTED_QUERY_PARAM))
        assertEquals(result.statusCode, HttpStatus.BAD_REQUEST)

    }

    @Test
    fun findByName_QueryParams_Not_Found_Throws_NotFoundException() {
        val result = testRestTemplate.getForEntity("/api/hosts?name=hej", String::class.java)
        assertNotNull(result)
        assertTrue(result.body.toString().contains(NO_HOST_FOUND_WITH_NAME))
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)

    }

    @Test
    fun findByNameAndAddress_QueryParams_One_Not_Found_Throws_NotFoundException() {
        val result = testRestTemplate.getForEntity("/api/hosts?name=desktop&address=nonExistentAddress", String::class.java)
        assertNotNull(result)
        assertTrue(result.body.toString().contains(NO_HOST_FOUND_WITH_NAME_AND_ADDRESS))
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)

    }

    @Test
    fun findByNameAndAddress_QueryParams_Normal_Operations() {
        val result = testRestTemplate.getForEntity("/api/hosts?name=desktop&address=192.168.1.102", String::class.java)
        assertNotNull(result)
        assertTrue(result.body.toString().contains("desktop"))
        assertEquals(result.statusCode, HttpStatus.OK)

    }

    @Test
    fun findByNameAndAddress_QueryParams_Normal_Operations_Only_Name() {
        val result = testRestTemplate.getForEntity("/api/hosts?name=desktop", String::class.java)
        assertNotNull(result)
        assertTrue(result.body.toString().contains("desktop"))
        assertEquals(result.statusCode, HttpStatus.OK)

    }

    @Test
    fun findByNameAndAddress_QueryParams_Normal_Operations_Only_Address() {
        val result = testRestTemplate.getForEntity("/api/hosts?address=192.168.1.102", String::class.java)
        assertNotNull(result)
        assertTrue(result.body.toString().contains("desktop"))
        assertEquals(result.statusCode, HttpStatus.OK)
    }

    @Test
    fun findOne_By_Id_Throws_NotFoundException_When_None_Exists() {
        val id = 99L
        val result = testRestTemplate.getForEntity("/api/hosts/$id", String::class.java)
        assertNotNull(result)
        assertTrue(result.body.toString().contains(NO_HOST_FOUND_WITH_ID))
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)
    }

    @Test
    fun findOne_ById() {
        val id = 3L
        val hostModelWithId = desktopHostModel.copy(id = id)
        val result = testRestTemplate.getForEntity("/api/hosts/$id", String::class.java)
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.OK)
        assertEquals(result.body, objectMapper.writeValueAsString(hostModelWithId))
    }

    @Test
    fun saveOne_Throws_ConflictException_When_Same_Ip_Exists() {
        val hostModel = HostModel(null, "192.168.1.102", "new-device", "nyc")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.postForEntity("/api/hosts/", request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.CONFLICT)
    }

    @Test
    fun saveOne_Throws_BadRequestException_On_Incorrect_Address_Format() {
        val hostModel = HostModel(null, "192.168.1.300", "new-device", "nyc")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.postForEntity("/api/hosts/", request, String::class.java)
        assertTrue(result.body.toString().contains(NOT_VALID_IPV4_ADDRESS))
        assertEquals(result.statusCode, HttpStatus.BAD_REQUEST)
    }

    @Test
    fun saveOne_Throws_BadRequestException_On_Blank_String_In_Name() {
        val hostModel = HostModel(null, "192.168.1.300", " ", "nyc")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.postForEntity("/api/hosts/", request, String::class.java)
        assertTrue(result.body.toString().contains(EMPTY_NAME_NOT_ALLOWED))
        assertEquals(result.statusCode, HttpStatus.BAD_REQUEST)
    }

    @Test
    fun saveOne_Throws_BadRequestException_On_Blank_String_In_Address() {
        val hostModel = HostModel(null, "", "new-device", "nyc")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.postForEntity("/api/hosts/", request, String::class.java)
        assertTrue(result.body.toString().contains(NOT_VALID_IPV4_ADDRESS))
        assertEquals(result.statusCode, HttpStatus.BAD_REQUEST)
    }

    @Test
    fun saveOne_Throws_BadRequestException_On_Blank_String_In_Alias() {
        val hostModel = HostModel(null, "192.168.1.103", "new-device", " ")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.postForEntity("/api/hosts/", request, String::class.java)
        assertTrue(result.body.toString().contains(EMPTY_ALIAS_NOT_ALLOWED))
        assertEquals(result.statusCode, HttpStatus.BAD_REQUEST)
    }

    @Test
    fun saveOne_Throws_ConflictException_When_Same_Name_Exists() {
        val hostModel = HostModel(null, "192.168.1.103", "desktop", "nyc")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.postForEntity("/api/hosts/", request, String::class.java)
        assertTrue(result.body.toString().contains(HOST_NAME_ALREADY_EXISTS))
        assertEquals(result.statusCode, HttpStatus.CONFLICT)
    }

    @Test
    fun saveOne_Throws_NotFoundException_When_No_Such_Alias_Exists() {
        val hostModel = HostModel(null, "192.168.1.103", "new-name", "aliasNotInDb")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.postForEntity("/api/hosts/", request, String::class.java)
        assertTrue(result.body.toString().contains(NO_ALIAS_FOUND_WITH_NAME))
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)
    }

    @Test
    fun saveOneNormalOperations() {
        val hostModel = HostModel(null, "192.168.1.103", "laptop", "nyc")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.postForEntity("/api/hosts/", request, String::class.java)
        val resultHostModel: HostModel = objectMapper.readValue(result.body ?: throw IllegalStateException())
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.OK)
        assertEquals(resultHostModel.name, hostModel.name)
    }


}