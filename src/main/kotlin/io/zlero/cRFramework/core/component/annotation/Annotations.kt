package io.zlero.cRFramework.core.component.annotation

/** 컴포넌트로 등록 — DI 컨테이너가 인스턴스를 생성·관리 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Component

/** 싱글턴 컴포넌트 — 한 번만 생성되어 재사용 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Singleton

/** 모듈 컴포넌트 — @Setup / @Teardown 생명주기 지원 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Module

/** 플러그인 활성화(onEnable) 시 호출 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Setup

/** 플러그인 비활성화(onDisable) 시 호출 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Teardown