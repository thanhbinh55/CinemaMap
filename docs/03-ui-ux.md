# Thiết kế giao diện và trải nghiệm sử dụng

## Định hướng

Giao diện sử dụng nền trắng, navy làm màu chủ đạo và cobalt cho ghế đang chọn. Sinh viên tham khảo UI/UX đặt ghế của MoMo, CGV và các ảnh minh họa để tìm hiểu cách phân cấp thông tin, bố trí chú thích và điều hướng sơ đồ. Logo và tài nguyên của các nền tảng đó không được sử dụng trong ứng dụng.

## Hệ màu

Các màu được khai báo tập trung trong `res/values/colors.xml` và dùng cho Canvas, chú thích và minimap.

| Vai trò | Màu nền | Màu chữ |
| --- | --- | --- |
| Giao diện chính | #FFFFFF | #102B4E; phụ #51647E |
| Điều khiển và màn chiếu | #002366 | Theo ngữ cảnh Material theme |
| Ghế thường | #CDDFF8 | #315D93 |
| Ghế VIP | #D5C8EC | #624383 |
| Ghế đôi | #BFE3DB | #276555 |
| Đã đặt | #9CA8B8 | #2D3A4B |
| Không khả dụng | #F3C9AD | #86482B |
| Đang chọn | #0057FF | #FFFFFF |

Ô ghế và mẫu chú thích được tô nền bo góc, không có viền hoặc ký hiệu trạng thái. Ghế đôi rộng hơn ghế đơn; thông tin chi tiết thể hiện hai chỗ và giá theo cặp. Mã ghế luôn hiển thị, cỡ chữ 11,2 đơn vị world, căn giữa và tự giảm nếu vượt chiều rộng cho phép. Vòng tròn cobalt biểu diễn ghế đang được định vị, độc lập với trạng thái chọn.

## Bố cục

Ở hướng dọc, bộ lọc nằm trên sơ đồ; thanh chú thích nằm giữa Canvas và Bottom Sheet. Khi Sheet mở, một phần Canvas vẫn nhận thao tác. Ở hướng ngang, Canvas/chú thích chiếm 64% chiều rộng bên trái, Sheet chiếm phần còn lại bên phải.

Nút Toàn phòng là biểu tượng 48 dp ở góc trái vùng sơ đồ, có tooltip và contentDescription. Minimap ở góc phải xuất hiện khi pan/zoom và hiển thị khung vùng đang quan sát. Chạm minimap đưa camera về mặc định.

Thông tin tóm tắt gồm rạp, phòng, giờ chiếu, số ghế/chỗ và tạm tính. Tạm tính được căn phải và in đậm. Nhãn Xem ghế/Thu gọn phản ánh nấc Sheet, bao gồm sau xoay màn hình. Chú thích có thể cuộn ngang; nút Chi tiết mở giải thích đầy đủ màu, trạng thái, ghế đôi, lối đi, ghế mờ và vòng định vị.

## Quy tắc tương tác

1. Ghế trống được chọn hoặc bỏ chọn bằng một lần chạm. Ghế đã đặt/không khả dụng chỉ hiển thị thông tin.
2. Bộ lọc gồm loại và trạng thái; hai điều kiện kết hợp AND. Điều kiện hiện trên nút, có thao tác Xóa lọc và thông báo khi không có kết quả.
3. Ghế ngoài lọc được làm mờ và không nhận chạm chọn trên sơ đồ. Nếu đã được chọn, ghế vẫn nằm trong danh sách với nhãn Ngoài bộ lọc.
4. Chạm dòng danh sách để định vị; Xóa loại riêng mục đó và cập nhật Canvas/tạm tính. Reset View chỉ thay góc nhìn.
5. Pinch dùng tâm hai ngón làm điểm neo; pan có giới hạn. Một chuỗi pinch không tạo thao tác chọn ghế ngoài ý muốn.
6. Xoay giữ trạng thái và tính lại camera theo vùng hiển thị mới. Insets dành chỗ cho thanh hệ thống.

## Giao diện 3D

Nút chung mở góc nhìn của ghế đang xem; nút 3D trên dòng danh sách mở đúng ghế của dòng đó. Ghế đôi có lựa chọn Trái/Phải, mặc định Trái. Kéo một ngón thay đổi hướng nhìn; Nhìn màn chiếu đưa hướng về mặc định.

Minimap mở sơ đồ lớn để đổi vị trí. Hai nút A/B lưu và chuyển giữa các góc nhìn; chuyển so sánh tạm dừng video. Menu cung cấp toàn màn hình, cấu hình phòng và xóa ghim. Khi quay về 2D, ghế vừa xem được định vị, còn lựa chọn và tạm tính được giữ.

## Giới hạn

Màu nền là dấu hiệu trạng thái trực tiếp trên sơ đồ; tên trạng thái còn được trình bày trong thông tin và chú thích. Canvas chưa cung cấp accessibility node riêng từng ghế. Khả năng hỗ trợ đầy đủ trình đọc màn hình, mọi cỡ chữ và mọi dạng rối loạn sắc giác chưa được kiểm chứng. Kết quả bố cục, tương tác và hiệu năng thuộc phạm vi ghi trong [báo cáo kiểm chứng](09-kiem-chung.md).
