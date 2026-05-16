package io.zlero.cRFramework.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * 명령어 인자를 타입 세이프하게 파싱하는 컨텍스트
 *
 * 사용법:
 *   fun onNick(ctx: CommandContext) {
 *       val player = ctx.player
 *       val nick   = ctx.string(0)
 *       val amount = ctx.intOrNull(1) ?: 1
 *   }
 */
class CommandContext(
    val sender: CommandSender,
    val args: Array<String>,
    val label: String = ""
) {
    val isPlayer: Boolean get() = sender is Player
    val player: Player
        get() = sender as? Player ?: throw CommandException("플레이어만 사용 가능한 명령어입니다.")

    // ─── Nullable 조회 ───────────────────────────────────
    fun stringOrNull(index: Int): String?  = args.getOrNull(index)
    fun intOrNull(index: Int): Int?        = args.getOrNull(index)?.toIntOrNull()
    fun longOrNull(index: Int): Long?      = args.getOrNull(index)?.toLongOrNull()
    fun doubleOrNull(index: Int): Double?  = args.getOrNull(index)?.toDoubleOrNull()
    fun booleanOrNull(index: Int): Boolean? = args.getOrNull(index)?.toBooleanStrictOrNull()
    fun playerOrNull(index: Int): Player?  = args.getOrNull(index)?.let { Bukkit.getPlayerExact(it) }

    // ─── Non-null (없으면 CommandException) ─────────────
    fun string(index: Int): String  = stringOrNull(index)  ?: missing(index)
    fun int(index: Int): Int        = intOrNull(index)     ?: badType(index, "정수")
    fun long(index: Int): Long      = longOrNull(index)    ?: badType(index, "정수")
    fun double(index: Int): Double  = doubleOrNull(index)  ?: badType(index, "소수")
    fun player(index: Int): Player  = playerOrNull(index)  ?: throw CommandException("플레이어 '${string(index)}'를 찾을 수 없습니다.")

    /** index 이후 인자를 공백으로 이어 붙임 */
    fun joinFrom(index: Int): String = args.drop(index).joinToString(" ")

    val size: Int get() = args.size

    private fun missing(i: Int): Nothing =
        throw CommandException("${i + 1}번째 인자가 필요합니다. 사용법: /$label")
    private fun badType(i: Int, type: String): Nothing =
        throw CommandException("${i + 1}번째 인자는 ${type}이어야 합니다.")
}

class CommandException(message: String) : Exception(message)