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

## 3. 운영체제 구조

### OS design principles(mechanism, policy)

- Policy: **무엇**이 되게 할 것인가?
- Mechanism: **어떻게** 할 것인가?
- Policy와 mechanism을 분리함으로써 OS 설계를 보다 모듈화할 수 있다.

### Methods for OS design

#### Layering

- OS의 복잡도를 낮추기 위한 방안
- 하나의 layer는 well-defined 함수들로 이뤄진다.
- 하나의 layer는 인접한 layer들과만 통신할 수 있다.
- 설계의 복잡도를 낮출 수는 있으나, 그로 인한 overhead가 발생한다.

- 장점:

  - Layer의 수정이 다른 layer들과 독립적으로 이뤄질 수 있다.

### User mode, kernel mode

- CPU는 2가지 이상의 실행 모드가 있다.

  - System protection을 위해 필요하다. 권한에 따라 접근할 수 있는 메모리, 실행 가능한 명령어가 제한되기 때문이다.
  - 각각의 mode별로 권한(privilege)이 설정된다.
  - Hardware의 지원이 필요하다.

- User mode에서 실행 중인 프로그램은 kernel mode의 권한이 필요한 작업을 수행할 수 없다.

- Kernel mode

  - 모든 권한을 가진 mode
  - OS가 실행되는 mode
  - Privilege 명령어 및 register 접근 가능

- User mode

  - Kernel mode에 비해 낮은 권한의 mode
  - Application이 실행되는 mode
  - Privilege 명령어 실행 불가

### System call

- User mode에서 kernel mode로 진입하기 위한 통로
- Register에 System call에 필요한 파라미터들 및 OS의 리턴값을 저장한다.

### Kernel designs

#### Monolithic kernel

- Kernel의 모든 service가 같은 주소 공간에 위치한다.
- Application은 자신의 주소 공간에 kernel code 영역을 mapping해 kernel service를 이용한다.
- Hardware 계층에 대한 **단일 abstraction을** 정의한다.  
  그리고 이를 사용하기 위해 라이브러리나 애플리케이션에게 **단일한 인터페이스** 를 제공한다.

  ![picture 77](../../images/TMP_OS_1.png)

- 장점: Application과 모든 kernel service가 **같은 주소 공간** 에 위치하기 때문에 system call 및  
  kernel service 간의 데이터 전달 overhead가 적다.

- 단점:

  - 모든 서비스 모듈에 **single binary** 로 되어있기에 **일부분의 수정이 전체에 영향을 미친다.**
  - 각 모듈이 유기적으로 연결되어 있기에 kernel 크기가 커질수록 **유지 보수가 어려워진다.**
  - **한 모듈의 버그가 전체 시스템에 영향을 끼친다.**

#### Micro kernel

- Kernel service를 **기능에 따라 모듈화** 해 각각 **독립된 주소 공간에서 실행** 된다.

  - 이러한 모듈들을 **서버**라 하며, **서버들은 독립된 프로세스로 구현** 된다.

- Micro kernel은 IPC와 같은 단순한 기능만을 제공한다.

- 장점:

  - 각 kernel service가 따로 구현되거 있기에 **서로 간의 의존성이 낮다.**  
    즉, 독립적인 개발이 가능하고 유지 보수가 monolithic kernel보다 상대적으로 용이하다.
  - Kernel service 서버의 간단한 시작, 종료가 가능하다.

- 단점: 독립된 서버들 간의 IPC 및 context switching이 필요해 monolithic kernel보다 성능이 나쁘다.

#### Hypervisor

- 가상화된 컴퓨터 hardware 자원을 제공하기 위한 관리 계층이다.  
  Guest OS와 hardware 사이에 위치한다.

- 각 guest OS들은 서로 다른 Virtual machine에서 수행되며, 서로의 존재를 알지 못한다.

- Hypervisor는 각 guest OS 간의 CPU, memory 등 시스템 자원을 분배하는 등 최소한의 역할을 수행한다.

- 장점:

  - 하나의 물리 컴퓨터에서 여러 종류의 guest OS 운용이 가능하다.
  - 실제 컴퓨터가 제공하는 것과 다른 형태의 명령어 집합 구조를 제공한다.

- 단점: Hardware를 직접적으로 사용하는 다른 OS에 비해 성능이 떨어진다.

![picture 78](../../images/TMP_OS_2.png)

---

## 4. Process

- Process가 만들어지는 과정은 아래와 같다.

  - `Source code` -compiler-> `Object file` -linker-> `Executable file` -loader-> `Memory`

- Compiler

  - Source code를 CPU가 이해할 수 있는 object file로 변환한다.
  - Object file은 object file 자체만으로는 수행이 이뤄지지 못한다.(ex. 외부 라이브러리 코드의 linking이 필요)
  - 프로세스로 변환되기 위한 정보가 삽입되어야 한다.
  - Relative address로 표현된다.

- Linker

  - 여러 object file들과 라이브러리들을 연결해 메모리로 loading될 수 있는 **하나의 executable file** 로 변환
  - Executable

    - 특정 OS에서 실행될 수 있는 파일
    - Absolute address로 표현된다.

