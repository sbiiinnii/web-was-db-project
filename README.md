# Linux Web/WAS/DB 3-Tier Architecture Project

Linux 환경에서 WEB, WAS, DB 서버를 각각 분리하여  
3-Tier Architecture를 직접 구축하고 서버 간 통신 및 보안 설정을 실습한 프로젝트입니다.

사용자의 요청이 WEB → WAS → DB 순서로 전달되고,
DB에서 조회한 결과가 다시 브라우저까지 반환되는 전체 흐름을 구현했습니다.

---

## 1. Architecture

```text
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
- Java Servlet
- JDBC
      |
      | TCP : 3306
      v
DB Server
- IP: 192.168.111.130
- MariaDB
- Database: projectdb
- Table: users
```

### 요청 흐름

```text
Windows Browser
      ↓
WEB - Nginx
      ↓ Reverse Proxy
WAS - Tomcat
      ↓
Java Servlet
      ↓ JDBC
DB - MariaDB
      ↓
users 테이블 조회
      ↓
Servlet에서 HTML 생성
      ↓
Tomcat → Nginx → Browser
```

---

## 2. Server Roles

### WEB Server

Nginx를 사용하여 사용자의 HTTP 요청을 가장 먼저 받습니다.

Nginx Reverse Proxy를 이용하여 WAS 서버의 Tomcat으로 요청을 전달합니다.

```text
Client
  ↓
Nginx :80
  ↓
Tomcat :8080
```

### WAS Server

Tomcat에서 Java Servlet을 실행합니다.

Servlet은 사용자의 요청을 처리하고 JDBC를 이용하여 MariaDB와 통신합니다.

현재 구현된 Servlet:

```text
/hello → HelloServlet
/users → UsersServlet
```

### DB Server

MariaDB를 사용하여 애플리케이션 데이터를 저장합니다.

```text
Database : projectdb
Table    : users
```

WEB이나 외부 사용자가 DB에 직접 접근하지 않도록 하고,
WAS 서버에서만 DB의 3306 포트에 접근할 수 있도록 제한했습니다.

---

## 3. Network / Firewall Policy

### WEB Server

```text
TCP 80
```

HTTP 요청을 위해 허용합니다.

SSH 파일 전송 실습을 위해 TCP 22는 WAS 서버에서 오는 연결만 허용했습니다.

```text
Source : 192.168.111.200
Port   : 22
```

### WAS Server

```text
Port   : 8080
Source : 192.168.111.100
```

WEB 서버만 Tomcat에 접근할 수 있도록 제한했습니다.

### DB Server

```text
Port   : 3306
Source : 192.168.111.200
```

WAS 서버만 MariaDB에 접근할 수 있도록 제한했습니다.

### 통신 구조

```text
Client
   ↓
WEB :80
   ↓
WAS :8080
   ↓
DB :3306
```

---

## 4. Nginx Reverse Proxy

WEB 서버의 Nginx에서 WAS 서버로 요청을 전달하도록 설정했습니다.

```nginx
location / {
    proxy_pass http://192.168.111.200:8080;
}
```

따라서 사용자는 WAS 서버의 8080 포트로 직접 접근하지 않고
WEB 서버를 통해 애플리케이션을 사용할 수 있습니다.

---

## 5. Java Web Application

Tomcat Web Application:

```text
/var/lib/tomcat10/webapps/dbapp/
```

주요 구조:

```text
dbapp/
└── WEB-INF/
    ├── classes/
    │   ├── HelloServlet.class
    │   └── UsersServlet.class
    │
    ├── lib/
    │   └── mariadb-java-client.jar
    │
    └── web.xml
```

Java 소스 파일은 `javac`로 컴파일하여 `.class` 파일로 변환한 뒤
Tomcat에서 실행했습니다.

---

## 6. Servlet Mapping

`web.xml`을 이용하여 URL과 Servlet을 연결했습니다.

```text
/hello → HelloServlet
/users → UsersServlet
```

예를 들어 다음 요청이 들어오면:

```text
/dbapp/users
```

Tomcat이 `web.xml`의 매핑 정보를 확인하여
`UsersServlet`을 실행합니다.

---

## 7. JDBC Connection

Java Servlet에서 MariaDB와 통신하기 위해 JDBC를 사용했습니다.

MariaDB JDBC Driver:

```text
mariadb-java-client.jar
```

Tomcat Web Application의 다음 위치에 배치했습니다.

```text
/var/lib/tomcat10/webapps/dbapp/WEB-INF/lib/
```

DB 연결 구조:

```text
UsersServlet
     ↓
JDBC
     ↓
MariaDB JDBC Driver
     ↓
MariaDB
```

JDBC URL:

```text
jdbc:mariadb://192.168.111.130:3306/projectdb
```

---

## 8. DB Query

`UsersServlet`에서 다음 SQL을 실행하여 데이터를 조회합니다.

```sql
SELECT * FROM users;
```

Java에서의 처리 흐름:

```text
Connection
→ DB 연결

Statement
→ SQL 실행

ResultSet
→ SELECT 실행 결과 저장

rs.next()
→ 조회 결과의 다음 행으로 이동

rs.getString("name")
→ 현재 행의 name 컬럼 값 조회
```

조회된 데이터는 HTML로 만들어 브라우저에 출력합니다.

---

## 9. DB Password Management

DB 비밀번호는 Java 소스 코드에 직접 작성하지 않습니다.

실제 비밀번호는 WAS 서버의 다음 파일에 저장합니다.

```text
/etc/dbapp/db.properties
```

파일 형식:

```properties
db.password=실제_DB_비밀번호
```

