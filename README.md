# 원티드 프리온보딩 백엔드 인턴십 - 선발 과제

- 지원자 성명: 김수현
- 환경
  + aws ec2: ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com
- 어플리케이션의 실행 방법
- 엔드포인트 호출 방법
- 데이터베이스 테이블 구조
  ![include](docs/erd.png)
- 구현한 API 동작을 촬영한 데모 영상 링크:

## API 명세

### 과제 1. 사용자 회원가입

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

### 과제 2. 사용자 로그인

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

### 과제 3. 새로운 게시글을 생성

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

|Name|Description| |
:---:|:---:| 
|auth_token|jwt 인증 토큰 유효기간 5분|

__요청 본문 포함 항목__

|Path|Type|Description| 
|:---:|:---:|:---:| 
|title|`String`|타이틀 |
|content|`String`|내용 |
|imageFileNames|`Array`|이미지 파일 이름 리스트 |
|thumbnail|`String`|썸네일 파일 이름|

_http 응답 예제__

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

### 과제 4. 게시글 목록을 조회

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

## 과제 5. 특정 게시글을 조회

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

## 과제 6. 특정 게시글을 수정

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

## 과제 7. 특정 게시글을 삭제

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
|postId|조회할 포스트 id

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





































































































