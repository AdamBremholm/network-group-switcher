package org.kepr.hostapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kepr.hostapi.exception.*
import org.kepr.hostapi.model.AliasModel
import org.kepr.hostapi.model.HostModel
import org.kepr.hostapi.repository.AliasRepository
import org.kepr.hostapi.repository.HostRepository
import org.kepr.hostapi.service.AliasService
import org.kepr.hostapi.service.HostService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HostControllerIntegrationTest {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    lateinit var hostService: HostService

    @Autowired
    lateinit var aliasService: AliasService

    @Autowired
    lateinit var hostRepository: HostRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var nycAlias : AliasModel
    lateinit var stockholmAlias : AliasModel
    lateinit var finlandAlias : AliasModel
    lateinit var desktopHostModel : HostModel
    lateinit var raspberryPi : HostModel

    @BeforeAll
    fun setup() {
        nycAlias = AliasModel(null, "nyc", mutableListOf())
        stockholmAlias = AliasModel(null, "stockholm", mutableListOf())
        finlandAlias = AliasModel(null, "finland", mutableListOf())
        desktopHostModel = HostModel(null, "192.168.1.102", "desktop", "nyc")
        raspberryPi = HostModel(null, "192.168.1.103", "raspberry-pi", "finland")
    }

    @Test
    fun findAll() {
        val savedAlias = aliasService.save(nycAlias)
        val savedHost = hostService.save(desktopHostModel)
        val result = testRestTemplate.getForEntity("/api/hosts", String::class.java)
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.OK)
        val resultModels: List<HostModel> = objectMapper.readValue(result.body ?: throw IllegalStateException())
        assertEquals(resultModels[0].name, desktopHostModel.name)
        savedHost.id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }
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
        val savedAlias = aliasService.save(nycAlias)
        val savedHost = hostService.save(desktopHostModel)
        val result = testRestTemplate.getForEntity("/api/hosts?name=desktop&address=192.168.1.102", String::class.java)
        assertNotNull(result)
        assertTrue(result.body.toString().contains("desktop"))
        assertEquals(result.statusCode, HttpStatus.OK)
        savedHost.id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }
    }

    @Test
    fun findByNameAndAddress_QueryParams_Normal_Operations_Only_Name() {
        val savedAlias = aliasService.save(nycAlias)
        val savedHost = hostService.save(desktopHostModel)
        val result = testRestTemplate.getForEntity("/api/hosts?name=desktop", String::class.java)
        assertNotNull(result)
        assertTrue(result.body.toString().contains("desktop"))
        assertEquals(result.statusCode, HttpStatus.OK)
        savedHost.id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }
    }

    @Test
    fun findByNameAndAddress_QueryParams_Normal_Operations_Only_Address() {
        val savedAlias = aliasService.save(nycAlias)
        val savedHost = hostService.save(desktopHostModel)
        val result = testRestTemplate.getForEntity("/api/hosts?address=192.168.1.102", String::class.java)
        assertNotNull(result)
        assertTrue(result.body.toString().contains("desktop"))
        assertEquals(result.statusCode, HttpStatus.OK)
        savedHost.id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }
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
    fun saveOne_Throws_ConflictException_When_Same_Ip_Exists() {
        val savedAlias = aliasService.save(nycAlias)
        val savedHost = hostService.save(desktopHostModel)
       val hostModel = HostModel(null, "192.168.1.102", "new-device", "nyc")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.postForEntity("/api/hosts/", request, String::class.java)
       assertEquals(result.statusCode, HttpStatus.CONFLICT)
        savedHost.id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }

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
        val savedAlias = aliasService.save(nycAlias)
        val savedHost = hostService.save(desktopHostModel)
        val hostModel = HostModel(null, "192.168.1.103", "desktop", "nyc")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.postForEntity("/api/hosts/", request, String::class.java)
        println(result.body.toString())
        assertTrue(result.body.toString().contains(HOST_NAME_ALREADY_EXISTS))
        assertEquals(result.statusCode, HttpStatus.CONFLICT)
        savedHost.id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }
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
        val savedAlias = aliasService.save(nycAlias)
        val hostModel = HostModel(null, "192.168.1.103", "laptop", "nyc")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.postForEntity("/api/hosts/", request, String::class.java)
        val resultHostModel: HostModel = objectMapper.readValue(result.body ?: throw IllegalStateException())
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.OK)
        assertEquals(resultHostModel.name, hostModel.name)
        resultHostModel.id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }
    }


    @Test
    fun updateNormalOperationsNameAndAddressAlias() {
        val savedAlias = aliasService.save(finlandAlias)
        val savedHost = hostService.save(raspberryPi)
        val id = savedHost.id
        val hostModel = HostModel(null, "192.168.1.123", "pfsense", "finland")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.exchange("/api/hosts/".plus(id), HttpMethod.PUT, request, String::class.java)
        val resultHostModel: HostModel = objectMapper.readValue(result.body ?: throw IllegalStateException())
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.OK)
        assertEquals(hostModel.address, resultHostModel.address)
        assertEquals(hostModel.name, resultHostModel.name)
        assertEquals(hostModel.alias, resultHostModel.alias)
        id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }

    }

    @Test
    fun updateWithAlreadyExistingNameInDb() {
        val savedAlias = aliasService.save(finlandAlias)
        val savedAlias2 = aliasService.save(nycAlias)
        val savedHost = hostService.save(raspberryPi)
        val savedHost2 = hostService.save(desktopHostModel)
        val hostModel = HostModel(null, "192.168.1.123", "desktop", "finland")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.exchange("/api/hosts/".plus(savedHost.id), HttpMethod.PUT, request, String::class.java)
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.CONFLICT)
        savedHost.id?.let { hostService.delete(it) }
        savedHost2.id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }
        savedAlias2.id?.let { aliasService.delete(it) }
    }

    @Test
    fun updateWithNoMatchingAlias() {
        val savedAlias = aliasService.save(finlandAlias)
        val savedAlias2 = aliasService.save(nycAlias)
        val savedHost = hostService.save(raspberryPi)
        val savedHost2 = hostService.save(desktopHostModel)
        val hostModel = HostModel(null, "192.168.1.123", "raspberry-pi", "norway")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.exchange("/api/hosts/".plus(savedHost.id), HttpMethod.PUT, request, String::class.java)
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)
        result.body?.contains(NO_ALIAS_FOUND_WITH_NAME.plus("norway"))?.let { assertTrue(it) }
        savedHost.id?.let { hostService.delete(it) }
        savedHost2.id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }
        savedAlias2.id?.let { aliasService.delete(it) }
    }

    @Test
    fun updateWithInvalidIp() {
        val savedAlias = aliasService.save(finlandAlias)
        val savedAlias2 = aliasService.save(nycAlias)
        val savedHost = hostService.save(raspberryPi)
        val savedHost2 = hostService.save(desktopHostModel)
        val hostModel = HostModel(null, "192.320.1.123", "raspberry-pi", "finland")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(hostModel, headers)
        val result = testRestTemplate.exchange("/api/hosts/".plus(savedHost.id), HttpMethod.PUT, request, String::class.java)
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.BAD_REQUEST)
        result.body?.contains(NOT_VALID_IPV4_ADDRESS)?.let { assertTrue(it) }
        savedHost.id?.let { hostService.delete(it) }
        savedHost2.id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }
        savedAlias2.id?.let { aliasService.delete(it) }
    }

    @Test
    fun deleteNormalOps() {
        val savedAlias = aliasService.save(nycAlias)
        val savedHost = hostService.save(desktopHostModel)
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(headers)
        val result = testRestTemplate.exchange("/api/hosts/".plus(savedHost.id), HttpMethod.DELETE, request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.OK)
        assertFalse(hostRepository.existsByName("desktop"))
        savedAlias.id?.let { aliasService.delete(it) }
    }

    @Test
    fun deleteNoneFound() {
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(headers)
        val result = testRestTemplate.exchange("/api/hosts/".plus(99), HttpMethod.DELETE, request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)
        println(result.body)
        result.body?.contains(NO_HOST_FOUND_WITH_ID)?.let { assertTrue(it) }
    }

}