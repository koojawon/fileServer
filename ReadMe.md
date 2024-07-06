# Flat Server

<hr>

![Static Badge](https://img.shields.io/badge/java-17.0.2-23ED8B00?logo=openjdk&logoColor=white)
![Static Badge](https://img.shields.io/badge/Spring%20Boot-3.2.0-236DB33F?logo=spring&logoColor=white)

## 기존 설계

FE -> Server(AWS) -> Listener Server -> AI Process

사전에 PDF형식의 악보를 업로드 하여 FE와 AI 프로세스가 사용할 수 있는 MXL로 변환,
WebRTC 프로토콜을 이용하여 Listener Server와 FE를 연결한 후 오디오를 실시간 전송하여
현재 악보 위치값을 WebRTC Data Channel을 통해 수신하며 현재 연주 위치를 추적함.


