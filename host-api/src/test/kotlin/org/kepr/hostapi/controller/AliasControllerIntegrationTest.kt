package org.kepr.hostapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kepr.hostapi.config.*
import org.kepr.hostapi.model.AliasModel
import org.kepr.hostapi.model.HostModel
import org.kepr.hostapi.repository.HostRepository
import org.kepr.hostapi.service.AliasService
import org.kepr.hostapi.service.HostService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AliasControllerIntegrationTest {
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

    lateinit var nycAlias: AliasModel
    lateinit var stockholmAlias: AliasModel
    lateinit var finlandAlias: AliasModel
    lateinit var desktopHostModel: HostModel
    lateinit var raspberryPi: HostModel

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
       val result = testRestTemplate.getForEntity(ALIAS_API_PATH, String::class.java)
       assertNotNull(result)
       assertEquals(result.statusCode, HttpStatus.OK)
       val resultModels: List<AliasModel> = objectMapper.readValue(result.body ?: throw IllegalStateException())
       assertEquals(resultModels[0].name, nycAlias.name)
       savedAlias.id?.let { aliasService.delete(it) }
   }

    @Test
    fun findByName(){
        val savedAlias = aliasService.save(nycAlias)
        val result = testRestTemplate.getForEntity(ALIAS_API_PATH.plus("?name=nyc"), String::class.java)
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.OK)
        assertTrue(result.body.toString().contains(nycAlias.name))
        savedAlias.id?.let { aliasService.delete(it) }
    }



    @Test
    fun findOne_By_Id_Throws_NotFoundException_When_None_Exists() {
        val id = 99L
        val result = testRestTemplate.getForEntity(ALIAS_API_PATH.plus("/$id"), String::class.java)
        assertNotNull(result)
        assertTrue(result.body.toString().contains(NO_ALIAS_FOUND_WITH_ID))
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)
    }

    @Test
    fun saveOne_Throws_ConflictException_When_Same_Name_Exists() {
        val savedAlias = aliasService.save(nycAlias)
        val savedHost = hostService.save(desktopHostModel)
        val aliasModel = AliasModel(null, "nyc", listOf())
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<AliasModel> = HttpEntity<AliasModel>(aliasModel, headers)
        val result = testRestTemplate.postForEntity(ALIAS_API_PATH, request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.CONFLICT)
        savedHost.id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }

    }

    @Test
    fun saveOne_Throws_BadRequestException_On_Blank_String_In_Name() {
        val aliasModel = AliasModel(null, " ", listOf())
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<AliasModel> = HttpEntity<AliasModel>(aliasModel, headers)
        val result = testRestTemplate.postForEntity(ALIAS_API_PATH, request, String::class.java)
        println(result.toString())
        assertTrue(result.body.toString().contains(EMPTY_NAME_NOT_ALLOWED))
        assertEquals(result.statusCode, HttpStatus.BAD_REQUEST)
    }
    @Test
    fun saveOne_Allows_no_HostList() {
        val aliasModel = AliasModel(null, "new-name")
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<AliasModel> = HttpEntity<AliasModel>(aliasModel, headers)
        val result = testRestTemplate.postForEntity(ALIAS_API_PATH, request, String::class.java)
        val resultAliasModel: AliasModel = objectMapper.readValue(result.body ?: throw IllegalStateException())
        assertEquals(result.statusCode, HttpStatus.OK)
        assertEquals(resultAliasModel.name, aliasModel.name)
        resultAliasModel.id?.let { aliasService.delete(it) }
    }

    @Test
    fun saveOne_Throws_BadRequestException_On_Hosts_That_Does_Not_Exists() {
        val aliasModel = AliasModel(null, "nyc", listOf("non-existent-host", "non-existent-host-2"))
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<AliasModel> = HttpEntity<AliasModel>(aliasModel, headers)
        val result = testRestTemplate.postForEntity(ALIAS_API_PATH, request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)
        assertTrue(result.body.toString().contains(NO_HOST_FOUND_WITH_NAME))
    }

    @Test
    fun saveOneNormalOperations() {
        val savedAlias = aliasService.save(nycAlias)
        val savedHost = hostService.save(desktopHostModel)
        val aliasModel = AliasModel(null, "new-alias", listOf())
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<AliasModel> = HttpEntity<AliasModel>(aliasModel, headers)
        val result = testRestTemplate.postForEntity(ALIAS_API_PATH, request, String::class.java)
        val resultAliasModel: AliasModel = objectMapper.readValue(result.body ?: throw IllegalStateException())
        assertEquals(result.statusCode, HttpStatus.OK)
        assertEquals(resultAliasModel.name, aliasModel.name)
        savedHost.id?.let { hostService.delete(it) }
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
        val result = testRestTemplate.exchange(ALIAS_API_PATH.plus("/").plus(id), HttpMethod.PUT, request, String::class.java)
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
        val result = testRestTemplate.exchange(ALIAS_API_PATH.plus("/").plus(savedHost.id), HttpMethod.PUT, request, String::class.java)
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
        val result = testRestTemplate.exchange(ALIAS_API_PATH.plus("/").plus(savedHost.id), HttpMethod.PUT, request, String::class.java)
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
        val result = testRestTemplate.exchange(ALIAS_API_PATH.plus("/").plus(savedHost.id), HttpMethod.PUT, request, String::class.java)
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
        val result = testRestTemplate.exchange(ALIAS_API_PATH.plus("/").plus(savedHost.id), HttpMethod.DELETE, request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.OK)
        assertFalse(hostRepository.existsByName("desktop"))
        savedAlias.id?.let { aliasService.delete(it) }
    }

    @Test
    fun deleteNoneFound() {
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(headers)
        val result = testRestTemplate.exchange(ALIAS_API_PATH.plus("/").plus(99), HttpMethod.DELETE, request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)
        println(result.body)
        result.body?.contains(NO_ALIAS_FOUND_WITH_ID)?.let { assertTrue(it) }
    }
}
