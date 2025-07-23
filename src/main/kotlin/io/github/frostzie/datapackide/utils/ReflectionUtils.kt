package io.github.frostzie.datapackide.utils
/*
import io.github.frostzie.datapackide.DataPackIDE
import java.lang.reflect.Field

object ReflectionUtils {
    /**
     * Tries to find a field in a class by checking a list of possible names.
     * This is useful for dealing with obfuscated environments.
     * @param clazz The class to search in.
     * @param potentialNames A list of possible names for the field.
     * @return The found Field, or null if it could not be found.
     */
    fun findField(clazz: Class<*>, vararg potentialNames: String): Field? {
        for (name in potentialNames) {
            try {
                val field = clazz.getDeclaredField(name)
                field.isAccessible = true // Make the protected/private field accessible
                return field
            } catch (e: NoSuchFieldException) {
                // This is expected if the name is not the correct one, so we continue to the next.
            }
        }
        DataPackIDE.LOGGER.error("Could not find field with any of the names ${potentialNames.joinToString()} in class ${clazz.name}")
        return null
    }
}
 */