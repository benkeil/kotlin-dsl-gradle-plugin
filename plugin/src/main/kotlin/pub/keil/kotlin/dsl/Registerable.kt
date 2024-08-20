package pub.keil.kotlin.dsl

import org.gradle.api.Project

interface Registerable<E> {
  fun register(project: Project, pluginExtension: E)
}
