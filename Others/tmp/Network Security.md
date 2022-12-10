## (9)

### 웹 보안

- 위협: 무결성, 기밀성, 서비스 거부, 인증

- TCP/IP에서 보안 기능의 위치

  - 네트워크 레벨: IP/IPSec
  - 전송 레벨: SSL, TLS
  - 응용 레벨: S/MIME, Kerberos..

### TLS: 전송 계층 보안

- TLS는 현재 SSL로 진화했으며 SSL만 사용된다.

- 구조: 2 계층의 프로토콜로 구성

  - 상위 프로토콜: 상호 인증, 키 교환
  - 레코드 프로토콜: MAC, 암호화

- 개념:

  - TLS session:

    - 한 클라이언트와 서버 사이의 association
    - 세션 시작은 handshake protocol 사용
    - 세션에서 암호적 보안 매개변수 정의
    - 매 연결 시마다 보안 매개변수들을 협상하지 않기 위해 session 사용

  - TLS connection: 암호화 단위, MAC 단위 -> 즉, key 사용 단위

- Session 상태 매개변수

  - 세션 식별자(session identifier)
  - 대등 인증서(peer certificate)
  - 압축 방법(compression method)
  - 암호 명세(cipher spec)
  - 마스터 비밀(master secret): TLS connection들은 master secret으로부터 유효한 key들을 사용한다.

- Connection 상태 매개변수: 모두 master secret으로부터 생성됨

  - 서버 기록 MAC 비밀(server write MAC secret): for MAC
  - 클라이언트 기록 MAC 비밀(cleint write MAC secret): for MAC
  - 서버 기록 key: for encryption
  - 클라이언트 기록 key: for encryption
  - initialization vectors

- TLS record protocol의 동작

  - `응용 데이터` -> `단편화` -> `압축` -> `MAC 첨부` -> `암호화` -> `SSL record 헤더 붙이기`

- TLS record protocol payload

  - Handshake protocol: 1byte(type) + 3byte(length) + _n_ byte(content)
  - 상위-경고 프로토콜: TLS 동작 간 warning, error 전달 수단
  - 상위-handshake protocol

    - server, client의 상호 인증
    - class 1(key 교환), class 2(server 인증), class 3(client 인증)이 있는데 대부분 class 1, 2까지만 수행
    - 암호화, MAC, SSL record 내의 데이터를 보호하는 데에 사용할 master secret 교환
    - 4단계: `보안 기능 설정` -> `서버 인증과 키 교환` -> `클라이언트 인증과 키 교환` -> `종료`

  - 과정

    |  방향  |         이름          | 단계 |      부가 설명       |
    | :----: | :-------------------: | :--: | :------------------: |
    | c -> s |    `client_hello`     |  1   |          -           |
    | s -> c |    `server_hello`     |  1   |          -           |
    | s -> c |     `certificate`     |  2   |  공개 key + 서명 값  |
    | s -> c | `server_key_exchange` |  2   |          -           |
    | s -> c | `certificate_request` |  2   | class 3 할 때만 수행 |
    | s -> c |  `server_hello_done`  |  2   |          -           |
    | c -> s |     `certificate`     |  3   | class 3 할 때만 수행 |
    | c -> s | `client_key_exchange` |  3   | class 3 할 때만 수행 |
    | c -> s | `certificate_verify`  |  3   | class 3 할 때만 수행 |
    | c -> s | `change_cipher_spec`  |  4   |          -           |
    | c -> s |      `finished`       |  4   |          -           |
    | s -> c | `change_cipher_spec`  |  4   |          -           |
    | s -> c |      `finished`       |  4   |          -           |

  - handshake 이후 client, server는 pre master secret을 각각 가지고 있다.  
    그리고 이 pre master secret으로부터 master secret을 계산해낸다.

- Heardbeat protocol: 정상 동작을 나타내기 위해 보내는 주기적 신호

### HTTPS

- HTTPS 암호화 요소

  - 요청 문서 URL
  - 문서 내용
  - Cookie, HTTP header

- 연결 개시

  - HTTP client
    - TLS handshake 마무리 후 첫 번째 HTTP request 전송
    - 모든 데이터는 TLS 응용 데이터로 전송

- 불완전 종료: 상대방이 종료 경보를 보낼 때까지 기다리지 않고 연결을 종료할 경우

### SSH

- 네트워크 통신을 위한 프로토콜
- 파일 전송, 전자 메일 기능 제공
- 사용자 인증을 위한 프로토콜이 따로 있으며, 서버 인증을 SSH 전송 층 프로토콜에서 진행

- 전송 계층의 host key

  - 서버 인증: 서버는 여러 개의 host key 가질 수 있음
  - 클라이언트는 서버의 host key를 사전에 알고 있어야 한다.

  - 신뢰 방식

    - (1): Client가 관리하는 host 공개 key local database
    - (2): PKI : 클라이언트는 오직 CA의 public key만 알고 있으며 CA가 인증한 모든 host key를 검증할 수 있다.

### 실습 - VPN

- Tunneling: 인터넷의 일정 구간에 별도의 channel을 형성해 사용하는 기법
- VPN(Virtual Private Network): Tunneling을 통해 마치 전용 회선을 가진 것처럼 동작

