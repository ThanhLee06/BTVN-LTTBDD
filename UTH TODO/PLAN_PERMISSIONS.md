# Kế hoạch đầy đủ: Vị trí, Thông báo và Camera cho UTH TODO

Ngày lập: 10/11/2026. Trạng thái: kế hoạch triển khai; chưa sửa mã ứng dụng trong yêu cầu lập kế hoạch này.

## 1. Kết quả cần đạt

Ứng dụng có đủ ba nhóm quyền trong ảnh bài tập, gồm giao diện giới thiệu, hộp thoại Android thật, trang quản lý trạng thái và chức năng thực tế sử dụng từng quyền.

| Nhóm | Chức năng triển khai | Nơi sử dụng |
| --- | --- | --- |
| Vị trí | Lấy vị trí hiện tại một lần, gắn vào công việc, xem lại và mở ứng dụng bản đồ | Thêm/sửa việc, Chi tiết |
| Thông báo | Xin quyền, kiểm tra trạng thái thông báo, thử thông báo và lập lịch nhắc trước hạn | Khi bật Nhắc nhở, Chi tiết, Cài đặt |
| Camera | Mở camera trong app, chụp ảnh, xem trước, lưu/xóa ảnh đính kèm | Chi tiết và biểu mẫu thêm/sửa việc |

Các chức năng địa điểm, ảnh và lịch nhắc là phần bổ sung mới. Đợt này chọn chụp ảnh đính kèm để trình diễn quyền Camera; quét QR trong hình minh họa không phải một tính năng bắt buộc của ứng dụng quản lý công việc.

Giữ bố cục và hành vi đã đạt của Trang chủ, Danh sách và Chi tiết. Các phần địa điểm/ảnh mở rộng xuất hiện trong khu vực Chi tiết hoặc biểu mẫu, không thay đổi cách đếm việc, lọc, thứ tự server và tiến độ hiện có. Màu chủ đạo dùng màu xanh UTH TODO; popup theo cấu trúc của slide: minh họa/icon, tiêu đề, mô tả ngắn, nút chính và Để sau.

## 2. Hiện trạng đã kiểm tra

- Manifest đã có `INTERNET` và `POST_NOTIFICATIONS`; chưa có quyền vị trí hoặc camera.
- Cài đặt trong `CloudScreens.kt` đã có nút xin quyền thông báo trên Android 13+.
- Trạng thái thông báo hiện chỉ được đọc lúc tạo màn và callback xin quyền; cần cập nhật lại khi quay từ Cài đặt hệ thống, kiểm tra cả bật/tắt thông báo của app và channel.
- `NotificationService` đã tạo channel, xử lý FCM, lưu hộp thư và hiển thị thông báo; chưa có bộ lập lịch nhắc việc trước hạn.
- `TodoTask` có `date`, `time`, `remind`, nhưng chưa có địa điểm hoặc ảnh đính kèm.
- REST mock có dữ liệu mẫu và không chứng minh được khả năng lưu thay đổi lâu dài. Không gửi thêm trường ảnh/vị trí vào API đang có.
- `TaskClock` đang hỗ trợ ngày mẫu 20/10/2026. Ngày này chỉ dùng để đối chiếu UI; lịch nhắc phải dùng đồng hồ hệ thống thật.
- Firebase đang quản lý danh tính. Tất cả dữ liệu bổ sung cần tách theo Firebase UID, kể cả tài khoản khách.

## 3. Luồng giao diện và cấp quyền

### 3.1. Giới thiệu ba quyền giống cấu trúc slide

1. Khi mở bản cập nhật lần đầu, hiện lời mời thiết lập quyền. Với người chưa đăng nhập, đặt trên nền màn Đăng nhập; với người đã đăng nhập, đặt trên nền Trang chủ.
2. Người dùng chọn Thiết lập để đi qua ba thẻ: **Vị trí → Thông báo → Camera**. Chỉ một popup đang hoạt động tại một thời điểm.
3. Mỗi thẻ có lời giải thích đúng chức năng của UTH TODO và hai lựa chọn:
   - **Cho phép / Bật thông báo:** kiểm tra trạng thái hiện tại rồi mới mở hộp thoại Android nếu cần.
   - **Để sau:** chuyển tiếp, không mở hộp thoại hệ thống.
