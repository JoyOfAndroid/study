# Kotlin Context Receiver

코틀린 1.6.20-M1 버전에 프로토타입으로 추가된 context api 에 대해 알아보았습니다.  

[제안서](https://github.com/Kotlin/KEEP/blob/master/proposals/context-receivers.md)에 설명된 내용을 토대로 작성하였으나, 오역 및 축약된 부분이 있을 수 있기 때문에 더 자세한 내용이 궁금하시면 
원문을 같이 확인해주세요.:)

## 사용하기 위한 설정
Android studio 기준으로, 아래의 방법을 통해 설정을 구성할 수 있습니다.
1. Preferences → Languages & Frameworks → kotlin 탭  
2. Early Access Preview 1.6.x 업데이트 → 재시작  
3. `build.gradle` 코틀린 버전 `1.6.20-M1` 으로 수정
4. `freeCompilerArgs` 추가
    ```
    kotlinOptions {
        jvmTarget = '1.8'
        freeCompilerArgs =["-Xcontext-receivers"]
    }
    ```

## 클래스 내부 확장 함수

```kotlin
interface Entity

interface Scope {
    fun Entity.doAction() {
        ...
    }
}
```

`Entity.doAction()` 메서드에서 `Scope` 의 참조를 얻으려면 `Scope` 내부의 멤버 함수로 선언해야 합니다.  
이렇게 선언된 함수를 외부에서 사용할 때는 아래와 같이 범위 지정 함수 내부에서 사용할 수 있습니다.
이 때, `scope` 는 암시적으로 매개변수 역할을 수행합니다.

```kotlin
with(scope) {
    entity.doAction()
}
```

### 클래스 내부 확장 함수의 제한
* 서드 파티 클래스에 대해서 선언할 수 없습니다. (ex. 만약 Scope 클래스가 서드파티 클래스라면)
* 하나의 리시버(scope)만 컨텍스트를 나타낼 수 있습니다. 두 개 이상의 스코프가 필요한 함수를 선언 할 수 없습니다.

## Context
기존에는 클래스 내부에 확장 함수를 선언하는 방식만 있었지만,  
`context` 라는 키워드를 함수 앞에 붙임으로서 스코프를 지정해줄 수 있게 되었습니다. 다음과 같이 사용 할 수 있습니다.

```kotlin
context(Scope)
fun Entity.doAction()

// top-level
context(Scope)
fun doAction()

// 다중 디스패치 리시버
context(A, B, C)
fun doAction()

// 함수 타입
val g: context(Context) Receiver.(Param) -> Unit

// 명시적 this 참조
context(Logger, Storage<User>)
fun userInfo(name: String): Storage.Info {
    this@Logger.info("Retrieving info about $name")
    return this@Storage.info(name)
}

// with 범위 지점 함수를 중첩해서 사용
with(logger) {
    with(storage)
        val info = userInfo("coder")
        println("info name: ${info.name}")
    }
}

// Storage 클래스 내부에서는 logger 범위 내에서 호출 가능
class Storage {
	fun print() {
		with(logger) {
			val info = userInfo("coder")
            println("info name: ${info.name}")
		}
	}
}
```

## 사용 예시 사례
제안에 있는 사례를 가져와봤습니다. 몇몇 케이스는 조금 각색을 했습니다.

- View 내부에서 사용하는 경우

```kotlin
// before
fun View.dp(px: Float) = px * this.resources.displayMetrics.density

fun View.dp(px: Int) = dp(px.toFloat())

// after
context(View)
val Float.dp get() = this * resources.displayMetrics.density

context(View)
val Int.dp get() = this.toFloat().dp
```

- CoroutineScope 내부에서 사용하는 경우

```kotlin
context(CoroutineScope)
fun <T> Flow<T>.launchFlow() {
    launch { collect() }  
}
```

- 리소스를 해제해야 하는 경우

