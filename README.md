# CRFramework

Paper 플러그인 개발을 위한 Kotlin 프레임워크입니다.  
DI 컨테이너, 반응형 GUI, DB ORM, 스케줄러, NMS 추상화를 제공합니다.

## 지원 버전

| 항목 | 버전 |
|------|------|
| Minecraft | 1.20.4 ~ 1.21.4 |
| Paper API | 1.20.4-R0.1-SNAPSHOT |
| Java | 17 |
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

## 플러그인 기본 구조

`JavaPlugin` 대신 `CRPlugin`을 상속하고 `components()`에 사용할 클래스를 등록합니다.

```kotlin
class MyPlugin : CRPlugin() {

    override fun components() = listOf(
        MainConfig::class,
        DatabaseModule::class,
        MoneyRepository::class,
        MoneyService::class,
        MoneyCommand::class,
        PlayerListener::class,
    )

    override fun onCREnabled() {
        val db = inject<DatabaseModule>()
        db.addTable(MoneyTable)
        db.addPlayerRepository(inject<MoneyRepository>())
    }
}
```

```kotlin
// 명령어
@Component
class MoneyCommand(private val service: MoneyService) {
    @Command("money", description = "잔액 확인", permission = "myplugin.money")
    fun onMoney(ctx: CommandContext) {
        ctx.player.sendMessage("잔액: ${service.getBalance(ctx.player.uniqueId)}G")
    }
}

// 이벤트
@Component
class PlayerListener(private val config: MainConfig) {
    @Subscribe
    fun onJoin(e: PlayerJoinEvent) {
        e.player.sendMessage(config.joinMessage)
    }
}
```

전체 API 문서는 [src/main/resources/README.md](src/main/resources/README.md) 를 참고하세요.

## 빌드

```bash
./gradlew shadowJar
```

결과물: `build/libs/CRFramework-1.0.1.jar`

> `jar` 태스크가 아닌 **반드시 `shadowJar`** 를 사용해야 합니다.  
> Exposed · HikariCP · sqlite-jdbc · kotlinx-coroutines 가 함께 번들링됩니다.

## 라이선스

All rights reserved © zlero