4. Cho phép bỏ qua toàn bộ luồng và tiếp tục đăng nhập/sử dụng app.
5. Lưu phiên bản giới thiệu và bước đã xem bằng DataStore. Sau khi đã hoàn tất hoặc bỏ qua, không tự chạy lại trong mỗi lần mở app. Cài đặt có nút Xem lại giới thiệu quyền.
6. Quyền đã cấp được bỏ qua hoặc hiển thị Đã cho phép; Android dưới 13 không có popup runtime thông báo.
7. Các card giới thiệu không được coi là đã cấp quyền. Chỉ callback/kết quả kiểm tra Android xác định quyền thật.

Nội dung dự kiến:

| Thẻ | Mô tả |
| --- | --- |
| Vị trí | Cho phép lấy vị trí khi bạn chọn gắn địa điểm vào công việc. |
| Thông báo | Cho phép UTH TODO gửi thông báo nhắc các công việc bạn đã bật nhắc nhở. |
| Camera | Cho phép chụp ảnh tài liệu hoặc kết quả công việc để đính kèm. |

### 3.2. Xin lại đúng lúc sử dụng chức năng

Khi bấm Gắn vị trí, Chụp ảnh hoặc bật Nhắc nhở, dùng chung một bộ điều phối:

`Thao tác → kiểm tra quyền → giải thích nếu cần → yêu cầu Android → xử lý kết quả → tiếp tục thao tác`

- Đã cấp: đi thẳng vào chức năng.
- Chưa cấp: chỉ yêu cầu sau hành động rõ ràng của người dùng, không tự bật cả ba popup Android liên tiếp.
- Từ chối: thông báo ngắn, giữ dữ liệu biểu mẫu, cho tiếp tục dùng app.
- Trường hợp Android không còn hiện hộp thoại yêu cầu: cung cấp nút Mở Cài đặt ứng dụng. Không suy luận trạng thái này chỉ từ `shouldShowRequestPermissionRationale=false` ở lần đầu; cần kết hợp lịch sử đã yêu cầu và callback.
- Sau khi quay từ Cài đặt, hoặc Activity chuyển `ON_RESUME`, đọc lại quyền thật và cập nhật UI.
- Cấp quyền một lần, thu hồi quyền, tự đặt lại quyền, và đổi từ chính xác sang gần đúng đều phải được xử lý. Không lưu cờ `granted=true` làm nguồn sự thật lâu dài.
- Nếu người dùng rời màn trong khi xin quyền/lấy vị trí/chụp ảnh, hủy thao tác hoặc bỏ kết quả không còn hợp lệ; không tự lưu vào task khác.

### 3.3. Cài đặt → Quyền ứng dụng

Ba hàng Vị trí / Thông báo / Camera, mỗi hàng có icon, trạng thái hiện tại và hành động phù hợp.

- Vị trí: Đã cho phép gần đúng / Đã cho phép chính xác / Chưa cho phép. Trạng thái GPS của thiết bị hiển thị riêng, vì cấp quyền không đồng nghĩa đã bật dịch vụ vị trí.
- Thông báo: phản ánh quyền runtime nếu có, trạng thái thông báo cấp app, và channel nhắc việc. Bật channel bị chặn bằng trang Cài đặt hệ thống phù hợp.
- Camera: Đã cho phép / Chưa cho phép / Thiết bị không có camera phù hợp. Quyền không phải công tắc mà app có thể tự bật/tắt.
- Nút Yêu cầu quyền hoặc Mở Cài đặt; có nút Thử thông báo và hướng dẫn chức năng dùng vị trí/camera.

## 4. Chức năng vị trí

### Quyền và truy cập

- Khai báo `ACCESS_COARSE_LOCATION` cho vị trí gần đúng.
- Khi cần tùy chọn vị trí chính xác, khai báo thêm `ACCESS_FINE_LOCATION` và xin cùng coarse trong một yêu cầu. Gần đúng vẫn là kết quả hợp lệ.
- Chỉ lấy vị trí khi người dùng bấm Gắn vị trí hiện tại; không theo dõi nền hoặc lấy liên tục khi mở app.
- Dùng Fused Location Provider để yêu cầu một vị trí hiện tại, có hủy và timeout. Nếu dùng kết quả cache dự phòng, phải kiểm tra độ cũ/độ chính xác và hiển thị đúng; không gọi vị trí cũ là vị trí hiện tại.
- Xử lý đã cấp quyền nhưng GPS tắt, không có Google Play services phù hợp, vị trí null, timeout và quyền bị thu hồi giữa thao tác.

