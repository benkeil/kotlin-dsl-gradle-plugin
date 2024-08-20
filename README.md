# Kotlin DSL Gradle Plugin

This plugin let you create special Kotlin classes from JsonSchemas and OpenAPI specifications that can be used to create
a custom DSL.

## Usage

__build.gradle.kts__

```kotlin
plugins {
    kotlin("jvm") version "2.0.10"
    id("io.github.benkei.kotlin-dsl-gradle-plugin") version "1.0.0"
}

kotlinDsl {
    jsonSchema {
        path = file("json-schema.json")
        canonicalName = "pub.keil.kotlin.dsl.test.TaskDefinition"
        aliases = mapOf("a.original.package.Class" to "your.dsl.package.Class")
        propertiesToSkip = listOf("status")
    }
    jsonSchema {
        path = file("json-schema-2.json")
        canonicalName = "pub.keil.kotlin.dsl.test.ContainerDefinition"
        aliases = mapOf("a.original.package.Class" to "your.dsl.package.Class")
        propertiesToSkip = listOf("status")
    }
    openApiSchema {
        path = file("kubernetes.json")
        aliases = mapOf("a.original.package.Class" to "your.dsl.package.Class")
        propertiesToSkip = listOf("apiVersion", "kind", "status")
    }
}
```

The generated classes are automatically added to the source set `main` and can be used in your project.

### kotlinDsl

| Property          | Type                  | Required | Description                                           | Default                        |
|-------------------|-----------------------|----------|-------------------------------------------------------|--------------------------------|
| `outputDirectory` | `Provider<Directory>` | No       | The directory where the generated classes are stored. | `generated/sources/kotlin-dsl` |

### jsonSchema

| Property           | Type                  | Required | Description                                            |
|--------------------|-----------------------|----------|--------------------------------------------------------|
| `path`             | `File`                | Yes      | The path to the JsonSchema file.                       |
| `canonicalName`    | `String`              | Yes      | The canonical name of the generated class.             |
| `aliases`          | `Map<String, String>` | No       | A map of aliases to replace the original package name. |
| `propertiesToSkip` | `List<String>`        | No       | A list of properties that are not rendered.            |

### openApiSchema

| Property           | Type                  | Required | Description                                            |
|--------------------|-----------------------|----------|--------------------------------------------------------|
| `path`             | `File`                | Yes      | The path to the JsonSchema file.                       |
| `aliases`          | `Map<String, String>` | No       | A map of aliases to replace the original package name. |
| `propertiesToSkip` | `List<String>`        | No       | A list of properties that are not rendered.            |

## Properties

#### canonicalName

The canonical name of the generated class. E.g. `pub.keil.kotlin.dsl.test.TaskDefinition`.

#### aliases

A map of aliases to replace the original package name. E.g. `a.original.package.Class` to `your.dsl.package.Class`.
As an example, in the kubernetes OpenAPI specification is a field with the type `IntOrString` which is not a valid
Kotlin type. You can set an alias to replace the type with a valid Kotlin type (e.g. just `String` or your own
implementation that handles this case).

#### propertiesToSkip

A list of properties that are not rendered. E.g. `status`.
As an example, in the kubernetes OpenAPI specification is a field with the name `status` which is a readonly property,
and you may don't want to include it into your DSL.
