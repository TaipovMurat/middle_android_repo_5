plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(gradleApi())
}
allprojects {
    repositories {
        mavenCentral()
    }
}

gradlePlugin {
    plugins {
        create("untranslate_strings") {
            id = "com.yandex.practicum.middle_homework_5.gradle_plugins.untranslated_strings"
            implementationClass =
                "com.yandex.practicum.middle_homework_5.gradle_plugins.FindUntranslatedStringsPlugin"
        }
    }
}