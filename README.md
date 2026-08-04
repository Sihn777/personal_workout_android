# 맨몸 코치 Android · Health Connect

기존 PWA를 Android WebView 앱으로 감싸고 Health Connect 읽기 연동을 추가한 개인용 프로젝트입니다.

## 포함 기능
- 기존 근력 루틴, 준비운동, 휴식 타이머, 평가, 운동일지
- Galaxy Watch → Samsung Health → Health Connect 경로의 달리기·수영 가져오기
- 최근 30일 운동 세션, 거리, 평균/최대 심박수, 총소모 칼로리
- 달리기 km 페이스, 수영 100m 페이스 계산
- 선택한 Health Connect 기록만 기존 운동일지에 추가
- GPS 경로 미사용, 읽기 전용, 로컬 저장

자세한 절차는 `BUILD_AND_INSTALL_KO.md`와 `HEALTH_CONNECT_SETUP_KO.md`를 참고하십시오.

## 문서
- `BUILD_AND_INSTALL_KO.md`: GitHub Actions 또는 Android Studio 빌드 절차
- `HEALTH_CONNECT_SETUP_KO.md`: Samsung Health와 Health Connect 권한 설정
- `VALIDATION_REPORT_KO.md`: 수행한 검사와 미검증 범위
- `RELEASE_NOTES_KO.md`: v6 변경사항

## 중요
이 압축파일은 Android Studio/GitHub Actions용 **소스 프로젝트**입니다. APK는 포함되어 있지 않으며, 포함된 GitHub Actions 워크플로로 debug APK를 생성할 수 있습니다.
