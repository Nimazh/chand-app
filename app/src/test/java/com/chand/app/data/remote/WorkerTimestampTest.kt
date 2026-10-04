package com.chand.app.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class WorkerTimestampTest {
    @Test
    fun parsesWorkerIsoTimestampWithMilliseconds() {
        assertEquals(123L, parseWorkerTimestamp("1970-01-01T00:00:00.123Z"))
    }

    @Test
    fun parsesWorkerIsoTimestampWithoutMilliseconds() {
        assertEquals(1000L, parseWorkerTimestamp("1970-01-01T00:00:01Z"))
    }

    @Test
    fun rejectsNonUtcOrMalformedTimestamp() {
        assertThrows(IllegalArgumentException::class.java) {
            parseWorkerTimestamp("1970-01-01T00:00:01+03:30")
        }
        assertThrows(IllegalArgumentException::class.java) {
            parseWorkerTimestamp("1970-02-30T00:00:01Z")
        }
    }
}
