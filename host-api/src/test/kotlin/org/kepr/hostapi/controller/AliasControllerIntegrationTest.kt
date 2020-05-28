package org.kepr.hostapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kepr.hostapi.model.AliasModel
import org.kepr.hostapi.model.HostModel
import org.kepr.hostapi.repository.HostRepository
import org.kepr.hostapi.service.AliasService
import org.kepr.hostapi.service.HostService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
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
       val result = testRestTemplate.getForEntity("/api/hosts", String::class.java)
       Assertions.assertNotNull(result)
       Assertions.assertEquals(result.statusCode, HttpStatus.OK)
       val resultModels: List<HostModel> = objectMapper.readValue(result.body ?: throw IllegalStateException())
       Assertions.assertEquals(resultModels[0].name, desktopHostModel.name)
       savedAlias.id?.let { aliasService.delete(it) }
   }
}
