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

## (12)

### APT(Advanced Persistent Threat) - 지능형 지속 위협

- Advanced: 고도화된 공격 도구 활용
- Persistent: 공격이 성공할 때까지 장기간 공격
- Threats: **특별히 선택된 target에 대해 많은 자원과 잘 조직된 기술로 공격**
- ex) Aurora, RSA, APT1, Stuxnet

### 바이러스

- 다른 프로그램을 "감염" 시킨다 -> **attach** or **modify**

  - 악성 코드를 다른 파일에 복사
  - Compression virus: 악성 코드를 압축해 파일의 전체 크기가 변화 없도록 함

- 은닉 방법에 따른 분류

  - 암호화 바이러스(코드를 암호화)

    - Random key로 나머지 부분을 암호화. Key는 암호문에 숨겨둬 key와 암호문을 구변하지 못하게 함.
    - 실행되면 자신만 아는 위치에서 key를 꺼내와 복호화
    - 복제 시 마다 다른 key 사용
    - 방어자는 key의 위치를 찾기 어렵다.

  - Polymorphic 바이러스: **감염 시 마다** 변이(mutate)해 signature 기반 탐지를 어렵게 함.
  - Metamorphic 바이러스
    - **실행 시 마다** 변이, 동작도 실행 시 마다 달라질 수 있음.

- 웜과 기타 확산

  - 코드를 붙이는 것이 아니라 다른 곳으로 자신을 전송시켜 리소스 고갈을 유도한다.
  - 메일, 메신저, 파일 공유 등의 전송 수단을 사용한다.
  - host tables, address books, trusted peers 등을 사용 + network scanning

- 웜 확산 모델

  ![picture 24](/images/TMP_NS_1.png)

- 모리스 웜: 인터넷 전체를 마비시킨 웜, UNIX 대상

  - 실행되고 현재 host를 신뢰하면 다른 host를 찾아 전송
  - UNIX finger protocol 버그, 리모트 프로세스 디버그 옵션의 취약점 활용

- 모바일 코드

  - Javascript 처럼 전송되어와서 실행되는 코드
  - **사용자의 명시적 실행 없이도 실행됨**
  - **바이러스, 웜, 트로이 목마 가능**

- 드라이브 바이 다운로드: 접근하기만 해도 다운로드, 브라우저의 취약성 이용

  - 피싱 등을 사용해 악성 웹사이트로 유도
  - 정상 웹사이트에 드라이브 바이 다운로드를 설치하는 watering-hole 공격 가능

- UI redress 공격: 정상 버튼 내부에 또다른 버튼 숨겨 클릭 유도, 클릭 시 악성 코드 설치
- 키스트로크 하이재킹: 비밀 번호 input 창을 가로챔. Key logging(keyboard interrupt 가로채기)과는 다름

- 스팸: bulk email, 최근에는 bot들에 의해 전송, 악성 코드 이송, 피싱 공격에 활용

- 트로이 목마: 히든 코드를 가진 프로그램

  - **정상 프로그램 기능을 수행하며** 악성행위 병행
  - **정상 프로그램의 기능을 변경해** 악성 행위 수행
  - **정상 프로그램의 기능을 완전히 수정해** 악성 행위만 수행

- 시스템 오염

  - 데이터 파괴, 이상한 메시지 출력, 랜섬웨어, 물리적 손상(바이오스 코드 변경), 논리 폭탄 등

- Attack agent

  - 감염된 컴퓨터(bot, zombie)를 조종해 악성행위 실행
  - DDoS 공격, spam mail 발송 등에 사용
  - 원격 조종을 한다는 점에서 웜과 구분된다.

- Payload - 정보 탈취

  - 키로거: 키보드 입력 도청
  - 스파이웨어: 모니터링, 정보 탈취
  - 피싱
  - 스피어피싱(대량 배포가 아닌 특정인 겨냥)

