package io.violabs.wesly


import io.mockk.confirmVerified
import io.mockk.mockkClass
import io.mockk.verify
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import io.mockk.every as mockkEvery
import kotlin.test.assertEquals as equals

abstract class Wesley {
    private val mocks: MutableList<Any> = mutableListOf()
    private val mockCalls = mutableListOf<MockTask<*>>()

    inline fun <reified T : Any> mock(): T = mockkClass(type = T::class)

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

    class MockTask<T : Any>(
        val mockCall: () -> T?,
        var returnedItem: T? = null,
        var throwable: Throwable? = null
    ) {

        infix fun returns(returnItem: T) {
            returnedItem = returnItem
        }

        infix fun throws(throwable: Throwable) {
            this.throwable = throwable
        }
    }

    fun <T : Any> every(mockCall: () -> T?): MockTask<T> {
        val task = MockTask(mockCall)
        mockCalls.add(task)
        return task
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

        mockCalls.forEach {
            verify { it.mockCall() }
        }

        if (mocks.isEmpty()) return

        confirmVerified(*mocks.toTypedArray())
        cleanup()
    }

    private fun cleanup() {
        mockCalls.clear()
    }

    inner class CrushIt<T> {
        private var expected: T? = null
        private var actual: T? = null
        internal var setupCall: () -> Unit = {}
        internal var expectCall: () -> Unit = {}
        internal var mockSetupCall: () -> Unit = this::processMocks
        var wheneverCall: () -> Unit = {}
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
            mockSetupCall = {
                mockSetupFn()
                processMocks()
            }
        }

        fun whenever(whenFn: () -> T?) {
            wheneverCall = { actual = whenFn() }
        }

        inline fun <reified U : Throwable> wheneverThrows(crossinline whenFn: () -> T) {
            wheneverCall = { assertFailsWith<U> { whenFn() } }
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

        fun teardown(tearDownFn: () -> Unit) {
            this.tearDownCall = tearDownFn
        }

        private fun processMocks() {
            val (throwables, runnables) = mockCalls.partition { it.throwable != null }

            throwables.onEach { mockkEvery { it.mockCall.invoke() } throws it.throwable!! }.count().also {
                println("Throwable amount: $it")
            }

            val (callOnly, returnable) = runnables.partition { it.returnedItem == null }

            callOnly.onEach { mockkEvery { it.mockCall.invoke() } }.count().also {
                println("Null amount: $it")
            }

            returnable.onEach { mockkEvery { it.mockCall.invoke() } returns it.returnedItem!! }.count().also {
                println("Returnable amount: $it")
            }
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