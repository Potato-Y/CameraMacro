# Camera Macro

이 프로젝트는 단순하게 카메라 메크로 기능을 수행하는 코드들 입니다.

### 이 프로젝트는 다음 오픈소스 및 코드를 사용합니다.

-   CameraX<br>
    [Apache License 2.0](https://github.com/arindamxd/android-camerax/blob/master/LICENSE)
-   camera-sample<br>
    [Apache License 2.0](https://github.com/android/camera-samples/tree/main/CameraXBasic)

### 다음의 기능을 포함하고 있습니다.

-   카메라 줌인 기능
-   설정한 초 동안 반복 촬영
    -   매일
    -   특정 요일
    -   특정 요일의 특정 시간 사이


### 확인된 문제
- 엑시노스 AP가 탑재된 갤럭시 디바이스에서만 테스트 완료.
- 단, 위 상황에서도 내부에서 오류가 발생하는 것으로 확인
- 본 코드를 그대로 사용할 경우 전체화면 관련 버그가 존재함
    - 전체화면 해제시 정상적이지 않은 화면 표시
    - 최신 안드로이드에서 작동을 보장하지 않음

