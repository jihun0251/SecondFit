# 01. 인증 — JWT와 Spring Security

> 모든 API 요청은 컨트롤러에 닿기 **전에** 여기를 지납니다. 이 문서를 이해하면 "왜 401이 뜨지?"로 헤매는 시간이 확 줄어듭니다.

---

## 1. 문제 정의: 서버는 당신이 누군지 모른다

HTTP는 **무상태(stateless)** 프로토콜입니다. 요청 하나하나가 독립적이고, 서버는 이전에 누가 뭘 했는지 기억하지 않습니다.

그런데 우리는 이런 걸 해야 합니다.
- "내 상품 목록"을 보여주려면 **내가 누군지** 알아야 한다
- 남의 상품은 수정 못 하게 막아야 한다
- 관리자만 입고 확인을 할 수 있어야 한다

로그인할 때 한 번 확인했는데, 그 다음 요청에서 어떻게 "아까 그 사람"인 걸 알까요?

### 방법 A: 세션 (전통적인 방식)

```
1. 로그인 성공 → 서버가 세션 ID 생성 (예: "abc123")
2. 서버 메모리에 저장:  abc123 → { userId: 12, role: USER }
3. 브라우저에 쿠키로 전달:  Set-Cookie: JSESSIONID=abc123
4. 이후 요청마다 브라우저가 자동으로 쿠키를 실어 보냄
5. 서버는 "abc123"으로 메모리를 뒤져서 누군지 알아냄
```

**문제**: 서버가 **기억**해야 합니다. 서버를 여러 대로 늘리면 1번 서버에 로그인한 사람이 2번 서버로 요청을 보냈을 때 세션이 없어서 인증이 풀립니다. 서버를 재시작해도 전부 로그아웃됩니다.

### 방법 B: JWT (우리가 쓴 방식)

```
1. 로그인 성공 → 서버가 "이 사람은 userId 12, role USER"라고 적힌 종이를 만듦
2. 그 종이에 서버만 아는 도장을 찍음 (서명)
3. 종이를 사용자에게 줌 (서버는 아무것도 기억하지 않음)
4. 이후 요청마다 사용자가 종이를 들고 옴
5. 서버는 도장이 진짜인지만 확인 → 진짜면 종이 내용을 믿음
```

**서버가 아무것도 기억하지 않습니다.** 서버를 100대로 늘려도, 재시작해도 상관없습니다. 종이(토큰) 안에 정보가 다 있으니까요.

---

## 2. JWT 구조 — 종이에 뭐가 적혀 있나

토큰은 이렇게 생겼습니다.

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMiIsInR5cGUiOiJhY2Nlc3MiLCJlbWFpbCI6Im1pbkBlbWFpbC5jb20iLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc1MzUwMDAwMCwiZXhwIjoxNzUzNTAzNjAwfQ.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk
└──────── header ────────┘ └──────────────────── payload ────────────────────┘ └──────── signature ────────┘
```

점(`.`)으로 세 부분이 나뉩니다.

| 부분 | 내용 | 비밀인가? |
|---|---|---|
| header | 어떤 알고리즘으로 서명했는지 (`HS256`) | ❌ 공개 |
| payload | 실제 데이터 (userId, email, role, 만료시각) | ❌ **공개** |
| signature | 서버 비밀키로 만든 서명 | ✅ 서버만 만들 수 있음 |

### ⚠️ 아주 중요: payload는 암호화가 아니라 인코딩이다

앞의 두 부분은 **Base64로 인코딩**된 것뿐입니다. 누구나 디코딩해서 읽을 수 있습니다. [jwt.io](https://jwt.io)에 붙여넣으면 내용이 그대로 보입니다.

**그래서 비밀번호나 주민번호 같은 걸 payload에 넣으면 절대 안 됩니다.**

그럼 왜 안전하냐? **위조가 불가능하기 때문**입니다.

```
공격자가 payload를 "role": "USER" → "role": "ADMIN"으로 바꾼다
   ↓
payload가 바뀌었으니 서명도 달라져야 함
   ↓
서명을 다시 만들려면 서버 비밀키가 필요함
   ↓
