# 혼자서 해보는 sns 클론 코딩


> kotiln + spring boot 사이드 프로젝트입니다. kotlin 언어 학습과, 다양한 기술을 적용해보는 실습을 해보기 위한 프로젝트입니다.

<br/>

## 주제 선정

sns 클론 코딩을 주제로 선택했습니다. sns를 선택한 이유는 다음과 같습니다.

1. sns 기능 중 가장 핵심인 포스팅 기능 구현을 통해 kotlin 기본 문법을 익히기 용이하다 판단
2. 단순한 기능부터 복잡한 기능까지 확장하기 용이한 주제라고 생각했음
3. 프로젝트 기간은 적당하다고 가정( 퇴근 후 1~2시간 투자 가능, 25년 2월 정도 까지 생각)

<br/>

## Kotlin 선택한 이유

이번 프로젝트에선 처음으로 코틀린을 사용했습니다. 코틀린은 공부할 수록 생각보다 매력적인 언어였습니다.

### 1. java와의 호환

- jvm 기반 언어로, java와 문법이 비슷해 학습이 다른 언어보다 상대적으로 빠를 것이라 판단
- java 라이브러리, spring 프레임워크와도 함께 잘 동작

### 2. 간결성, 생산성 있는 코드 작성 가능

- java와 비교했을 때 상대적으로 간결한 코드로 동일한 동작을 구현 가능


### 3. 함수형 프로그래밍 패러다임 적용

- 언어 설계가 함수형 프로그래밍 지원을 고려해서 설계되었기에, java보다 짧은 코드로 함수형 프로그래밍 구현이 가능


<br>




## 개발 과정

### 작은 규모로 부터 시작
kotlin 언어를 처음 사용해봐서 익숙하지 않았기에, 최소한의 기능 동작을 구현하는 걸 목표로 시작했습니다.








## 개발 스펙

- kotlin version : 1.9.25
- spring boot version : 3.2.9

  







## 주요 기능

![image](https://github.com/attlet/cloud_project/assets/62745451/1d1820c0-dad2-4477-b1ee-5e93773b18ff)


### 실행중인 인스턴스 목록 확인 
- 내 계정에서 실행되고 있는 인스턴스 목록 확인
  <br>
![image](https://github.com/attlet/cloud_project/assets/62745451/e96253b2-f4e3-4f72-b412-ae772b87bd05)


### 인스턴스 생성 및 삭제
- 미리 생성되어 있는 ami를 통해 인스턴스 생성

- ami의 모습<br>
![image](https://github.com/attlet/cloud_project/assets/62745451/075f4775-7665-4d60-b79f-3187b960ca9d)

- 인스턴스를 생성한 모습<br>
![image](https://github.com/attlet/cloud_project/assets/62745451/14985243-f569-4916-8cba-1683076ccf74)

- 이 옵션을 실행 후 다시 인스턴스 목록을 확인하면 새로 실행되는 모습을 볼 수 있다.
<br>
![image](https://github.com/attlet/cloud_project/assets/62745451/96ea9428-f888-419c-aed7-e4731106289b)


### condor_status 확인

- slave 인스턴스 생성 시, 실행되고 있는 master 노드에 조인이 되는 지 확인할 수 있다.
- htcondor라는 HTC을 통해 작업을 유휴 컴퓨팅 자원 노드에 할당할 수 있다.

- master노드에서 처음 확인한 condor_status <br>
![image](https://github.com/attlet/cloud_project/assets/62745451/d60c5464-5b3f-4c54-89d4-36a8cbccdac3)

- slave노드 새로 생성 후 master의 status <br>
![image](https://github.com/attlet/cloud_project/assets/62745451/7375d8e7-82de-4ce3-bf77-115ec7d8edf4)

### 이미지 생성
- 노드 기반으로 이미지를 생성할 수 있다.

![image](https://github.com/attlet/cloud_project/assets/62745451/f318a9d3-ae27-42b4-a684-63d74431bf2c)



##배운 점, 아쉬운 점

- 파이썬의 boto3를 이용해 aws 인스턴스를 동적으로 생성, 삭제하고, 유휴 자원 상태를 원격으로 관리해보는 경험이었다.
- 작업을 할당해서 조인된 노드끼리 작업이 나눠지는 지 확인을 해보지 못한 게 아쉽다. aws 무료가 끝나 다른 블로그 글들로 공부해 봐야겠다.




