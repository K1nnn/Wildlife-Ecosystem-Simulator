# 🌍 WildLife Ecosystem Simulation

Mô phỏng hệ sinh thái hoàn chỉnh, sẵn sàng sử dụng được xây dựng bằng [LibGDX](https://libgdx.com/) và Java 11+.

**Trạng thái**: ✅ Hoàn thiện | **Phiên bản**: 2.0 | **Framework**: LibGDX (LWJGL3)

---

## ✨ Tổng Quan Tính Năng

✅ **Hệ thống Săn mồi - Bỏ trốn**: Cơ chế săn đuổi và lẩn trốn phức tạp sử dụng thuật toán tìm đường A*.
✅ **Chỉ số Sinh tồn**: Sự suy giảm chân thực của Năng lượng (Energy), Nước (Hydration) và Máu (Health).
✅ **Trí Tuệ Nhân Tạo (AI)**: Chuyển đổi trạng thái linh hoạt: `Đi dạo` → `Đi Săn` → `Bỏ trốn` → `Ăn` → `Tìm Nước` → `Sinh sản`.
✅ **Hệ thống Mùa màng**: Tự động luân phiên giữa Mùa Sinh Sản (tài nguyên dồi dào) và Mùa Hạn Hán (tài nguyên cạn kiệt, lão hóa nhanh).
✅ **5 Loài Động vật**: Thỏ 🐰, Hươu 🦌, Voi 🐘, Sói 🐺, Hổ 🐯.
✅ **2 Loài Thực vật**: Cỏ 🌱 (mọc nhanh), Cây 🌳 (mọc chậm).
✅ **Âm thanh (Audio)**: Tích hợp `SoundManager` để phát ra tiếng gầm/hú khi ăn của Hổ và Sói.
✅ **Tối ưu hóa Không gian**: Sử dụng lưới `ZoneManager` để tối ưu hóa phát hiện va chạm với độ phức tạp O(1).
✅ **Object Pooling**: Khởi tạo thực thể không cấp phát bộ nhớ (zero-allocation) bằng LibGDX `Pool`, ngăn chặn giật lag do Garbage Collection (Dọn rác bộ nhớ).
✅ **Giao diện HUD**: Giao diện Scene2D hiển thị trực tiếp số lượng quần thể, FPS và trạng thái Cân bằng Sinh thái.

---

## 🎮 Hệ Sinh Thái

### Động vật Ăn cỏ (Con mồi)
- **Thỏ 🐰**: Nhỏ nhất, nhanh nhất, lẩn trốn giỏi. Sinh sản nhanh. Độ ưu tiên (Dominance) = 20.
- **Hươu 🦌**: Kích thước trung bình, tốc độ và tầm nhìn cân bằng. Độ ưu tiên = 40.
- **Voi 🐘**: Khổng lồ, chậm chạp, ăn Cây. Động vật ăn cỏ bá chủ (không có thiên địch). Độ ưu tiên = 100.

### Động vật Ăn thịt (Thợ săn)
- **Sói 🐺**: Tốc độ săn theo bầy, độ ưu tiên trung bình (60). Sẽ hú lên khi ăn mồi.
- **Hổ 🐯**: Thợ săn tối thượng, tầm nhìn cực rộng, độ ưu tiên cao (80). Sẽ gầm lên khi ăn mồi.

### Thực vật (Nguồn thức ăn)
- **Cỏ 🌱**: Mọc nhanh, là thức ăn cho Thỏ và Hươu.
- **Cây 🌳**: Mọc chậm, là thức ăn duy nhất cho Voi.

*Lưu ý: Tất cả thực vật đều có vòng đời: `HẠT GIỐNG` → `MẦM` → `TRƯỞNG THÀNH` (có thể ăn) → `HÉO ÚA` → `CHẾT`.*

---

## 🏗️ Kiến trúc & Design Patterns

Mã nguồn được tối ưu, dọn dẹp và tuân thủ nghiêm ngặt các nguyên tắc Lập trình Hướng đối tượng (OOP).

### Cấu trúc Phân cấp (Class Hierarchy)
```text
Entity (Lớp trừu tượng, implements Pool.Poolable)
├── Animal (Lớp trừu tượng: Chỉ số sinh tồn & AI State Machine)
│   ├── Herbivore (Lớp trừu tượng: Tìm thức ăn & Bỏ trốn)
│   │   ├── Rabbit (Implements IPrey - Con mồi)
│   │   ├── Deer   (Implements IPrey)
│   │   └── Elephant
│   └── Carnivore (Lớp trừu tượng: Đi săn & Tìm mồi, implements IPredator)
│       ├── Wolf
│       └── Tiger
└── Plant (Lớp trừu tượng: Vòng đời sinh trưởng)
    ├── Grass
    └── Tree
```

### Các Design Patterns được áp dụng
- **Template Method**: Hàm `Animal.update()` định hướng một luồng xử lý cố định, cho phép các lớp con tùy biến qua hàm `specificBehavior()`.
- **State Machine**: Điều khiển AI dựa trên enum `AnimalState` và hàm `makeDecision()`.
- **Object Pool**: Sử dụng 7 Pool chuyên dụng trong `EntityManager` để tái sử dụng object thay vì tạo mới.
- **Singleton**: Áp dụng cho các lớp quản lý tập trung như `EntityManager`, `ResourceTracker`, `SoundManager`.
- **Facade**: `MapManager` đóng vai trò che giấu độ phức tạp của TiledMap API từ LibGDX.

---

## 🚀 Cài đặt & Chạy Game

### 1. Yêu cầu hệ thống
- **JDK 11** trở lên.
- **File Âm Thanh**: Bắt buộc phải có file `tiger.mp3` và `wolf.mp3` đặt bên trong thư mục `assets/` để âm thanh khi ăn mồi hoạt động.

### 2. Build và Run
Project sử dụng Gradle. Mở terminal (CMD/PowerShell) tại thư mục gốc của dự án và chạy lệnh:

```bash
# Lệnh để biên dịch (build) dự án
./gradlew build

# Lệnh để khởi chạy game
./gradlew lwjgl3:run
```

---

## 🕹️ Điều khiển

| Thao tác | Phím / Chuột |
|---|---|
| **Kéo bản đồ (Pan)** | Giữ và Kéo Chuột Trái |
| **Phóng to (Zoom In)** | Cuộn Chuột Lên / `Ctrl` + `+` |
| **Thu nhỏ (Zoom Out)** | Cuộn Chuột Xuống / `Ctrl` + `-` |
| **Tương tác UI** | Click vào các nút ở góc phải trên cùng (HUD) |

---

## 📊 Các hệ thống Quản lý (System Managers)

- `EntityManager`: Trung tâm quản lý. Cập nhật và vẽ (render) tất cả các thực thể. Xử lý việc dọn dẹp xác chết và sinh sản.
- `MapManager`: Phân tích bản đồ `ecosystem.tmx`. Xử lý nhận diện vật cản (nước, giới hạn map) và tìm vị trí spawn ngẫu nhiên an toàn.
- `PathFinding`: Triển khai thuật toán A* (A-Star) kết hợp với khoảng cách Manhattan và logic chống kẹt tường.
- `ResourceTracker`: Hệ thống tối ưu giúp tìm kiếm con mồi, cây cối, hoặc hồ nước gần nhất mà không cần chạy vòng lặp kép (O(N^2)) tốn kém.
- `SoundManager`: Tải trước các file âm thanh vào RAM để tránh bị giật lag (I/O Block) khi phát âm thanh trong lúc chơi.

---

## 📝 License
MIT - Bạn hoàn toàn có thể tự do sử dụng, chỉnh sửa, học tập và chia sẻ project này. Chúc bạn mô phỏng vui vẻ! 🌍
