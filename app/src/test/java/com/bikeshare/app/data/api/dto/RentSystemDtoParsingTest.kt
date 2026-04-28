package com.bikeshare.app.data.api.dto

import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RentSystemDtoParsingTest {

    private val moshi = Moshi.Builder().build()

    @Test
    fun `RentSystemResultDto parses success payload with code and params`() {
        val json = """
            {
              "error": false,
              "message": "Bike 5: Open with code 1234.",
              "code": "bike.rent.success",
              "params": {
                "bikeNumber": 5,
                "currentCode": "1234",
                "newCode": "5678",
                "hasNote": "false"
              }
            }
        """.trimIndent()

        val dto = moshi.adapter(RentSystemResultDto::class.java).fromJson(json)
        assertNotNull(dto)
        assertEquals(false, dto!!.error)
        assertEquals("bike.rent.success", dto.code)
        assertEquals(5, dto.params?.bikeNumber)
        assertEquals("1234", dto.params?.currentCode)
        assertEquals("5678", dto.params?.newCode)
        assertEquals("false", dto.params?.hasNote)
    }

    @Test
    fun `RentSystemParamsDto handles new fields hasCreditChange creditChange`() {
        val json = """
            {
              "bikeNumber": 12,
              "standName": "MAIN",
              "currentCode": "4321",
              "hasNote": "true",
              "note": "broken bell",
              "hasCreditChange": "true",
              "creditChange": 0.5,
              "creditCurrency": "€"
            }
        """.trimIndent()

        val params = moshi.adapter(RentSystemParamsDto::class.java).fromJson(json)
        assertNotNull(params)
        assertEquals("true", params!!.hasNote)
        assertEquals("broken bell", params.note)
        assertEquals("true", params.hasCreditChange)
        assertEquals(0.5, params.creditChange!!, 1e-6)
        assertEquals("€", params.creditCurrency)
    }

    @Test
    fun `RentSystemParamsDto handles error-specific fields`() {
        val json = """
            {
              "minRequiredCredit": 5.0,
              "creditCurrency": "€",
              "count": 3,
              "stackTopBike": 17,
              "code": "0011"
            }
        """.trimIndent()

        val params = moshi.adapter(RentSystemParamsDto::class.java).fromJson(json)
        assertNotNull(params)
        assertEquals(5.0, params!!.minRequiredCredit!!, 1e-6)
        assertEquals(3, params.count)
        assertEquals(17, params.stackTopBike)
        assertEquals("0011", params.code)
    }

    @Test
    fun `ProblemDetail parses extended payload with code and params`() {
        val json = """
            {
              "type": "about:blank",
              "title": "Conflict",
              "status": 409,
              "detail": "Bike 5 is already rented.",
              "instance": "/api/v1/rentals",
              "requestId": "abc-123",
              "code": "bike.rent.error.already_rented",
              "params": { "bikeNumber": 5 }
            }
        """.trimIndent()

        val problem = moshi.adapter(ProblemDetail::class.java).fromJson(json)
        assertNotNull(problem)
        assertEquals("bike.rent.error.already_rented", problem!!.code)
        // Moshi parses Map<String, Any?> JSON numbers as Double.
        assertEquals(5.0, problem.params?.get("bikeNumber"))
    }

    @Test
    fun `ProblemDetail tolerates missing code and params for legacy responses`() {
        val json = """
            {
              "type": "about:blank",
              "title": "Conflict",
              "status": 409,
              "detail": "Generic error",
              "instance": "/api/v1/rentals",
              "requestId": "abc-123"
            }
        """.trimIndent()

        val problem = moshi.adapter(ProblemDetail::class.java).fromJson(json)
        assertNotNull(problem)
        assertNull(problem!!.code)
        assertNull(problem.params)
    }
}
