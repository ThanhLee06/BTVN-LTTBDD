# UTH SmartTasks — Tuần 04 · bản 1.2

Ứng dụng Android dùng Kotlin và Jetpack Compose. Ba màn công việc được nối với mock REST API của môn học:

- **Trang chủ:** tiến độ theo ngày, số việc còn lại theo danh mục, danh sách việc hôm nay.
- **Danh sách:** tìm theo tên không phân biệt hoa/thường, lọc tại máy, tách Cần làm / Đã xong và giữ thứ tự server. Số việc chưa xong đếm toàn bộ danh sách, không phụ thuộc bộ lọc.
- **Chi tiết:** tải theo ID, hiển thị ghi chú/hạn/nhắc nhở/ưu tiên, thêm/sửa/xóa/tick việc nhỏ, hoàn thành và xóa công việc.

Nút + và Sửa mở biểu mẫu nhập đủ dữ liệu công việc. Ba màn chia sẻ một repository; thành công ở màn Chi tiết cập nhật ngay Trang chủ, Danh sách và thống kê hồ sơ. Firebase tiếp tục quản lý đăng nhập và các dịch vụ tài khoản/thiết bị; không còn listener Firestore ghi đè dữ liệu công việc REST.

## Vị trí, Thông báo và Camera

- Lần mở đầu có lời mời thiết lập ba quyền trên nền Đăng nhập hoặc Trang chủ. Chọn Thiết lập để đi qua các thẻ, hoặc Để sau; bỏ qua không chặn app. Popup giải thích chỉ mở hộp thoại Android thật sau khi bấm Cho phép.
- **Cài đặt → Quyền ứng dụng**: xem trạng thái thật, yêu cầu quyền hoặc mở Cài đặt hệ thống, xem lại giới thiệu và thử thông báo.
- **Chi tiết công việc**: cuộn xuống khu vực Địa điểm/Ảnh đính kèm. Gắn vị trí hiện tại một lần, đặt tên gợi nhớ, mở bản đồ hoặc chủ động sao chép tọa độ. Gần đúng vẫn sử dụng được.
- **Chụp ảnh**: CameraX chạy trong app, có preview, chụp lại và Dùng ảnh. Có thể chọn ảnh bằng Photo Picker. Tối đa 5 ảnh/task; ảnh lưu riêng trên thiết bị, cạnh dài tối đa 1600 px. Bấm thumbnail để xem lớn, Xóa ảnh để gỡ.
- Biểu mẫu thêm/sửa cũng hỗ trợ địa điểm và ảnh. Ảnh nháp được giữ khi chuyển sang camera rồi quay lại, và dọn khi hủy. Ảnh/vị trí không được gửi lên API mock.
- **Nhắc việc**: bật Nhắc nhở và lưu hạn trong tương lai. Lịch trước 15 phút dùng thời gian thật/múi giờ thiết bị, độc lập với ngày mẫu của giao diện. Với hạn còn dưới 15 phút, thao tác lưu/bật nhắc có thể gửi một lần ngay; việc quá hạn không bị nhắc hàng loạt.
- Android có thể trì hoãn lịch không chính xác trong Doze/tiết kiệm pin. App không yêu cầu Exact alarms. Khởi động thiết bị/đổi giờ/cập nhật app hoặc mở lại app sẽ đối chiếu và phục hồi lịch hợp lệ. Sau force-stop cần mở lại app.
- Hoàn thành, xóa, tắt nhắc hoặc đăng xuất hủy lịch tương ứng. Bấm thông báo mở đúng task/account; nếu mock không có task thì dùng bản lưu cục bộ.
- Room giữ bản task đã chỉnh, ảnh, vị trí, lịch và dấu hủy riêng theo UID qua restart. Dữ liệu riêng không tham gia cloud backup/device transfer. Liên kết Google cùng UID giữ được dữ liệu khách; đổi UID không nhìn thấy dữ liệu của tài khoản khác.

Kịch bản demo: đăng nhập khách → bỏ qua/xin ba quyền → mở task → gắn vị trí và chụp ảnh → xem lại sau khi mở app lại. Để thử nhắc việc, sửa ngày/giờ thành **thời gian thật trên thiết bị + khoảng 5–10 phút**, bật Nhắc nhở rồi lưu. Các task mock ngày 20/10/2026 đã quá hạn so với thời gian thật nên không tự phát thông báo.

## Cấu hình máy phát triển

Android SDK và các dependency theo Gradle của dự án. Giữ file `app/google-services.json` của dự án Firebase trên máy; file này và `local.properties` đã được loại khỏi Git.

Thêm vào `local.properties`, giữ nguyên dòng `sdk.dir` đang có:

