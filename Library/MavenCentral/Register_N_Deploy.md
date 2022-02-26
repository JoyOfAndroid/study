# MavenCentral에 라이브러리 배포하기
## 1. SonarType 계정 생성
[SonarType JIRA](https://issues.sonatype.org/secure/Dashboard.jspa) 접속하여 계정 생성
## 2. [Nexus Repository Manager](https://s01.oss.sonatype.org/) 접근 권한 요청
### step1. 이슈 생성 ([참고](https://issues.sonatype.org/browse/OSSRH-77050))
중앙에 `만들기` 버튼을 클릭하여 진입
<img src="https://user-images.githubusercontent.com/48344355/155836506-b1095993-fa8c-4e31-ad0f-7a8d8751c3fd.png">
<img src="https://user-images.githubusercontent.com/48344355/155836578-61e37c72-2b8f-42b8-bd70-baa5aae5308d.png">
- 프로젝트 : `Community Support - Open Source Project Repository Hosting (OSSRH)`
- 이슈 유형 : `New Project`
- 요약 : 제목
- 설명 : 라이브러리에 대한 설명
- Group Id : 그룹 아이디
  - 레파지토리가 깃헙일 경우, 반드시 `io.github.username`의 형태이어야 함
  - Group Id와 배포할 라이브러리의 패키지명이 일치하지 않아도 됨
    - 배포할 라이브러리의 패키지명이 `io.github.username` 의 형태가 아니여도 됨 
- Project URL : 프로젝트 경로
- SCM url : clone할 수 있는 git 경로 (Project URL 뒤에 .git 추가)
### step2. 코맨트가 달리면 이슈번호 이름으로 Repository 생성
ownership을 체크하기 위함으로, public으로 생성해줘야함
### step3. 이슈의 상태 변경
열림 -> WAITING FOR RESPONSE
### step4. 코맨트가 달리면 `has been prepared` 관련 내용이 있는지 확인
### step5. [Nexus Repository Manager](https://s01.oss.sonatype.org/) 에 접속하여 로그인이 되는지 확인
## 3. 배포 스크립트 작성
### step1. [GPG 키 생성](https://central.sonatype.org/publish/requirements/gpg/)
아래 순서대로 진행
1. [Installing GnuPG](https://central.sonatype.org/publish/requirements/gpg/#installing-gnupg)
2. [Generating a Key Pair](https://central.sonatype.org/publish/requirements/gpg/#generating-a-key-pair)
3. [Distributing Your Public Key](https://central.sonatype.org/publish/requirements/gpg/#distributing-your-public-key)
### step2. properties를 활용하여 스크립트 작성
#### publish-mavencentral.gradle
- 모듈 내부에 생성 (복붙해서 쓰세요 :sweat_smile:)
```groovy
apply plugin: 'maven-publish'
apply plugin: 'signing'

task androidSourcesJar(type: Jar) {
    archiveClassifier.set('sources')
    from android.sourceSets.main.java.srcDirs
}

artifacts {
    archives androidSourcesJar
}

group = GROUP
version = VERSION_NAME

ext["signing.keyId"] = ''
ext["signing.password"] = ''
ext["signing.secretKeyRingFile"] = ''

ext["ossrhUsername"] = ''
ext["ossrhPassword"] = ''

File secretPropsFile = project.rootProject.file('local.properties')
if (secretPropsFile.exists()) {
    Properties prop = new Properties()
    prop.load(new FileInputStream(secretPropsFile))
    prop.each { name, value ->
        ext[name] = value
    }
}

publishing {
    publications {
        release(MavenPublication) {
            groupId GROUP
            artifactId POM_ARTIFACT_ID
            version VERSION_NAME

            if (project.plugins.findPlugin("com.android.library")) {
                artifact("$buildDir/outputs/aar/${project.getName()}-release.aar")
            }
            artifact androidSourcesJar

            pom {
                name = POM_ARTIFACT_ID
                description = POM_DESCRIPTION
                url = POM_URL

                licenses {
                    license {
                        name = LICENSE_NAME
                        url = LICENSE_URL
                    }
                }

                developers {
                    developer {
                        id = DEVELOPER_ID
                        name = DEVELOPER_NAME
                        email = DEVELOPER_EMAIL
                    }
                }

                scm {
                    connection = POM_SCM_URL
                    developerConnection = POM_SCM_CONNECTION
                    url = POM_SCM_DEV_CONNECTION
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            url = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            credentials {
                username ossrhUsername
                password ossrhPassword
            }
        }
    }
}

signing {
    sign publishing.publications
}
```
### gradle.properties
- 본인 입맛에 맞춰서 변경하시면 됨
- POM_ARTIFACT_ID는 대문자를 적용할 수 없음 (BeokLibrary -> beoklibrary)
```properties
GROUP=io.github.beokbeok
VERSION_NAME=1.0.0
POM_ARTIFACT_ID=beoklibrary
POM_URL=https://github.com/BeokBeok/BeokLibrary
POM_DESCRIPTION=Android base library for reduce boilerplate
POM_SCM_URL=https://github.com/BeokBeok/BeokLibrary
POM_SCM_CONNECTION=scm:git:git://github.com/BeokBeok/BeokLibrary.git
POM_SCM_DEV_CONNECTION=scm:git:ssh://git@github.com/BeokBeok/BeokLibrary.git
LICENSE_NAME=Apache License Version 2.0
LICENSE_URL=http://www.apache.org/licenses/LICENSE-2.0
DEVELOPER_ID=BeokBeok
DEVELOPER_NAME=BeokBeok
DEVELOPER_EMAIL=kekemusa37@gmail.com
```
### local.properties
- 보안상 파일 내부 데이터 가림
- git에 올리면 안되고, 내부에서 관리해야함
```properties
signing.keyId=
signing.password=
signing.secretKeyRingFile=

ossrhUsername=
ossrhPassword=
```
#### keyId
- 터미널에 `gpg --list-keys --keyid-format SHORT` 입력
- 출력 결과 중에서 알파벳+숫자 조합(`A00000AA`)와 같은 형식 -- keyId
#### password
- GPG 키 생성 시 입력한 패스워드
#### secretKeyRingFile
- 터미널에 `gpg --export-secret-keys >~/.gnupg/secring.gpg` 입력
- 위 명령어 입력 시 secring.gpg 파일이 `/Users/사용자명/.gnupg/secring.gpg` 에 생성되며, 이 경로를 넣어주면 됨
#### ossrhUsername, ossrhPassword
- SonarType JIRA 계정 정보
## 4. 스크립트 빌드
1. 터미널에 `./gradlew publish` 입력
2. [Nexus Repository Manager](https://s01.oss.sonatype.org/) 에 접속
3. 왼쪽 메뉴의 `Build Promotion -> Staging Repositories`를 클릭하여 라이브러리가 올라갔는지 확인
4. 상단 `Close` 버튼을 눌러서 검수절차 진행
5. 검수 성공 시 상단의 `Release` 버튼이 활성화되며, `Release` 버튼을 눌러서 라이브로 배포
6. 배포한 라이브러리 상태가 closed로 되었는지 확인
## 5. 배포 확인
배포 성공 시 생성한 이슈에 첫 배포에 대한 코맨트가 달림
### 5-1 배포한 라이브러리 사용 방법
build.gradle (project)
```groovy
buildscript {
    repositories {
        mavenCentral()
        maven {
            url "https://repo1.maven.org/maven2/"
        }
    }
}
```
build.gradle (using module)
```groovy
dependencies {
    implementation 'io.github.beokbeok:beoklibrary:x.y.z'
}
```
## 참고
[BeokLibrary](https://github.com/BeokBeok/BeokLibrary)