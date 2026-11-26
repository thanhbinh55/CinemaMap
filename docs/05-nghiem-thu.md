# Lộ trình, nghiệm thu và nộp bài

Đề cho một tuần thực hiện. Bảng dưới đây sắp các nhóm công việc theo quan hệ phụ thuộc để sinh viên chọn theo tốc độ học và tình hình triển khai. Một nhóm có thể trải qua nhiều buổi hoặc sinh nhiều commit có ý nghĩa. Sinh viên chọn phần cần học trước, cách Agent hỗ trợ và thời điểm chốt từng thay đổi.

| Nhóm công việc | Công việc | Điểm kiểm tra | Ví dụ thông điệp commit |
| --- | --- | --- | --- |
| A · Thiết lập | Khởi tạo Git và dự án Android, chốt toolchain, mở trên thiết bị | Build sạch, chạy màn hình đầu, giữ được cấu hình dọc/ngang | `chore: bootstrap Android Views project and document setup` |
| B · Canvas | Dữ liệu phòng 196 ghế, hình học và Canvas, trạng thái mô phỏng | T01–T02, phân khu và toàn phòng rõ | `feat: render the cinema layout on a single canvas` |
| C · Camera | Camera, pan, pinch, hit-testing, Reset View | T03–T06 ở phạm vi camera, kể cả chạm mép và chuyển số ngón | `feat: implement bounded camera and transformed seat hit testing` |
| D · Chọn ghế | ViewModel, chọn ghế, giá và Bottom Sheet hai nấc | T02, T06, T09–T10 ở phạm vi trạng thái và Sheet | `feat: synchronize seat selection with the bottom sheet` |
| E · UX và xoay | Lọc, chú thích, định vị, xoay và insets | T07–T12, phối hợp mọi tính năng | `feat: add seat filters legend and rotation restoration` |
| F · Kiểm thử | QA thiết bị, test biên, hiệu năng và xử lý lỗi | T01–T13, kiểm tra trên emulator hoặc máy thật, lưu minh chứng | `test: verify gesture edges rotation and seat state consistency` |
| G · Hồ sơ | README, demo, chuẩn bị nộp | T14–T16, clone sạch và quyền truy cập link | `docs: finalize setup verification and demo evidence` |

Commit khi đã gom được một thay đổi có ý nghĩa và kiểm tra tương ứng. Thời điểm và cách chia commit do sinh viên quyết định theo nhịp thực tế. Đối chiếu mốc “05–07” trong đề và giữ lịch sử phân bố trong thời gian thực hiện. Chọn cải tiến sau khi các yêu cầu bắt buộc đạt và còn thời gian.

Ví dụ khi học nhóm C, sinh viên có thể yêu cầu giải thích hệ tọa độ và tự viết phép thuận/nghịch, sau đó nhờ review. Khi đã tự kiểm tra một ca chạm mép ghế sau zoom, sinh viên có thể yêu cầu Agent viết thêm các ca cùng mẫu. Những bước học này có thể trải qua nhiều buổi trước khi chốt commit.

## Tiến độ cập nhật ngày 27/11/2026

Bản hiện tại có sơ đồ 196 ghế, camera vừa phòng khi mở, pinch trong khoảng 0,8×–5×, kéo bằng một ngón và kéo tiếp bằng ngón còn lại sau pinch. Hit-testing đổi tọa độ chạm về sơ đồ và ghi mã ghế vào Logcat. Nút Toàn phòng đưa camera về góc vừa phòng bằng hiệu ứng 220 ms. Khi chạm vào sơ đồ, hiệu ứng dừng ở vị trí đang hiển thị. Build và unit test của template đã qua. Phép thử tạm trên Pixel_7 xác nhận pinch tăng tỷ lệ và pan ngang theo cả hai hướng, kể cả sau khi nhấc một ngón.

Mốc mã nguồn đối chiếu là `915b1a7`. Đã nối `onSeatClickListener` → `BookingViewModel.onSeatTapped()` → `StateFlow` → `CinemaMapView.selectedSeatIds`. Ghế chọn có viền chàm và dấu ✓, ghế đã đặt/không khả dụng được chặn chọn. State chứa ghế đang xem, số ghế, số chỗ và tổng tiền. `removeSeat()` và `clearSelection()` đã có trong ViewModel, còn chờ UI sử dụng. Bottom Sheet, định vị, bộ lọc và chú thích đang chờ triển khai.

