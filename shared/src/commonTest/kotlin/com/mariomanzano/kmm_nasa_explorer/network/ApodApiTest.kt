package com.mariomanzano.kmm_nasa_explorer.network

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ApodApiTest {

    @Test
    fun dateIsFormattedAsTwoDigitYearMonthAndDay() {
        assertEquals("261110", LocalDate(2026, 11, 10).toApodDate())
    }

    @Test
    fun singleDigitMonthsAndDaysArePaddedWithZeros() {
        assertEquals("260105", LocalDate(2026, 1, 5).toApodDate())
    }

    @Test
    fun yearsBeforeTwoThousandKeepTheirLastTwoDigits() {
        // The first Astronomy Picture of the Day.
        assertEquals("950616", LocalDate(1995, 6, 16).toApodDate())
    }

    @Test
    fun firstYearsOfTheCenturyArePaddedWithZeros() {
        assertEquals("000101", LocalDate(2000, 1, 1).toApodDate())
        assertEquals("090731", LocalDate(2009, 7, 31).toApodDate())
    }

    @Test
    fun leapDayIsFormatted() {
        assertEquals("240229", LocalDate(2024, 2, 29).toApodDate())
    }
}
