# Kế hoạch 3 màn UTH SmartTasks — Tuần 04

Ngày lập: 10/11/2026. Trạng thái: đã triển khai và kiểm tra trên emulator. Kết quả và giới hạn xác minh server được ghi tại `artifacts/VALIDATION.md`.

## 1. Mục tiêu và phạm vi

Dựng Trang chủ, Danh sách và Chi tiết bằng Kotlin/Jetpack Compose trong dự án hiện tại. Giao diện bên trong màn hình điện thoại phải bám sát ba ảnh mẫu người dùng cung cấp: bố cục, màu, phông chữ, khoảng cách, góc bo, icon, độ dày nét, trạng thái hoàn thành và thanh điều hướng.

Khung iPhone đen, sidebar giới thiệu và bảng API trong ảnh thuộc trang trình diễn, không phải nội dung ứng dụng Android. Giữ luồng đăng nhập, hồ sơ, thông báo và cài đặt hiện có; bổ sung điều hướng cần thiết từ ba màn này. Nút + và Sửa nối vào chức năng thêm/sửa hiện có sau khi chuyển sang nguồn dữ liệu API; chưa mở rộng thành các màn mới ngoài phạm vi.

Không đưa token vào mã nguồn, tài liệu hoặc GitHub. Đưa kế hoạch, mã và kết quả kiểm tra vào bộ thay đổi có thể review; thao tác commit/push thực hiện khi người dùng yêu cầu.

## 2. Kết quả khảo sát

- `HomeScreen.kt` đã có Trang chủ nhưng chưa khớp mẫu; đang dùng toàn bộ task để tính tiến độ thay vì chỉ việc hôm nay.
- `TodoRepository.kt` hiện quản lý công việc bằng Firestore; model thiếu ngày, ghi chú, ưu tiên, nhắc nhở và việc nhỏ.
- `AppNavigation.kt` chưa có route Danh sách và Chi tiết. Tab Danh sách hiện chỉ đổi biến trạng thái; “Xem tất cả” đang gọi đồng bộ lại.
- `TodoComponents.kt` có hàng công việc, vòng tiến độ và bottom bar, nhưng cần chỉnh hình dáng và hành vi bấm. Nội dung hàng phải mở Chi tiết; chỉ checkbox đổi hoàn thành.
- Đã xác nhận GET danh sách, GET `/tasks/1` và GET `/tasks/2` đều trả HTTP 200 với token người dùng đã cung cấp.
- Danh sách có 6 việc: 4 việc ngày 20/10/2026, trong đó 2 việc hoàn thành; 4 việc chưa hoàn thành trên toàn danh sách; mỗi danh mục có 2 việc còn lại.
- `/tasks/1` là “Nộp bài tập Lập trình di động”. Màn Chi tiết trong ảnh là `/tasks/2`: “Ôn chương 3 – Cấu trúc dữ liệu”, có 2 việc nhỏ, đã hoàn thành 1.
- Trang tài liệu `/m/teach/get/tasks` trả 401 trong lần kiểm tra trước. Nội dung tab Rules và hợp đồng PATCH/DELETE đầy đủ chưa đọc được; không coi các chi tiết đó đã được xác minh.

## 3. Thứ tự triển khai

### Bước 1 — Chốt thông số hình ảnh và dữ liệu đối chiếu

1. Cắt vùng nội dung điện thoại từ ba ảnh gốc, xác định tỷ lệ màn hình và đo vị trí các thành phần. Lưu bản tham chiếu trong thư mục đối chiếu giao diện của dự án khi triển khai.
2. Lập bảng thông số dùng chung: nền xám rất nhạt, thẻ trắng, xanh tím chủ đạo, chữ đen/xám, badge học tập xanh nhạt, cuộc sống cam nhạt, ưu tiên gấp đỏ nhạt. Giá trị màu và kích thước ban đầu phải được đo lại từ ảnh, không coi giá trị đang có trong theme là chuẩn.
3. Chốt cỡ chữ, độ đậm, chiều cao dòng và font thay thế phù hợp nếu chưa lấy được font gốc. Đối chiếu đặc biệt các dòng tiếng Việt và vị trí xuống dòng tiêu đề Chi tiết.
4. Tạo cấu hình đối chiếu với ngày cố định **20/10/2026**, khách có avatar **K**, lời chào **Chào bạn!**, dữ liệu 6 task tương ứng API. Cấu hình chạy thực dùng ngày hiện tại; không tự đổi ngày của task để ép kết quả thống kê.
5. Chỉ chỉnh theme cho nhóm màn công việc nếu thay đổi theme chung làm lệch các màn đã có. Ẩn banner quảng cáo trên ba màn này vì ảnh mẫu không có.

