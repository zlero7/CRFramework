package io.zlero.cRFramework.command.annotation

/**
 * 함수에 붙이면 서버 명령어로 자동 등록
 *
 * 사용법:
 *   @Command("nick", description = "닉네임 변경", permission = "cr.nick")
 *   fun onNick(ctx: CommandContext) { ... }
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
    val name: String,
    val description: String = "",
    val permission: String = "",
    val aliases: Array<String> = [],
    val usage: String = ""
)