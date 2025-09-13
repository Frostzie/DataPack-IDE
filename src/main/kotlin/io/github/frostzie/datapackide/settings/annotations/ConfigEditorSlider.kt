package io.github.frostzie.datapackide.settings.annotations

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigEditorSlider(
    val minValue: Double = 0.0,
    val maxValue: Double = 100.0,
    val stepSize: Double = 1.0
)