Kết quả: bộ thông số và dữ liệu chụp ảnh có thể lặp lại. Từ ảnh chụp hiện có chưa thể cam kết trùng tuyệt đối từng pixel trên mọi thiết bị; tiêu chí nghiệm thu là đối chiếu tại cùng kích thước nội dung, mật độ và tỷ lệ chữ đã chốt.

### Bước 2 — Nối nguồn dữ liệu REST API

1. Thêm lớp gọi API và DTO đủ trường: `id`, `title`, `note`, `category`, `date`, `time`, `priority`, `remind`, `done`, `subtasks`. Mỗi subtask gồm `title`, `done`; không giả định API có ID riêng cho subtask.
2. Tách repository công việc của API khỏi phần tài khoản Firebase. Ba màn dùng một nguồn trạng thái chung; không để listener Firestore ghi đè danh sách API. Giữ cơ chế tài khoản và các dịch vụ Firebase độc lập đang có.
3. Dùng ViewModel/StateFlow để quản lý tải danh sách, chi tiết theo ID, dữ liệu đã sửa, trạng thái tải, rỗng và lỗi. Gọi mạng ngoài luồng giao diện, tránh tải lại chỉ vì Compose recompose.
4. Đọc `MOCK_API_TOKEN` từ cấu hình cục bộ hoặc biến môi trường khi build. Gửi `Authorization: Bearer <token>` cho các request; không ghi header xác thực vào log. Token được đóng gói vào ứng dụng Android không phải bí mật được bảo vệ tuyệt đối; cấu hình này nhằm tránh đưa token vào repository.
5. Trước khi triển khai ghi dữ liệu, xác minh body, response và khả năng lưu thay đổi của mock đối với PATCH/DELETE. Nếu mock chỉ trả dữ liệu mẫu, vẫn gửi request đúng hợp đồng và giữ thay đổi đã thành công trong phiên, ghi rõ giới hạn lưu bền. Khi lỗi, giữ/khôi phục trạng thái trước đó và cho thử lại.

| API | Dùng ở đâu |
| --- | --- |
| `GET /mock/teach/tasks` | Tải danh sách khi vào Trang chủ/Danh sách; làm mới có kiểm soát |
| `GET /mock/teach/tasks/{id}` | Mở Chi tiết theo đúng việc được bấm |
| `PATCH /mock/teach/tasks/{id}` | Đổi hoàn thành, sửa task, cập nhật danh sách việc nhỏ theo hợp đồng đã xác minh |
| `DELETE /mock/teach/tasks/{id}` | Xóa task từ Chi tiết |
| `POST /mock/teach/tasks` | Nối nút + vào chức năng thêm hiện có nếu cần để luồng hoạt động đầy đủ |

Base URL: `https://share.tryapps.store`. Lệnh người dùng cung cấp là ví dụ cho ID 1, không phải ID cố định của mọi trang Chi tiết:

```bash
curl "https://share.tryapps.store/mock/teach/tasks/1" \
  -H "Authorization: Bearer $MOCK_API_TOKEN"
```

### Bước 3 — Trang chủ

