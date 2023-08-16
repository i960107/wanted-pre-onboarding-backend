# 원티드 프리온보딩 백엔드 인턴십 - 선발 과제(지원자 김수현)
[1.정보](#정보)  
[2.구현 방법 및 이유에 대한 간략한 설명](#구현-방법-및-이유에-대한-간략한-설명)  
[3.API 명세](#api-명세)  
- [과제 1. 사용자 회원가입](#과제-1-사용자-회원가입)
- [과제 2. 사용자 로그인](#과제-2-사용자-로그인)
- [과제 3. 새로운 게시글을 생성](#과제-3-새로운-게시글을-생성)
- [과제 4. 게시글 목록을 조회](#과제-4-게시글-목록을-조회)
- [과제 5. 특정 게시글을 조회](#과제-5-특정-게시글을-조회)
- [과제 6. 특정 게시글을 수정](#과제-6-특정-게시글을-수정)
- [과제 7. 특정 게시글을 삭제](#과제-7-특정-게시글을-삭제)
- [기타.파일 업로드를 위한 s3 presigned key 발급](#파일-업로드를-위한-s3-presigned-key-발급)
- [기타.만료된 토큰을 갱신](#만료된-토큰을-갱신)

## 정보
### 사용 기술
Java/Spring Boot
### 배포 환경
![](docs/aws.png)
+ aws ec2: 포트포워딩 구성하지 않았으므로 8080포트로 요청.
```
엔드포인트: ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080
```
+ aws rds
```
엔드포인트: wanted-backend-challenge.csdx0qwjpxpv.ap-northeast-2.rds.amazonaws.com
엔진버전: MySQL 8.0.33
```
+ aws s3

### 어플리케이션의 실행 방법
1. 레포지토리를 클론한다.  
    ```git clone https://github.com/i960107/wanted-pre-onboarding-backend.git```
2. 프로젝트 폴더로 이동 후 그레이들을 이용해 빌드한다.   
    ```./gradlew build```
3. jar 파일을 실행한다.  
   ``` java -jar 생성된 <JAR파일 경로> ```
   + 생성된 JAR파일 위치: 프로젝트폴더/build/libs/  

### 엔드포인트 호출 방법
+ 각 [API 명세](#api-명세)의 호출 방법 참고
+ 단, [과제 2. 사용자 로그인](#과제-2-사용자-로그인-1)을 먼저 실행해 발급받은 유효한 jwt 토큰을 이용해야한다.
###  데이터베이스 테이블 구조
  ![include](docs/erd.png)
### 구현한 API 동작을 촬영한 데모 영상 링크
https://www.youtube.com/watch?v=4hXbTjHK2P4

## 구현 방법 및 이유에 대한 간략한 설명
### 이미지 업로드
+ 게시글에 이미지를 추가할 수 있도록 하기 위해 Post 테이블과 PostImage 테이블이 1:N의 관계를 맺도록 설계.
+ 서버에 직접 이미지를 업로드하는 경우에는 요청이 무거워지고 클라이언트에서 업로드 진행 상황을 알 수 없기 때문에 클라이언트에서 직접 S3에 이미지를 업로드.
  1. 클라이언트는 서버에 요청해 presigned Key를 발급받고
  2. 발급받은 presigned Key를 활용해 일정시간안에 S3에 이미지를 업로드한 후
  3. S3에 업로드 된 파일 이름을 서버 게시글 생성 요청에 포함한다.
### 유저/게시글 생성
+ 유저/게시글을 생성하는 경우에는 응답코드를 ```302 FOUND```로 하였다. 이를 통해 응답이 바로 redirect 되도록 해(POST -> CREATE -> GET), 동일한 요청이 중복해서 서버에 전송되는 경우를 방지.
+ ```LOCATION``` 헤더로 redirect 될 url을 표시. 유저 생성 요청이 성공할 경우에는 LOCATION 헤더 값이 ```/```가 되고, 게시글 생성 요청이 성공할 경우에는 LOCATION 헤더 값이 생성된 포스트 리소스 경로를 나타냄(ex /api/posts/1).
### 게시글 삭제
+ 실제 서비스에서는 게시글이 삭제된다고 해서 DB에서 직접 데이터가 삭제되는 것이 위험하기 때문에 테이블의 'enabled' 컬럼값을 false로 수정해 게시글 노출 여부를 표시.

### 인증과 인가
+ 인증과 인가는 모든 요청에서 공통된 관심사이기 때문에 Spring Security를 통해 관심사를 분리해서 관리. 비지니스로직에 인증, 인가 관련 로직이 침투하지 않아 응집도를 높이고 결합도를 낮춤.
+ Spring Security Filter Chain에 **로그인 성공 후 JWT 발급 필터와** 요청시 **JWT 토큰을 체크하는 필터**와 토큰으로 조회한 유저 정보를 이용해서 **요청과 관련된 권한을 체크**하는 PermissionEvaluator를 구현하였다.
### validation
+ validation은 여러 곳에서 진행될 경우 정책이 변경되었을때 일관되지 않은 정책이 적용될 가능성이 있어 요청 DTO 에서만 유효성을 검증.
+ validation이 실패하면 컨트롤러에 진입하기 전에 빠르게 예외를 반환하여 응답속도를 향상.
+ validation 실패시에는 실패한 필드에 대한 정보를 응답 본문에 포함.
### 테스트
+ SpringBootTest를 통해 web 계층과 business 계층을 통합해서 테스트.

## API 명세

### 과제 1. 사용자 회원가입

__api 호출 방법__

```
$ curl 'http://ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080/api/users' -i -X POST \
    -H 'Content-Type: application/json' \
    -d '{
  "name" : "ruby kim",
  "email" : "ruby@gmail.com",
  "nickName" : "ruby",
  "password" : "rubypassword1111"
}'
```

__http 요청 예제__

```
POST /api/users HTTP/1.1
Content-Type: application/json
Content-Length: 113
Host: ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080

{
"name" : "ruby kim",
"email" : "ruby@gmail.com",
"nickName" : "ruby",
"password" : "rubypassword1111"
}
```

__요청 본문 포함 항목__

|Path|Type|Description|
|:---:|:---:|:---:| 
|name|`String`|이름||
email|`String`|이메일 @ 필수 포함|
|nickName|`String`|닉네임|
|password|`String`|패스워드 최소 8자리|

__http 응답 예제__

```
HTTP/1.1 302 Found
Location: /
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY

```

__응답 헤더 포함 항목__

|Name|Description| 
|:---:|:---:| 
|Location|새로 생성된 리소스 주소|

***

### 과제 2. 사용자 로그인

__api 호출 방법__

```
$ curl 'http://ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080/login' -i -X POST \
    -H 'Content-Type: application/json' \
    -d '{
  "email" : "henry@gmail.com",
  "password" : "henrypassword1111"
}'
```

__http 요청 예제__

```
POST /login HTTP/1.1
Content-Type: application/json
Content-Length: 69
Host: ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080

{
  "email" : "henry@gmail.com",
  "password" : "henrypassword1111"
}
```

__요청 본문 포함 항목__

|Path|Type|Description| 
|:---:|:---:|:---:| 
|email|`String`|이메일 @ 필수 포함|
|password|`String`|패스워드 최소 8자리|

__http 응답 예제__

```
HTTP/1.1 200 OK
Content-Type: application/json
auth_token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMCIsImV4cCI6MTY5MjExNzIxNX0.V9diQ7UEY-oG_AeTHDe3B-wa5Q-rZ3lx-0zVtryWMaM
refresh_token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMCIsImV4cCI6MTY5MjEyMDgxM30.ooXC9VCxOfZhpKy9j3fipMpFxebtCp5RHVExoD5ls48
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY

```

__응답 헤더 포함 항목__

|Name|Description|
|:---:|:---:| 
|auth_token|jwt 인증 토큰 유효기간 5분| 
|refresh_token|jwt 리프레시 토큰 유효기간 1시간|

***

### 과제 3. 새로운 게시글을 생성

__api 호출 방법__

+ header의 auth_token 값을 [과제 2. 사용자 로그인](#과제-2-사용자-로그인-1)을 먼저 실행해 발급받은 auth_token으로 교체 필요

```
$ curl 'http://ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080/api/posts' -i -X POST \
    -H 'Content-Type: application/json' \
    -H 'auth_token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1IiwiZXhwIjoxNjkyMTE3MjE0fQ.yl7c5DJhRQRw2LXy_eJ_WJbqjkz3jnJLrnMvamNLw74' \
    -d '{
  "title" : "수정 전 타이틀",
  "content" : "수정전 컨텐츠",
  "imageFileNames" : [ "image1-before", "image2-before" ],
  "thumbnail" : "thumbnail-before"
}'
```

__http 요청 예제__

```
POST /api/posts HTTP/1.1
Content-Type: application/json
auth_token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1IiwiZXhwIjoxNjkyMTE3MjE0fQ.yl7c5DJhRQRw2LXy_eJ_WJbqjkz3jnJLrnMvamNLw74
Content-Length: 170
Host: ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080

{
  "title" : "수정 전 타이틀",
  "content" : "수정전 컨텐츠",
  "imageFileNames" : [ "image1-before", "image2-before" ],
  "thumbnail" : "thumbnail-before"
}
```

__요청 헤더 포함 항목__

|Name|Description| 
|:---:|:---:| 
|auth_token|jwt 인증 토큰 유효기간 5분|

__요청 본문 포함 항목__

|Path|Type|Description| 
|:---:|:---:|:---:| 
|title|`String`|타이틀 |
|content|`String`|내용 |
|imageFileNames|`Array`|이미지 파일 이름 리스트 |
|thumbnail|`String`|썸네일 파일 이름|

__http 응답 예제__

```
HTTP/1.1 302 Found
Location: /api/posts/5
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY

```

__응답 헤더 포함 항목__

|Name|Description| 
|:---:|:---:| 
|Location|새로 생성된 리소스 주소|

***

### 과제 4. 게시글 목록을 조회

__api 호출 방법__

```
$ curl 'http://ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080/api/posts?size=2&page=0' -i -X GET
```

__http 요청 예제__

```
GET /api/posts?size=2&page=0 HTTP/1.1
Host: ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080

```

__요청 파라미터 포함 항목__

|Parameter|Description| 
|:---:|:---:| 
|size|페이지 사이즈 default 10|
|page|페이지 번호 0부터 시작 default 0|

_http 응답 예제__

```
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 355

{
  "pageNo" : 0,
  "pageSize" : 2,
  "totalElements" : 1,
  "totalPages" : 1,
  "last" : true,
  "posts" : [ {
    "postId" : 1,
    "title" : "수정 전 타이틀",
    "content" : "수정전 컨텐츠",
    "thumbnail" : "thumbnail-before",
    "authorId" : 1,
    "authorNickname" : "henry",
    "lastUpdatedAt" : "2023-08-16T01:33:31.916106"
  } ]
}
```

__응답 본문 포함 항목__

|Path|Type|Description|
|:---:|:---:|:---:| 
|pageNo|`Number`|페이지 번호 0부터 시작
|pageSize|`Number`|페이지 사이즈
|totalElements|`Number`| 총 포스트 개수| 
|totalPages|`Number`| 총 페이지 수
|last|`Boolean`| 마지막 페이지라면 true 마지막 페이지가 아니라면 false
|posts|`Array`|조회된 포스트 배열
|posts[].postId|`Number`|포스트 id
|posts[].title|`String`|타이틀
|posts[].content|`String`|내용
|posts[].authorNickname|`String`|작성자 닉네임
|posts[].authorId|`Number`|작성자 id
|posts[].thumbnail|`String`|썸네일 파일 이름
|posts[].lastUpdatedAt|`String`|마지막으로 수정된 날짜

***

### 과제 5. 특정 게시글을 조회

__api 호출 방법__

```
$ curl 'http://ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080/api/posts/2' -i -X GET
```

__http 요청 예제__

```
GET /api/posts/2 HTTP/1.1
Host: ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080

```

__요청 경로 변수__

|Parameter|Description| 
|:---:|:---:| 
|postId|조회할 포스트 id
|title|`String`|타이틀 |
|content|`String`|내용 |
|imageFileNames|`Array`|이미지 파일 이름 리스트 |
|thumbnail|`String`|썸네일 파일 이름|

__http 응답 예제__

```
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 372

{
  "id" : 2,
  "title" : "수정 전 타이틀",
  "content" : "수정전 컨텐츠",
  "authorNickname" : "henry",
  "authorId" : 2,
  "images" : [ {
    "index" : 1,
    "filename" : "image1-before"
  }, {
    "index" : 2,
    "filename" : "image2-before"
  } ],
  "thumbnail" : "thumbnail-before",
  "enabled" : true,
  "lastUpdatedAt" : "2023-08-16T01:33:32.201156"
}
```

__응답 본문 포함 항목__

|Path|Type|Description|
|:---:|:---:|:---:|
|id|`Number`|조회한 포스트 id
|title|`String`|타이틀
|content|`String`|내용
|authorNickname|`String`|작성자 닉네임
|authorId|`Number`|작성자 id
|images[]|Array|이미지 리스트
|images[].filename|`String`|이미지 파일 이름
|images[].index|`Number`|이미지 파일 노출 순서 1부터 시작
|thumbnail|`String`|썸네일 파일 이름
|enabled|Boolean|포스트 활성화 여부
|lastUpdatedAt|`String`|마지막으로 수정된 날짜

***

### 과제 6. 특정 게시글을 수정

__api 호출 방법__

+ header의 auth_token 값을 [과제 2. 사용자 로그인](#과제-2-사용자-로그인-1)을 먼저 실행해 발급받은 auth_token으로 교체 필요

```
$ curl 'http://ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080/api/posts/4' -i -X PUT \
    -H 'Content-Type: application/json' \
    -H 'auth_token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0IiwiZXhwIjoxNjkyMTE3MjE0fQ.8vbIps4c65GkIxEL7vgUF1PTEG2bFEeVYAx7499Z9og' \
    -d '{
  "title" : "수정 후 타이틀",
  "content" : "수정 후 컨텐츠",
  "imageFileNames" : [ "image-after1", "image-after2" ],
  "thumbnail" : "thumbnail-after"
}'
```

__http 요청 예제__

```
PUT /api/posts/4 HTTP/1.1
Content-Type: application/json
auth_token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0IiwiZXhwIjoxNjkyMTE3MjE0fQ.8vbIps4c65GkIxEL7vgUF1PTEG2bFEeVYAx7499Z9og
Content-Length: 168
Host: ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080

{
  "title" : "수정 후 타이틀",
  "content" : "수정 후 컨텐츠",
  "imageFileNames" : [ "image-after1", "image-after2" ],
  "thumbnail" : "thumbnail-after"
}
```

__요청 경로 변수__

|Parameter|Description| 
|:---:|:---:| 
|postId|조회할 포스트 id

__요청 본문 포함 항목__

|Path|Type|Description| 
|:---:|:---:|:---:| 
|title|`String`|타이틀 |
|content|`String`|내용 |
|imageFileNames|`Array`|이미지 파일 이름 리스트 |
|thumbnail|`String`|썸네일 파일 이름|

__http 응답 예제__

```
HTTP/1.1 200 OK
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY

```

***

### 과제 7. 특정 게시글을 삭제

__api 호출 방법__

+ header의 auth_token 값을 [과제 2. 사용자 로그인](#과제-2-사용자-로그인-1)을 먼저 실행해 발급받은 auth_token으로 교체 필요

```
$ curl 'http://ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080/api/posts/3' -i -X DELETE \
    -H 'Content-Type: application/json' \
    -H 'auth_token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwiZXhwIjoxNjkyMTE3MjE0fQ.v5D8b2-Q9Ky_cznXz13LZAEnVlkMhC81nlLZFbazHwI'
```

__http 요청 예제__

```
DELETE /api/posts/3 HTTP/1.1
Content-Type: application/json
auth_token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwiZXhwIjoxNjkyMTE3MjE0fQ.v5D8b2-Q9Ky_cznXz13LZAEnVlkMhC81nlLZFbazHwI
Host: ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080

```

__요청 경로 변수__

|Parameter|Description| 
|:---:|:---:| 
|postId|삭제할 포스트 id

__http 응답 예제__

```
HTTP/1.1 200 OK
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY

```

***

### 파일 업로드를 위한 s3 presigned key 발급

__api 호출 방법__

+ header의 auth_token 값을 [과제 2. 사용자 로그인](#과제-2-사용자-로그인-1)을 먼저 실행해 발급받은 auth_token으로 교체 필요

```
$ curl 'http://ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080/api/images?fileName=testfile1.jpg' -i -X GET \
    -H 'Content-Type: application/json' \
    -H 'auth_token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4IiwiZXhwIjoxNjkyMTE3MjE1fQ.ztRmvXw6u3k4-oKknXbq4NNrDTzMIn6SO1j5AG0K-PM'
```

__http 요청 예제__

```
GET /api/images?fileName=testfile1.jpg HTTP/1.1 Content-Type: application/json auth_token: Bearer
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4IiwiZXhwIjoxNjkyMTE3MjE1fQ.ztRmvXw6u3k4-oKknXbq4NNrDTzMIn6SO1j5AG0K-PM
Host: ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080

```

__요청 파라미터 포함 항목__

|Parameter|Description| 
|:---:|:---:| 
|fileName|업로드할 파일 이름|

__http 응답 예제__

```
HTTP/1.1 200 OK Content-Type: application/json X-Content-Type-Options: nosniff X-XSS-Protection: 1; mode=block
Cache-Control: no-cache, no-store, max-age=0, must-revalidate Pragma: no-cache Expires: 0 X-Frame-Options: DENY
Content-Length: 425

{
"
presignedKey" : "https://wanted-2023-backend-challenge-bucket.s3.ap-northeast-2.amazonaws.com/testfile1.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20230815T163333Z&X-Amz-SignedHeaders=host&X-Amz-Expires=119&X-Amz-Credential=AKIA4H5Y33C4HIUCEIM5%2F20230815%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Signature=1dae4a29728d200c5fb859cba1a4455cb12cbf2f9f689e0ed433b239a3248d2c"
,
"expiresAt" : "2023-08-16T01:35:33"
}

```

__응답 본문 포함 항목__

|Path|Type|Description|
|:---:|:---:|:---:|
|presignedKey|`String`|s3 파일 업로드를 위한 presignedKey 유효기간 2분
|expiresAt|`String`|만료시간

***

### 만료된 토큰을 갱신

__api 호출 방법__

+ 요청 본문의 refreshToken 값을 [과제 2. 사용자 로그인](#과제-2-사용자-로그인-1)을 먼저 실행해 발급받은 refresh_token으로 교체 필요

```
$ curl 'http://ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080/login' -i -X POST \
    -H 'Content-Type: application/json' \
    -d '{
  "refreshToken" : "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5IiwiZXhwIjoxNjkyMTIwODEzfQ.5k7SAWWCU-p9Zk1fyxXWc9VcLdYrih9mJVEG1m-YGe4"
}'
```

__http 요청 예제__

```
POST /login HTTP/1.1
Content-Type: application/json
Content-Length: 144
Host: ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com:8080

{
  "refreshToken" : "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5IiwiZXhwIjoxNjkyMTIwODEzfQ.5k7SAWWCU-p9Zk1fyxXWc9VcLdYrih9mJVEG1m-YGe4"
}
```

__요청 본문 포함 항목__

|Path|Type|Description|
|:---:|:---:|:---:|
|refreshToken|`String`|jwt 리프레시 토큰

__http 응답 예제__

```
HTTP/1.1 200 OK
Content-Type: application/json
auth_token: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5IiwiZXhwIjoxNjkyMTE3MjE1fQ.vyWVm6jzFW-Uc-QlkZ8_v7vGTqfCn9zXDBj13iGteI0
refresh_token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5IiwiZXhwIjoxNjkyMTIwODEzfQ.5k7SAWWCU-p9Zk1fyxXWc9VcLdYrih9mJVEG1m-YGe4
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY

```

__응답 헤더 포함 항목__

|Name|Description|
|:---:|:---:| 
|auth_token|jwt 인증 토큰 유효기간 5분| 
|refresh_token|jwt 리프레시 토큰 유효기간 1시간|