### UI và dữ liệu

- Trong biểu mẫu: nút Gắn vị trí hiện tại, trạng thái đang lấy, kết quả và Xóa vị trí.
- Trong Chi tiết: khu vực Địa điểm có tên gợi nhớ tùy nhập, tọa độ, mô tả gần đúng/chính xác và nút Mở bản đồ.
- Chuyển tọa độ sang địa chỉ là tiện ích dự phòng, không được chặn việc lưu nếu Geocoder không trả kết quả.
- Mở bản đồ bằng geo intent; nếu không có ứng dụng xử lý thì hiển thị tọa độ và cho sao chép, không crash.
- Lưu `latitude`, `longitude`, `accuracyMeters`, `capturedAt`, `label`, mức chính xác đã cấp. Không đưa tọa độ vào Analytics/Crashlytics.

Hoàn thành khi người dùng cấp quyền, gắn được địa điểm thật hoặc vị trí mô phỏng trên emulator, mở lại task thấy dữ liệu và mở được bản đồ nếu có ứng dụng tương ứng.

## 5. Chức năng Camera và ảnh đính kèm

### Camera thật trong ứng dụng

- Khai báo `CAMERA`, camera feature là tùy chọn để app vẫn cài/chạy được trên thiết bị không có camera.
- Dùng CameraX với preview gắn lifecycle. Chỉ bind khi màn camera đang hoạt động và có quyền; giải phóng khi rời màn.
- Luồng: **Chụp ảnh → xin quyền nếu cần → preview → chụp → Xem trước → Dùng ảnh / Chụp lại / Hủy**.
- Hỗ trợ chọn camera trước/sau khi phần cứng có sẵn, sửa hướng ảnh và ngăn bấm chụp lặp khi đang xử lý.
- Thiết bị không có camera, camera đang bận, ghi file lỗi, hết chỗ hoặc quyền bị thu hồi phải có lỗi cụ thể và đường quay lại task.

### Đính kèm và lưu file

- Có khu vực Ảnh đính kèm trong Chi tiết: thumbnail, mở ảnh lớn và xóa ảnh.
- Trong biểu mẫu, ảnh vừa chụp là bản nháp; chỉ gắn vào task khi lưu thành công. Hủy biểu mẫu không để ảnh mồ côi.
- Giới hạn ban đầu: tối đa 5 ảnh/task. Resize ảnh lớn xuống cạnh dài tối đa 1600 px và nén JPEG để tránh tăng bộ nhớ khi xem.
- Lưu ảnh vào thư mục riêng của app; thumbnail tải bằng Coil. Không tự đưa ảnh vào thư viện công cộng hoặc upload lên API mock.
- Khi thay/xóa ảnh hoặc xóa task thành công, dọn file và metadata tương ứng. Khởi động lại app có tác vụ dọn bản nháp cũ, không xóa ảnh đang được tham chiếu.
- Có thể bổ sung Chọn ảnh từ thư viện bằng Photo Picker trong cùng khu vực; không xin quyền đọc toàn bộ thư viện. Tính năng này là đường thay thế khi người dùng không muốn dùng camera.

Hoàn thành khi chụp được ảnh, xác nhận, thấy thumbnail, xem ảnh lớn, khởi động lại vẫn xem được ảnh của đúng tài khoản/task và xóa được file liên quan.

## 6. Thông báo và nhắc việc thật

### Quyền và khả năng hiển thị

- Tái sử dụng `POST_NOTIFICATIONS`; xin runtime trên Android 13+, kiểm tra thông báo cấp app trên mọi phiên bản được hỗ trợ.
- Tạo channel Nhắc việc riêng để quản lý độc lập với channel FCM hiện có.
- Khi bật Nhắc nhở mà quyền chưa có, giải thích và yêu cầu. Nếu từ chối, không hiển thị trạng thái khiến người dùng tưởng lịch nhắc đã hoạt động.
- Phân biệt mong muốn `task.remind=true` với trạng thái nhắc thực tế: đang hoạt động / bị chặn thông báo / đã quá hạn / chưa lập lịch.
- Nút Thử thông báo gửi thông báo trên thiết bị sau hành động của người dùng; phải kiểm tra hiển thị thật, không chỉ hiện toast thành công.