- Payload - Stealthing

  - Backdoor(trapdoor): 특정한 입력 감지해 실행.(ex 특정 네트워크 포트 listen 등)
  - Rootkit: 프로그램 실행 시 root 권한 획득, 시스템의 모든 것 control 가능
    - syscall 등을 사용해 프로세스나 파일 등에 관한 동작을 숨겨 자신을 은닉한다.

### 대응 방안

- `정책` -> `탐지/인지` -> `취약점 완화` -> `공격 완화`
- 예방
- 대응 방안: 탐지, 식별, 제거

  - 요구사항: 일반성, 시의성, 저항성

- Host-based scanner(백신)

  - 1세데: signature 기반 탐지, 식별
  - 2세대: heuristic rule을 사용해 탐지, integrity checking을 통해 코드 변조 확인
  - 3세대: 프로그램의 행위 모니터링, 행위 기반 탐지(AI)
  - 4세대: 모든 기법들을 혼합해 사용

- Perimeter scanning 방법

  - Firewall, IDS 단에서 악성코드 탐지

    - Mail server, web proxy에서 동작
    - IDS의 트래픽 분석기에서 동작

  - Ingress monitor
  - Egress monitor

### Perimiter 웜 대응

- Class A: signature 기반 탐지, 차단
- Class B: 내용까지 확인
- Class C: packet 단위로 웜의 포함 여부 확인
- Class D: Random destination scan 여부로 판단
- Class E: Rate limiting(외부에서 내부를 scanning하는 트래픽 제한)
- Class F: Rate halting(트래픽량 기준치 초과 시 차단)

### 웜 대응 분산 구조

- 네트워크 상의 다양한 위치에 놓인 센서가 웜 탐지 후 sandboxed environment에 보내어  
  의심스러운 SW를 해당 sandbox에서 실행시켜 악성 분석. patch를 생성해 sandbox에서 실험 후 patch 배포

### 실습

- DoS(Denial of Service) 공격

  - 파괴 공격: 디스크, 데이터, 시스템 파괴
  - 시스템 자원 고갈 공격
  - 네트워크 자원 고갈 공격

- Ping of Death 공격

  - ping을 이용해 ICMP packet 크기를 정상보다 훨씬 크게 만든다.
  - 큰 packet은 네트워크를 통해 routing되어 공격 네트워크에 도달하는 동안 아주 작은 조각들로 쪼개어진다.
  - 공격 대상은 조각화된 packet들을 모두 처리해야 하므로 정상적인 ping보다 훨씬 부하가 많이 걸린다.
  - 보안 대책: 반복적으로 들어오는 일정 개수 이상의 ICMP packet 무시.

- SYN Flooding 공격

  - 공격자가 수많은 SYN packet을 서버애 보내고, 서버는 받은 SYN packet 각각에 대해 SYN/ACK packet을 보내게 된다.  
    이때 서버는 자신이 보낸 SYN/ACK packet에 대한 ACK 응답을 클라이언트로부터 받지 못하면 서버는 session 연결을  
    기다리게 되고, 이로써 서버의 자원이 고갈되게 된다.

  - `sudo hping3 --rand-source $HOST_IP -p 80 -S`
  - 결과: 수많은 `SYN_RECEIVED` 상태의 connection이 생긴다.
  - 보안 대책:
    - 시스템 패치 설치, 짧은 시간에 동일한 형태의 packet을 보내는 공격임을 인지할 경우 해당 IP 주소 대역 차단
    - SYN cookie: 클라이언트로부터 SYN packet 수신하면 간단한 인증 정보를 담은 SYN cookie를 sequence값에 넣고  
      세션을 일단 닫는다. 이후 클라이언트가 SYN cookie 값이 포함된 ACK를 보내면 세션을 다시 열고 통신 시작

- Smurf attack

  - ICMP request의 sender address를 공격자의 IP 주소로 설정하고, broadcast 수행

---

## (13)

### 내부자 위협

