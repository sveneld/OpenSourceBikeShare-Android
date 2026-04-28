package com.bikeshare.app.util

import androidx.test.core.app.ApplicationProvider
import com.bikeshare.app.data.api.dto.RentSystemParamsDto
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RentMessageRendererTest {

    private lateinit var renderer: RentMessageRenderer

    @Before
    fun setUp() {
        renderer = RentMessageRenderer(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `unknown code falls back to provided string`() {
        val result = renderer.render(code = "no.such.code", params = emptyMap(), fallback = "fallback text")
        assertEquals("fallback text", result)
    }

    @Test
    fun `null code falls back`() {
        val result = renderer.render(code = null, params = null, fallback = "server text")
        assertEquals("server text", result)
    }

    @Test
    fun `null code with null fallback yields empty string`() {
        val result = renderer.render(code = null, params = null, fallback = null)
        assertEquals("", result)
    }

    @Test
    fun `bike_rent_success without note renders main only`() {
        val result = renderer.render(
            code = "bike.rent.success",
            params = mapOf(
                "bikeNumber" to 42,
                "currentCode" to "1234",
                "newCode" to "5678",
                "hasNote" to "false",
            ),
        )
        assertEquals(
            "Bike #42: Open with code 1234. Change code immediately to 5678 (open, rotate metal part, set new code, rotate metal part back).",
            result,
        )
    }

    @Test
    fun `bike_rent_success with note appends issue line`() {
        val result = renderer.render(
            code = "bike.rent.success",
            params = mapOf(
                "bikeNumber" to 7,
                "currentCode" to "0001",
                "newCode" to "9999",
                "hasNote" to "true",
                "note" to "front tire flat",
            ),
        )
        assertEquals(
            "Bike #7: Open with code 0001. Change code immediately to 9999 (open, rotate metal part, set new code, rotate metal part back).\nReported issue: front tire flat",
            result,
        )
    }

    @Test
    fun `bike_return_success renders both note and credit appendices`() {
        val result = renderer.render(
            code = "bike.return.success",
            params = mapOf(
                "bikeNumber" to 12,
                "standName" to "MAIN",
                "currentCode" to "4321",
                "hasNote" to "true",
                "note" to "broken bell",
                "hasCreditChange" to "true",
                "creditChange" to 0.5,
                "creditCurrency" to "€",
            ),
        )
        assertEquals(
            "Bike #12 returned to stand MAIN. Lock with code 4321. Please, rotate the lockpad to 0000 when leaving. Wipe the bike clean if it is dirty, please." +
                "\nYou have also reported this problem: broken bell." +
                "\nCredit change: -0.5€.",
            result,
        )
    }

    @Test
    fun `bike_return_success without flags renders main only`() {
        val result = renderer.render(
            code = "bike.return.success",
            params = mapOf(
                "bikeNumber" to 12,
                "standName" to "MAIN",
                "currentCode" to "4321",
                "hasNote" to "false",
                "hasCreditChange" to "false",
            ),
        )
        assertEquals(
            "Bike #12 returned to stand MAIN. Lock with code 4321. Please, rotate the lockpad to 0000 when leaving. Wipe the bike clean if it is dirty, please.",
            result,
        )
    }

    @Test
    fun `bike_revert_success renders three params`() {
        val result = renderer.render(
            code = "bike.revert.success",
            params = mapOf(
                "bikeNumber" to 99,
                "standName" to "DEPOT",
                "code" to "0011",
            ),
        )
        assertEquals("Bike #99 reverted to DEPOT with code 0011.", result)
    }

    @Test
    fun `bike_rent_error_not_found`() {
        val result = renderer.render(
            code = "bike.rent.error.not_found",
            params = mapOf("bikeNumber" to 5),
        )
        assertEquals("Bike #5 does not exist.", result)
    }

    @Test
    fun `bike_rent_error_already_rented`() {
        val result = renderer.render(
            code = "bike.rent.error.already_rented",
            params = mapOf("bikeNumber" to 8),
        )
        assertEquals("Bike #8 is already rented.", result)
    }

    @Test
    fun `bike_rent_error_already_rented_by_current_user`() {
        val result = renderer.render(
            code = "bike.rent.error.already_rented_by_current_user",
            params = mapOf("bikeNumber" to 8, "currentCode" to "1212"),
        )
        assertEquals("You have already rented the bike #8. Code is 1212.", result)
    }

    @Test
    fun `bike_rent_error_insufficient_credit`() {
        val result = renderer.render(
            code = "bike.rent.error.insufficient_credit",
            params = mapOf("minRequiredCredit" to 5.0, "creditCurrency" to "€"),
        )
        assertEquals("You are below required credit 5.0€. Please, recharge your credit.", result)
    }

    @Test
    fun `bike_rent_error_zero_limit`() {
        val result = renderer.render(code = "bike.rent.error.zero_limit", params = emptyMap())
        assertEquals("You can not rent any bikes. Contact the admins to lift the ban.", result)
    }

    @Test
    fun `bike_rent_error_limit plural one`() {
        val result = renderer.render(
            code = "bike.rent.error.limit",
            params = mapOf("count" to 1),
        )
        assertEquals("You can only rent 1 bike at once.", result)
    }

    @Test
    fun `bike_rent_error_limit plural other`() {
        val result = renderer.render(
            code = "bike.rent.error.limit",
            params = mapOf("count" to 3),
        )
        assertEquals("You can only rent 3 bikes at once.", result)
    }

    @Test
    fun `bike_rent_error_service_stand`() {
        val result = renderer.render(code = "bike.rent.error.service_stand", params = emptyMap())
        assertEquals(
            "Renting from service stands is not allowed: The bike probably waits for a repair.",
            result,
        )
    }

    @Test
    fun `bike_rent_error_stack_top_bike`() {
        val result = renderer.render(
            code = "bike.rent.error.stack_top_bike",
            params = mapOf("bikeNumber" to 11, "stackTopBike" to 17),
        )
        assertEquals("Bike #11 is not rentable now, you have to rent bike #17 from this stand.", result)
    }

    @Test
    fun `bike_return_error_stand_not_found`() {
        val result = renderer.render(
            code = "bike.return.error.stand_not_found",
            params = mapOf("standName" to "ghost"),
        )
        assertEquals("Stand name 'ghost' does not exist. Stands are marked by CAPITAL LETTERS.", result)
    }

    @Test
    fun `bike_return_error_no_rented_bikes`() {
        val result = renderer.render(code = "bike.return.error.no_rented_bikes", params = emptyMap())
        assertEquals("You currently have no rented bikes.", result)
    }

    @Test
    fun `bike_revert_error_not_rented`() {
        val result = renderer.render(
            code = "bike.revert.error.not_rented",
            params = mapOf("bikeNumber" to 1),
        )
        assertEquals("Bicycle #1 is not rented right now. Revert not successful!", result)
    }

    @Test
    fun `bike_revert_error_no_stand_or_code`() {
        val result = renderer.render(
            code = "bike.revert.error.no_stand_or_code",
            params = mapOf("bikeNumber" to 2),
        )
        assertEquals("No last stand or code for bicycle #2 found. Revert not successful!", result)
    }

    @Test
    fun `numeric params arriving as String are coerced to Int`() {
        val result = renderer.render(
            code = "bike.rent.error.not_found",
            params = mapOf("bikeNumber" to "42"),
        )
        assertEquals("Bike #42 does not exist.", result)
    }

    @Test
    fun `typed RentSystemParamsDto delegates to map-based render`() {
        val result = renderer.renderFromDto(
            code = "bike.rent.success",
            params = RentSystemParamsDto(
                bikeNumber = 3,
                currentCode = "1111",
                newCode = "2222",
                hasNote = "false",
            ),
        )
        assertEquals(
            "Bike #3: Open with code 1111. Change code immediately to 2222 (open, rotate metal part, set new code, rotate metal part back).",
            result,
        )
    }

    @Test
    fun `flagParam treats missing as false`() {
        val result = renderer.render(
            code = "bike.return.success",
            params = mapOf(
                "bikeNumber" to 1,
                "standName" to "S",
                "currentCode" to "0000",
            ),
        )
        // No hasNote / hasCreditChange => treated as false => no appendices.
        assertEquals(
            "Bike #1 returned to stand S. Lock with code 0000. Please, rotate the lockpad to 0000 when leaving. Wipe the bike clean if it is dirty, please.",
            result,
        )
    }
}
