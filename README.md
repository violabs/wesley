# Wesley

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

Wesley is a lightweight testing framework that emulates some functionality of [Spock](http://spockframework.org/). 
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
    testImplementation("com.github.violabs:wesley:1.0.0")
}

```


# Usage
To use Wesley, create a new test class and extend it with `Wesly()`.

The functions defined run in a specific order, but where you defined them doesn't matter. You can have
`whenever()` before or after `expect` and so on.

##Defining Tests

To define a test, create a new function in your test class and call the `test()` function. Within the `test()` function, you can define the behavior of the test using a lambda expression.

Here's an example test:

```kotlin
fun `test something`() = test<String> {
    expect { "Hello, world!" }

    whenever { "Hello, world!" }

    thenEquals("The strings should be equal") { expected, actual ->
        assertEquals(expected, actual)
    }
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

## Mocking
Wesley supports mocking using the `verifyMock()` function. This function takes a mock object, 
a return value, and a lambda expression that defines the behavior of the mock.

To set up a mock object, you can define a function using the `setupMocks()` function. 
Within the `setupMocks()` function, you can create mock objects and define their behavior
using the `verifyMock()` function. Here's an example:

```kotlin
class MyTest : Wesley() {
    val mockObject = mock<MyClass>()
    val focus = Focus(mockObject)
    
    
    fun `test something`() = test<Unit> {
        setupMocks {
            verifyMock(mockObject, returnedItem = "Hello, world!") { mock ->
                mock.someMethod()
            }
        }

        expect { "Hello, world!" }

        whenever { focus.doTheThing() }
    }
}
```

In this example, we create a mock object using the `mock()` function from *Wesley*, and define its 
behavior using the `verifyMock()` function. We then use the `expect()` function to define the expected output, 
and the `whenever()` function to define the actual output, which calls the mocked someMethod() function.

If you don't explicitly define the behavior of a mocked object using `verifyMock()`, 
the default behavior is to return `null` for any method call.

# Contribution Guidelines
Contributions to Wesley are welcome! If you'd like to report a bug, request a feature, or submit a pull request, please see our contribution guidelines.

# License
This project is licensed under the MIT License. See the LICENSE file for details.

# Acknowledgements
Wesley was inspired by the Spock Framework, which was created by Peter Niederwieser and released under the Apache License, Version 2.0. We thank Peter for his contributions to the testing community.

# Contact
If you have any questions or comments about Wesley, please contact us at violabs.software@gmail.com.