```kotlin
interface AutoCloseScope {
    fun defer(closeBlock: () -> Unit)
}

context(AutoCloseScope)
fun File.open(): BufferedReader {
    return inputStream().bufferedReader().also {
        defer {
            it.close()
            println("closed: $it")
        }
    }
}

class AutoCloseScopeImpl : AutoCloseScope {
    private val deferList = mutableListOf<Function0<Unit>>()

    override fun defer(closeBlock: () -> Unit) {
        deferList.add(closeBlock)
    }

    fun close() {
        deferList.forEach {
            it.invoke()
        }
    }
}

fun withAutoClose(block: context(AutoCloseScope) () -> Unit) {
    val scope = AutoCloseScopeImpl()
    try {
        block(scope)
    } finally {
        scope.close()
    }
}

// 사용
fun main() {
    withAutoClose {
        val input = File("input.txt").open()
        val config = File("config.txt").open()

        println(input.readLine())
        println(config.readLine())
    }
}

Hello, World!
kotlin version: 1.6.20-M1
closed: java.io.BufferedReader@6aaa5eb0
closed: java.io.BufferedReader@3498ed
```

- json 생성

```kotlin
fun json(build: JSONObject.() -> Unit) = JSONObject().apply { build() }

context(JSONObject)
infix fun String.by(build: JSONObject.() -> Unit) = put(this, JSONObject().build())

context(JSONObject)
infix fun String.by(value: Any) = put(this, value)

fun main() {
    val json = json {
        "name" by "Kotlin"
        "age" by 10
        "creator" by { // JSONObject.() -> Unit
            "name" by "JetBrains"
            "age" by "21"
        }
    }
}
```

## 권장하는 코딩 스타일
- 동일한 메서드 이름으로 인하여 context 에 의해 오버라이딩 되지 않게 주의해야 합니다.

```kotlin
context(PrintWriter)
fun hello() {
    println("Hello")
    world()
}

fun world() {
    println("World")
}

class PrintWriter(private val logger: Logger) {
    fun println(text: String) {
        logger.info(text)
    }
}

val logger = object : Logger {
    override fun info(text: String) {
        println("logger info: $text")
    }
}

val printWriter = PrintWriter(logger)
with(printWriter) {
    hello()
}

logger info: Hello
World
```

- 객체에 대한 작업 수행은 확장 함수로 작성하는 것을 권장합니다.

```kotlin
context(User)
fun updateNow() {
    updateTime = now() // BAD STYLE: Don't use a context receiver here
}

fun User.updateNow() {
    updateTime = now() // GOOD STYLE: Action is performed on an extension receiver
}
```

- 컨텍스트를 암시적 파라미터의 용도로 넘기지 않아야 합니다.

```kotlin
fun User.recordLastLogin(address: InetAddress) {
    lastLoginAddress = address // GOOD STYLE: passing parameter explicitly
}

context(InetAddress)
fun User.recordLastLogin() {
    lastLoginAddress = this@InetAddress // BAD STYLE: Don't use context as an implict parameter
}
```

- 작업에 대한 추가 컨텍스트로 사용하는 것을 권장합니다.

```kotlin
interface TimeSource {
    fun now(): Instant
}

context(TimeSource)
fun updateNow() {
    updateTime = now() // GOOD STYLE: Use time source from the context
}
```

- 간단한 경우 기존 코틀린 빌더 형식도 추천합니다.

```kotlin
fun someObject(builder: SomeObjectBuilder.() -> Unit) =
    SomeObjectBuilder().run {
        builder()
        build()
    }

// Later in code
someObject {
    property = value
    ...
}
```

- 코틀린 빌더를 구현할 때, Context 를 지정하는 방식을 추천합니다.

```kotlin
// 확장 함수 파라미터
fun withVirtualTimeSource(block: TimeSource.() -> Unit) { ... }

// context 적용
// GOOD STYLE: Better for newly designed code
fun withVirtualTimeSource(block: context(TimeSource) () -> Unit) { ... }
```

다음 코드가 `this` 를 변경하지 않기 때문에 더 좋다고 합니다.

```kotlin
// Later in code
withVirtualTimeSource {
    val time = now() // provides virtual time in this block
}

class Subject { 
    fun doSomething() {
        withVirtualTimeSource {
            val subject = this // `this` still refers to Subject instance
        }
    }
}
```

## 결론
Context를 지정해두면 여러 인자가 필요한 경우 함수를 간편하게 사용할 수 있다는 이점이 있지만,  
메서드 오버라이딩 문제, 코드의 가독성 문제도 감안해서 팀내 협의가 잘 된 상황에서 사용해야 할 것 같습니다.  