- 탐지, 예방이 어렵다.
- 대응 방안:
  - 로그 관리(접근 대상, 행위 등)
  - 문서 중앙 관리: 각자 PC에 저장되는 문서가 없도록

### Intrusion detection(IDS, 탐지)

- 다양한 유형의 침해 탐지
- 가정: 침입자의 행위 패턴은 정상 사용자와 차이가 있으며, 그 차이는 계량할 수 있다.
- 요구사항:
  - 피해 발생 전 최대한 빨리 탐지해 차단
  - 침입 시도 단계에서 차단하는 예방 기능 도입
  - 침입 탐지 과정의 데이터를 모아 추후 탐지력 강화

### 침입 탐지 방법

- 통계적 이상 탐지

  - 일정 기간 동안 정상 사용자 패턴 데이터 수집
  - 공격자 데이터가 있으면 2class로 학습하지만 일반적으로 공격자 데이터는 적다.
  - Threshold 기반 탐지: 평균에서 벗어난 정도를 기준으로 탐지.
  - 사용자 별 profile 생성: 각 사용자마다 패턴을 수집하고 수상하면 도용되었다고 판단

- 규칙 기반 탐지

  - 침입 패턴을 규정하는 규칙 구성, 진화
  - 시그니처(침입자의 고유한 특징) 탐지

### 감사 기록(audit records)

- 침입 탐지에서 활용되는 패턴의 기초 자료
- Native audit records

  - OS가 제공하는 감사 기록
  - 추가적인 수집 software 불필요
  - 필요한 정보가 없을 수도 있으며, 가공이 필요하다.

- 탐지용 audit records

  - IDS가 필요한 정보를 수집하기 위해 별도의 프로그램 사용
  - Native, 탐지용 프로그램 2개가 돌아가는 overhead 발생

- USTAT: 감사기록을 플랫폼에 독립적으로 사용하기 위한 추상화된 체계

### 규칙 기반 탐지 종류

- 규칙 기반 이상 탐지

  - 과거 감사기록으로부터 사용패턴을 식별해 규칙을 자동으로 생성한다.

- 규칙 기반 침입 탐지

  - 전문가가 과거 감사기록 분석해 패턴 찾은 후 이를 규칙화한다.
  - 알려진 침입 기록, 보고된 취약성 등을 중심으로 한다.

### 기본 비율 오류(base-rate fallacy)

- 미탐: 침입을 탐지 못하는 것
- 오탐: 정상 행위를 침입으로 판단하는 것
- 미탐, 오탐률은 threshold와 밀접한 관계를 가진다.  
  threshold가 높으면 미탐율이 높아지고, 낮으면 오탐율이 높아진다.

### 분산 침입 탐지

- 전통 시스템은 단일 host에 대한 침입만 탐지하지만, 여러 host들에 분산된 탐지가 더 효과적이다.

  - 감사 기록 자체의 소스 인증, 무결성 보장이 중요하다.

### Honeypot

- 공격자를 중요한 시스템으로부터 다른 곳으로 유도하는 미끼(가짜) 시스템
- 진짜처럼 보이게 만들어 honeypot으로 끌어낼 수 있어야 한다.
- 중요 시스템으로부터 차단하고, 대응 시간을 벌 수 있다.

---

## (14)

### 무선 네트워크 보안 위협 요소

- 채널: 도청, jamming에 취약
- 이동성
- 자원
- 접근성

### 무선 전송 보안

- 위협: 도청, 메시지 변경 및 삽입, 통신 방해(jamming)
- 대응 방안:
  - 신호 은닉 기술
    - SSID broadcasting 끄기, 신호 강도 줄이기, 지향성 안테나와 신호 차단 기술 활용
  - 암호화

### 무선 접속점 보안

- 위협: 불법 네트워크 접근
- 대응 방안: IEEE 802.1X
  - Port 기반의 네트워크 접근 통제

