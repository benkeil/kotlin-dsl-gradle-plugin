package pub.keil.kotlin.dsl.utils

import kotlin.io.path.toPath

internal fun getResourceAsPath(path: String) =
    object {}.javaClass.classLoader.getResource(path)!!.toURI().toPath()

internal fun getResource(path: String) =
    object {}.javaClass.classLoader.getResource(path)!!.readText()
