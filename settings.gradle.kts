operator fun Settings.getValue(any: Any?, property: kotlin.reflect.KProperty<*>): String =
        this.javaClass.getMethod("getProperty", String::class.java).invoke(this, property.name) as String

val repoUrl: String by settings

pluginManagement {
    repositories {
        maven("$repoUrl/groups/public")
    }
}

rootProject.name = "gradle-webpack-plugin"