### 무선 네트워크 보안

- 암호 사용
- 단말 보안 강화
- Router 관리자 비밀번호 변경
- 허가된 MAC 주소를 가진 기기만 연결 허용(but, MAC spoofing에 의해 우회 가능)

### 모바일 기기 보안 전략

- 기기 보안: BYOD(Bring Your Own Device) 정책

  - 자신의 단말로 조직 서비스에 접속
  - IT 관리자는 네트워크 접속 허가 전 각 기기 조사
  - 내 PC 지키미 같은 sw 사용

- 트래픽 보안

  - 모든 트래픽을 TLS나 IPv6처럼 암호화해 안전하게 전송
  - VPN 사용

- 장벽 보안

  - 침입 차단 시스템 정책 수립, 침입 탐지 및 예방 시스템 설정

### 무선 LAN의 보안

- RSN(Robust Security Network)의 서비스와 프로토콜

  ![picture 25](../../images/TMP_NS_2.png)

- IEEE 802.11I 동작 단계

  ![picture 26](../../images/TMP_NS_3.png)

  - STA: 통신할 때 사용할 네트워크 존재 확인
  - AP: 주기적으로 RSN IE(Information Element)로 표현된 자신의 보안 기능을 beacon frame을 통해  
    특정 channel로 broadcast

### 개방 시스템 인증

- IEEE 802.11I 장비와의 하휘 호환성을 위함.  
  보안은 제공하지 않고, 단순히 STA와 API의 식별자만 교환한다.

### 연관(association)

- STS와 API가 연관이 맺어지면 아래 정보를 각각 가진다.

  - 하나의 인증 및 키 관리 도구
  - 하나의 암호 도구 쌍
  - 하나의 그룹 키 암호 도구

- IEEE 802.11I의 인증 동작 단계

  ![picture 27](../../images/TMP_NS_4.png)

### 802.1X의 접근 통제

- 요청자는 네트워크 접속점에 요청을 보내면 네트워크 접속점은 인증 서버의 open port를 통해 인증하고,  
  인증이 완료되면 통제된 port에 접근 가능해진다.

  - 802.1X 제어 채널: 통제 안된 port, open 상태
  - 802.1X 데이터 채널: 통제된 port, 즉 closed 상태 -> 인증 후 접근 가능

### 안전 데이터 전송 단계

- IEEE 802.11I가 데이터를 전송하기 위해 사용하는 두 가지 시스템: TKIP, CCMP

- TKIP(Temporal Key Integrity Protocol)

  - 임시 key 무결성 프로토콜
  - 두 가지 서비스 제공
    - 메기지 무결성(Michael 알고리즘 사용)
    - 데이터 기밀성

- CCMP(Counter Mode CBC MAC Protocol)

  - 메시지 무결성(CBC-MAC 사용)
  - 데이터 기밀성(AES의 CTR mode 사용)

### Firewall

- Firewall: 자체 네트워크와 인터넷 사이에 배치되어 경계(perimiter) 형성

- Firewall 정책

  - IP 주소, protocol 값: source, destination 주소와 port 번호
  - Application(protocol): 허용하는 프로그램
  - User identity
  - Network activity

- Bastion host

  - Proxy로 동작. 일부 기능만 수행하고 일부 서버만 담당

- 제로 트러스트

  - Perimiter 보안과 반대로 **모든 접근에 대해 강화된 인증과 접근제어를 적용** 하는 방어

- 실습

  - `ufw emable`: default가 deny이므로 ftp 접속 실패
  - `ufw allow 21`: ftp 접속 성공
  - `ufw delete 1`: 1번 rule 삭제
  - 상충되는 규칙은 덮어쓴다.
  - 범위가 다른 규칙은 rule 번호가 낮은 규칙이 적용된다.
  - `ufw allow from 192.168.0.0 to any port 21`
  - `ufw insert 1 allow from 192.168.0.0 to any port 21`

---
