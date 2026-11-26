# Sơ đồ phòng chiếu rạp phim

**Trạng thái: đã có Canvas tổng quan, pinch, pan, nút Toàn phòng và Bottom Sheet đồng bộ với lựa chọn ghế qua ViewModel.** Sinh viên xác định công việc tiếp theo và cách Agent hỗ trợ. Duyệt kế hoạch xác nhận hướng làm chung. Mỗi yêu cầu cụ thể xác định phần việc Agent được làm trong workspace.

Đồ án cá nhân trong một tuần cho học phần Phát triển phần mềm cho thiết bị di động. Đề tài số 2 là sơ đồ phòng chiếu rạp phim. Phương án đề xuất dùng dữ liệu giả lập, Android Kotlin và một Custom View vẽ toàn bộ sơ đồ bằng Canvas.

## Chạy phiên bản hiện tại

1. Mở thư mục chứa README này (`CinemaMap/`) trong Android Studio và đợi Gradle Sync.
2. Chọn máy ảo `Pixel_7`, rồi Run cấu hình `app`.
3. Khi mở app, kiểm tra màn chiếu, lối đi ở giữa và ba khu ghế. Thử pinch, kéo và bấm Toàn phòng để về góc mặc định.
4. Chạm ghế trống để chọn hoặc bỏ chọn, quan sát viền chàm và dấu ✓. Chạm ghế đã đặt/không khả dụng để kiểm tra chúng không được thêm vào lựa chọn. Bottom Sheet hiện ghế đang xem, số ghế/chỗ và tổng tiền. Bấm Xem ghế để mở danh sách, bấm dòng ghế để định vị, bấm Xóa để bỏ chọn.
5. Xoay dọc/ngang để kiểm tra bố cục và lựa chọn. ViewModel giữ lựa chọn khi Activity được tạo lại do xoay, còn camera hiện được tính lại về góc toàn phòng. Ca nghiệm thu xoay đầy đủ đang chờ.

Dự án được tạo với Kotlin/Views, Gradle Wrapper 9.6.0, AGP 9.4.1, Gradle JDK 25, compile/target SDK 37 và min SDK 26. Kiểm tra dòng lệnh từ thư mục `CinemaMap/` bằng `./gradlew --offline :app:assembleDebug :app:testDebugUnitTest` khi đã có cache. `CinemaMap/` là gốc dự án Android và repo Git.

## Tiến độ hiện tại

