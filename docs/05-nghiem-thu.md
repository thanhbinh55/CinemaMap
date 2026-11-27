# Đối chiếu kiểm chứng và video minh họa

## Nguyên tắc đánh giá

Các ca T01–T16 xác định điều kiện kiểm tra theo [đặc tả R01–R18](01-yeu-cau.md). Kết quả đã thực thi, môi trường và log được lưu trong [báo cáo kiểm chứng](09-kiem-chung.md). Điều kiện kỳ vọng của một ca không được sử dụng thay bằng chứng thực thi.

## Các ca đối chiếu

| Ca | Phạm vi | Điều kiện kiểm tra và minh chứng |
| --- | --- | --- |
| T01 | Dữ liệu và bố cục | 196 ID duy nhất, 212 chỗ, ba loại ghế; màn chiếu/lối đi/nhãn; một Canvas. Fixture, MapGeometryTest, mã View và ảnh chính |
| T02 | Chọn/bỏ chọn | A05 + E08 + K03: 3 ghế, 4 chỗ, 385.000 đồng; bỏ E08 còn 275.000 đồng. Ghế đã đặt/không khả dụng không thêm vào lựa chọn. Unit test, Sheet test và video |
| T03 | Pan | Kéo một ngón theo hai trục, tới bốn góc vẫn còn sơ đồ; không phát tap khi kéo. MapNavigationTest và MapInteractionTest |
| T04 | Pinch | Kiểm tra tâm lệch giữa, min/max, chuỗi nhiều ngón không phát tap. MapInteractionTest và video |
| T05 | Hit-testing | Tâm và sát trong/ngoài bốn cạnh của từng ghế qua nhiều camera; khe/lối đi không trúng. MapGeometryTest |
| T06 | Reset View | Đang pan/zoom có lựa chọn/lọc; reset về fit và giữ trạng thái. BookingSheetTest và video |
| T07 | Chú thích | Đầy đủ loại/trạng thái và quy tắc ghế đôi/lối đi/ngoài lọc. BookingSheetTest và ảnh/video |
| T08 | Bộ lọc | AND loại/trạng thái, điều kiện hiện hành, xóa lọc, tổ hợp rỗng, giữ lựa chọn ngoài lọc. SeatFilterTest, BookingSheetTest và video |
| T09 | Bottom Sheet | Thu/mở, danh sách cuộn và còn vùng Canvas tương tác ở hai hướng. BookingSheetTest và video |
| T10 | Đồng bộ hai chiều | Định vị và xóa theo ID từ danh sách; thay đổi phản ánh lên sơ đồ và tạm tính. BookingViewModelTest, BookingSheetTest và ca điểm ảnh |
| T11 | Xoay | Dọc → ngang → dọc giữ lựa chọn/lọc/tổng/camera/nấc Sheet và nhãn. MapInteractionTest và video |
| T12 | Phục hồi UI | Dựng lại Activity và phục hồi từ SavedStateHandle; lọc ID/enum không hợp lệ. Unit test và instrumentation. Không suy ra kết quả hủy tiến trình Android thực tế |
| T13 | Hiệu năng | Kiểm tra tái sử dụng tài nguyên và mẫu gfxinfo pan. Có số đo; độ mượt trên emulator còn hạn chế, chưa ghi nhận đạt toàn bộ tiêu chí |
| T14 | Build và lịch sử Git | Build app/test, unit test, lint; log build nguồn sạch; lịch sử trên 7 commit phân bố theo thời gian thực hiện |
| T15 | Tài liệu và AI | README có nội dung mục 5.4 và nhật ký mục 5.5; vai trò AI và hoạt động sinh viên mô tả theo công việc thực tế |
| T16 | Video và liên kết | Video 4:49 thể hiện các thao tác mục 5.3; liên kết GitHub/Drive trong README, kèm kiểm tra metadata và quyền truy cập |

## Nội dung video

Video do sinh viên tự quay thao tác trên ứng dụng; hậu kỳ cắt thời gian chờ, bổ sung con trỏ, chú thích và giọng đọc tổng hợp nam tiếng Việt. Không thay thế nội dung giao diện bằng mockup. Bản 4K có lời đọc giữ nguyên luồng hình từ bản dựng 4K không âm thanh; kiểm tra hash luồng video xác nhận không mã hóa lại hình khi ghép lời đọc.

| Mốc | Nội dung |
| --- | --- |
| 0:04 | Sơ đồ, ba loại ghế và chú thích |
| 0:17 | Pinch, pan và minimap |
| 0:35 | Chọn/bỏ chọn và cập nhật lựa chọn |
| 0:52 | Ghế thường/VIP/đôi, số chỗ, giá và ghế không thể chọn |
| 1:21 | Sheet, định vị, xóa và đồng bộ Canvas |
| 1:37 | Danh sách cuộn và vùng tương tác sơ đồ |
| 1:53 | Bộ lọc, lựa chọn ngoài lọc, trạng thái rỗng và Reset View |
| 3:03 | Bố cục ngang, giữ trạng thái và quay lại dọc |
| 3:23 | Phần mở rộng 3D: đổi ghế, ghế đôi, A/B, phim và cấu hình |
| 4:35 | Quay về sơ đồ và giữ danh sách/tạm tính |

Nguồn quay dọc có độ phân giải 720 × 1600 và tốc độ khung hình thay đổi. Bản dựng 4K là bản phóng từ nguồn này; độ phân giải xuất không được diễn giải thành độ chi tiết quay gốc hoặc số FPS của ứng dụng. Đoạn quay ngang đã được trình quay thu nhỏ trong khung dọc, vì vậy có giới hạn chi tiết. Bản liên kết trực tiếp trong README hiện là bản 1080p có lời đọc, cùng nội dung và thời lượng.

## Hồ sơ và phạm vi còn giới hạn

Mã nguồn, các tài liệu, hình ảnh và log kiểm chứng được quản lý trong repository. Video được lưu trên Drive; tệp build và các bản quay lớn được loại khỏi Git. Lịch sử được giữ nguyên, không gộp hoặc tạo lại để làm thay đổi tiến trình thực hiện.

Bằng chứng chức năng, video và hồ sơ đã có không loại bỏ giới hạn về hiệu năng. Tiêu chí thao tác mượt cần được xem xét cùng kết quả trong tài liệu 09. Khả năng giải trình mã nguồn thuộc trách nhiệm cá nhân của sinh viên theo quy định học phần.