Unit test hiện có bao phủ phần logic T02: A05 + E08 + K03 cho 3 ghế, 4 chỗ, 385.000 đ; bỏ E08 còn 275.000 đ; chạm A03 đã đặt chỉ cập nhật ghế đang xem. Còn chờ kiểm tra riêng ghế không khả dụng và phần hiển thị của T02. ViewModel giữ lựa chọn khi xoay theo cơ chế hiện tại, còn camera trở về toàn phòng. SavedStateHandle và kiểm thử phục hồi đầy đủ đang chờ.

### Các phần cần hiểu khi tiếp tục

- Đọc [CameraTransform.kt](../app/src/main/java/vn/edu/midterm/cinemamap/map/CameraTransform.kt) và giải thích `worldToView()`, `viewToWorld()`, `scale` và `offset` bằng ví dụ ghế A03.
- Đọc [MapGeometry.kt](../app/src/main/java/vn/edu/midterm/cinemamap/map/MapGeometry.kt) và lần theo `findSeatAt()` từ điểm world đến mã ghế. Giải thích quy ước bao gồm cạnh trái/trên và loại trừ cạnh phải/dưới.
- Đọc [CinemaMapView.kt](../app/src/main/java/vn/edu/midterm/cinemamap/map/CinemaMapView.kt) theo chuỗi `onSizeChanged()` → `onDraw()` → `onTouchEvent()`. Ôn `Canvas.save/translate/scale/restoreToCount`, `GestureDetector` với `onDown/onScroll/onSingleTapUp`, và `ScaleGestureDetector` với `onScaleBegin/onScale`, `focusX/Y`, `scaleFactor`.
- Lần theo `MotionEvent` khi đặt thêm và nhấc bớt ngón. Giải thích `hadMultiplePointers`, ID và tọa độ ngón còn lại, `panBy()`, `limitOffset()`, `invalidate()` và `performClick()`.
- Lần theo nút Toàn phòng từ listener trong Activity đến `resetView()`, xem `fitCamera` lưu đích thế nào và `ValueAnimator` cập nhật tỷ lệ và độ lệch giữa các khung hình.

- Đọc `BookingUiState`, `BookingViewModel` và collector trong Activity để hiểu cách một lần chọn ghế cập nhật Canvas và các tổng dẫn xuất.

### Checklist còn lại

- [x] Thêm Reset View về góc vừa phòng với hoạt ảnh.
- [ ] Hoàn thành kiểm thử nhóm C. Kiểm tra kéo tới bốn biên, pinch lệch tâm, giới hạn zoom, chạm sát mép ghế/lối đi sau pan+zoom và sau xoay. Hoàn tất T03–T06 theo phạm vi từng mốc.
- [x] Nhóm D. Nối ViewModel với Canvas để chọn/bỏ chọn và vẽ viền cùng dấu ✓. Tính số ghế/chỗ/tổng tiền trong state, lưu ghế đang xem và chặn chọn ghế đã đặt/không khả dụng.
- [ ] Nhóm D. Thêm thông tin suất giả lập và Bottom Sheet hai nấc có danh sách cuộn, hiện tóm tắt, giá và chi tiết ghế đang xem. Nối xóa từng ghế, định vị trên Canvas và nghiệm thu đồng bộ hai chiều. Đây là bước triển khai kế tiếp.
- [ ] Nhóm E. Thêm chú thích và bộ lọc, có điều kiện đang áp dụng, thao tác xóa và kết quả rỗng. Giữ trạng thái lựa chọn, bộ lọc và Sheet khi xoay. Xác minh bố cục ở cả hai hướng.
- [ ] Nhóm F. Hoàn thành các ca tích hợp, lưu minh chứng, đo độ mượt và chạy build/test/lint từ bản clone sạch.
- [ ] Nhóm G. Cập nhật README và nhật ký AI theo bản thực tế, đối chiếu lịch sử Git theo đề, quay video 1–5 phút và kiểm tra quyền truy cập repo/video.

## Ca nghiệm thu