- Header gồm avatar K bên trái, ngày ở trên lời chào, chuông nền tròn trắng với chấm đỏ bên phải.
- Thẻ tiến độ xanh tím: “Tiến độ hôm nay”, **2/4 việc**, câu “Đã hơn nửa chặng, cố lên!”, vòng tiến độ **50%** ở bên phải trong cấu hình đối chiếu. Tiến độ tính trên task có ngày bằng ngày đang xét; xử lý riêng trường hợp 0 việc.
- Hai thẻ Học tập/Cuộc sống với icon nét mảnh, nền icon màu nhạt; mỗi thẻ hiển thị **2 việc còn lại** từ các task chưa xong của danh mục.
- “Việc hôm nay” bên trái, “Xem tất cả” bên phải; chỉ hiển thị 4 việc ngày 20/10/2026 trong cấu hình đối chiếu, giữ thứ tự server (dữ liệu hiện tại tăng dần theo giờ).
- Hàng đã xong có checkbox xanh, dấu tick trắng và tiêu đề xám gạch ngang. Giữ badge, giờ và chevron như ảnh Trang chủ.
- Thanh đáy trắng, đường phân cách nhẹ, 4 mục và nút + tròn nhô lên ở giữa; không dùng kiểu bo góc thanh đáy khác mẫu. Nội dung cuộn phía trên thanh đáy cố định.
- “Xem tất cả” và tab Danh sách mở đúng màn; bấm thẻ danh mục mở Danh sách với bộ lọc tương ứng; bấm task mở Chi tiết; bấm checkbox cập nhật tiến độ.

### Bước 4 — Danh sách

- Dòng đầu: **Danh sách** chữ đậm bên trái; **4 việc chưa xong** bên phải trong dữ liệu đối chiếu.
- Ô tìm kiếm nền xám, icon kính lúp và placeholder **Tìm công việc**.
- Ba chip **Tất cả / Học tập / Cuộc sống**; chip đang chọn nền đen, chữ trắng, chip khác nền trắng và viền mảnh.
- Nhóm **CẦN LÀM** gồm 4 việc; hiển thị badge và hạn tương đối như ảnh: Hôm nay, Ngày mai, Tuần này. Ngoài khoảng này dùng ngày cụ thể; ngày quá hạn không được gắn nhãn Hôm nay.
- Chỉ hàng “Họp nhóm đồ án” trong nhóm chưa xong có badge **Gấp** theo dữ liệu mẫu. Không tự thêm badge vào hàng đã xong.
- Nhóm **ĐÃ XONG** gồm 2 hàng gọn với checkbox xanh và tiêu đề gạch ngang; ẩn metadata và chevron như ảnh Danh sách.
- Tìm kiếm theo tiêu đề, không phân biệt hoa/thường; kết hợp với danh mục và lọc tại máy. Theo Rules người dùng bổ sung, số việc chưa xong luôn đếm toàn bộ danh sách, không phụ thuộc bộ lọc; giữ nguyên thứ tự server trả về. Giữ trạng thái tìm kiếm, bộ lọc và vị trí cuộn khi quay lại từ Chi tiết.
- Chọn tab Danh sách bằng màu xanh; kích thước và vị trí bottom bar giống Trang chủ.

### Bước 5 — Chi tiết

- Không có bottom bar. Hàng đầu là nút quay lại ghi **Trang chủ** hoặc **Danh sách** theo nơi mở, nút **Sửa** bên phải.
- Badge danh mục và ưu tiên ở trên tiêu đề. Tiêu đề đậm, xuống dòng tự nhiên đúng tỷ lệ ảnh đối chiếu.
- Khối trắng gồm hai hàng **Hạn chót** và **Nhắc nhở**, icon bên trái và giá trị bên phải, có đường phân cách mảnh. `remind=true` hiển thị “Trước 15 phút” theo mẫu; đây là quy ước giao diện vì API hiện chỉ cung cấp boolean, không cung cấp số phút.
- Nhãn **Ghi chú**, khối trắng chứa nội dung; xử lý ghi chú rỗng mà không điền nội dung bịa.
- Nhãn **Việc nhỏ**, số **1/2**, thanh tiến độ 50% trong task ID 2. Danh sách dùng checkbox vuông, tiêu đề gạch ngang khi xong, icon sửa và xóa.
- Hàng cuối có dấu +, ô nhập **Thêm việc nhỏ**, nút **Thêm**. Chặn nội dung rỗng, cập nhật số lượng và thanh tiến độ khi thêm/sửa/xóa/tick.
- Nút chính xanh tím **Đánh dấu hoàn thành** ở gần đáy; đổi nhãn phù hợp khi task đã xong. Bên dưới là **Xóa công việc** màu đỏ. Khu vực nội dung cuộn và chừa chỗ để không đè lên nút hoặc bàn phím trên màn thấp.
- Tất cả thao tác thành công cập nhật đồng thời Trang chủ và Danh sách. Xóa thành công quay lại nơi mở; lỗi giữ nguyên task và cho thử lại.

