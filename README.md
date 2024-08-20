# Kotlin DSL Gradle Plugin

This plugin let you create special Kotlin classes from JsonSchemas and OpenAPI specifications that can be used to create
a custom DSL.

## Example

The specification

```json
{
  "definitions": {
    "io.k8s.api.admissionregistration.v1.AuditAnnotation": {
      "description": "AuditAnnotation describes how to produce an audit annotation for an API request.",
      "properties": {
        "key": {
          "description": "key specifies the audit annotation key. The audit annotation keys of a ValidatingAdmissionPolicy must be unique. The key must be a qualified name ([A-Za-z0-9][-A-Za-z0-9_.]*) no more than 63 bytes in length.\n\nThe key is combined with the resource name of the ValidatingAdmissionPolicy to construct an audit annotation key: \"{ValidatingAdmissionPolicy name}/{key}\".\n\nIf an admission webhook uses the same resource name as this ValidatingAdmissionPolicy and the same audit annotation key, the annotation key will be identical. In this case, the first annotation written with the key will be included in the audit event and all subsequent annotations with the same key will be discarded.\n\nRequired.",
          "type": "string"
        },
        "valueExpression": {
          "description": "valueExpression represents the expression which is evaluated by CEL to produce an audit annotation value. The expression must evaluate to either a string or null value. If the expression evaluates to a string, the audit annotation is included with the string value. If the expression evaluates to null or empty string the audit annotation will be omitted. The valueExpression may be no longer than 5kb in length. If the result of the valueExpression is more than 10kb in length, it will be truncated to 10kb.\n\nIf multiple ValidatingAdmissionPolicyBinding resources match an API request, then the valueExpression will be evaluated for each binding. All unique values produced by the valueExpressions will be joined together in a comma-separated list.\n\nRequired.",
          "type": "string"
        }
      },
      "required": [
        "key",
        "valueExpression"
      ],
      "type": "object"
    }
  }
}
```

will generate

```kotlin
package io.k8s.api.admissionregistration.v1

import kotlin.String
import kotlin.Unit

/** AuditAnnotation describes how to produce an audit annotation for an API request. */
public class AuditAnnotation {
    /**
     * key specifies the audit annotation key. The audit annotation keys of a
     * ValidatingAdmissionPolicy must be unique. The key must be a qualified name
     * ([A-Za-z0-9][-A-Za-z0-9_.]*) no more than 63 bytes in length.
     *
     * The key is combined with the resource name of the ValidatingAdmissionPolicy to construct an
     * audit annotation key: "{ValidatingAdmissionPolicy name}{@literal /}{key}".
     *
     * If an admission webhook uses the same resource name as this ValidatingAdmissionPolicy and the
     * same audit annotation key, the annotation key will be identical. In this case, the first
     * annotation written with the key will be included in the audit event and all subsequent
     * annotations with the same key will be discarded.
     *
     * Required.
     */
    public var key: String? = null

    /**
     * valueExpression represents the expression which is evaluated by CEL to produce an audit
     * annotation value. The expression must evaluate to either a string or null value. If the
     * expression evaluates to a string, the audit annotation is included with the string value. If
     * the expression evaluates to null or empty string the audit annotation will be omitted. The
     * valueExpression may be no longer than 5kb in length. If the result of the valueExpression is
     * more than 10kb in length, it will be truncated to 10kb.
     *
     * If multiple ValidatingAdmissionPolicyBinding resources match an API request, then the
     * valueExpression will be evaluated for each binding. All unique values produced by the
     * valueExpressions will be joined together in a comma-separated list.
     *
     * Required.
     */
    public var valueExpression: String? = null

    public companion object {
        public operator fun invoke(block: AuditAnnotation.() -> Unit): AuditAnnotation =
            AuditAnnotation().apply(block)
    }
}
```

which can be used as

```kotlin
val auditAnnotation = AuditAnnotation {
    key = "key"
    valueExpression = "valueExpression"
}
```

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
