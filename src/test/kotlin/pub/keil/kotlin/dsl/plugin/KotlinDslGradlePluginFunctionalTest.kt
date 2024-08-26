package pub.keil.kotlin.dsl.plugin

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import java.io.File
import kotlin.io.path.createTempDirectory
import org.gradle.testkit.runner.GradleRunner

class KotlinDslGradlePluginFunctionalTest :
    FunSpec({
      val projectDir: File by lazy { createTempDirectory().toFile() }

      val buildFile by lazy { projectDir.resolve("build.gradle.kts") }
      val settingsFile by lazy { projectDir.resolve("settings.gradle.kts") }
      val jsonSchema by lazy { projectDir.resolve("json-schema.json") }
      val openApiSchema by lazy { projectDir.resolve("openapi-schema.json") }

      afterProject { projectDir.deleteRecursively() }

      test("can run task") {
        settingsFile.writeText("")
        jsonSchema.writeText(getResource("task-definition.json"))
        openApiSchema.writeText(getResource("kubernetes.json"))
        buildFile.writeText(
            """
            plugins {
                kotlin("jvm") version "2.0.10"
                id("io.github.benkeil.kotlin-dsl-gradle-plugin")
            }
            
            kotlinDsl {
                jsonSchema {
                    path = file("json-schema.json")
                    canonicalName = "pub.keil.kotlin.dsl.test.TaskDefinition"
                    aliases = mapOf("foo" to "bar")
                    propertiesToSkip = listOf("baz")
                }
                openApiSchema {
                    path = file("openapi-schema.json")
                    aliases = mapOf("foo" to "bar")
                    propertiesToSkip = listOf("baz")
                }
            }
            """
                .trimIndent())

        // Run the build
        val result =
            GradleRunner.create()
                .withProjectDir(projectDir)
                .forwardOutput()
                .withPluginClasspath()
                .withArguments("generateDslClasses")
                .build()

        // Verify the result
        println(result.output)
        val files =
            projectDir
                .resolve("build/generated/sources/kotlin-dsl")
                .walk()
                .toList()
                .filter { it.isFile }
                .map { it.name }
        files shouldContainExactlyInAnyOrder listOf("TaskDefinition.kt", "AuditAnnotation.kt")
      }
    })

internal fun getResource(path: String) =
    object {}.javaClass.classLoader.getResource(path)!!.readText()