### Bộ lập lịch

- Tính thời điểm từ ngày và giờ task theo múi giờ thiết bị, trừ 15 phút. Dùng đồng hồ hệ thống thật, không dùng `TASK_REFERENCE_DATE` của ảnh mẫu.
- Dùng AlarmManager với lịch **không chính xác tuyệt đối** (`setAndAllowWhileIdle`) cho phạm vi ba quyền này. Mục tiêu là nhắc trước hạn; Android có thể trì hoãn khi tiết kiệm pin/Doze. UI cần mô tả ngắn khả năng trì hoãn ở phần cài đặt nhắc việc.
- Nếu sau này yêu cầu nhắc đúng từng phút kể cả Doze, đó là phương án mở rộng cần đánh giá quyền truy cập đặc biệt Exact alarms; không tự thêm một luồng cấp quyền thứ tư vào bài tập ba quyền hiện tại.
- `ReminderReceiver` đọc snapshot lịch nhắc đã lưu; mỗi task/account có PendingIntent và notification ID riêng, tránh ghi đè nhau.
- Lập lịch sau khi lưu task hoặc bật nhắc thành công. Đổi ngày/giờ: hủy lịch cũ rồi đặt lịch mới. Hoàn thành, tắt nhắc hoặc xóa thành công: hủy lịch và đánh dấu hủy trong dữ liệu cục bộ.
- Task đã quá hạn: không đẩy hàng loạt thông báo cũ. Nếu hạn còn trong tương lai nhưng mốc trước 15 phút đã qua, chỉ nhắc ngay một lần sau thao tác lưu/bật nhắc của người dùng.
- Bấm thông báo mở đúng công việc; nếu chưa đăng nhập thì đi qua đăng nhập và chỉ mở khi khớp tài khoản. Với task không còn trên mock, hiển thị bản tóm tắt snapshot cục bộ thay vì mở nhầm một task trùng ID.
- Lưu lịch nhắc và phục hồi sau khởi động thiết bị, cập nhật app, thay đổi giờ hoặc múi giờ. Dùng receiver và quyền cài đặt thông thường `RECEIVE_BOOT_COMPLETED`; đây không phải hộp thoại quyền nhạy cảm thứ tư.
- Sau force-stop, Android có thể chặn hoạt động nền cho đến khi người dùng mở lại app; khi mở lại phải đối chiếu và lập lại lịch hợp lệ. Không cam kết nhắc khi app đang bị force-stop.
- Đăng xuất hủy lịch đang hoạt động của tài khoản cũ. Đăng nhập lại đối chiếu lịch còn hiệu lực; không phát lại lịch đã hủy chỉ vì mock GET trả mẫu cũ.

Hoàn thành khi đặt một task có hạn tương lai gần, quan sát được thông báo thật, bấm mở đúng task, và xác minh sửa/hủy không để lịch cũ tiếp tục phát.

## 7. Lưu dữ liệu bổ sung và xử lý API mock

### Cấu trúc lưu trữ

Dùng Room cho dữ liệu bổ sung và snapshot lịch; dùng DataStore cho trạng thái giới thiệu quyền. Khi triển khai, kiểm tra phiên bản Room/KSP tương thích Kotlin/AGP hiện tại trước khi thêm dependency.

| Dữ liệu | Khóa/nội dung |
| --- | --- |
| LocalTaskIdentity | UID + nguồn dữ liệu + ID task + định danh phiên bản cục bộ ổn định |
| TaskLocation | Khóa task, tọa độ, độ chính xác, thời điểm và nhãn |
| TaskAttachment | UUID ảnh, khóa task, đường dẫn file, kích thước và thời điểm |
| ReminderSnapshot | Khóa task, tiêu đề, ngày/giờ gốc, thời điểm nhắc, trạng thái, phiên bản lịch và dấu đã phát/hủy |
| PermissionIntro | Phiên bản đã xem, bước hiện tại, đã hoàn tất/để sau và lịch sử từng yêu cầu |

