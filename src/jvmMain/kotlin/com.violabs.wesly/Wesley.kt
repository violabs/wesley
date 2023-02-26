package com.violabs.wesly

import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals as equals

abstract class Wesley {
    var mocks = listOf<Any>()
    private val mockCalls = mutableListOf<() -> Unit>()
    private val verifiable = mutableListOf<() -> Unit>()

    fun <U> assertEquals(expected: U, actual: U, message: String? = null) {
        equals(
            expected,
            actual,
            """
        $message
        EXPECT: $expected,
        ACTUAL: $actual
      """.trimIndent()
        )
    }

    fun <MOCK, R> verifyMock(mock: MOCK, returnItem: R, times: Int = 1, mockCall: (MOCK) -> R) {
        whenever(mockCall(mock)).thenReturn(returnItem)
        verifiable.add { mockCall(verify(mock, times(times))) }
    }

    fun <T> test(runnable: CrushIt<T>.() -> Unit) {
        val spec = CrushIt<T>()

        runnable(spec)

        spec.expectCall()
        spec.mockSetupCall()
        spec.wheneverCall()
        spec.thenCall()

        verifiable.forEach { it.invoke() }

        if (mocks.isEmpty()) return

        verifyNoMoreInteractions(*mocks.toTypedArray())
        cleanup()
    }

    private fun cleanup() {
        verifiable.clear()
        mockCalls.clear()
    }

    class CrushIt<T> {
        var expected: T? = null
        var actual: T? = null
        var expectCall: () -> Unit = {}
        var mockSetupCall: () -> Unit = {}
        var wheneverCall: () -> Unit = {}
        var thenCall: () -> Unit = this::defaultThenEquals
        var expectedExists = false

        fun expect(givenFn: () -> T?) {
            if (expectedExists) throw Exception("Can only have expect or given and not both!!")
            expectedExists = true
            expectCall = { expected = givenFn() }
        }

        fun given(givenFn: () -> T?) {
            if (expectedExists) throw Exception("Can only have expect or given and not both!!")
            expectedExists = true
            expectCall = { expected = givenFn() }
        }

        fun setupMocks(mockSetupFn: () -> Unit) {
            mockSetupCall = mockSetupFn
        }

        fun whenever(whenFn: () -> T?) {
            wheneverCall = { actual = whenFn() }
        }

        fun then(thenFn: (T?, T?) -> Unit) {
            thenCall = {
                thenFn(expected, actual)
            }
        }

        fun thenEquals(message: String, runnable: (() -> Unit)? = null) {
            thenCall = {
                runnable?.invoke()

                assert(expected == actual) {
                    println("FAILED $message")
                    println("EXPECT: $expected")
                    println("ACTUAL: $actual")
                }
            }
        }

        fun defaultThenEquals() {
            assert(expected == actual) {
                println("EXPECT: $expected")
                println("ACTUAL: $actual")
            }
        }
    }
}