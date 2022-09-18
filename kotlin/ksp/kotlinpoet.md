# kotlinpoet
- 코틀린 소스 코드를 파일로 만드는 것을 도와주는 라이브러리
- "코틀린 시인"을 의미함

## 예시
```HelloWorld.kt
class Greeter(val name: String) {
  fun greet() {
    println("""Hello, $name""")
  }
}

fun main(vararg args: String) {
  Greeter(args[0]).greet()
}
```
위 코드를 생성해보자
```kotlin
val greeterClass = ClassName(packageName = "", fileName = "Greeter")
val file = FileSpec.builder(packageName = "", fileName = "HelloWorld")
  .addType(
    TypeSpec.classBuilder(name = "Greeter")
      .primaryConstructor(
        FunSpec.constructorBuilder()
          .addParameter(name = "name", type = String::class)
          .build()
      )
      .addProperty(
        PropertySpec.builder(name = "name", type = String::class)
          .initializer(format = "name") // 생성자 "name"으로 초기화
          .build()
      )
      .addFunction(
        FunSpec.builder(name = "greet")
          .addStatement(format = "println(%P)", "Hello, \$name")
          .build()
      )
      .build()
  )
  .addFunction(
    FunSpec.builder(name = "main")
      .addParameter(name = "args", type = String::class, KModifier.VARARG)
      .addStatement(format = "%T(args[0]).greet()", greeterClass)
      .build()
  )
  .build()

file.writeTo(System.out)
```
### 포멧
- %S : 문자열
- %P : 문자열 템플릿
- %T : 타입
- %M : 멤버함수 및 멤버변수
- %N : XXXSpec을 변수로 정의한 값
- %L : 리터럴
### 범위 
```kotlin
val iterator = MemberName("com.squareup.tacos.internal", KOperator.ITERATOR)
val meat = ClassName("com.squareup.tacos.ingredient", "Meat")
val minusAssign = MemberName("com.squareup.tacos.internal", KOperator.MINUS_ASSIGN)

/* ... */

.beginControlFlow("for (ingredient %M taco)", iterator)
.addStatement("if (ingredient is %T) taco %M ingredient", meat, minusAssign)
.endControlFlow()

/* ... */
```
소스 코드로 만들면
```kotlin
for (ingredient in taco) {
  if (ingredient is Meat) taco -= ingredient
}
```
- beginControlFlow : scope 시작점 "{"
- endControlFlow : scope 끝점 "}"

## 참고
ref. https://square.github.io/kotlinpoet/#kotlinpoe
