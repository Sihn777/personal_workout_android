# 검증 보고서

검증일: 2026-08-04

## 완료한 검사

- `index.html` 내부 JavaScript 추출 후 Node.js 구문 검사: 통과
- AndroidManifest 및 모든 Android XML 리소스 파싱: 통과
- Health Connect 의존성 및 읽기 권한 선언 점검: 통과
- 달리기·트레드밀·실내수영·야외수영 필터 존재 확인: 통과
- GPS 운동경로 권한을 요청하지 않는지 확인: 통과
- GitHub Actions의 debug APK 빌드 명령과 APK artifact 경로 확인: 통과
- 브라우저 UI 스모크 테스트: 통과
- 모의 Health Connect 달리기 기록의 표시, 중복 방지, 운동일지 추가: 통과

## 구현 범위

- 읽기 전용 권한: 운동 세션, 거리, 심박수, 총소모 칼로리
- 최근 30일 달리기·수영 세션 동기화
- 달리기 평균 페이스와 수영 100m 페이스 계산
- 사용자가 선택한 세션만 기존 운동일지에 추가
- 데이터 로컬 저장 및 JSON 백업/복원
- GPS 경로, 위치, 수면, 체중 등은 요청하지 않음

## 검증하지 못한 항목

이 실행환경에는 Android SDK와 완전한 Gradle 빌드 환경이 없어 네이티브 APK를 여기서 직접 컴파일하거나 실제 Galaxy 기기에 설치하지 못했습니다. 대신 프로젝트에 GitHub Actions 빌드 워크플로를 포함했습니다. 사용자의 GitHub 저장소에서 워크플로가 실행되면 `app-debug.apk`가 생성됩니다.

실제 기기에서는 다음을 확인해야 합니다.

1. Health Connect 권한 화면이 정상 표시되는지
2. Samsung Health가 기록한 달리기·수영 세션이 보이는지
3. 거리·심박수·칼로리 중 Samsung Health가 Health Connect에 실제 전달한 항목이 표시되는지
4. PWA JSON 백업을 APK로 가져왔을 때 기존 기록이 유지되는지

## 알려진 범위 제한

- 수영 영법, 스트로크 수, 세부 인터벌은 Health Connect에 전달되지 않을 수 있습니다.
- Health Connect에서 읽을 수 있는 항목은 Samsung Health가 동기화한 항목에 한정됩니다.
- 이 버전은 백그라운드 자동 동기화가 아니라 사용자가 `최근 30일 동기화`를 누르는 방식입니다.
- debug APK는 개인 시험용입니다. 정식 배포에는 서명키, 개인정보처리방침 공개 URL, Play Console Health Apps declaration 등이 추가로 필요합니다.
