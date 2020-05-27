package org.kepr.hostapi.controller

import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired


/**
 * Makes sure that the test DB is cleaned up after each test
 * @author Sebastien Dubois
 */
open class AbstractIntegrationTest : AbstractTest() {
    @field:Autowired
    private lateinit
    var truncateDatabaseService: DatabaseCleanupService

    // TODO add @BeforeEach, BeforeClass, After... and a parameter (enum) to define if and when to perform the cleanup

    /**
     * Cleans up the test database after each test method.
     */
    @AfterEach
    fun cleanupAfterEach() {
        println("Cleanup up the test database")
        truncateDatabaseService.truncate()
    }
}

open class AbstractTest {

}