| Ca | Yêu cầu | Thực hiện và kết quả kỳ vọng | Minh chứng dự kiến |
| --- | --- | --- | --- |
| T01 | R01, R13 | Start dọc và ngang, fit đủ 196 ghế/212 chỗ, 72 Thường/108 VIP/16 Đôi, ID duy nhất, màn chiếu và lối đi rõ, một Custom View cho toàn sơ đồ. Kiểm tra màn nhỏ và hai nấc Sheet. | Unit test fixture, Layout Inspector, ảnh dọc/ngang |
| T02 | R02, R05, R09, R12 | Chọn A05, E08, K03. Sheet hiện 3 ghế, 4 chỗ, 385.000 đ. Bỏ E08 còn 2 ghế, 3 chỗ, 275.000 đ; E08 về Trống, các mục khác giữ nguyên. Chạm A03 Đã đặt và B01 Không khả dụng, chỉ hiện chi tiết. | Test ViewModel và video |
| T03 | R03, R13 | Ở mức 3× fit, kéo qua bốn biên và góc, viewport không mất toàn sơ đồ. Fit lại, trục dư vùng nhìn căn giữa. Thử kéo rồi thả và kiểm tra không sinh lựa chọn. | Unit test giới hạn và demo |
| T04 | R04, R13 | Pinch ở vị trí lệch tâm theo nhiều hướng, gồm dịch tâm hai ngón, theo dõi đúng điểm neo ở trường hợp không chạm giới hạn. Kiểm tra min/max, nhấc một ngón rồi kéo tiếp, hoàn tất mà không phát tap. | Test phép toán và thao tác hai ngón |
| T05 | R05, R13 | Round-trip một số điểm world qua transform. Kiểm tra tap chính giữa và sát trong/ngoài từng cạnh, ở khe ghế, lối giữa và trên ghế đôi ở mặc định, sau pan, sau zoom, sau kết hợp pan+zoom, sau xoay. Vùng dưới mép Sheet không nhận tap bản đồ. | Unit test tham số hóa và kiểm tra trên thiết bị |
| T06 | R08, R12 | Đang zoom/pan và giữ lựa chọn + bộ lọc, bấm Toàn phòng. Camera về fit mượt, lựa chọn, giá, bộ lọc và nấc Sheet giữ nguyên. Chạm hoặc kéo khi đang reset để xác nhận camera tiếp nhận điều khiển ổn định. | Video và kiểm thử UI |
| T07 | R06 | Mở Chú thích từ cả hai hướng. Kiểm đủ loại, bốn trạng thái, lối/khu, ghế đôi và nghĩa ghế mờ. Đóng chú thích và trở lại đúng góc nhìn cùng lựa chọn. Thử cỡ chữ lớn. | Ảnh và checklist thao tác |
| T08 | R07, R12 | Phiên mới lọc Đôi + Đang chọn để thấy 0 và nút xóa. Xóa và kiểm thấy đủ 196 ghế. Chọn A05 rồi lọc VIP, ghế mờ trên map và vẫn nằm trong Sheet có nhãn ngoài lọc. Chọn một ghế VIP trong lọc Trống rồi xác minh số kết quả cập nhật ngay. | Test ViewModel và video |
| T09 | R09, R10 | Thu/mở Sheet bằng kéo và nút. Chọn đủ ghế để danh sách cuộn, thao tác được trên Canvas khi mở ở dọc/ngang. Kiểm tra tương tác giữa cuộn danh sách và kéo Sheet. | Kiểm tra UI cả hai hướng |
| T10 | R11, R12 | Từ Sheet định vị A05 khi đang lọc VIP, thấy vòng định vị, xóa A05 thì tạm tính giảm ngay. Xóa hai lần cùng ID không ảnh hưởng ghế khác. Xóa lọc rồi thấy A05 về Trống. Tap/bỏ tap luân phiên trên Canvas và kiểm tra không có ID trùng trong Sheet. | Test ViewModel và video |
| T11 | R12, R14 | Khi đã zoom, lọc, chọn và mở Sheet, xoay dọc → ngang → dọc. Giữ ghế/giá/lọc/nấc Sheet, không trùng hoặc trống dữ liệu cũ. Camera giữ mức zoom tương đối và tâm trong giới hạn mới. Tap tiếp đúng vào ghế. | Xoay trên thiết bị và test dựng lại Activity |
| T12 | R14 | Kiểm tra khi rời app/quay lại và dựng lại Activity, system bars không che nút, chữ lớn vẫn thao tác được. Kiểm tra phục hồi bằng SavedStateHandle trong phạm vi dự kiến khi hệ thống dựng lại tiến trình. | Ghi môi trường và kết quả thao tác |
| T13 | R15 | Soát `onDraw()` tái sử dụng đồ họa, không có vòng invalidate tự chạy khi đứng yên. Trace pan/pinch/tap ở 196 và 250 ghế, ghi thiết bị, build, refresh rate, frame time/jank và nhận xét. Mục tiêu nội bộ 16,7 ms mỗi frame trên máy 60 Hz khi đo render, điều tra nếu giật rõ rệt. | Mã nguồn và Android Studio System Trace hoặc Perfetto |
| T14 | R17 | Clone sạch, xác minh Gradle Wrapper/JDK/SDK, test/lint/build/cài/chạy theo README, soát tệp sinh tự động, số lượng commit theo mục 5.2, ý nghĩa và phân bố commit. | Log lệnh, commit và cấu hình thiết bị |
| T15 | R16, R18 | Đối chiếu tất cả mục README, nhật ký AI có đóng góp thực tế, lỗi/hạn chế và kết quả kiểm tra/sửa của sinh viên. Sinh viên giải thích được camera, hit-testing, lọc và xoay. | README và tự duyệt |
| T16 | R18 | Video 1–5 phút có đủ thao tác yêu cầu; thử link repository và video bằng quyền người chấm hoặc phiên độc lập. | Link và kết quả kiểm tra quyền |

