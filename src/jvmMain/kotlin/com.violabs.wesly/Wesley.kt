package com.violabs.wesly

import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
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

    fun <MOCK> verifyThrows(mock: MOCK, throwable: Throwable, times: Int = 1, mockCall: (MOCK) -> Unit) {
        whenever(mockCall(mock)).thenThrow(throwable)
        verifiable.add { mockCall(verify(mock, times(times)))}
    }

    fun <T> test(runnable: CrushIt<T>.() -> Unit) {
        val spec = CrushIt<T>()

        runnable(spec)

        spec.setupCall()
        spec.expectCall()
        spec.mockSetupCall()
        spec.wheneverCall()
        spec.thenCall()
        spec.tearDownCall()

        verifiable.forEach { it.invoke() }

        if (mocks.isEmpty()) return

        verifyNoMoreInteractions(*mocks.toTypedArray())
        cleanup()
    }

    inline fun <reified E : Exception> testThrows(runnable: () -> Unit) {
        assertFailsWith(E::class, runnable)
    }

    private fun cleanup() {
        verifiable.clear()
        mockCalls.clear()
    }

    class CrushIt<T> {
        private var expected: T? = null
        private var actual: T? = null
        internal var setupCall: () -> Unit = {}
        internal var expectCall: () -> Unit = {}
        internal var mockSetupCall: () -> Unit = {}
        internal var wheneverCall: () -> Unit = {}
        internal var thenCall: () -> Unit = this::defaultThenEquals
        internal var tearDownCall: () -> Unit = {}
        private var expectedExists = false

        fun setup(setupFn: () -> Unit) {
            this.setupCall = setupFn
        }

        fun expect(givenFn: () -> T?) {
            if (expectedExists) throw Exception("Can only have expect or given and not both!!")
            expectedExists = true
            expectCall = { expected = givenFn() }
        }

        fun expectNull() = expect { null }

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

        fun tearDown(tearDownFn: () -> Unit) {
            this.tearDownCall = tearDownFn
        }
    }
}

fun Wesley.CrushIt<Boolean>.expectTrue() = expect { true }
fun Wesley.CrushIt<Boolean>.expectFalse() = expect { false }

fun Wesley.CrushIt<String>.thenContains() = then { e, a ->
    val existingE: String = e ?: return@then assertTrue(false)
    val existingA: String = a ?: return@then assertTrue(false)
    assertContains(existingE, existingA)
}

fun Wesley.CrushIt<String>.expectContains(givenFn: () -> String?) {
    this.expect(givenFn)

    thenContains()
}