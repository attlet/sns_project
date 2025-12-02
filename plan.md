# Feature: [githubOauthIntegration]

## 목표
- github OAuth2.0을 이용해 github 계정으로 로그인 기능 구현
- 사용자는 github 계정이 있으면 로그인 및 회원가입 가능
- OAuth 토큰을 안전하게 저장 및 관리

## 테스트 목록

### [ ] Test 1: [github 인증 페이지로 redirection]
- **Given**: application.yml에 올바른 github OAuth 설정이 되어있음
- **When**: /auth/github/login 엔드포인트에 GET 요청을 보냄
- **Then**: response.sendRedirect 가 호출되어 github 인증 페이지로 리다이렉트 됨
- **Notes**: OAuth 설정이 올바르지 않는 경우도 테스트

### [ ] Test 2: [github 콜백 처리 및 토큰 발급]
- **Given**: github에서 인증 후 콜백 URL로 code 파라미터와 함께 요청이 옴
- **When**: /auth/callback 엔드포인트에 GET 요청을 보냄
- **Then**: jwt 토큰이 발급
- **Notes**: 잘못된 code 파라미터에 대한 처리도 테스트


[//]: # (## 리팩터링 백로그)

[//]: # (- [ ] Extract validation logic to separate class)

[//]: # (- [ ] Rename confusing variable names in Parser)