### Bước 6 — Kiểm tra chức năng và đối chiếu ảnh

1. Build debug, chạy lint và các kiểm tra bị ảnh hưởng. Thêm kiểm thử có ý nghĩa cho lọc theo ngày, phần trăm tiến độ, kết hợp tìm kiếm/danh mục và cập nhật trạng thái giữa ba màn.
2. Kiểm tra tích hợp: GET đúng ID, header xác thực, tải/rỗng/lỗi, cập nhật hoàn thành, sửa việc nhỏ, xóa và quay lại đúng màn. Chỉ dùng task thử nghiệm có thể xóa cho kiểm tra ghi dữ liệu.
3. Chụp ba màn trên emulator ở kích thước đã chốt, dữ liệu/ngày cố định và font scale 1.0. Cắt ảnh theo vùng nội dung ứng dụng để so với ảnh gốc, không tính khung điện thoại hoặc trang web bao quanh.
4. Chồng ảnh và xem ảnh sai khác để chỉnh từng điểm: khoảng cách header, mép thẻ, chiều cao hàng, đường cơ sở chữ, màu, độ bo, icon, vòng tiến độ và nút +. Không kết luận “giống hoàn toàn” chỉ dựa vào build thành công.
5. Kiểm tra thêm một kích thước Android khác và khi bàn phím mở: không cắt chữ, không che nút, không chồng nội dung. Ghi rõ sai khác bắt buộc do thanh hệ thống hoặc font nếu còn.

## 4. Các tệp dự kiến thay đổi

| Tệp/nhóm | Công việc |
| --- | --- |
| `screens/HomeScreen.kt` | Sửa bố cục, thống kê theo ngày, thao tác và nối trạng thái API |
| `screens/TaskListScreen.kt` (mới) | Danh sách, tìm kiếm, bộ lọc, hai nhóm công việc |
| `screens/TaskDetailScreen.kt` (mới) | Chi tiết, việc nhỏ, hoàn thành và xóa |
| `components/TodoComponents.kt` | Hàng task theo từng màn, bottom bar, vòng tiến độ |
| `navigation/AppNavigation.kt` | Route danh sách, chi tiết theo ID và quay lại đúng nguồn |
| `model/` và `data/` | DTO, model, API client, repository công việc và ánh xạ dữ liệu |
| `viewmodel/` (mới) | Trạng thái dùng chung và trạng thái Chi tiết |
| `ui/theme/` | Thông số giao diện cho nhóm màn công việc |
| `app/build.gradle.kts` và cấu hình cục bộ | Thư viện mạng/ViewModel nếu cần, đọc token khi build |
| Tài liệu và ảnh đối chiếu | Hướng dẫn cấu hình, báo cáo kiểm tra, ảnh ba màn |

## 5. Tiêu chí hoàn thành

- [x] Ba màn có điều hướng thật và dùng REST API đã cung cấp.
- [x] Ngày đối chiếu 20/10/2026 cho Trang chủ 2/4 việc, 50%; hai danh mục mỗi loại 2 việc còn lại.
- [x] Danh sách mặc định có 4 việc cần làm và 2 việc đã xong, đúng thứ tự ảnh.
- [x] Mở “Ôn chương 3…” gọi `/tasks/2`, hiển thị đúng ghi chú và 1/2 việc nhỏ; mở task ID 1 hiển thị dữ liệu ID 1.
- [x] Tick, sửa, thêm/xóa việc nhỏ và xóa task dùng chung repository, đã kiểm thử ghi bằng API giả lập. GET server đã xác minh; chưa thử PATCH/DELETE server vì giới hạn ghi nhận trong báo cáo.
- [x] Có ảnh đối chiếu ba màn; đã kiểm tra màu, chữ, khoảng cách, icon và thanh điều hướng tại cùng kích thước. Sai khác font Android/prototype được ghi rõ.
- [x] Không có quảng cáo chen vào ba màn; token chỉ nằm trong cấu hình cục bộ được loại khỏi Git. Chưa thực hiện commit/push.
- [x] Build và các kiểm tra liên quan đạt; các giới hạn của mock hoặc sai khác hình ảnh còn lại được ghi rõ.