```properties
MOCK_API_TOKEN=<access-token-do-giang-vien-cap>
# Chỉ dùng khi đối chiếu ảnh mẫu:
TASK_REFERENCE_DATE=2026-10-20
```

Cũng có thể cung cấp hai giá trị trên bằng biến môi trường; biến môi trường được ưu tiên. Không commit token. Token được đóng gói vào APK khi build để gọi API nên không coi APK là nơi bảo vệ bí mật.

Để chạy theo ngày thực tế, xóa `TASK_REFERENCE_DATE` khỏi cấu hình cục bộ và biến môi trường, rồi build lại. Không sửa ngày dữ liệu API để ép kết quả thống kê. Bản build đối chiếu dùng ngày 20/10/2026 cho kết quả 2/4 việc và 50%; task Chi tiết “Ôn chương 3…” có ID **2**, còn ID **1** là “Nộp bài tập Lập trình di động”.

## API và giới hạn mock

Base URL: `https://share.tryapps.store`. Mọi request gửi header `Authorization: Bearer <token>`; client không ghi token vào log.

| Method | Path | Mục đích |
| --- | --- | --- |
| GET | `/mock/teach/tasks` | Tải danh sách |
| GET | `/mock/teach/tasks/{id}` | Tải chi tiết đúng ID |
| POST | `/mock/teach/tasks` | Tạo công việc |
| PATCH | `/mock/teach/tasks/{id}` | Sửa các trường hoặc việc nhỏ |
| DELETE | `/mock/teach/tasks/{id}` | Xóa công việc |

Đã xác nhận POST trả một mẫu cố định khác nội dung request. Vì vậy sau HTTP 2xx ứng dụng giữ nội dung người dùng vừa gửi và ghép các thay đổi này khi tải lại; không dùng mẫu trả về để ghi đè tiêu đề/việc nhỏ vừa sửa. Từ bản 1.2, Room giữ thêm bản đã chỉnh và dấu xóa cục bộ theo tài khoản qua restart. Đây là lưu trên thiết bị; không cam kết lưu bền trên máy chủ mock hoặc đồng bộ sang thiết bị khác.

PATCH/DELETE đã được triển khai trong client, kiểm thử bằng API giả lập. Chưa thử ghi/xóa ID thật trên máy chủ vì POST không trả đúng nội dung bản ghi thử. Kiểm thử tích hợp trên emulator chỉ đọc GET danh sách và GET Chi tiết. Khi lỗi ghi, ứng dụng giữ dữ liệu cũ và hiển thị lỗi; có thể thực hiện lại thao tác.

Nhắc “Trước 15 phút” là quy ước từ giao diện mẫu khi trường `remind=true`; API không trả số phút. Thống kê tuần ở Hồ sơ đếm task đã xong theo **ngày hạn**, vì API không trả thời điểm hoàn thành.

## Build và kiểm tra

PowerShell tại thư mục dự án:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Kiểm thử giao diện và tích hợp chỉ đọc, cần một emulator/thiết bị đang kết nối:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.uthtodo.TaskScreensTest'
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.uthtodo.TaskApiReadTest'
```

`TaskRulesTest` kiểm tra thống kê theo ngày, giữ thứ tự, bộ lọc, hạn tương đối và validation. `TasksRepositoryTest` kiểm tra đồng bộ thay đổi trong phiên, lỗi ghi, xóa, ID mẫu lặp, GET trả sai ID và loại phản hồi của phiên tài khoản cũ. `TaskScreensTest` kiểm tra hành vi giao diện và lưu ảnh vào thư mục external files `screenshots` của app.

Các kiểm thử bản 1.2: `ReminderRulesTest`, `FeatureStoreTest`, `PermissionFeaturesTest`, `FullFeatureFlowTest`, `NotificationFlowTest`, `PermissionDenialTest` và `ReminderRecoveryTest`. Các lớp kiểm thử dùng quyền thật cần emulator đã đăng nhập khách, camera/vị trí mô phỏng và thông báo cho phép; lớp Denial/Recovery có bước chuẩn bị riêng được mô tả trong mã. Không chạy hai emulator và build nặng đồng thời nếu máy thiếu RAM.

APK debug: `app/build/outputs/apk/debug/app-debug.apk`.

Kế hoạch thiết kế: [PLAN_WEEK04.md](PLAN_WEEK04.md). Ảnh kiểm tra sẽ được lưu trong `artifacts/screenshots/`; bố cục cần đối chiếu tại cùng kích thước nội dung và font scale, không tính khung iPhone/bảng API bao quanh ảnh mẫu.

Kế hoạch ba quyền: [PLAN_PERMISSIONS.md](PLAN_PERMISSIONS.md). Ảnh và báo cáo bản 1.2 được lưu trong `artifacts/permissions/`.
