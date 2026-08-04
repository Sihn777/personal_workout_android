# Android APK 빌드·설치

## 방법 A: GitHub Actions
1. 이 프로젝트 전체를 새 GitHub 저장소 최상위에 업로드합니다.
2. 저장소의 **Actions** 탭에서 `Build Android debug APK` 워크플로를 실행합니다.
3. 완료된 실행의 **Artifacts**에서 `bodyweight-coach-debug-apk`를 받습니다.
4. 압축 안의 `app-debug.apk`를 갤럭시 휴대폰에 전송해 설치합니다.
5. Android가 출처 불명 앱 설치 허용을 요구하면 해당 브라우저/파일 앱에 일시적으로 허용합니다.

## 방법 B: Android Studio
1. Android Studio에서 이 폴더를 엽니다.
2. JDK 17, Android SDK 35를 설치합니다.
3. Gradle 동기화 후 **Build > Build APK(s)**를 선택합니다.
4. `app/build/outputs/apk/debug/app-debug.apk`를 설치합니다.

## 주의
- 디버그 APK는 개인 테스트용입니다.
- 다른 키로 서명한 APK는 기존 앱 위에 설치되지 않을 수 있습니다.
- 기존 PWA와 Android APK는 저장공간이 달라 PWA의 기록이 자동 이전되지 않습니다. PWA에서 JSON 내보내기 후 APK에서 가져오기를 사용하십시오.
