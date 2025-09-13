package io.github.frostzie.datapackide.settings.annotations

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigOption(
    val name: String,
    val desc: String = ""
)