`UsersServlet.java`에서는 해당 파일을 읽어 비밀번호를 가져옵니다.

```java
Properties props = new Properties();

FileInputStream fis =
        new FileInputStream("/etc/dbapp/db.properties");

props.load(fis);
fis.close();

String password = props.getProperty("db.password");
```

### 파일 권한

비밀번호 파일은 일반 사용자가 읽지 못하도록 권한을 제한합니다.

```bash
sudo chown root:tomcat /etc/dbapp/db.properties
sudo chmod 640 /etc/dbapp/db.properties
```

의미:

```text
root   → 읽기 / 쓰기
tomcat → 읽기
others → 접근 불가
```

---

## 10. GitHub에서 다시 구축할 때

실제 DB 비밀번호는 GitHub에 업로드하지 않습니다.

Repository에는 다음 예시 파일만 포함합니다.

```text
config/db.properties.example
```

내용:

```properties
db.password=YOUR_DB_PASSWORD
```

새 WAS 서버를 구축할 때 다음 위치에 실제 설정 파일을 생성해야 합니다.

```text
/etc/dbapp/db.properties
```

예시:

```bash
sudo mkdir -p /etc/dbapp
sudo nano /etc/dbapp/db.properties
```

그 후:

```properties
db.password=실제_DB_비밀번호
```

를 입력하고 권한을 설정합니다.

```bash
sudo chown root:tomcat /etc/dbapp/db.properties
sudo chmod 640 /etc/dbapp/db.properties
```

> 실제 DB 비밀번호가 포함된 `/etc/dbapp/db.properties` 파일은 GitHub에 업로드하지 않습니다.

---

## 11. Repository Structure

```text
web-was-db-project/
├── README.md
│
├── config/
│   └── db.properties.example
│
└── was/
    ├── HelloServlet.java
    ├── UsersServlet.java
    └── web.xml
```

`config/db.properties.example`은 실제 비밀번호 파일이 아니라
새로운 서버를 구축할 때 참고하기 위한 예시 파일입니다.

---

## 12. Connection Test

### WEB → WAS

Nginx Reverse Proxy를 통해 Tomcat 페이지 출력 확인

```text
WEB → WAS :8080
```

### WAS → DB

MariaDB 원격 접속 확인

```text
WAS → DB :3306
```

### Full 3-Tier Test

Windows 브라우저에서 다음 주소로 접속:

```text
http://192.168.111.100/dbapp/users
```

DB `users` 테이블의 데이터가 브라우저에 출력되는 것을 확인했습니다.

따라서 다음 전체 통신이 정상적으로 동작합니다.

```text
Windows
   ↓
Nginx
   ↓
Tomcat
   ↓
UsersServlet
   ↓
JDBC
   ↓
MariaDB
   ↓
UsersServlet
   ↓
Tomcat
   ↓
Nginx
   ↓
Browser
```

---

## 13. Troubleshooting

### 1. JDBC Driver 인식 오류

발생한 오류:

```text
No suitable driver found for jdbc:mariadb://...
```

MariaDB JDBC Driver 파일이 존재했지만
Java에서 드라이버가 자동으로 인식되지 않았습니다.

다음 코드를 추가하여 MariaDB JDBC Driver를 명시적으로 로딩했습니다.

```java
Class.forName("org.mariadb.jdbc.Driver");
```

이후 JDBC Driver 오류가 해결되었습니다.

### 2. DB 인증 오류

JDBC Driver 문제 해결 후 다음 오류가 발생했습니다.

```text
Access denied for user 'appuser'
```

드라이버 단계는 통과했지만 DB 인증 과정에서 실패한 것으로 판단했습니다.

WAS 서버에서 MariaDB에 직접 로그인하여 동일한 문제가 발생하는지 확인했고,
DB에 설정된 계정 비밀번호와 애플리케이션 설정 파일의 비밀번호가 일치하지 않는 것을 확인했습니다.

비밀번호를 동일하게 설정한 후 정상적으로 연결되었습니다.

### 장애 분석 과정

```text
No suitable driver
        ↓
JDBC Driver 단계 문제

Driver 문제 해결
        ↓
Access denied
        ↓
DB 연결은 도달
        ↓
계정 / 비밀번호 인증 문제

인증 문제 해결
        ↓
SELECT 성공
        ↓
브라우저 출력 성공
```

오류 메시지를 통해 전체 통신 과정 중
어느 단계까지 성공했고 어느 단계에서 실패했는지 확인하며 문제를 해결했습니다.

---

## 14. Completed

### WEB

- Nginx 설치 및 실행
- HTTP 80 포트 허용
- Reverse Proxy 구성
- WEB → WAS 연결 확인

### WAS

- OpenJDK 17 설치
- Apache Tomcat 10 설치
- Java Servlet 구성
- JDBC Driver 구성
- WEB 서버에서만 8080 접근 허용
- DB 비밀번호 외부 설정 파일 분리
- MariaDB 데이터 조회 구현

### DB

- MariaDB 설치 및 실행
- bind-address 설정
- WAS 서버에서만 3306 접근 허용
- `projectdb` 생성
- WAS 전용 DB 계정 구성
- `users` 테이블 생성
- 데이터 INSERT 및 SELECT 확인

### Final Result

```text
Windows Browser
      ↓
Nginx
      ↓
Tomcat
      ↓
Java Servlet
      ↓
JDBC
      ↓
MariaDB
      ↓
DB 데이터 조회
      ↓
Browser 출력 성공
```

3-Tier Architecture의 전체 요청/응답 흐름을 구현하고 확인했습니다.
