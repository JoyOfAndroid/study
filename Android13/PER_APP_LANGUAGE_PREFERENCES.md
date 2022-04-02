# Per-app language preferences
## 기능
- 시스템 언어를 변경하지 않고도, 앱 내부에서 언어를 변경하는 기능
- 앱 종료 후 재시작 시, 마지막으로 설정된 언어로 보임
- 앱에만 영향
## 전제조건
- Android13(Tiramisu)에서만 가능
- 하위호환성 제공하지 않음
## 파해치기
### Locale
#### forLanguageTag
- 언어 태그(예, ko-KR, en-US 등)를 읽어서 Locale 객체로 반환
### LocaleManager
- Android13(Tiramisu)부터 제공
- 시스템 Locale 서비스 접근 제공
- 앱단 Locale 제어
#### getApplicationLocales
- 앱별로 설정된 Locale 리스트 조회
- 설정한 적이 없다면 빈 List 반환하며, 이 때에는 시스템 언어를 따라감
#### setApplicationLocales
- 앱단에 적용할 Locale 등록
- Locale 등록 시 앱 재시작
- 여러 Locale 등록 가능하며, 이 때에는 첫 번째 요소의 Locale 적용됨