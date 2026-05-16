package io.zlero.cRFramework.yaml.annotation

/**
 * YAML 설정 파일 클래스 마커 — DI 컨테이너에서 설정으로 인식
 *
 * 사용법:
 *   @Configuration("config.yml")
 *   class MainConfig(plugin: JavaPlugin) : CRYamlConfiguration(plugin, "config.yml") {
 *       val prefix get() = colorString("prefix", "&8[&bServer&8]")
 *   }
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Configuration(val fileName: String = "config.yml")