# Kết quả kiểm tra Tuần 04

Ngày: 10/11/2026, múi giờ Asia/Saigon.

## Kết quả

| Kiểm tra | Kết quả |
| --- | --- |
| `assembleDebug` | Đạt, APK tại `app/build/outputs/apk/debug/app-debug.apk` |
| `testDebugUnitTest` | 11/11 đạt: 10 kiểm thử quy tắc/repository mới và 1 kiểm thử có sẵn |
| `lintDebug` | Đạt: 0 lỗi, 43 cảnh báo; chưa nâng cấp các dependency ngoài phạm vi |
| Android trên Pixel 8 API 37, gần 390 × 844 dp | 7/7 đạt: 6 giao diện + 1 GET API thật |
| Android trên Pixel 8 API 37, 360 × 800 dp | 6/6 kiểm thử giao diện đạt |
| Luồng ứng dụng thật | Đăng nhập khách → Trang chủ → Xem tất cả → Danh sách → Chi tiết ID 2, đúng ghi chú và 1/2 việc nhỏ |

## Những hành vi đã kiểm tra

- Tiến độ chỉ tính ngày đang xét; ngày 20/10/2026 cho 2/4 việc và 50%; trường hợp 0 việc không chia cho 0.
- Tìm kiếm không phân biệt hoa/thường và kết hợp danh mục; không thay đổi thứ tự server; số việc chưa xong luôn đếm toàn bộ danh sách.
- Thông báo không có kết quả, lỗi từ máy chủ và nút Thử lại.
- Bỏ tick việc đã xong chuyển về Cần làm, tăng số việc chưa xong.
- Bấm việc mở đúng ID; Chi tiết có hạn và số việc nhỏ đúng.
- Thêm, sửa, xóa và tick việc nhỏ; cập nhật thanh tiến độ; đổi hoàn thành công việc.
- Thay đổi thành công không bị dữ liệu mẫu cũ ghi đè khi GET lại; xóa không tự xuất hiện lại trong phiên.
- Lỗi ghi giữ nguyên dữ liệu trước đó; ID mẫu lặp khi tạo không ghi đè việc đã tạo trước.
- Không nhận task sai ID hoặc phản hồi tải cũ sau khi đổi phiên tài khoản.
- GET danh sách và GET `/tasks/2` qua client Android thật có Bearer token, trả về dữ liệu khớp nhau. Không thử sửa/xóa các task có sẵn.

## Ảnh đối chiếu

- `screenshots/home.png`, `screenshots/list.png`, `screenshots/detail.png`: kích thước native 1080 × 2337 px, density 443, gần 390 × 844 dp.
- `screenshots/preview.png`: ghép ba ảnh trên để xem nhanh, thu về 390 × 844 cho từng màn.
- `screenshots/360x800/`: ảnh thiết bị 1080 × 2400 px, density 480.
- `comparison-list.png`: ảnh mẫu được cắt theo vùng ứng dụng và bản Android, cùng chiều rộng; một ít khung bo điện thoại ở mép ảnh mẫu không thuộc giao diện cần dựng.
- Màu chủ đạo được đo từ ảnh gốc: xanh `#495DE1`, nền `#F5F5F7`, chữ `#1C1C1E`, badge học tập `#E8EDFD`, badge cuộc sống `#F9ECDB`.

Đã xem ảnh chụp và chỉnh khoảng cách trên, chiều cao hàng/thanh đáy, góc bo, font weight, màu nút và icon nét mảnh. Đây là bản bám sát mẫu trên Android; font hệ thống, cách raster chữ và thanh hệ thống có thể khác prototype web. Không coi bộ ảnh này là chứng nhận trùng tuyệt đối từng pixel.

## Giới hạn xác minh server

POST trả HTTP 201 nhưng nội dung là mẫu cố định, khác task thử đã gửi. Kiểm tra phê duyệt tự động từ chối thao tác PATCH/DELETE ID 7 vì không chứng minh được đó là bản ghi thử; không tiếp tục ghi/xóa ID đó. Luồng PATCH/DELETE đã kiểm thử với API giả lập; client HTTP đã triển khai nhưng chưa xác minh ghi trên máy chủ.

Ứng dụng chỉ cập nhật sau HTTP thành công và giữ các thay đổi trong phiên. Khởi động lại tiến trình hoặc đăng xuất có thể trở về dữ liệu mock. Xem README để cấu hình token và ngày đối chiếu; token thật chỉ nằm trong cấu hình cục bộ được loại khỏi Git.

Log Android cuối: `android-tests.txt` và `android-tests-360x800.txt`. Kết quả Gradle/JUnit và báo cáo lint nằm dưới `app/build/`.
