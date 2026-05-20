# CRFramework

Paper 플러그인 개발을 위한 Kotlin 프레임워크입니다.  
DI 컨테이너, 반응형 GUI, DB ORM, 스케줄러, NMS 추상화를 제공합니다.

## 지원 버전

| 항목 | 버전 |
|------|------|
| Minecraft | 1.20.4 ~ 1.21.7 |
| Paper API | 1.20.4-R0.1-SNAPSHOT |
| Java | 21 |
| Kotlin | 2.3.10 |

## 기능

- **DI 컨테이너** — 생성자 주입 자동 해결, `@Setup` / `@Teardown` 생명주기 관리
- **명령어 자동 등록** — `@Command` 하나로 `plugin.yml` 없이 명령어 등록
- **이벤트 자동 등록** — `@Subscribe` 하나로 리스너 등록, 퇴장 시 자동 해제
- **반응형 GUI** — 상태(State) 변경 시 인벤토리 슬롯 자동 리렌더, Navigator 지원
- **YAML 설정** — `@Configuration` 기반 타입 세이프 설정, 위치 직렬화 포함
- **데이터베이스** — Exposed ORM 래퍼, SQLite / MySQL / H2 지원, 플레이어 캐시 레포지토리
- **스케줄러** — tick / 초 단위 반복·지연 실행, 코루틴(`withMain` / `withAsync`) 지원
- **NMS 추상화** — 1.17 ~ 1.21.7 자동 감지, ActionBar / Title / NBT / 엔티티 등

## 의존성

CRFramework 자체는 외부 플러그인이 필요 없습니다.  
CRFramework를 **사용하는 플러그인**은 아래와 같이 의존성을 선언합니다.

```yaml
# plugin.yml
depend: [CRFramework]
```

### 방법 1) JitPack (권장)

```kotlin
// build.gradle.kts
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.zlero7:CRFramework:v1.0.3")
}
```