비밀키를 모르니 유효한 서명을 못 만듦
   ↓
서버가 검증할 때 "서명 불일치" → 거부
```

즉 JWT는 **"내용을 숨기는" 기술이 아니라 "내용이 변조되지 않았음을 보장하는" 기술**입니다.

---

## 3. 우리 코드 — JwtTokenProvider

토큰을 만들고 검증하는 클래스입니다.

```java
// auth/jwt/JwtTokenProvider.java
@Component
public class JwtTokenProvider {

    private final SecretKey key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret, ...) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret은 32바이트 이상이어야 합니다. 현재: " + keyBytes.length);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        ...
    }
```

### 왜 32바이트를 강제하나?

HS256은 256비트(=32바이트) 이상의 키를 요구합니다. 짧은 키를 쓰면 무차별 대입으로 뚫릴 수 있어서 jjwt 라이브러리가 아예 거부합니다.

여기서 중요한 설계 판단이 있습니다. **이 검사를 생성자에 넣었습니다.**

```java
// ❌ 만약 토큰 만들 때 검사했다면
public String createAccessToken(User user) {
    if (key가 짧으면) throw ...;   // 첫 로그인 시도할 때야 터짐
}

// ✅ 생성자에서 검사
public JwtTokenProvider(...) {
    if (keyBytes.length < 32) throw ...;   // 애플리케이션이 아예 안 뜸
}
```

`@Component`니까 스프링이 시작할 때 이 객체를 만듭니다. 생성자에서 예외가 나면 **애플리케이션이 부팅에 실패**합니다.

> 💡 **왜 이게 더 좋은가?**
> 설정이 잘못됐다면 서버가 뜨자마자 알아야 합니다. 서버는 멀쩡히 떠 있는데 사용자가 로그인 버튼을 눌렀을 때야 500 에러가 나는 것보다, 배포하자마자 실패해서 "아 설정 빠뜨렸네" 하는 게 훨씬 낫습니다. 이런 걸 **fail-fast**라고 합니다.

### 토큰 만들기

```java
private String build(Long userId, String email, User.Role role, String type, long validityMs) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + validityMs);

    JwtBuilder builder = Jwts.builder()
            .subject(String.valueOf(userId))   // 이 토큰의 주인
            .claim("type", type)               // access인지 refresh인지
            .issuedAt(now)                     // 발급 시각
            .expiration(expiry)                // 만료 시각
            .signWith(key);                    // 서명

    if (email != null) builder.claim("email", email);
    if (role != null) builder.claim("role", role.name());

    return builder.compact();
}
```

- `subject`는 JWT 표준에 정의된 필드로 "이 토큰이 누구에 대한 것인가"를 뜻합니다. 우리는 userId를 넣었습니다.
- `claim`은 우리가 임의로 추가하는 정보입니다.
- `expiration`이 **반드시 필요합니다.** 만료가 없으면 토큰이 한 번 유출됐을 때 영원히 악용됩니다.

### 액세스 토큰과 리프레시 토큰

우리는 두 종류를 발급합니다.

| | 수명 | 담긴 정보 | 용도 |
|---|---|---|---|
| accessToken | 1시간 | userId, email, role | 실제 API 호출 |
| refreshToken | 14일 | userId만 | 액세스 토큰 재발급 |

**왜 나누나?**

액세스 토큰은 요청마다 실려 다녀서 유출 위험이 큽니다. 그래서 수명을 짧게 합니다. 하지만 1시간마다 로그인하라고 하면 불편하죠. 그래서 오래 사는 리프레시 토큰을 따로 주고, 이건 재발급할 때만 씁니다(= 노출 빈도가 낮음).

> ⚠️ **솔직한 고백**: 우리 프로젝트는 리프레시 토큰을 **발급만 하고 재발급 API를 안 만들었습니다.** 명세에 없었거든요. 지금은 1시간 지나면 다시 로그인해야 합니다. 나중에 `POST /auth/refresh`를 추가하면 완성됩니다.

---

## 4. Spring Security 필터 체인 — 요청이 컨트롤러에 닿기 전

Spring Security의 핵심 개념입니다. **요청은 컨트롤러에 도착하기 전에 여러 개의 필터를 순서대로 통과합니다.**

```
HTTP 요청
   ↓
