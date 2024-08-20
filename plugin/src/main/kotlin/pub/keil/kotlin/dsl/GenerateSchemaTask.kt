package pub.keil.kotlin.dsl

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import pub.keil.kotlin.dsl.converter.Converter
import pub.keil.kotlin.dsl.reader.JsonSchemaReader
import pub.keil.kotlin.dsl.reader.OpenApiReader

open class GenerateSchemaTask
@Inject
constructor(
    private val pluginExtension: KotlinDslGradlePluginExtension,
) : DefaultTask() {

  init {
    group = KotlinDslGradlePlugin.GROUP_NAME
    description = "Generates classes for a custom DSL"
  }

  @TaskAction
  fun generate() {
    val config = pluginExtension.build()
    val converter =
        Class.forName(config.converter).getDeclaredConstructor().newInstance() as Converter
    config.jsonSchemas.forEach { schema ->
      println("Generating JsonSchema classes for ${schema.path}")
      JsonSchemaReader(aliases = schema.aliases, propertiesToSkip = schema.propertiesToSkip)
          .read(schema.canonicalName, schema.path)
          .map(converter::convert)
          .forEach { it.writeTo(config.outputDirectory) }
      println("Classes generated at ${config.outputDirectory}")
    }

    config.openApiSchemas.forEach { schema ->
      println("Generating OpenAPI classes for ${schema.path}")
      val jsonSchemaReader =
          JsonSchemaReader(aliases = schema.aliases, propertiesToSkip = schema.propertiesToSkip)
      OpenApiReader(jsonSchemaReader).read(schema.path).map(converter::convert).forEach {
        it.writeTo(config.outputDirectory)
      }
      println("Classes generated at ${config.outputDirectory}")
    }
  }

  companion object : Registerable<KotlinDslGradlePluginExtension> {
    override fun register(project: Project, pluginExtension: KotlinDslGradlePluginExtension) {
      project.tasks
          .register("generateDslClasses", GenerateSchemaTask::class, pluginExtension)
          .configure {
            val compileJavaTask =
                project.tasks.findByName("compileKotlin") ?: error("Plugin `jvm` must be applied")
            dependsOn(compileJavaTask)
          }

      val config = pluginExtension.build()
      val sourceSets = project.properties["sourceSets"] as SourceSetContainer
      sourceSets.named("main") {
        java.srcDirs(config.outputDirectory)
        println("added ${config.outputDirectory} to sourceSets")
      }
    }
  }
}