[![](https://jitpack.io/v/zlero7/CRFramework.svg)](https://jitpack.io/#zlero7/CRFramework)

특정 커밋이나 `main-SNAPSHOT` 도 사용 가능합니다.

### 방법 2) 로컬 JAR

```kotlin
// build.gradle.kts
dependencies {
    compileOnly(files("libs/CRFramework.jar"))
}
```

## 설치

1. `CRFramework-*.jar` 를 서버의 `plugins/` 폴더에 복사
2. 내 플러그인의 `plugin.yml` 에 `depend: [CRFramework]` 추가
3. 서버 시작

## 목차

1. [플러그인 기본 구조](#플러그인-기본-구조)
2. [DI 컨테이너](#di-컨테이너)
3. [명령어 @Command](#명령어-command)
4. [이벤트 @Subscribe](#이벤트-subscribe)
5. [YAML 설정](#yaml-설정)
6. [데이터베이스](#데이터베이스)
7. [GUI View](#gui-view)
8. [스케줄러](#스케줄러)
9. [NMS](#nms)
10. [전체 예시](#전체-예시)

---

## 플러그인 기본 구조

`JavaPlugin` 대신 `CRPlugin`을 상속합니다.  
`components()`에 사용할 클래스를 등록하면 DI 컨테이너가 자동으로 생성·주입합니다.

```kotlin
class MyPlugin : CRPlugin() {

    override fun components() = listOf(
        MainConfig::class,        // YAML 설정
        DatabaseModule::class,    // DB (선택)
        MoneyRepository::class,   // DB 레포지토리 (선택)
        MoneyService::class,      // 비즈니스 로직
        MoneyCommand::class,      // 명령어
        PlayerListener::class,    // 이벤트
    )

    override fun onCREnabled() {
        // DB 사용 시 테이블/레포지토리 등록
        val db = inject<DatabaseModule>()
        db.addTable(MoneyTable)
        db.addPlayerRepository(inject<MoneyRepository>())
    }

    override fun onCRDisabled() {
        // 종료 시 추가 정리 로직
    }
}
```

| 메서드 | 설명 |
|--------|------|
| `components()` | DI에 등록할 클래스 목록 (필수 구현) |
| `onCREnabled()` | 모든 컴포넌트 초기화 후 호출 |
| `onCRDisabled()` | 플러그인 종료 직전 호출 |
| `inject<T>()` | 등록된 빈 꺼내기 |
| `scheduler` | `CRScheduler` 인스턴스 |
| `nms` | `NMSServiceManager` 인스턴스 |

---

## DI 컨테이너

생성자에 등록된 타입을 선언하면 자동으로 주입됩니다.  
의존성 순서는 자동으로 해결되므로 `components()` 목록 순서를 신경 쓰지 않아도 됩니다.

```kotlin
// ✅ MainConfig, MoneyRepository를 자동으로 주입받음
@Singleton
class MoneyService(
    private val config: MainConfig,
    private val repo: MoneyRepository
) {
    fun give(player: Player, amount: Long) {
        repo.update(player.uniqueId) { money += amount }
        player.sendMessage("${config.prefix}§a+${amount}G")
    }
}
```

**어노테이션 종류:**

| 어노테이션 | 대상 | 설명 |
|-----------|------|------|
| `@Component` | 클래스 | 일반 컴포넌트 |
| `@Singleton` | 클래스 | 싱글턴 (동일 인스턴스 재사용) |
| `@Module` | 클래스 | `@Setup` / `@Teardown` 생명주기 사용 가능 |
| `@Setup` | 함수 | `onEnable` 시점에 자동 호출 |
| `@Teardown` | 함수 | `onDisable` 시점에 역순으로 자동 호출 |

```kotlin
@Module
class MyModule(private val plugin: JavaPlugin) {

    @Setup
    fun onSetup() {
        plugin.logger.info("초기화!")
    }

    @Teardown
    fun onTeardown() {
        plugin.logger.info("정리!")
    }
}
```

---

## 명령어 @Command

함수에 `@Command`를 붙이면 서버 명령어로 자동 등록됩니다.  
`plugin.yml`에 명령어를 따로 선언할 필요가 없습니다.

```kotlin
@Component
class MoneyCommand(
    private val config: MainConfig,
    private val service: MoneyService
) {
    @Command("money", description = "돈 관련 명령어", permission = "myplugin.money")
    fun onMoney(ctx: CommandContext) {
        when (ctx.stringOrNull(0)) {
            "give" -> {
                val target = ctx.player(1)
                val amount = ctx.long(2)
                service.give(target, amount)
                ctx.sender.sendMessage("${config.prefix}§a지급 완료")
            }
            "check" -> {
                val balance = service.getBalance(ctx.player.uniqueId)
                ctx.player.sendMessage("${config.prefix}잔액: §e${balance}G")
            }
            else -> ctx.player.sendMessage("§c사용법: /money give <플레이어> <금액>")
        }
    }
}
```

**CommandContext API:**

```kotlin
ctx.sender          // CommandSender
ctx.player          // Player (콘솔이면 예외 발생)
ctx.isPlayer        // Boolean
ctx.size            // 인자 개수

// 인자 파싱 (없거나 잘못된 타입이면 자동으로 오류 메시지 전송)
ctx.string(0)       // 필수 String
ctx.int(0)          // 필수 Int
ctx.long(0)         // 필수 Long
ctx.double(0)       // 필수 Double
ctx.player(0)       // 필수 온라인 Player

// nullable 버전
ctx.stringOrNull(0)
ctx.intOrNull(0)
ctx.playerOrNull(0)

ctx.joinFrom(1)     // 1번 인자부터 끝까지 공백으로 합치기
```

---

## 이벤트 @Subscribe

함수에 `@Subscribe`를 붙이면 이벤트 리스너로 자동 등록됩니다.  
`Listener` 인터페이스를 구현하거나 직접 등록할 필요가 없습니다.

```kotlin
@Component
class PlayerListener(
    private val config: MainConfig,
    private val repo: MoneyRepository
) {
    @Subscribe
    fun onJoin(e: PlayerJoinEvent) {
        e.player.sendMessage(config.joinMessage)
    }

    @Subscribe(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onDamage(e: EntityDamageByEntityEvent) {
        // 높은 우선순위, 취소된 이벤트 무시
    }
}
```

---

## YAML 설정

`CRYamlConfiguration`을 상속하면 타입 세이프하게 설정값을 읽을 수 있습니다.  
`resources/` 폴더에 기본 파일을 두면 없을 때 자동으로 복사됩니다.

```kotlin
@Configuration("config.yml")
class MainConfig(plugin: JavaPlugin) : CRYamlConfiguration(plugin, "config.yml") {

    val prefix      get() = colorString("prefix",           "&8[&bMyPlugin&8] &r")
    val joinMessage get() = colorString("messages.join",    "&a{player}님 접속!")
    val maxNickLen  get() = int("nickname.max-length",      16)
    val allowColor  get() = boolean("nickname.allow-color", true)
    val blacklist   get() = stringList("nickname.blacklist")
    val spawnLoc    get() = getLocation("spawn")
}
```

**사용 가능한 메서드:**

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `string(path, default)` | `String` | 문자열 |
| `int(path, default)` | `Int` | 정수 |
| `long(path, default)` | `Long` | 정수 (큰 수) |
| `double(path, default)` | `Double` | 소수 |
| `boolean(path, default)` | `Boolean` | 참/거짓 |
| `stringList(path)` | `List<String>` | 문자열 목록 |
| `colorString(path, default)` | `String` | `&` → `§` 색상 변환 |
| `colorStringList(path)` | `List<String>` | 색상 변환 목록 |
| `getLocation(path)` | `Location?` | 위치 역직렬화 |
| `setLocation(path, loc)` | `Unit` | 위치 직렬화 후 저장 |
| `set(path, value)` | `Unit` | 값 저장 (즉시 파일에 씀) |
| `reload()` | `Unit` | 파일에서 재로드 |

---

## 데이터베이스

Exposed ORM 기반입니다. `DatabaseModule`을 `components()`에 추가하고 `onCREnabled()`에서 테이블과 레포지토리를 등록합니다.

### 테이블 정의

```kotlin
object MoneyTable : IntIdTable("player_money") {
    val uuid  = varchar("uuid", 36).uniqueIndex()
    val money = long("money").default(0L)
}
```

### PlayerRepository (접속 중인 플레이어 캐시)

접속 시 자동 로드, 퇴장 시 자동 저장되는 레포지토리입니다.

```kotlin
@Repository
@Singleton
class MoneyRepository : PlayerRepository<MoneyData, MoneyTable>(MoneyTable) {

    override fun load(uuid: UUID): MoneyData? = query {
        MoneyTable.select { MoneyTable.uuid eq uuid.toString() }
            .firstOrNull()
            ?.let { MoneyData(uuid, it[MoneyTable.money]) }
    }

    override fun save(uuid: UUID, data: MoneyData): Unit = query {
        val exists = MoneyTable.select { MoneyTable.uuid eq uuid.toString() }.count() > 0
        if (exists) {
            MoneyTable.update({ MoneyTable.uuid eq uuid.toString() }) {
                it[money] = data.money
            }
        } else {
            MoneyTable.insert {
                it[MoneyTable.uuid]  = uuid.toString()
                it[MoneyTable.money] = data.money
            }
        }
    }

    override fun createDefault(uuid: UUID) = MoneyData(uuid, money = 0L)

    // 커스텀 메서드
    fun getMoney(uuid: UUID): Long         = get(uuid)?.money ?: 0L
    fun addMoney(uuid: UUID, amount: Long) = update(uuid) { money += amount }
}

data class MoneyData(val uuid: UUID, var money: Long = 0L)
```

### 플러그인 메인에 등록

```kotlin
override fun onCREnabled() {
    val db = inject<DatabaseModule>()
    db.addTable(MoneyTable)
    db.addPlayerRepository(inject<MoneyRepository>())
}
```

### DB 설정 변경 (기본값: SQLite)

```kotlin
// MySQL 사용 시
DatabaseModule(plugin, DatabaseConfig(
    type     = DatabaseType.MYSQL,
    host     = "localhost",
    port     = 3306,
    database = "myserver",
    username = "root",
    password = "1234"
))
```

**PlayerRepository API:**

| 메서드 | 설명 |
|--------|------|
| `get(uuid)` | 캐시에서 조회 (접속 중 플레이어만) |
| `update(uuid) { ... }` | 캐시 수정 + dirty 마킹 |
| `flush(uuid)` | 즉시 DB에 저장 |
| `isOnline(uuid)` | 캐시에 있는지 확인 |
| `load(uuid)` | DB에서 직접 로드 (오버라이드 필수) |
| `save(uuid, data)` | DB에 직접 저장 (오버라이드 필수) |
| `createDefault(uuid)` | 신규 플레이어 기본값 (오버라이드 권장) |

---

## GUI View

반응형 GUI 시스템입니다. 상태(State)가 바뀌면 자동으로 인벤토리가 업데이트됩니다.

### ViewModel 정의

```kotlin
class ShopViewModel : CRViewModel() {
    val balance = state(0L)   // 잔액 상태
    val page    = state(0)    // 현재 페이지
}
```

### View 정의

```kotlin
class ShopView(
    plugin: JavaPlugin,
    private val vm: ShopViewModel
) : View(plugin, "§8아이템 상점", rows = 4) {

    // 버튼/레이아웃 정의 (열릴 때 1회 실행)
    override fun CreateScope.onCreate() {

        // balance 상태가 바뀌면 이 버튼 자동 리렌더
        button(slot = 11, state = vm.balance) {
            item { _ ->
                ItemStack(Material.DIAMOND).apply {
                    itemMeta = itemMeta!!.also {
                        it.setDisplayName("§b다이아몬드 §7(500G)")
                        it.lore = listOf("§7잔액: §e${vm.balance.value}G")
                    }
                }
            }
            onClick { player ->
                if (vm.balance.value >= 500) {
                    vm.balance.value -= 500
                    player.inventory.addItem(ItemStack(Material.DIAMOND))
                } else {
                    player.sendMessage("§c잔액이 부족합니다!")
                }
            }
        }

        // 닫기 버튼
        button(slot = 31) {
            item(Material.BARRIER)
            onClick { player -> player.closeInventory() }
        }

        // 테두리 장식
        border(rows = 4) {
            item(Material.GRAY_STAINED_GLASS_PANE)
        }
    }

    // 매 렌더링마다 실행 (동적 아이템)
    override fun RenderScope.onRender(player: Player) {
        slot(4) {
            ItemStack(Material.PAPER).apply {
                itemMeta = itemMeta!!.also {
                    it.setDisplayName("§f${vm.page.value + 1}페이지")
                }
            }
        }
    }

    override fun onClose(player: Player) {
        player.sendMessage("§7상점을 닫았습니다.")
    }
}
```

### View 열기

```kotlin
// 명령어에서
@Command("shop")
fun onShop(ctx: CommandContext) {
    val vm = ShopViewModel().apply {
        balance.value = moneyRepo.getMoney(ctx.player.uniqueId)
    }
    ShopView(plugin, vm).open(ctx.player)
}
```

### CreateScope DSL

| 함수 | 설명 |
|------|------|
| `button(slot) { ... }` | 단일 슬롯 버튼 |
| `button(slot, state = vm.xxx) { ... }` | 상태 연결 버튼 (상태 변경 시 자동 리렌더) |
| `buttons(slots) { ... }` | 여러 슬롯에 동일한 버튼 |
| `border(rows) { ... }` | 인벤토리 테두리 전체 |
| `fill(rows) { ... }` | 인벤토리 전체 채우기 |

### ButtonBuilder DSL

```kotlin
button(slot = 13) {
    item { player -> ItemStack(Material.DIAMOND) }  // 플레이어별 다른 아이템
    item(ItemStack(Material.DIAMOND))               // 고정 아이템
    item(Material.DIAMOND)                          // 간단한 소재만
    onClick { player -> /* 클릭 처리 */ }
}
```

### Navigator (여러 View 간 이동)

```kotlin
val nav = Navigator(player)
nav.open(MainMenuView(plugin))     // 첫 화면
nav.push(ShopView(plugin, vm))     // 다음 화면으로 (스택에 쌓임)
nav.goBack()                       // 이전 화면으로
nav.close()                        // 전부 닫기
```

---

## 스케줄러

```kotlin
// CRPlugin 안에서
scheduler.runLater(60L) {
    player.sendMessage("3초 후 메시지")
}

scheduler.runEverySeconds(5L) {
    server.broadcastMessage("§e5초마다 반복")
}

scheduler.runTimes(3, 20L) { remaining ->
    player.sendMessage("§a${remaining}번 남음")
}

// 비동기 작업 후 메인 스레드 콜백
scheduler.async(
    task = { heavyDatabaseQuery() },
    then = { result -> player.sendMessage(result) }
)
```

**전체 API:**

| 메서드 | 설명 |
|--------|------|
| `run { }` | 메인 스레드 즉시 실행 |
| `runLater(ticks) { }` | n틱 후 실행 |
| `runTimer(delay, period) { }` | delay 후 period마다 반복 |
| `runAsync { }` | 비동기 즉시 실행 |
| `runLaterAsync(ticks) { }` | n틱 후 비동기 실행 |
| `runTimerAsync(delay, period) { }` | 비동기 반복 |
| `runAfterSeconds(sec) { }` | n초 후 실행 |
| `runEverySeconds(sec) { }` | n초마다 반복 |
| `runTimes(n, period) { remaining -> }` | n번 반복 후 자동 취소 |
| `async(task, then)` | 비동기 작업 → 메인 콜백 |

### 코루틴

```kotlin
val scope = pluginScope()

scope.launch {
    val data = withAsync { loadFromDatabase() }  // 비동기 스레드
    withMain { player.sendMessage(data) }         // 메인 스레드
}
```

---

## NMS

버전별 NMS 코드를 신경 쓰지 않고 사용할 수 있습니다.  
1.17 ~ 1.21.7을 지원하며 버전을 자동으로 감지합니다.

```kotlin
val nms = NMSServiceManager  // 또는 CRPlugin의 nms 프로퍼티

// 플레이어
nms.player.sendActionBar(player, "§a액션바 메시지")
nms.player.sendTitle(player, "§b타이틀", "§7서브타이틀", 10, 70, 20)
nms.player.setTabName(player, "§e탭리스트 이름")
nms.player.setNameTag(player, "§c[Admin] ", " §7님")
nms.player.clearNameTag(player)

// 아이템 NBT
val item  = nms.item.setString(itemStack, "custom_id", "my_sword")
val id    = nms.item.getString(itemStack, "custom_id")
val has   = nms.item.has(itemStack, "custom_id")
val item2 = nms.item.setInt(itemStack, "damage", 10)

// 엔티티
nms.entity.setCustomName(entity, "§cBoss §7(Lv.50)")
nms.entity.setInvulnerable(livingEntity, true)

// 버전 확인
if (nms.version.isAtLeast(NmsVersion.V1_21)) {
    // 1.21 이상에서만 실행
}
```

---

## 전체 예시

```kotlin
// config.yml (resources/config.yml)
// prefix: "&8[&bMyPlugin&8] &r"
// messages:
//   join: "&a{player}님이 접속했습니다!"

@Configuration("config.yml")
class MainConfig(plugin: JavaPlugin) : CRYamlConfiguration(plugin, "config.yml") {
    val prefix      get() = colorString("prefix")
    val joinMessage get() = colorString("messages.join")
}

object MoneyTable : IntIdTable("money") {
    val uuid  = varchar("uuid", 36).uniqueIndex()
    val money = long("money").default(0L)
}

data class MoneyData(val uuid: UUID, var money: Long = 0L)

@Repository @Singleton
class MoneyRepository : PlayerRepository<MoneyData, MoneyTable>(MoneyTable) {
    override fun load(uuid: UUID) = query {
        MoneyTable.select { MoneyTable.uuid eq uuid.toString() }
            .firstOrNull()?.let { MoneyData(uuid, it[MoneyTable.money]) }
    }
    override fun save(uuid: UUID, data: MoneyData) = query {
        MoneyTable.upsert { it[MoneyTable.uuid] = uuid.toString(); it[money] = data.money }
    }
    override fun createDefault(uuid: UUID) = MoneyData(uuid)
    fun getMoney(uuid: UUID) = get(uuid)?.money ?: 0L
    fun addMoney(uuid: UUID, amount: Long) = update(uuid) { money += amount }
}

@Singleton
class MoneyService(private val config: MainConfig, private val repo: MoneyRepository) {
    fun give(player: Player, amount: Long) {
        repo.addMoney(player.uniqueId, amount)
        player.sendMessage("${config.prefix}§a+${amount}G 지급됨")
    }
}

@Component
class MoneyCommand(private val service: MoneyService) {
    @Command("money", description = "돈 관리", permission = "myplugin.money")
    fun onMoney(ctx: CommandContext) {
        val amount = ctx.longOrNull(0) ?: 100L
        service.give(ctx.player, amount)
    }
}

@Component
class JoinListener(private val config: MainConfig) {
    @Subscribe
    fun onJoin(e: PlayerJoinEvent) {
        e.player.sendMessage(config.joinMessage.replace("{player}", e.player.name))
    }
}

class MyPlugin : CRPlugin() {
    override fun components() = listOf(
        MainConfig::class,
        DatabaseModule::class,
        MoneyRepository::class,
        MoneyService::class,
        MoneyCommand::class,
        JoinListener::class,
    )
    override fun onCREnabled() {
        inject<DatabaseModule>().also {
            it.addTable(MoneyTable)
            it.addPlayerRepository(inject<MoneyRepository>())
        }
    }
}
```

---

## 빌드

```bash
./gradlew shadowJar
```

결과물: `build/libs/CRFramework-1.0.3.jar`

> `jar` 태스크가 아닌 **반드시 `shadowJar`** 를 사용해야 합니다.  
> Exposed · HikariCP · sqlite-jdbc · kotlinx-coroutines 가 함께 번들링됩니다.

## 라이선스

MIT License © 2026 zlero — 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.
