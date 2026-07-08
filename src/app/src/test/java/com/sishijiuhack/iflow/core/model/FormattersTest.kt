package com.sishijiuhack.iflow.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneId

class FormattersTest {
    private val zone = ZoneId.of("Asia/Shanghai")

    @Test
    fun editableTime_roundTripsMillisAtMinutePrecision() {
        val millis = parseEditableTime("2026-07-08 23:15", zone)

        assertNotNull(millis)
        assertEquals("2026-07-08 23:15", millis!!.formatEditableTime(zone))
    }

    @Test
    fun editableTime_trimsUserInput() {
        val millis = parseEditableTime(" 2026-07-08 23:15 ", zone)

        assertNotNull(millis)
        assertEquals("2026-07-08 23:15", millis!!.formatEditableTime(zone))
    }

    @Test
    fun editableTime_acceptsFullWidthUserInput() {
        val millis = parseEditableTime("２０２６－０７－０８　２３：１５", zone)

        assertNotNull(millis)
        assertEquals("2026-07-08 23:15", millis!!.formatEditableTime(zone))
    }

    @Test
    fun editableTime_acceptsChineseDateTimeMarkers() {
        val millis = parseEditableTime("2026年07月08日 23时15分", zone)

        assertNotNull(millis)
        assertEquals("2026-07-08 23:15", millis!!.formatEditableTime(zone))
    }

    @Test
    fun editableTime_acceptsFullWidthChineseDateTimeMarkers() {
        val millis = parseEditableTime("２０２６年０７月０８日　２３时１５分", zone)

        assertNotNull(millis)
        assertEquals("2026-07-08 23:15", millis!!.formatEditableTime(zone))
    }

    @Test
    fun editableTime_acceptsSlashDateSeparators() {
        val asciiSlash = parseEditableTime("2026/07/08 23:15", zone)
        val fullWidthSlash = parseEditableTime("２０２６／０７／０８ ２３：１５", zone)

        assertNotNull(asciiSlash)
        assertNotNull(fullWidthSlash)
        assertEquals("2026-07-08 23:15", asciiSlash!!.formatEditableTime(zone))
        assertEquals("2026-07-08 23:15", fullWidthSlash!!.formatEditableTime(zone))
    }

    @Test
    fun editableTime_rejectsInvalidInput() {
        assertNull(parseEditableTime("not time", zone))
    }

    @Test
    fun exportFileTime_usesCompactLocalTimestamp() {
        val millis = parseEditableTime("2026-07-08 23:15", zone)

        assertNotNull(millis)
        assertEquals("20260708-231500", millis!!.formatExportFileTime(zone))
    }

    @Test
    fun ledgerTime_usesMonthDayAndMinute() {
        val millis = parseEditableTime("2026-07-08 23:15", zone)

        assertNotNull(millis)
        assertEquals("07-08 23:15", millis!!.formatLedgerTime(zone))
    }
}