### Quy tắc dữ liệu

- Mọi truy vấn metadata/ảnh/lịch đều ràng buộc UID. Liên kết Google giữ nguyên UID thì giữ dữ liệu tài khoản khách; đổi tài khoản không nhìn thấy dữ liệu của tài khoản khác.
- Khóa cục bộ phải phân biệt các công việc mới dù mock lặp ID trả về. Lưu ánh xạ ID và fingerprint/snapshot; không gắn ảnh/vị trí cũ sang công việc khác chỉ vì cùng ID 7.
- Task API vẫn được đọc/ghi qua client hiện có, không thay hợp đồng JSON hoặc đưa file ảnh vào request task.
- Sau khi API báo thành công, lưu metadata cục bộ. Nếu phần lưu cục bộ lỗi, báo rõ phần nào chưa lưu và giữ bản nháp để thử lại; không báo toàn bộ thao tác thành công hoặc giả vờ đã rollback server.
- Dữ liệu ảnh/vị trí và snapshot lịch tồn tại qua việc đóng/mở app. Task mock có thể trở về mẫu cũ; lịch đã hủy và khóa định danh không được tự tái kích hoạt từ mẫu cũ.
- Task tạo cục bộ không xuất hiện lại trên API sau restart vẫn có snapshot cho nhắc việc và màn tóm tắt; không giả vờ nó đã được đồng bộ bền lên server.
- Đăng xuất xóa dữ liệu đang hiển thị trong bộ nhớ và ngừng lịch cũ; dữ liệu riêng được giữ tách biệt trên thiết bị để dùng khi đăng nhập lại. Có thao tác rõ ràng để xóa dữ liệu bổ sung trên thiết bị.
- Cấu hình backup/device transfer phù hợp cho dữ liệu địa điểm/ảnh/lịch; không tự đưa dữ liệu riêng này lên cloud backup hoặc khôi phục lịch tài khoản sai sau chuyển thiết bị.

## 8. Các phần mã dự kiến thay đổi

Các đường dẫn dưới đây tính từ thư mục gốc dự án `D:/Bai tap dai hoc/LTA/UTH TODO`.

| Tệp hoặc nhóm | Công việc |
| --- | --- |
| `app/src/main/AndroidManifest.xml` | Quyền, camera feature tùy chọn, khai báo receiver nhắc việc/khởi động |
| `navigation/AppNavigation.kt` | Điều phối giới thiệu, route Camera, xem ảnh, trang quyền và đích mở thông báo |
| `screens/LoginScreen.kt`, `screens/HomeScreen.kt` | Điểm hiện lời mời thiết lập quyền lần đầu theo trạng thái đăng nhập |
| `screens/CloudScreens.kt` | Mục Quyền ứng dụng, trạng thái thông báo thực tế, liên kết Cài đặt hệ thống |
| `screens/TaskScreenComponents.kt` | Biểu mẫu địa điểm/ảnh, xin quyền khi bật nhắc, giữ draft |
| `screens/TaskDetailScreen.kt` | Địa điểm, ảnh đính kèm, trạng thái nhắc thực tế |
| `permissions/` mới | Điều phối yêu cầu, đọc trạng thái, lịch sử yêu cầu và đồng bộ lifecycle |
| `screens/PermissionIntroScreen`, `AppPermissionsScreen` mới | Thẻ giới thiệu và quản lý ba quyền |
| `screens/CameraScreen`, `AttachmentViewer` mới | Preview, chụp, xem trước và xem ảnh lớn |
| `data/local/` mới | Room, DAO, định danh task, metadata ảnh/vị trí/lịch và migration |
| `services/TaskLocationService` mới | Lấy vị trí một lần, timeout và hủy |
| `services/TaskReminderScheduler`, `ReminderReceiver`, `ReminderRescheduleReceiver` mới | Đặt/hủy/phục hồi lịch |
| `services/NotificationService.kt` | Channel nhắc việc, thông báo theo task, mở đúng đích và kiểm tra trạng thái hiển thị |
| `model/TodoRepository.kt`, `viewmodel/TasksViewModel.kt` | Tích hợp vòng đời tài khoản và cập nhật extras/lịch sau mutation thành công |
| `res/xml/backup_rules.xml`, `data_extraction_rules.xml` | Quy tắc backup dữ liệu bổ sung |
| Gradle và version catalog | CameraX, location, Room/KSP và DataStore tương thích dự án |
| Kiểm thử, README, ảnh và báo cáo | Hướng dẫn chạy/demo, kiểm tra quyền và các giới hạn thực tế |

