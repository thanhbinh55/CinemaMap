# Sơ đồ phòng chiếu rạp phim

**Trạng thái: đã thiết lập dự án Android và Canvas tổng quan.** Sinh viên xác định công việc tiếp theo và cách Agent hỗ trợ. Duyệt kế hoạch xác nhận hướng làm chung. Mỗi yêu cầu cụ thể xác định phần việc Agent được làm trong workspace.

Đồ án cá nhân trong một tuần cho học phần Phát triển phần mềm cho thiết bị di động. Đề tài số 2 là sơ đồ phòng chiếu rạp phim. Phương án đề xuất dùng dữ liệu giả lập, Android Kotlin và một Custom View vẽ toàn bộ sơ đồ bằng Canvas.

## Chạy phiên bản hiện tại

1. Mở thư mục chứa README này (`CinemaMap/`) trong Android Studio và đợi Gradle Sync.
2. Chọn máy ảo `Pixel_7`, rồi Run cấu hình `app`.
3. Khi mở app, kiểm tra màn chiếu, lối đi ở giữa và ba khu ghế. Xoay dọc/ngang để kiểm tra sơ đồ vẫn nằm trong vùng nhìn.

Dự án được tạo với Kotlin/Views, Gradle Wrapper 9.6.0, AGP 9.4.1, Gradle JDK 25, compile/target SDK 37 và min SDK 26. Kiểm tra dòng lệnh từ thư mục `CinemaMap/` bằng `./gradlew --offline :app:assembleDebug :app:testDebugUnitTest` khi đã có cache. `CinemaMap/` là gốc dự án Android và repo Git.

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
- Tiến độ nghiệm thu được ghi bằng kết quả build, test và demo thực tế. Bản hiện tại có Canvas tĩnh và dữ liệu độc lập. Kiểm thử tương tác và minh chứng UI trên máy ảo đang chờ.

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

Khi triển khai, bổ sung hướng dẫn chạy theo môi trường đã kiểm chứng, kiến trúc mã nguồn thực tế, tổ chức dữ liệu, công thức đổi tọa độ, hit-testing, quản lý trạng thái, tối ưu hiệu năng và liên kết video. Cập nhật nhật ký sau mỗi phiên sử dụng AI theo [quy chuẩn ghi chép](docs/04-quy-chuan.md).

## Tài liệu đầu vào

- Đề thi giữa kỳ, học phần Phát triển phần mềm cho thiết bị di động, 5 trang, tệp `De_thi_giua_ky_CNTN2024.pdf`.
- Ảnh người dùng cung cấp về bước chọn ghế trong MoMo tại Lotte Phú Thọ, dùng để quan sát cách định hướng sơ đồ, đọc chú thích và xem tạm tính.