Mốc mã nguồn đang đối chiếu là `915b1a7`. Checklist và phạm vi nghiệm thu chi tiết nằm trong [tài liệu nghiệm thu](docs/05-nghiem-thu.md#checklist-còn-lại).

| Hạng mục | Trạng thái |
| --- | --- |
| Canvas, dữ liệu 196 ghế, hình học và hit-testing | Đã triển khai. Kiểm thử đầy đủ các cạnh ghế sau pan/zoom/xoay đang chờ |
| Pan, pinch 0,8×–5× và Toàn phòng | Đã triển khai. Có phép thử camera trên Pixel_7, còn ca tích hợp với Sheet và bộ lọc |
| ViewModel, chọn/bỏ chọn và phản hồi trên Canvas | Đã triển khai. Unit test bao phủ phần logic T02 với ba loại ghế và ghế đã đặt |
| Số ghế, số chỗ, tổng tiền và ghế đang xem | Đã có trong BookingUiState. Đã hiển thị trong phần tóm tắt của Bottom Sheet |
| Bottom Sheet và định vị ghế | Đã có hai nấc, danh sách cuộn, định vị và xóa từng ghế. Có kiểm thử tích hợp trên Pixel_7 |
| Chú thích và bộ lọc | Chờ triển khai |
| Xoay màn hình và phục hồi | Lựa chọn được giữ bằng ViewModel khi xoay. Còn kiểm chứng đầy đủ, giữ camera và bổ sung SavedStateHandle |
| Kiểm thử tổng thể và hồ sơ nộp | Đang thực hiện. Còn đo hiệu năng, clone sạch, video và kiểm tra quyền truy cập liên kết |

**Việc tiếp theo** là chú thích, bộ lọc và kiểm thử phối hợp với Bottom Sheet.

## Bộ kế hoạch

| Tệp | Nội dung |
| --- | --- |
| [01 — Đối chiếu đề và rubric](docs/01-yeu-cau.md) | Ma trận yêu cầu, điều kiện đạt và các giới hạn khi chấm |
| [02 — Kiến trúc và thiết lập](docs/02-kien-truc.md) | Toolchain, cấu trúc tệp, mô hình ghế và trạng thái, thuật toán tọa độ, gesture, hiệu năng |
| [03 — Quy chuẩn UI/UX](docs/03-ui-ux.md) | Ba bảng màu, wireframe dọc/ngang, luồng thao tác và tính năng bổ sung |
| [04 — Quy chuẩn dự án, môn học và AI](docs/04-quy-chuan.md) | Quy tắc mã nguồn, tài liệu, làm việc cá nhân, lịch sử Git và mẫu ghi chép AI |
| [05 — Lộ trình, nghiệm thu và nộp bài](docs/05-nghiem-thu.md) | Các nhóm công việc linh hoạt, ca kiểm thử, minh chứng và kịch bản video |
| [06 — Làm từng phần với Codex](docs/06-prompt-codex.md) | Cách giao từng việc trong chat hiện tại và prompt dùng khi bắt đầu chat mới |

## Quyết định đề xuất để duyệt

- Android Kotlin, XML, Material Components và Custom View Canvas. Một Activity và một module ứng dụng.
- Một phòng, một suất giả lập, 196 đối tượng ghế tương ứng 212 chỗ ngồi. Ghế đôi là một đối tượng có hai chỗ.
- Lọc theo loại và trạng thái. Ghế ngoài bộ lọc được làm mờ và khóa thao tác trên sơ đồ. Các ghế đã chọn tiếp tục xuất hiện trong Bottom Sheet và người dùng có thể định vị hoặc xóa từng mục.
- Hai hướng màn hình dùng cùng một sơ đồ. Reset View đưa camera về góc nhìn toàn phòng, giữ lựa chọn và điều kiện lọc.
- Tone **Kem – Chàm** được đề xuất. Hai tone thay thế và các cải tiến nằm trong tài liệu UI/UX.
- Tiến độ nghiệm thu được ghi bằng kết quả build, test và demo thực tế. Phần đã kiểm tra và việc còn lại được ghi trong [checklist](docs/05-nghiem-thu.md#checklist-còn-lại).

## Cách học và làm từng phần

Bắt đầu bằng việc đọc tiêu chí và hiểu khái niệm liên quan. Sau đó xác định việc cần làm, yêu cầu Agent setup phần cần thiết và hướng dẫn theo từng bài thực hành nhỏ. Sinh viên tự viết phần đang học, chạy thử, rồi nhờ Agent gợi ý debug hoặc review. Khi đã tự làm và hiểu một mẫu, sinh viên có thể giao Agent hoàn thiện phần lặp lại đã chỉ định.

Chia commit theo phần thay đổi có ý nghĩa và nhịp làm thực tế. Theo dõi yêu cầu “05–07” của đề và giữ lịch sử phân bố trong thời gian làm bài. Xem [quy trình phối hợp](docs/04-quy-chuan.md) và [các nhóm công việc](docs/05-nghiem-thu.md).

## Nhật ký sử dụng AI Agent

| Ngày | Agent và nhiệm vụ | Tệp hoặc phần được tạo/chỉnh | Hạn chế và cách sinh viên kiểm tra |
| --- | --- | --- | --- |
| 2026-11-23 | Codex đọc 5 trang đề, đối chiếu rubric, lập đặc tả, phương án UI/UX, ca nghiệm thu và prompt bàn giao | README này và 6 tài liệu trong `docs/` | Các quyết định kỹ thuật và UI là đề xuất. Sinh viên đối chiếu mã yêu cầu với PDF, duyệt cách tính ghế đôi và hành vi lọc, sau đó kiểm tra bằng build và demo khi có ứng dụng. Chờ sinh viên xác nhận. |
| 2026-11-23 | Codex cập nhật kế hoạch theo quy trình học do sinh viên mô tả | README và các mục lộ trình, Git, thiết lập, quy chuẩn phối hợp và prompt làm từng phần | Sinh viên đã phản hồi về lịch commit và phạm vi tự động hóa. Bản cập nhật ghi lại quy trình làm từng phần. Việc xác nhận nội dung cập nhật đang chờ. |
| 2026-11-23 | Codex cập nhật hướng dẫn theo lựa chọn tiếp tục dùng Codex | README và `docs/06-prompt-codex.md` | Hướng dẫn giao từng việc trong chat hiện tại và khởi động lại ngữ cảnh khi mở chat mới. |
| 2026-11-23 | Codex thiết lập nền dự án và nối dữ liệu phòng thật vào Canvas | Git local, cấu hình bỏ qua tệp máy, mô hình ghế, dữ liệu giả lập, hình học và Canvas tổng quan | Gradle build và unit test của dự án chạy thành công. Kiểm tra riêng trên các lớp đã biên dịch xác nhận 196 ghế, 212 chỗ, mã duy nhất và lối đi giữa. Kiểm chứng UI trên Pixel_7 ở hai hướng đang chờ. Camera tương tác và chọn ghế sẽ triển khai ở phần kế tiếp. |
| 2026-11-24 | Codex nới giới hạn zoom và sửa chuyển từ pinch sang kéo bằng ngón còn lại | Camera trong `CinemaMapView.kt` và giới hạn zoom trong tài liệu | Build và unit test thành công. Phép thử tạm gửi sự kiện vào View trên Pixel_7 xác nhận kéo trái/phải sau zoom và kéo tiếp sau khi nhấc một ngón. Sinh viên kiểm tra lại bằng thao tác trực tiếp. |
| 2026-11-24 | Codex triển khai Reset View bằng nút Toàn phòng | Layout, MainActivity và camera trong `CinemaMapView.kt` | Build và unit test đã qua. Phép thử trên Pixel_7 xác nhận nút đưa camera về đúng tọa độ và độ phóng mặc định, và chạm vào sơ đồ dừng hoạt ảnh. T06 tích hợp sẽ kiểm tra lại khi có lựa chọn, lọc và Sheet. |
| 2026-11-24 | Antigravity cùng sinh viên triển khai BookingViewModel và chọn ghế trên Canvas | `BookingUiState.kt`, `BookingViewModel.kt`, `BookingViewModelTest.kt`, `CinemaMapView.kt`, `MainActivity.kt` | Build và unit test bao phủ phần logic T02 đã qua: chọn/bỏ chọn, tổng số ghế/chỗ, giá theo từng đối tượng và chặn ghế đã đặt. Nhánh không khả dụng đã có trong mã, còn chờ test riêng. Hiển thị thông tin và tạm tính qua Bottom Sheet cùng nghiệm thu T02 trên UI đang chờ. |
| 2026-11-27 | Codex đồng nhất tiến độ README và checklist với mã nguồn 915b1a7 | README.md và docs/05-nghiem-thu.md | Đối chiếu ViewModel, luồng Activity–Canvas và unit test hiện có. Ghi rõ phạm vi logic T02 đã có bằng chứng, các ca UI và xoay đang chờ. |
| 2026-11-27 | Codex triển khai Bottom Sheet hai nấc và nối với state hiện có | MainActivity, layout, SelectedSeatAdapter, locateSeat và kiểm thử BookingSheetTest | Build/unit test đã qua. Hai ca tích hợp trên Pixel_7 kiểm tra tổng tiền, xóa/định vị, dựng lại Activity và vùng map/danh sách cuộn ở hướng ngang. Bộ lọc và khôi phục sau khi hệ thống hủy tiến trình còn chờ. |

Khi triển khai, bổ sung hướng dẫn chạy theo môi trường đã kiểm chứng, kiến trúc mã nguồn thực tế, tổ chức dữ liệu, công thức đổi tọa độ, hit-testing, quản lý trạng thái, tối ưu hiệu năng và liên kết video. Cập nhật nhật ký sau mỗi phiên sử dụng AI theo [quy chuẩn ghi chép](docs/04-quy-chuan.md).

## Tài liệu đầu vào

- Đề thi giữa kỳ, học phần Phát triển phần mềm cho thiết bị di động, 5 trang, tệp `De_thi_giua_ky_CNTN2024.pdf`.
- Ảnh người dùng cung cấp về bước chọn ghế trong MoMo tại Lotte Phú Thọ, dùng để quan sát cách định hướng sơ đồ, đọc chú thích và xem tạm tính.
