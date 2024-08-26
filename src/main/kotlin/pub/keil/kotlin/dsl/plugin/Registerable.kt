package pub.keil.kotlin.dsl.plugin

import org.gradle.api.Project

interface Registerable<E> {
  fun register(project: Project, pluginExtension: E)
}
