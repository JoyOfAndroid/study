# KSP란
- Kotlin Symbol Processing
- 경량 컴파일러 플러그인 구축
- 보일러 플레이트를 생성하거나, 코드에 강력한 기능을 추가할 수 있음
## 장점
- 코틀린의 모든 기능을 사용할 수 있음
  - 자바와는 다르게, 코틀린에서만 제공하는 sealed class와 data class를 처리할 수 있음
- 코틀린을 자바로 변환하는 과정을 거치지 않아도 됨
  - 컴파일러에서 소요되는 시간을 25% 감소
  - ref. https://kotlinlang.org/docs/ksp-why-ksp.html#comparison-to-kapt

# 만들기
## 1. annotations 모듈 생성 및 구현
- 어노테이션 정의부
### Target 지정
- AnnotationTarget.CLASS : 클레스에 어노테이션 적용
- AnnotationTarget.PROPERTY : 프로퍼티에 어노테이션 적용
## 2. processor 모듈 생성
- 어노테이션 구현부(처리기)
### 의존성 추가
```gradle
implementation project(':annotations') // 어노테이션 선언부 모듈
implementation 'com.google.devtools.ksp:symbol-processing-api:1.7.0-1.0.6' // processor 관련
```
## 3. ProcessorProvider 구현
- `SymbolProcessorProvider` 구현
### 3-1. SymbolProcessor 구현
- `SymbolProcessor` 구현
#### 3-1-1. Visitor 구현
- `KSVisitorVoid` 구현
- `visitClassDeclaration` 함수 오버라이딩
  - 클래스를 파일로 생성하는 로직

# 적용하기
```gradle
plugins {
    id 'com.google.devtools.ksp'
}
```
```gradle
dependencies {
    implementation(project(":annotations"))
    ksp(project(":processor"))
}
```

# 적용 Repo
- https://github.com/BeokBeok/ksp-builder-sample

