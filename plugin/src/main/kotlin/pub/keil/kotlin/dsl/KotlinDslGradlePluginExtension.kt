package pub.keil.kotlin.dsl

import java.io.File
import java.nio.file.Path
import kotlin.properties.Delegates
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import pub.keil.kotlin.dsl.converter.AllOptionalConverter

data class KotlinDslGradlePluginConfig(
    var outputDirectory: Path,
    var converter: String,
    var jsonSchemas: List<JsonSchema>,
    var openApiSchemas: List<OpenApiSchema>,
)

open class KotlinDslGradlePluginExtension(project: Project) {
  private var jsonSchemas: MutableList<JsonSchema> = mutableListOf()
  private var openApiSchemas: MutableList<OpenApiSchema> = mutableListOf()

  var outputDirectory: Provider<Directory> =
      project.layout.buildDirectory.dir("generated/sources/kotlin-dsl")

  var converter: String = AllOptionalConverter::class.qualifiedName!!

  fun jsonSchema(block: JsonSchema.() -> Unit) {
    jsonSchemas += JsonSchema().apply(block)
  }

  fun openApiSchema(block: OpenApiSchema.() -> Unit) {
    openApiSchemas += OpenApiSchema().apply(block)
  }

  internal fun build() =
      KotlinDslGradlePluginConfig(
          outputDirectory = outputDirectory.get().asFile.toPath(),
          converter = converter,
          jsonSchemas = jsonSchemas,
          openApiSchemas = openApiSchemas,
      )
}

class JsonSchema {
  var path: File by Delegates.notNull()
  var canonicalName: String by Delegates.notNull()
  var aliases: Map<String, String> = mapOf()
  var propertiesToSkip: List<String> = listOf()
}

class OpenApiSchema {
  var path: File by Delegates.notNull()
  var aliases: Map<String, String> = mapOf()
  var propertiesToSkip: List<String> = listOf()
}
