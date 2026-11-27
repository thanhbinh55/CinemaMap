# Sử dụng AI trong quá trình thực hiện

## Công cụ và phạm vi hỗ trợ

Codex được sử dụng trong phân tích yêu cầu, đề xuất kiến trúc, giải thích khái niệm, sinh/chỉnh sửa mã nguồn, hỗ trợ gỡ lỗi, viết kiểm thử và biên tập tài liệu. Đóng góp của AI bao gồm mã được sử dụng trong sản phẩm, không chỉ nội dung tư vấn. Nhật ký theo giai đoạn được trình bày trực tiếp trong [README](../README.md#nhật-ký-sử-dụng-ai-agent) theo mục 5.5 của đề.

| Nhóm công việc | Thành phần có AI hỗ trợ |
| --- | --- |
| Sơ đồ 2D | Dữ liệu giả lập, mô hình ghế, hình học, phép biến đổi, Canvas và gesture |
| Trạng thái và giao diện | ViewModel, bộ lọc, danh sách Bottom Sheet, đồng bộ và phục hồi trạng thái |
| Thiết kế UI/UX | Đề xuất bố cục, palette, chú thích, bản đồ nhỏ và các điều chỉnh theo phản hồi |
| Mở rộng | Hình học phòng 3D, renderer OpenGL, camera, A/B và phát video |
| Tài nguyên | Mã tạo clip lửa trại; công cụ biên tập video minh họa và lời đọc tổng hợp |
| Kiểm chứng | Unit test, instrumentation test, kiểm tra build/lint và thu thập số đo |

## Hoạt động của sinh viên

Sinh viên lựa chọn đề tài, xác định yêu cầu bổ sung, yêu cầu giải thích từng phần và tự viết lại một số đoạn cơ bản để tìm hiểu cấu trúc và luồng hoạt động. Nội dung tìm hiểu gồm quan hệ giữa hàng ghế và vị trí ghế, tọa độ world/pixel, phép biến đổi thuận/nghịch, tâm pinch, xử lý thao tác và đồng bộ ViewModel với Canvas/Bottom Sheet.

Sinh viên tìm hiểu UI/UX của MoMo, CGV và cung cấp ảnh tham khảo cho luồng chọn ghế. Các điều chỉnh được thực hiện theo phản hồi về bố cục ngang, màu trạng thái, bo góc, cỡ chữ, vị trí tạm tính, bản đồ nhỏ và ghế đôi. Sinh viên tự quay video thao tác, duyệt nội dung lời đọc và lựa chọn giọng nam tiếng Việt.

Hoạt động tự thực hành tập trung vào các đoạn cơ bản và việc đọc luồng xử lý. Kiểm thử tự động bằng công cụ và phản hồi thực tế của sinh viên được ghi nhận riêng trong hồ sơ.

## Kiểm tra và điều chỉnh đầu ra

| Nội dung | Phương pháp đối chiếu | Điều chỉnh được ghi nhận |
| --- | --- | --- |
| Hình học và chạm ghế | Kiểm tra ID, số lượng, tọa độ và điểm sát biên qua nhiều camera | Dùng phép biến đổi nghịch đảo trước hit-testing |
| Pan và pinch | Thử thao tác, sự kiện nhiều ngón và giới hạn camera | Điều chỉnh min/max; xử lý ngón còn lại sau pinch |
| Canvas–Bottom Sheet | Kiểm tra chọn, định vị, xóa, bộ lọc và xoay | Dùng nguồn trạng thái thống nhất; đồng bộ nhãn và nấc Sheet |
| Ghế đôi trong 3D | Đối chiếu sức chứa hai chỗ và phản hồi sinh viên | Chỉ giữ hai vị trí Trái/Phải; phục hồi trạng thái cũ về vị trí hợp lệ |
| Video trong cảnh 3D | Kiểm tra metadata, điểm ảnh, pause/resume và vòng đời Surface | Quản lý decoder theo Surface hiện tại, lưu vị trí phát |
| Hiệu năng | Đọc đường vẽ và thu thập gfxinfo | Tái sử dụng tài nguyên, cache thông số chữ, bỏ vẽ ghế ngoài viewport; giữ các giới hạn đo trong báo cáo |
| Tài liệu | So sánh với mã nguồn, log và đề | Loại bỏ nội dung kế hoạch không còn áp dụng và phát biểu chưa có chứng cứ |

## Giới hạn

Phòng 3D và giá vé sử dụng dữ liệu giả lập. Số đo hiệu năng thuộc môi trường được ghi trong báo cáo; kiểm thử chức năng đạt không đồng nghĩa mọi thiết bị đều đạt cùng độ mượt. Phục hồi qua xoay/dựng lại Activity và phục hồi bằng SavedStateHandle được phân biệt với việc kiểm thử Android hủy tiến trình thực tế.

Trách nhiệm hiểu, kiểm tra và giải trình mã nguồn thuộc sinh viên theo mục 4 của đề. [Báo cáo kiểm chứng](09-kiem-chung.md) lưu phạm vi đã kiểm tra và những hạn chế còn tồn tại.
