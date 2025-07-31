package com.yandex.practicum.middle_homework_5.gradle_plugins

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

abstract class FindUntranslatedStringsTask : DefaultTask() {

    @TaskAction
    fun findUntranslatedStrings() {
        val resDir = File(project.projectDir, "src/main/res")
        val strings = File(resDir, "values/strings.xml")

        /**
         * Получаем все строки из values/strings.xml для локализации по умолчанию
         * Для уменьшения дублирующего кода - оздан метод getStringsFromVaalues
         */
        val defaultStrings = getStringsFromXmlFile(strings)

        /**
         * Получаем все имеющиеся каталоги values-*
         */
        val otherLocalizations = resDir.listFiles { file ->
            file.isDirectory && file.name.startsWith("values-")
        } ?: emptyArray()

        /**
         * Создаём Map, в которой будут находится нелокализованные строки
         */
        val missingStrings = mutableMapOf<String, MutableList<String>>()

        /**
         * Для каждой найденной локализации проверяем недостощий перевод
         */
        otherLocalizations.forEach { localeDir ->
            val localeName = localeDir.name.removePrefix("values-")
            val localizationStringsFile = File(localeDir, "strings.xml")


            /**
             * При отсутствии файла strings.xml для локализации записываем
             * каждую строку в непереведённые/пропавшие
             */
            if (!localizationStringsFile.exists()) {
                defaultStrings.keys.forEach { stringName ->
                    missingStrings.getOrPut(stringName) { mutableListOf() }.add(localeName)
                }
            } else {
                /**
                 * Если файл strings.xml есть - получаем все строки, которые он содержит в виде Map
                 * Далее, проходимся по дефолтным строкам, чтобы понять,
                 * содержит ли локализация тот или иной ключ.
                 * Если ключ не найден - записываем в непереведённые/пропавшие
                 */
                val localizationStrings = getStringsFromXmlFile(localizationStringsFile)

                defaultStrings.keys.forEach { stringName ->
                    if (!localizationStrings.containsKey(stringName)) {
                        missingStrings.getOrPut(stringName) { mutableListOf() }.add(localeName)
                    }
                }
            }
        }

        /**
         * При нахождении непереведённых строк - выбрасываем GradleException
         */
        if (missingStrings.isNotEmpty()) {
            val stringBuilderErrorText =
                StringBuilder("Missing translations").append(System.lineSeparator())
            missingStrings.forEach { (missingString, missingLocalizations) ->
                stringBuilderErrorText
                    .append("=== $missingString ===")
                    .append(System.lineSeparator())
                    .append(missingLocalizations.joinToString(separator = System.lineSeparator()))
                    .append(System.lineSeparator())

            }
            throw GradleException(stringBuilderErrorText.toString())
        }
    }

    /**
     * Метод для получения всех имеющихся строк в передаваемом файле strings.xml
     */
    private fun getStringsFromXmlFile(file: File): Map<String, String> {
        /**
         * Проверка на существования передаваемого файла
         */
        if (!file.exists()) return emptyMap()

        val stringsFromXml = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(file)
            .getElementsByTagName("string")

        return stringsFromXml.let { nodeList ->
            (0 until nodeList.length).associate { i ->
                val node = nodeList.item(i)
                val name = node.attributes?.getNamedItem("name")?.nodeValue ?: ""
                name to node.textContent
            }
        }
    }
}

class FindUntranslatedStringsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("untranslatedStrings", FindUntranslatedStringsTask::class.java)
    }
}