## 9. Thứ tự thực hiện và điểm kiểm tra

1. **Chuẩn bị:** lưu ảnh nền ba màn đang đạt, kiểm tra build/test nền, chốt model lưu cục bộ và giao diện mới. Mốc đạt: baseline và schema có thể review.
2. **Bộ quản lý quyền:** manifest, kiểm tra trạng thái, Activity Result contracts, đồng bộ ON_RESUME và xử lý từ chối. Mốc đạt: ba quyền có thể xin thật trên emulator phù hợp.
3. **UI giới thiệu và Cài đặt:** hoàn thiện thẻ và các nhánh Để sau/đã cấp/mở Cài đặt, lưu trạng thái lần đầu. Mốc đạt: không kẹt luồng đăng nhập, không lặp popup khi xoay màn/mở lại.
4. **Room và vòng đời dữ liệu:** UID/task identity, draft, ảnh, vị trí, snapshot lịch và backup. Mốc đạt: dữ liệu không lẫn tài khoản và tồn tại qua restart.
5. **Vị trí:** lấy vị trí một lần, gắn/xóa, hiển thị và bản đồ. Mốc đạt: cả gần đúng và chính xác hoạt động, có lỗi GPS tắt/null/timeout.
6. **Camera:** preview, chụp/xác nhận/chụp lại, thumbnail/xem lớn/xóa, dọn draft. Mốc đạt: ảnh thật lưu và đọc lại đúng task.
7. **Nhắc việc:** quyền/channel, lịch trước hạn, hủy/đặt lại, nhận thông báo và deep link. Mốc đạt: chứng minh bằng thông báo thật trên một task thử có hạn tương lai.
8. **Tích hợp:** khôi phục lịch, đăng xuất/đổi tài khoản, mock trả mẫu cũ/ID lặp, sửa/xóa task thất bại. Mốc đạt: không thông báo nhầm, không ảnh/vị trí nhầm task.
9. **Kiểm tra và bàn giao:** test ma trận bên dưới, đối chiếu UI gốc, xuất APK, ảnh, video demo ngắn hoặc chuỗi ảnh cấp quyền và báo cáo. Mốc đạt: các mục nghiệm thu đã có bằng chứng hoặc giới hạn cụ thể.

## 10. Ma trận kiểm thử

| Nhóm | Tình huống bắt buộc |
| --- | --- |
| Phiên bản Android | API 24 hoặc phiên bản thấp nhất có emulator khả dụng; API 31/32; API 33+; API 37 hiện tại |
| Giới thiệu | Lần đầu, bỏ qua một bước/tất cả, mở lại, xoay màn, người đã đăng nhập và người chưa đăng nhập |
| Quyền | Chấp nhận, từ chối, hỏi lại khi phù hợp, không còn popup, mở Settings quay lại, quyền một lần và thu hồi |
| Vị trí | Gần đúng, chính xác, GPS tắt, null/cache cũ, timeout, bỏ màn khi đang lấy và không có app bản đồ |
| Camera | Chấp nhận/từ chối, camera không có/bận, chụp lại/hủy, đổi hướng, chuyển nền, hết chỗ, giới hạn 5 ảnh và dọn draft |
| Ảnh/vị trí | Restart tiến trình, đăng xuất/đổi UID, mock ID lặp, task bị xóa, lỗi lưu file/database |
| Thông báo | Runtime denied, app bị tắt thông báo, channel bị tắt, Thử thông báo, bấm mở đúng task |
| Lịch | Trước 15 phút, hạn dưới 15 phút, quá hạn, sửa giờ, tắt nhắc, hoàn thành/xóa thành công và thất bại |
| Khôi phục | Khởi động thiết bị, cập nhật app, đổi giờ/múi giờ, mở lại sau force-stop và logout/login |
| Hồi quy | 2/4 việc, 50%, số đếm toàn bộ, lọc giữ thứ tự server, Chi tiết đúng ID và các thao tác task cũ |
| Kích thước | Gần 390×844 và 360×800 dp, bàn phím mở, font scale lớn hơn mặc định |

