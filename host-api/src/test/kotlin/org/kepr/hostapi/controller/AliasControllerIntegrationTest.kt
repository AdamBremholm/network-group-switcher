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
import org.kepr.hostapi.repository.AliasRepository
import org.kepr.hostapi.repository.HostRepository
import org.kepr.hostapi.service.AliasService
import org.kepr.hostapi.service.HostService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*


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
    lateinit var aliasRepository: AliasRepository
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
        assertTrue(result.body.toString().contains(THESE_HOSTS_WERE_NOT_FOUND))
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
    fun update_Hosts_In_Aliases_Throws_Error_On_Not_found_Hosts() {
        val savedAlias = aliasService.save(finlandAlias)
        val savedHost = hostService.save(raspberryPi)
        val aliasModel = AliasModel(name ="newalias", hosts =  listOf("raspberry-pi", "unknown host"))
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val request: HttpEntity<AliasModel> = HttpEntity<AliasModel>(aliasModel, headers)
        val result = testRestTemplate.exchange(ALIAS_API_PATH.plus("/").plus(savedAlias.id), HttpMethod.PUT, request, String::class.java)
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)
        savedHost.id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }

    }

    @Test
    fun update_Normal_Operations(){
        val savedAlias = aliasService.save(finlandAlias)
        val savedHost = hostService.save(raspberryPi)
        val aliasModel = AliasModel(name = "new-alias", hosts =  listOf("raspberry-pi"))
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<AliasModel> = HttpEntity<AliasModel>(aliasModel, headers)
        val result = testRestTemplate.exchange(ALIAS_API_PATH.plus("/").plus(savedAlias.id), HttpMethod.PUT, request, String::class.java)
        val resultAliasModel: AliasModel = objectMapper.readValue(result.body ?: throw IllegalStateException())
        assertNotNull(result)
        assertEquals(result.statusCode, HttpStatus.OK)
        assertEquals(aliasModel.name, resultAliasModel.name)
        assertEquals(aliasModel.hosts, aliasModel.hosts)
        savedHost.id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }
    }

    @Test
    fun update_New_Name_Already_Exists_In_Db(){
        val savedAlias = aliasService.save(finlandAlias)
        val savedAlias2 = aliasService.save(nycAlias)
        val savedHost = hostService.save(raspberryPi)
        val aliasModel = AliasModel(name = "nyc", hosts =  listOf("raspberry-pi"))
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<AliasModel> = HttpEntity<AliasModel>(aliasModel, headers)
        val result = testRestTemplate.exchange(ALIAS_API_PATH.plus("/").plus(savedAlias.id), HttpMethod.PUT, request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.CONFLICT)
        savedHost.id?.let { hostService.delete(it) }
        savedAlias.id?.let { aliasService.delete(it) }
        savedAlias2.id?.let { aliasService.delete(it) }
    }



    @Test
    fun deleteNormalOps() {
        val savedAlias = aliasService.save(nycAlias)
        hostService.save(desktopHostModel)
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<HostModel> = HttpEntity<HostModel>(headers)
        println(aliasService.findAll())
        testRestTemplate.exchange(ALIAS_API_PATH.plus("/").plus(savedAlias.id), HttpMethod.DELETE, request, String::class.java)
        assertFalse(aliasRepository.existsByName("nyc"))
        assertFalse(hostRepository.existsByName("desktop"))
    }

    @Test
    fun deleteNoneFound() {
        val headers = HttpHeaders()
        headers.set("X-COM-PERSIST", "true")
        val request: HttpEntity<AliasModel> = HttpEntity<AliasModel>(headers)
        val result = testRestTemplate.exchange(ALIAS_API_PATH.plus("/").plus(99), HttpMethod.DELETE, request, String::class.java)
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)
        println(result.body)
        result.body?.contains(NO_ALIAS_FOUND_WITH_ID)?.let { assertTrue(it) }
    }
}
