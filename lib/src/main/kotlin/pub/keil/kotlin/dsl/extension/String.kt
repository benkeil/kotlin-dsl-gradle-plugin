package pub.keil.kotlin.dsl.extension

fun String.capitalize(): String =
    this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