[CorsFilter]              ← 다른 출처(localhost:5173)에서 온 요청 허용?
   ↓
[JwtAuthenticationFilter] ← ⭐ 우리가 만든 것. 토큰 까서 신원 확인
   ↓
[UsernamePasswordAuthenticationFilter]  ← 스프링 기본 (우리는 안 씀)
   ↓
[AuthorizationFilter]     ← 이 URL에 접근할 권한이 있나?
   ↓
DispatcherServlet → ProductController
```

필터는 **문지기가 여러 명 서 있는 복도**라고 생각하면 됩니다. 각 문지기가 통과시켜야 다음으로 갑니다.

### 우리 필터가 하는 일

```java
// auth/jwt/JwtAuthenticationFilter.java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {

    String token = resolveToken(request);   // "Bearer xxx"에서 xxx만 추출

    if (StringUtils.hasText(token) && jwtTokenProvider.isValidAccessToken(token)) {
        Claims claims = jwtTokenProvider.parseClaims(token);

        UserPrincipal principal = new UserPrincipal(
                jwtTokenProvider.getUserId(claims),
                jwtTokenProvider.getEmail(claims),
                jwtTokenProvider.getRole(claims));

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);  // ⭐ 여기
    }

    filterChain.doFilter(request, response);   // 다음 필터로
}
```

핵심은 `SecurityContextHolder.getContext().setAuthentication(...)` 한 줄입니다.

**SecurityContextHolder**는 "지금 이 요청을 보낸 사람이 누구인가"를 담아두는 보관함입니다. 내부적으로는 `ThreadLocal`을 씁니다 — 요청마다 스레드가 하나씩 배정되니까, 스레드별로 다른 값을 담을 수 있는 저장소를 쓰는 겁니다.

여기에 인증 정보를 넣어두면 나중에 컨트롤러에서 이렇게 꺼내 쓸 수 있습니다.

```java
@PostMapping
public ResponseEntity<...> create(@AuthenticationPrincipal UserPrincipal principal, ...) {
    productService.create(principal.getUserId(), request);   // 내가 누군지 알 수 있음
}
```

### ⭐ 여기서 중요한 설계 판단 두 가지

**(1) 필터에서 401을 직접 내지 않는다**

토큰이 없거나 잘못됐으면 그냥 **아무것도 안 하고 통과**시킵니다. 왜?

`GET /api/v1/products`(상품 목록)는 로그인 없이도 볼 수 있어야 합니다. 만약 필터에서 "토큰 없으면 401!"이라고 막아버리면 비로그인 사용자가 상품을 못 봅니다.

그래서 필터는 **"신원 확인"만** 하고, **"통과시킬지 말지"는 뒤쪽 AuthorizationFilter가 판단**합니다. 역할을 나눈 겁니다.

- 인증(Authentication) = **당신이 누구인가**
- 인가(Authorization) = **당신이 이걸 할 수 있는가**

**(2) DB를 조회하지 않는다**

요청마다 `userRepository.findById()`를 하면 API 호출 1회당 쿼리가 1개씩 추가됩니다. 우리는 토큰 안의 claim(userId, email, role)만으로 `UserPrincipal`을 만들기 때문에 **DB 조회가 0번**입니다.

> 💡 단점도 있습니다. 관리자가 DB에서 사용자를 `ADMIN`으로 승격시켜도, 그 사람이 이미 갖고 있는 토큰에는 여전히 `USER`가 적혀 있습니다. **다시 로그인해야 새 역할이 반영됩니다.** 지훈님이 관리자 계정 만들 때 "재로그인 필요"라고 안내한 이유가 이것입니다.

---

## 5. SecurityConfig — 규칙 정의

```java
// global/config/SecurityConfig.java
.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers("/uploads/**").permitAll()

        .requestMatchers(HttpMethod.POST, "/api/v1/users/signup").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()

        .requestMatchers(HttpMethod.GET, "/api/v1/products/me").authenticated()
        .requestMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/*").permitAll()

        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

        .anyRequest().authenticated()
)
```

### ⚠️ 순서가 전부다

규칙은 **위에서부터 순서대로 검사하고, 처음 걸리는 규칙이 이깁니다.**

`/api/v1/products/me`가 `/api/v1/products/*`보다 **위에** 있어야 하는 이유:

```
요청: GET /api/v1/products/me

만약 순서가 반대라면:
  1. "/api/v1/products/*" 와 매칭됨 → permitAll → 통과 ❌
     ("me"가 상품 ID 자리로 해석됨)

지금 순서라면:
  1. "/api/v1/products/me" 와 매칭됨 → authenticated → 토큰 필요 ✅
```

같은 이유로 컨트롤러에서도 `@GetMapping("/me")`가 `@GetMapping("/{productId}")`보다 **위에** 있어야 합니다.

### `hasRole("ADMIN")`의 함정

```java
.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
```

`hasRole("ADMIN")`은 내부적으로 **`"ROLE_ADMIN"`** 권한을 찾습니다. 그래서 필터에서 권한을 만들 때 접두사를 붙였습니다.

```java
new SimpleGrantedAuthority("ROLE_" + role.name())   // "ROLE_ADMIN"
```

만약 `hasAuthority("ADMIN")`을 쓴다면 접두사 없이 그대로 비교합니다. 헷갈리기 쉬운 부분이니 기억해 두세요.

### 무상태 설정

```java
.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

"세션을 만들지 마라"는 뜻입니다. JWT를 쓰기로 했으니 서버가 세션을 들고 있을 이유가 없습니다. 이걸 안 하면 스프링이 습관적으로 세션을 만들어서 메모리를 낭비합니다.

### CSRF를 끈 이유

```java
.csrf(csrf -> csrf.disable())
```

CSRF 공격은 **브라우저가 쿠키를 자동으로 실어 보내는 성질**을 악용합니다. 악성 사이트가 몰래 `POST /transfer` 요청을 보내면 브라우저가 쿠키를 같이 보내버려서 인증이 통과되는 거죠.

우리는 쿠키가 아니라 **`Authorization` 헤더**로 인증합니다. 헤더는 브라우저가 자동으로 붙여주지 않습니다. JS 코드가 명시적으로 넣어야 하고, 악성 사이트의 JS는 우리 도메인의 localStorage를 읽을 수 없습니다. 그래서 CSRF 위험이 구조적으로 없습니다.

> ⚠️ 만약 나중에 토큰을 쿠키에 저장하도록 바꾼다면, **CSRF 보호를 다시 켜야 합니다.**

---

## 6. 401과 403 — 뭐가 다른가

| 코드 | 의미 | 상황 |
|---|---|---|
| **401** Unauthorized | "당신이 **누군지 모르겠다**" | 토큰 없음 / 만료 / 위조 |
| **403** Forbidden | "누군진 알겠는데 **권한이 없다**" | USER가 관리자 API 호출 |

이름이 헷갈리게 지어져 있습니다. 401이 "Unauthorized(인가 안 됨)"인데 실제로는 **인증** 실패고, 403 "Forbidden"이 **인가** 실패입니다. 외우는 수밖에 없습니다.

### 왜 별도 핸들러를 만들었나

```java
// auth/jwt/JwtAuthenticationEntryPoint.java  (401 담당)
@Override
public void commence(HttpServletRequest request, HttpServletResponse response,
                     AuthenticationException authException) throws IOException {
    ErrorCode ec = ErrorCode.UNAUTHORIZED;
    response.setStatus(ec.getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    objectMapper.writeValue(response.getWriter(), ApiResponse.error(ec.getCode(), ec.getMessage()));
}
```

**문제**: 우리는 `@RestControllerAdvice`(GlobalExceptionHandler)로 예외를 잡아 예쁜 JSON을 만듭니다. 그런데 그건 **컨트롤러에서 발생한 예외**만 잡습니다.

401/403은 **필터 단계**에서 발생합니다. 컨트롤러에 도달하기 전이라 `@RestControllerAdvice`가 못 잡습니다. 그냥 두면 스프링 기본 에러 페이지(HTML)가 나가서 프론트가 파싱에 실패합니다.

그래서 필터 단계 전용 핸들러를 만들어 **같은 JSON 형식**으로 맞춘 겁니다.

```java
.exceptionHandling(e -> e
        .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 401
        .accessDeniedHandler(jwtAccessDeniedHandler)            // 403
)
```

이제 프론트는 어떤 에러든 `{success: false, error: {code, message}}` 하나만 처리하면 됩니다.

---

## 7. 로그인 서비스 — 보안 디테일

```java
// auth/service/AuthService.java
public LoginResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new BusinessException(ErrorCode.LOGIN_FAILED);
    }
    ...
}
```

### 왜 "없는 이메일"과 "비밀번호 틀림"을 같은 에러로 처리하나?

만약 이렇게 만들었다면:

```java
.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));   // ❌ "없는 이메일입니다"
```

공격자가 이메일을 하나씩 넣어보면서 응답을 비교합니다.

```
test1@gmail.com → "존재하지 않는 회원입니다"  → 가입 안 됨
test2@gmail.com → "비밀번호가 틀렸습니다"     → ⭐ 가입되어 있음!
```

이렇게 **가입된 이메일 목록을 알아낼 수 있습니다.** 이걸 **사용자 열거 취약점(User Enumeration)** 이라고 합니다. 알아낸 이메일로 피싱 메일을 보내거나 무차별 대입 공격을 집중할 수 있죠.

그래서 둘 다 똑같이 `"이메일 또는 비밀번호가 올바르지 않습니다"`로 응답합니다.

### 비밀번호는 어떻게 저장되나

```java
// global/config/PasswordConfig.java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

DB에는 비밀번호 원문이 아니라 **BCrypt 해시**가 저장됩니다.

```
"abcd1234"  →  $2a$10$N9qo8uLOickgx2ZMRZoMye1VdLLKPYqYJdTGF0kL8bqRZ0K1x8yFa
```

- **단방향**입니다. 해시에서 원문을 되돌릴 수 없습니다.
- 그래서 로그인 검증은 "해시를 풀어서 비교"가 아니라 **"입력값을 같은 방식으로 해시해서 비교"** 입니다. 그게 `passwordEncoder.matches(입력, 저장된해시)`입니다.
- BCrypt는 같은 비밀번호라도 매번 다른 해시를 만듭니다(salt 때문). 그래서 `equals()`로 비교하면 절대 안 되고 반드시 `matches()`를 써야 합니다.

---

## 8. 로그아웃 — 서버가 할 수 있는 게 없다

```java
// auth/controller/AuthController.java
@PostMapping("/logout")
public ResponseEntity<ApiResponse<Map<String, String>>> logout() {
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok(ApiResponse.success(Map.of("message", "logout success")));
}
```

이 코드는 사실상 **아무것도 하지 않습니다.**

세션 방식이라면 서버 메모리에서 세션을 지우면 끝입니다. 하지만 JWT는 **서버가 토큰을 들고 있지 않습니다.** 이미 발급된 토큰은 만료 시각까지 유효합니다. 서버가 "그거 무효야"라고 할 방법이 없습니다.

**그래서 실제 로그아웃은 프론트가 합니다.**

```typescript
// frontend/src/contexts/AuthContext.tsx
const logout = () => {
    authApi.logout().catch(() => undefined);   // 서버 호출은 형식적
    tokenStorage.clear();                       // ⭐ 실제로는 이게 로그아웃
    localStorage.removeItem(USER_KEY);
    setUser(null);
};
```

**서버에서 강제로 무효화하려면** 두 가지 방법이 있습니다.
1. **블랙리스트**: Redis에 폐기된 토큰을 저장하고, 요청마다 대조 → 하지만 이러면 "무상태"의 장점이 사라짐
2. **짧은 액세스 토큰 + 리프레시 토큰 폐기**: 액세스 토큰은 어차피 1시간이면 만료되니, 리프레시 토큰만 DB에서 지워서 재발급을 막음 → 현실적인 절충안

우리는 개인 프로젝트라 여기까지 안 갔지만, 컨트롤러 주석에 확장 방법을 적어뒀습니다.

---

## 9. 전체 흐름 다시 보기

```
[로그인]
POST /api/v1/auth/login  { email, password }
   ↓ SecurityConfig: permitAll → 필터 통과
AuthController.login()
   ↓
AuthService.login()
   ├─ userRepository.findByEmail()
   ├─ passwordEncoder.matches()         ← BCrypt 대조
   ├─ status가 ACTIVE인지 확인
   └─ jwtTokenProvider.createAccessToken()  ← 서명된 토큰 생성
   ↓
{ accessToken: "eyJ...", refreshToken: "eyJ...", user: {...} }
   ↓
프론트: localStorage에 저장

[이후 모든 요청]
GET /api/v1/products/me
Authorization: Bearer eyJ...
   ↓
JwtAuthenticationFilter
   ├─ 헤더에서 토큰 추출
   ├─ 서명·만료 검증
   └─ SecurityContext에 UserPrincipal 저장
   ↓
AuthorizationFilter: "/products/me"는 authenticated → 인증됐으니 통과
   ↓
ProductController.getMyProducts(@AuthenticationPrincipal UserPrincipal principal)
   → principal.getUserId()로 내 상품만 조회
```

---

## 스스로 확인해보기

**Q1.** JWT payload에 비밀번호를 넣으면 안 되는 이유는?

**Q2.** `JwtAuthenticationFilter`에서 토큰이 유효하지 않을 때 401을 바로 던지지 않고 그냥 통과시키는 이유는?

**Q3.** 관리자로 승격시킨 뒤 **재로그인**해야 하는 이유는?

**Q4.** `SecurityConfig`에서 `/api/v1/products/me` 규칙을 `/api/v1/products/*`보다 아래에 두면 어떤 일이 벌어질까요?

**Q5.** 로그인 실패 시 "없는 이메일"과 "비밀번호 틀림"을 구분해서 알려주면 어떤 위험이 있나요?

<details>
<summary>답 보기</summary>

**A1.** payload는 암호화가 아니라 Base64 인코딩일 뿐이라 누구나 디코딩해서 읽을 수 있습니다. JWT는 내용을 숨기는 기술이 아니라 변조를 막는 기술입니다.

**A2.** 상품 목록처럼 로그인 없이도 볼 수 있는 API가 있기 때문입니다. 필터는 "신원 확인"만 하고, "통과 여부"는 뒤쪽 AuthorizationFilter가 URL별 규칙에 따라 판단합니다. 인증과 인가의 책임 분리입니다.

**A3.** 역할(role)이 JWT 토큰 안에 박혀 있고, 필터는 DB를 조회하지 않기 때문입니다. DB를 ADMIN으로 바꿔도 이미 발급된 토큰에는 USER가 적혀 있습니다. 새 토큰을 받아야 반영됩니다.

**A4.** `/api/v1/products/*`가 먼저 매칭되어 permitAll이 적용됩니다. 그러면 "me"가 상품 ID 자리로 해석되어, 로그인하지 않은 사람도 이 URL에 접근할 수 있게 됩니다(그리고 `principal`이 null이라 NullPointerException이 납니다).

**A5.** 공격자가 이메일을 하나씩 넣어보며 응답 차이로 가입 여부를 알아낼 수 있습니다(사용자 열거 취약점). 알아낸 이메일 목록은 피싱이나 무차별 대입 공격의 표적이 됩니다.

</details>

---

## 직접 해보면 좋은 것

1. 로그인 후 받은 accessToken을 복사해서 [jwt.io](https://jwt.io)에 붙여넣어 보세요. payload가 그대로 읽히는 걸 눈으로 확인하면 "암호화가 아니다"가 확 와닿습니다.
2. `test.http`의 7번(잘못된 토큰)을 실행해서 401 JSON이 어떻게 오는지 보세요.
3. `application.yaml`의 `access-token-validity-ms`를 `10000`(10초)으로 바꾸고 로그인 후 10초 뒤에 API를 호출해 보세요. 만료가 어떻게 동작하는지 체감할 수 있습니다. (확인 후 되돌리세요)
