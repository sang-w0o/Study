# OS

## 1. 개요

- 운영체제란 무엇인가?

  - 하드웨어를 손쉽게, 그리고 효율적으로 사용할 수 있는 **abstraction** 제공
  - 자원의 공유 및 분배를 위한 **policy** 결정

### Abstractions

- Program

  - 컴퓨터를 실행시키기 위한 일련의 순차적으로 구성된 명령어의 모음
  - Disk와 같은 secondary storage에 저장되어 있다.
  - static하다.

- Process

  - **실행되고 있는** 프로그램의 추상화
  - Program Counter, Stack, Data Section으로 구성
  - Dynamic하다.
  - 각 process가 사용하는 virtual memory space는 서로 철저히 독립되어 있어야 한다.

- Address space

  - Process가 차지하는 메모리 공간
  - virtual, not physical
  - 서로의 주소 공간을 침범할 수 없도록 해주는 protection domain의 역할을 한다.

- File

  - Process에서 읽고 쓸 수 있는 persistent storage
  - Process는 데이터가 실제로 저장되는 위치를 알지 않는다.

- Port

  - 컴퓨터 시스템이 메시지를 주고받는 communication endpoint
  - Port를 통해 송수신하는 데이터를 어떤 process가 처리해야 하는지 지정한다.

- Software의 구분

  - System software: 컴퓨터 시스템을 구동시키는 software  
    OS, compiler, assembler 등

  - Application software: 특정 용도로 사용되는 software  
    web browser 등

- OS는 **항상 동작** 한다.  
  또한 통제 기능을 가져 항상 자원에 대한 관리, 감시 활동을 하며 하드웨어에 대한 제어도 한다.

- `hardware` <-> `OS` <-> `Application` 의 3-tier 구조로 이루어져 있다.

---

## 2. 운영체제 구조

- Automatic Job Sequencing

  - 사람의 관여 없음
  - 이전 작업 종료되자마자 다음 작업 실행 -> 일괄 처리(batch)보다 성능 향상
  - I/O에 의해 발생하는 CPU idle time 개선은 없음

- Spooling(Simultaneous Peripheral Operation On-Line)

  - CPU의 idle time을 줄이기 위함
  - I/O와 computation의 동시 진행 가능
  - Spooling을 통해 사용자는 여러 개의 인쇄 작업을 프린터에 순차적으로 요청 가능

- Multiprogramming

  - CPU의 idle time을 줄이기 위함
  - 2개 이상의 작업을 동시에 실행
  - OS는 여러 개의 작업을 메모리에 동시에 유지
  - 현재 실행중인 작업이 I/O를 할 경우, 다음 작업을 순차적으로 실행
  - 스케쥴링: FCFS(First Come First Served)
  - 단점: 사용자가 실행 중인 작업에 대해 관여할 수 없다.

  - 문제점들

    - 다른 job이 수행되기 위해서는 현재 수행되는 job이 I/O를 해야 한다.
    - 공평성을 유지할 수 있어야 한다.
    - High priority로 수행할 필요성은 job scheduling으로 해결되지 못했다.

- Timesharing

  - CPU의 실행 시간을 time slice로 나눈 것.
  - 모든 프로그램은 time slice 동안 CPU를 점유하고, 그 시간에 끝나면 CPU를 양보(yield) 한다.
  - 여러 개의 작업들이 CPU switching(context switching)을 통해 동시에 실행된다.
  - CPU switching이 매우 빈번하게 일어난다.  
    하지만 context switching이 일어나는 동안 사용자는 실행중인 프로그램에 관여가 가능해진다.

- Multitasking

  - 여러 개의 task들이 CPU와 같은 자원을 공유하도록 한다.
  - 하나의 job은 동시에 실행할 수 있는 여러 개의 task들로 나눠질 수 있다.  
    `job = n * task`
  - 사용자가 여러 개의 프로그램을 실행할 수 있도록 하며, CPU가 idle 상태일 때는 background 작업을 실행 가능하도록 한다.

  - 문제점들

    - 복잡한 메모리 관리 시스템: 동시에 여러 개의 프로그램이 메모리에 상주해야 하며 그에 맞는 관리, 보호 시스템이 필요하다.
    - 적절한 응답 시간을 제공하기 어렵다.
    - 서로 독립된 task들이 동시에 실행되는 concurrent execution을 제공하기 위한 CPU scheduling이 필요하다.
    - Job들 간의 ordered execution이 필요하다.

---
