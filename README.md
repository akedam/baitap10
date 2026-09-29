# Bài tập JWT với Spring Boot 3, Security 6 và Nimbus

Ví dụ này triển khai đúng luồng của bài giảng nhưng thay ba thư viện `jjwt-*` bằng
`com.nimbusds:nimbus-jose-jwt`.

## Điểm chính

- `JwtService` tạo JWT HS256 bằng `SignedJWT` + `MACSigner`.
- Token được parse và xác minh bằng `SignedJWT.parse` + `MACVerifier`.
- Kiểm tra riêng thuật toán, chữ ký, `iss`, `sub`, `iat` và `exp`.
- Spring Security chạy stateless với `OncePerRequestFilter`.
- Mật khẩu được băm BCrypt; phản hồi API không bao giờ trả trường mật khẩu.
- H2 in-memory giúp chạy bài tập ngay mà không cần cài MySQL.

## Chạy ứng dụng

Yêu cầu Java 17+ và Maven 3.9+:

```powershell
mvn spring-boot:run
```

Ứng dụng chạy tại `http://localhost:8005`.

## Thử API

Tạo tài khoản:

```powershell
$body = @{
  email = "student@example.com"
  password = "Password123!"
  fullName = "Nguyen Van A"
  image = "u1.jpg"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:8005/auth/signup `
  -ContentType application/json -Body $body
```

Đăng nhập và lấy token:

```powershell
$login = @{email="student@example.com"; password="Password123!"} | ConvertTo-Json
$result = Invoke-RestMethod -Method Post -Uri http://localhost:8005/auth/login `
  -ContentType application/json -Body $login
$token = $result.token
```

Gọi tài nguyên được bảo vệ:

```powershell
Invoke-RestMethod -Uri http://localhost:8005/users/me `
  -Headers @{Authorization="Bearer $token"}

Invoke-RestMethod -Uri http://localhost:8005/users `
  -Headers @{Authorization="Bearer $token"}
```

## Các endpoint

| Method | URL | Quyền |
|---|---|---|
| POST | `/auth/signup` | Công khai |
| POST | `/auth/login` | Công khai |
| GET | `/users/me` | Bearer token |
| GET | `/users` | Bearer token |
| GET | `/login` | Công khai |
| GET | `/profile` | Trang HTML công khai, API bên trong yêu cầu token |

## Cấu hình bí mật

Giá trị mặc định trong `application.yml` chỉ dành cho demo. Khi chạy ngoài môi trường học tập,
hãy đặt biến môi trường `JWT_SECRET` là chuỗi ngẫu nhiên có ít nhất 32 byte. Không commit khóa thật.

Trang demo lưu token trong `localStorage` để bám sát bài giảng. Với hệ thống thực tế, cần đánh giá
rủi ro XSS và cân nhắc cookie `HttpOnly`, `Secure`, `SameSite` cùng cơ chế chống CSRF phù hợp.
