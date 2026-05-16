package io.zlero.cRFramework.database.annotation

/**
 * Exposed Table 오브젝트에 붙이면 DatabaseModule이 서버 시작 시 자동 생성
 *
 * 사용법:
 *   @Table
 *   object PlayerTable : IntIdTable("players") { ... }
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Table

/** DI 컨테이너에 Repository로 등록 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Repository