- VPN 되려면 ip forwarding 되어야함.

  - `/etc/sysctl.conf`에서 `net.Ipv4.Ip_forward=1`로 수정

---

## (10)

- AI 와 보안

  - Security by AI: 악성 코드 탐지, 네트워크 침입 탐비, 보안 관제, 사용자 인증
  - Security for AI: 학습 데이터 요염, 기만 공격
  - Threats by AI: AI Hacker, Deepfake, privacy 침투
  - Safety of AI

> Security: 악의적인 공격자의 공격을 방어하는 관점
> Safety: 오류, 결함, 실수 등에 대한 대응

- AI에 대한 적대적 공격(Security for AI)

  - Backdoor attack: 학습 데이터를 오염시켜 모델을 공격
  - Inversion attack: 학습된 모델에 질의해 학습 데이터를 재현해낸다. -> 학습 데이터의 노출
  - Physical attack: noise를 추가해 결과를 다르게 도출해낸다.

- 방어 기술

  - 적대적 학습: 적대적 예제를 학습 데이터로 활용
  - 탐지: 적대적 예제의 특성을 고려해 정상 데이터와 구분
  - 필터링: 적대적 예제에 추가된 noise 제거

### 실습 - SSL MITM

- `PC(브라우저)` <- HTTPS -> `AWS(공격자 서버)` <- HTTPS -> `website`

- MITM 유도 방법

  - ARP spoofing
  - DNS spoofing: DNS query에 대한 잘못된 response를 주도록 해 가짜 webserver에 접속하게 함
  - DNS보다 먼저 참조하는 hosts 파일 변경

- 피싱, 파밍은 가짜 사이트를 만들어 운용하는 것이지만, MITM은 진짜 사이트를 중계(relay)하기 때문에 모든 인증 수단도 중계 가능

---

## (11)

### 이메일 구조 및 형식

- SMTP: Simple Mail Transfer Protocol, 메일 전송을 위한 프로토콜
  - `MSA` -> `MTA` -> `MDA`
- IMAP, POP3: 메일 서버에서 메시지를 수신하기 위해 사용

- SMTP client-server protocol

  - Client가 server에 TCP 접속을 하면 시작
  - client -> server 명령: 1줄의 텍스트, 4글자 명령 + argument
    - ex) `HELO bar.com`, `RCPT TO:<foo@bar.com>`, `DATA`, `QUIT`
  - server -> client 응답: 1줄 이상의 텍스트, 3자리수 코드 + 추가정보
    - ex) `250 OK`, `550 No such user here`

- POP3

  - POP3 user agent는 TCP를 통해 서버에 연결(port 110)
  - username, password 입력 후 통과하면 메일 검색, 삭제 등을 위한 POP3 명령어 사용

- IMAP

  - TCP port 143
  - POP3보다 강화된 인증
  - POP3에서 제공하지 않는 기능 제공
    - 메시지 읽음 표시 기능
    - email 수신 시 장치로 push

### 이메일 format

- 봉투(envelope): SMTP가 사용, 전송하고 배달 완수를 위한 모든 정보 입력
- 컨텐츠: 수신자에게 전달하려하는 객체로 구성

  - 여러 개의 header들과 body로 구성

- SMTP/5322의 한계

  - 실행 파일이나 바이너리 객체 전송 불가
  - 크기 제한이 있음, 모두 text로만 전송 가능

- MIME: 다국어, 바이너리 전송을 위한 포맷으로 SMTP/5322의 확장이다.

### 이메일 위협과 이메일 보안

- 이메일 보안 위협

  - 인증 관련 위협: 기업 이메일 시스템에 허가받지 않은 접근 허용
  - 무결성 관련 위협: 이메일 컨텐츠의 허가받지 않은 수정 허용
  - 기밀성 관련 위협: 민감한 정보의 허가받지 않은 노출 허용
  - 가용성 관련 위협: 종단 사용자가 메일 전송이나 수신 방해

- 기타 위협: 위장된 송신 주소, 도메인 등을 사용한 평판 실추
- 완화 방법: 도메인 기반 인증 기술 도입, 이메일에 디지털 서명 도입, 이메일 전송의 TLS 암호화, e2e 암호화

### `S/MIME`

- RSA PKCS#7을 MIME type에 추가
- 기능:

  - 디지털 서명
  - 메시지 암호화
  - 압축

- S/MIME contents type

  - signedData, envelopedData, compressedData
  - encoding: 모두 BER(Base Encoding Rules) 포맷을 사용한다.  
    외부 MIME message 안에서 base64로 전
  - envelopedData의 recipient info에는 수신자의 공개 key 인증서 식별자가 있다.
    - 생성 과정: pseudo random session key 생성 -> 수신자의 RSA 공개 key로 session key 암호화 -> recipient info 준비 -> session key로 메시지 암호화
  - signedData 생성과정:
    - (1) 메시지 digest 알고리즘 선택(SHA or MD5)
    - (2) 서명될 내용의 message digest나 hash function 값 계산
    - (3) 서명자의 개인 키를 이용해 message digest 암호화
    - (4) signer info block 생성:
      - 서명자의 공개 키 인증서, message digest 알고리즘 식별자, 암호화된 message digest(서명값) 포함

---
