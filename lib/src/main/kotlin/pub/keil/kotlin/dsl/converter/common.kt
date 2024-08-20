package pub.keil.kotlin.dsl.converter

fun escapeDoc(doc: String): String = doc.replace("/", "{@literal /}").replace("%", "%%")
