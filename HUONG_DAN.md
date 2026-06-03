# ShopGiayDep Backend - Hướng dẫn chạy

## Yêu cầu
- Java 17+
- Maven
- Docker Desktop

---

## Bước 1: Chạy MySQL bằng Docker

```bash
cd shopgiaydep-be
docker-compose up -d
```

Kiểm tra MySQL đã chạy chưa:
```bash
docker ps
```

Truy cập phpMyAdmin để xem database:
→ http://localhost:8081
→ Server: mysql | User: shopuser | Pass: shop123

---

## Bước 2: Chạy Spring Boot

### Dùng IntelliJ IDEA
1. Mở folder `shopgiaydep-be` trong IntelliJ
2. Đợi Maven tải dependencies (lần đầu hơi lâu)
3. Mở file `ShopGiayDepApplication.java`
4. Click nút ▶ Run

### Dùng Terminal
```bash
./mvnw spring-boot:run
```

Server chạy tại: http://localhost:8080

---

## Bước 3: Test API

### Login admin
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
→ Copy token từ response

### Lấy danh sách sản phẩm
```bash
curl http://localhost:8080/api/products
```

### Thêm sản phẩm (cần token)
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "name": "Nike Air Max 270",
    "brand": "Nike",
    "price": 3200000,
    "category": "men",
    "stock": 50,
    "isNew": true,
    "isSale": false,
    "sizes": ["38","39","40","41","42"],
    "colorVariants": [
      {"color": "Đen", "imageUrl": "https://..."},
      {"color": "Trắng", "imageUrl": "https://..."}
    ]
  }'
```

---

## Cấu trúc API

| Method | URL | Mô tả | Auth |
|--------|-----|-------|------|
| POST | /api/auth/login | Đăng nhập admin | Public |
| GET | /api/products | Danh sách SP (filter, phân trang) | Public |
| GET | /api/products/{id} | Chi tiết SP theo ID | Public |
| GET | /api/products/slug/{slug} | Chi tiết SP theo slug | Public |
| POST | /api/products | Thêm SP | Admin |
| PUT | /api/products/{id} | Sửa SP | Admin |
| DELETE | /api/products/{id} | Xóa SP | Admin |
| GET | /api/categories | Danh sách danh mục | Public |
| POST | /api/categories | Thêm danh mục | Admin |
| PUT | /api/categories/{id} | Sửa danh mục | Admin |
| DELETE | /api/categories/{id} | Xóa danh mục | Admin |
| POST | /api/orders | Đặt hàng | Public |
| GET | /api/orders | Danh sách đơn hàng | Admin |
| GET | /api/orders/{id} | Chi tiết đơn hàng | Admin |
| PUT | /api/orders/{id}/status | Cập nhật trạng thái | Admin |
| GET | /api/orders/stats | Thống kê dashboard | Admin |

---

## Tài khoản mặc định
- Username: `admin`
- Password: `admin123`

---

## Kết nối với Frontend Vue

Trong frontend, thay đổi stores để gọi API thay vì dùng mock data.
Xem file `/shopgiaydep/src/api/` để tích hợp.
