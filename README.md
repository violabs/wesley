# Wesley

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

Wesley is a lightweight testing framework that emulates some functionality of [Spock](http://spockframework.org/)
 with the functionality of [Mockk](https://mockk.io). 
It provides an expressive and readable syntax for writing tests, and supports both unit and integration testing.

## Installation

To use Wesley in your project, add the following dependencies to your Gradle build file:

```kotlin
repository {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    // other dependencies
    testImplementation("com.github.violabs:wesley:{version}")
}

```


# Usage
To use Wesley, create a new test class and extend it with `Wesly()`.

The functions defined run in a specific order, but where you defined them doesn't matter. You can have
`whenever()` before or after `expect` and so on.

## Defining Tests

To define a test, create a new function in your test class and call the `test()` function. 
Within the `test()` function, you can define the behavior of the test using a lambda expression.

Here's an example test:

```kotlin
fun `test something`() = test<String> {
    setup { println("Optional START") }
    
    expect { "Hello, world!" }

    whenever { "Hello, world!" }

    thenEquals("The strings should be equal") { expected, actual ->
        println("do other stuff $expected $actual")
    }

    teardown { println("Optional END") }
}
```

In this test, we define the expected output using the `expect()` function, 
and the actual output using the `whenever()` function. We then use the `thenEquals()` function 
to check that the expected and actual values are equal.

If you don't explicitly use `thenEquals()`, Wesley will automatically check that the results of `expect()` and 
`whenever()` are equal. Here's an example:

```kotlin
fun `test something`() = test<String> {
    expect { "Hello, world!" }

    whenever { "Hello, world!" }
}
```

In this test, Wesley will automatically check that the expected and actual values are equal. 
If they are not equal, the test will fail.

### Testing Exceptions

There is a simple test function that wraps the main content in an exception assertion. Here is an example:

```kotlin
fun `test something throws exception`() = testThrows<IndexOutOfBoundsException> {
    focus.doSomething()
}
```

## Mocking
Wesley supports mocking similar to Mockk. Under the hood it uses Mockk, but it provides a way
to not have to call verify on every mock object. Wesley will verify all mocks at the end of the test
and that there are no more interactions with the mock objects.

It is limited compared to Mockk, but you are able to inter mix the library.

```kotlin
class MyTest : Wesley() {
    val mockObject = mock<MyClass>()
    val focus = Focus(mockObject)
    
    
    fun `test something`() = test<String> {
        every { mockObject.someMethod() } returns "Hello, world!"
        
        // or you can let the build do the work with this nested function
        setupMocks {
            every { mockObject.someMethod() } returns "Hello, world!"
        }

        expect { "Hello, world!" }

        whenever { focus.doTheThing() }
    }
}
```

### Throwing Exceptions

You can provide an expected thrown item by using the throws mock function.

```kotlin
class MyTest : Wesley() {
    val mockObject = mock<MyClass>()
    val focus = Focus(mockObject)
    
    fun `test something`() = test<String> {
        every { mockObject.someMethod() } throws Exception("Error")

        // or you can let the build do the work with this nested function
        setupMocks {
            every { mockObject.someMethod() } throws Exception("Error")
        }
        
        expect { "Hello, world!" }
        
        whenever { focus.doTheThing() }
    }
}
```

# Future Features

- [ ] WebFlux support (blocking) : This will help with converting old projects into newer forms.
- [ ] WebFlux support (non-blocking) : Utilize Coroutines to help with testing.
- [ ] Tests around the library itself - but how to catch test throws

# Contribution Guidelines
Contributions to Wesley are welcome! If you'd like to report a bug, request a feature, or submit a pull request, please see our contribution guidelines.

# License
This project is licensed under the MIT License. See the LICENSE file for details.

# Acknowledgements
Wesley was inspired by the Spock Framework, which was created by Peter Niederwieser and released under the Apache License, Version 2.0. We thank Peter for his contributions to the testing community.

# Contact
If you have any questions or comments about Wesley, please contact us at violabs.software@gmail.com.