T01 và T03–T06 đã được kiểm tra một phần. T02 đã có unit test cho phần logic chọn ghế và tính tổng, còn hiển thị Sheet và ca ghế không khả dụng cần kiểm chứng. T09–T10 chờ UI Bottom Sheet. T11–T12 còn kiểm chứng xoay, camera và phục hồi. Phép thử Reset View trên Pixel_7 xác nhận camera về đúng góc mặc định và dừng hoạt ảnh khi chạm vào sơ đồ. Các ca còn lại cần đối chiếu khi các tính năng tương ứng hoàn thành. Khi thực hiện ghi commit, thiết bị, API/hướng, trạng thái và liên kết minh chứng. Dùng thiết bị hoặc emulator có thể mô phỏng đa điểm. Xoay thực tế và chạm hai ngón là phần kiểm tra của sinh viên, bổ sung cho unit test.

## Video dự kiến dài 3 phút 20 giây

| Thời gian | Thao tác cần thấy |
| --- | --- |
| 0:00–0:20 | Mở app, chỉ màn chiếu, lối giữa, ba khu, 196 đối tượng và legend |
| 0:20–0:55 | Pan một ngón tới biên, pinch quanh tâm lệch giữa, chạm cạnh trong ghế sau pan/zoom, chạm khe giữa để so sánh |
| 0:55–1:25 | Chọn và bỏ chọn ghế thường, VIP và đôi, cho thấy số chỗ/tạm tính đổi; chạm ghế đã đặt để xem chi tiết |
| 1:25–1:55 | Mở Sheet, cuộn danh sách nhiều mục, định vị và xóa một ghế, chỉ Canvas và tạm tính cập nhật |
| 1:55–2:25 | Lọc VIP, chỉ điều kiện, ghế mờ và lựa chọn ngoài lọc. Bấm Toàn phòng để thấy camera fit trong lúc bộ lọc và lựa chọn còn nguyên |
| 2:25–3:00 | Xoay dọc → ngang → dọc khi còn nhóm ghế chọn và bộ lọc, thao tác thêm trong hướng ngang |
| 3:00–3:20 | Xóa các ghế đôi đang chọn nếu có, rồi lọc Đôi + Đang chọn để ra tổ hợp rỗng. Hiện thông báo 0, xóa lọc và thấy lại toàn phòng |

Khi quay, chuẩn bị trước các ghế mẫu và tổ hợp lọc theo fixture mặc định. Kết thúc sau khi kiểm tra video đúng thời lượng và liên kết có thể truy cập.
