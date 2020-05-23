package org.kepr.hostapi.model


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.kepr.hostapi.data.Alias
import org.kepr.hostapi.data.Host

internal class AliasModelTest{

    @Test
    fun ctor() {
        val alias = Alias("adam", mutableListOf(Host("192.168.1.102", "stockholm")))
       val aliasModel = AliasModel.toModel(alias)
        assertEquals(aliasModel.name, alias.name)
    }
}