- Loader

  - **Executable을 실제 메모리로 올려주는 역할** 을 하는 **OS의 일부**
  - 동작 과정

    - (1) Executable의 header를 읽어 text, data 크기 결정
    - (2) 프로그램을 위한 address space(virtual memory space) 생성
    - (3) 실행 명령어들과 data를 executable로부터 생성한 address space로 복사
    - (4) 프로그램의 argument들을 stack으로 복사
    - (5) CPU 내의 register들을 초기화하고, start-up routing으로 jump

- Runtime system

  - 응용 프로그램의 효율적인 실행을 지원하기 위해 프로그램과 연결해 상호작용한다.
  - C runtime system program execution

    - Process 시작을 위해 kernel은 program counter를 `_start()` 함수의 주소로 지정
    - `_start()` 함수는 동적으로 link된 C library 및 thread 환경을 초기화하는 `_libc_start_main()` 함수 호출
    - 라이브러리 초기화 후 프로그램의 `main()` 함수 호출

### Process management

- Process - abstraction

  - Process는 OS의 abstraction중 하나로, Os가 프로그램을 쉽게 동작시켜주기 위해 존재한다.
  - Execution unit(스케쥴링 단위)
  - Protection domain(서로 침범 불가)

  - 구성요소: Program counter, Text section, Stack, Data section, BSS

    > BSS: 초기화되지 않은 전역변수들 저장

  - Process는 disk에 저장된 프로그램으로부터 변환되어 메모리로 loading된다.

- Linux의 process memory layout은 아래와 같다.

  ![picture 79](../../images/TMP_OS_3.png)

- Process state

  - New: 프로세스가 생성되는 중
  - Running: 프로세스가 실행 중
  - Waiting: 프로세스가 특정 event가 발생하기를 기다리는 중(ex. I/O completion)
  - Ready: 프로세스가 processor에게 할당 받기를 기다리는 중
  - Terminated: 프로세스가 실행을 완료하고 종료된 상태

  > Waiting에서 ready로 가고, ready에서 언제든지 running으로 갈 수 있다.

- Kernel은 ready queue, waiting queue, running queue를 사용해 process들을 상태에 따라 관리한다.

  ![picture 80](../../images/TMP_OS_4.png)

- PCB(Process Control Block)

  - OS가 process들을 관리하기 위해 만들어진 자료구조
  - 아래 내용들을 가진다.

    - Process state
    - Program counter(running 상태일 때 실행되어야 하는 instruction)
    - CPU registers
    - CPU scheduling information
    - Memory management information
    - Accounting information
    - I/O status information

- Context switching

  - CPU가 다른 process를 실행할 때 kernel은 아래 2개 과정을 거쳐야 한다.

    - (1) 이전 process의 상태 저장
    - (2) 새로운 process의 상태 복원

  - Context switching time은 overhead이며, hardware 지원에 따라 좌우된다.

- Processor 구조에 따른 context switching의 차이

  - CISC

    - 복잡한 명령어 set => 효율은 높지만 clock 속도는 낮다.
    - 복잡한 회로로 인해 차지하는 물리적 공간이 많아져 register 용량이 작다.
    - CPU register 개수가 적어 context switching이 빠르다.

  - RISC

    - 간단한 명령어 set => clock 속도가 높고 수행 속도도 빠르다.
    - 절약된 물리적 공간에 많은 register를 장착해야 하기에 context switching 시 보다 큰 overhead가 발생한다.

- Process creation

  - 시스템 내의 process들은 여러 개가 동시에 실행될 수 있어야 하고, 동적으로 생성 및 종료되어야 한다.
  - OS는 process creation과 termination mechanism을 제공한다.

  - Parent process, child process의 관계를 가진다.

    - Child는 parent의 리소스를 공유받을 수도 있고, 아닐 수도 있다.
    - Parent과 child는 동시에 실행될 수 있으며, parent는 child의 종료를 기다릴 수 있다.

  - 메모리 관점에서 child process는 parent process의 중복(duplicate)이다.
    - Child process는 load된 program이 있다.
    - Unix에서 새로운 process는 `fork()`에 의해 생성되며, parent process memory의 복사본을 가진다.  
      그리고 `exec()`를 호출해 새로운 program을 load한다.

  ```c
  int main(void) {
      pid_t pid;
      pid = fork();
      if(pid < 0) { // Error in fork()
  	    fprintf(stderr, "Fork Failed");
  	    exit(-1);
      }     else if(pid > 0) { // Parent process
  	// PARENT!
      }     else if(pid == 0) { // Child process
  	// CHILD!
      }
      return 1;
  }
  ```

- Process termination

  - Process는 `exit()`을 호출해 종료될 수 있으며, `wait()`을 사용해 parent는 child의 종료를 기다릴 수 있다.  
    문제가 생길 때 `abort()`를 호출하면 core dump가 생성되며 `SIGABRT` signal이 호출자 process에 전달된다.

- Cooperating processes

  - 독립적인 process들은 다른 process의 실행에 의해 영향받을 수 없다.

---
