# v6.0 Health Connect 릴리스 노트

## 새 기능

- 기존 맨몸 코치 PWA를 Android WebView 앱으로 패키징
- Galaxy Watch → Samsung Health → Health Connect 경로의 달리기·수영 기록 읽기
- 최근 30일 세션 동기화
- 시간, 거리, 평균·최대 심박수, 총소모 칼로리 표시
- 달리기 min/km 및 수영 min/100m 계산
- 선택한 기록만 기존 운동일지에 추가
- Health Connect ID 기반 중복 추가 방지
- Android 시스템 파일 선택기를 이용한 JSON 백업 저장·가져오기
- Health Connect 권한 설명·온보딩 화면

## 개인정보 보호 기본값

- 읽기 전용
- GPS 경로 권한 미요청
- 외부 서버 전송 없음
- 휴대폰 앱 저장공간에 보관

## 이전 PWA와의 관계

APK와 기존 홈 화면 PWA는 서로 다른 앱 저장공간을 사용합니다. 기존 PWA에서 JSON을 내보낸 뒤 APK의 일지 화면에서 가져오십시오.
