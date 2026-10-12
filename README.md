# Linux Web/WAS/DB 3-Tier Architecture Project

Linux 환경에서 Web, WAS, DB 서버를 분리하여
3-Tier Architecture를 직접 구축하는 프로젝트입니다.

## Architecture

Windows Client
      |
      | HTTP : 80
      v
WEB Server
- IP: 192.168.111.100
- Nginx
      |
      | HTTP : 8080
      v
WAS Server
- IP: 192.168.111.200
- Apache Tomcat 10
- OpenJDK 17
      |
      | TCP : 3306
      v
DB Server
- IP: 192.168.111.130
- MariaDB
- Database: projectdb

## Firewall Policy

### WEB Server
- TCP 80 허용

### WAS Server
- TCP 8080 허용
- Source: 192.168.111.100 (WEB Server)

### DB Server
- TCP 3306 허용
- Source: 192.168.111.200 (WAS Server)

## Completed

### WEB
- Nginx 설치 및 실행
- UFW TCP 80 허용
- Nginx Reverse Proxy 설정
- WEB -> WAS 연결 확인

### WAS
- OpenJDK 17 설치
- Apache Tomcat 10 설치 및 실행
- TCP 8080 LISTEN 확인
- WEB Server에서만 8080 접근 허용

### DB
- VMware NAT DHCP 문제 해결
- MariaDB 설치 및 실행
- bind-address를 DB 서버 IP로 변경
- TCP 3306 LISTEN 확인
- WAS Server에서만 3306 접근 허용
- projectdb 데이터베이스 생성
- WAS 전용 DB 계정 생성 및 권한 설정
- users 테이블 생성
- 테스트 데이터 추가 및 조회

## Connection Test

WEB -> WAS

- Nginx Reverse Proxy를 통해 Tomcat 기본 페이지 출력 확인

WAS -> DB

- TCP 3306 연결 성공
- MariaDB 원격 로그인 성공
- projectdb의 users 테이블 조회 성공

## Next Step

Tomcat에서 Java 애플리케이션을 구성하여
MariaDB의 데이터를 조회하고 브라우저에 출력할 예정입니다.