Unit test dùng fake permission/location/camera/storage/scheduler và clock để kiểm tra quyết định/trạng thái, không viết test chỉ sao chép triển khai. Instrumentation kiểm tra UI và hộp thoại Android bằng thao tác hệ thống; smoke test trên thiết bị thật hoặc emulator có camera/vị trí mô phỏng để chứng minh phần cứng và thông báo.

Không dùng `pm grant` làm bằng chứng thay cho luồng xin quyền người dùng. Có thể dùng reset/revoke để chuẩn bị tình huống thử, sau đó vẫn bấm hộp thoại thật. Không sửa/xóa task sẵn có trên server để thử; dùng fake API và task thử xác minh được danh tính. Các lần thử bị Android từ chối, thiết bị không hỗ trợ hoặc thiếu môi trường phải được ghi rõ.

## 11. Tiêu chí nghiệm thu

- [ ] Có ba thẻ giới thiệu và hai lựa chọn Cho phép/Để sau; bỏ qua không chặn app.
- [ ] Cho phép gọi đúng hộp thoại Android khi cần; quyền đã cấp không bị hỏi lại vô ích.
- [ ] Cài đặt hiển thị đúng quyền và cập nhật sau khi quay từ hệ thống.
- [ ] Chỉ các nhóm quyền nhạy cảm cần thiết cho vị trí, thông báo và camera được thêm vào luồng bài tập.
- [ ] Gắn được vị trí gần đúng/chính xác và mở lại đúng địa điểm.
- [ ] Chụp/xác nhận/xem/xóa ảnh thật; xử lý camera không có và từ chối quyền.
- [ ] Ảnh và vị trí lưu bền trên thiết bị, đúng tài khoản và định danh task.
- [ ] Có thông báo nhắc việc thật cho hạn tương lai; dùng giờ thực tế, không dùng ngày mẫu UI.
- [ ] Sửa/tắt/hoàn thành/xóa hủy hoặc cập nhật lịch đúng; không phát lại lịch đã hủy từ mock cũ.
- [ ] Bấm thông báo mở đúng task hoặc bản tóm tắt hợp lệ, không mở nhầm ID/account.
- [ ] Có bằng chứng kiểm tra khôi phục lịch và mô tả đúng giới hạn Doze/force-stop.
- [ ] Những trường hợp bị từ chối/thu hồi quyền vẫn cho dùng các phần còn lại.
- [ ] Ba màn UI/API hiện có qua kiểm tra hồi quy, không đưa tọa độ/ảnh/token vào log.
- [ ] Build/lint/test đạt; bàn giao APK, ảnh cấp quyền, ảnh chức năng và báo cáo kiểm thử.

## 12. Tài liệu kỹ thuật tham chiếu

Các lựa chọn kiến trúc, giới hạn ảnh và thứ tự triển khai là quyết định của kế hoạch. Các hành vi nền tảng dưới đây được đối chiếu với tài liệu chính thức Android:

- [Runtime permissions](https://developer.android.com/training/permissions/requesting): xin quyền theo ngữ cảnh, xử lý từ chối, Activity Result contracts và hộp thoại hệ thống.
- [Quyền vị trí](https://developer.android.com/develop/sensors-and-location/location/permissions/runtime): gần đúng/chính xác và kiểm tra kết quả cấp quyền.
- [Fused location](https://developer.android.com/develop/sensors-and-location/location/retrieve-current): client vị trí và tình huống không có kết quả.
- [Quyền thông báo](https://developer.android.com/develop/ui/compose/notifications/notification-permission): runtime trên Android 13+.
- [Lập lịch bằng AlarmManager](https://developer.android.com/develop/background-work/services/alarms): lịch không chính xác, Doze, exact alarms và lựa chọn phương thức.
- [CameraX preview](https://developer.android.com/media/camera/camerax/preview): preview và gắn camera vào lifecycle.
- [Photo Picker](https://developer.android.com/training/data-storage/shared/photo-picker): chọn ảnh cụ thể thay vì đọc toàn bộ thư viện.
- [App-specific files](https://developer.android.com/training/data-storage/app-specific): lưu file riêng của ứng dụng.
