package pub.keil.kotlin.dsl

import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinDslGradlePlugin : Plugin<Project> {
  companion object {
    const val GROUP_NAME = "Kotlin DSL"
    const val EXTENSION_NAME = "kotlinDsl"
  }

  override fun apply(project: Project) {
    val extension =
        project.extensions.create(
            EXTENSION_NAME, KotlinDslGradlePluginExtension::class.java, project)

    GenerateSchemaTask.register(project, extension)
  }
}

fun Project.kotlinDsl(
    block: KotlinDslGradlePluginExtension.() -> Unit
): KotlinDslGradlePluginExtension {
  val extension =
      extensions.getByName(KotlinDslGradlePlugin.EXTENSION_NAME) as? KotlinDslGradlePluginExtension
          ?: throw IllegalStateException(
              "${KotlinDslGradlePlugin.EXTENSION_NAME} is not of the correct type")
  return extension.apply(block)
}
