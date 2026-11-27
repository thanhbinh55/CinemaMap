# UI/UX đang triển khai

## Nhận diện và màu

Nền trắng, chủ đạo navy, ghế chưa chọn pastel và ghế đang chọn cobalt nổi bật theo lựa chọn sinh viên. Không sao chép logo hay tài nguyên của MoMo/CGV; ảnh sinh viên cung cấp chỉ tham khảo thứ bậc thông tin, điều hướng và thao tác.

| Vai trò | Nền | Chữ |
| --- | --- | --- |
| App / Canvas / Sheet / dialog | #FFFFFF | Chính #102B4E; phụ #51647E |
| Điều khiển / màn chiếu | Navy #002366 | Theo Material theme |
| Thường | #CDDFF8 | #315D93 |
| VIP | #D5C8EC | #624383 |
| Ghế đôi | #BFE3DB | #276555 |
| Đã đặt | #9CA8B8 | #2D3A4B |
| Không khả dụng | #F3C9AD | #86482B |
| Đang chọn | #0057FF | #FFFFFF |

Ô ghế và mẫu Legend bo góc, **không vẽ viền**, không dùng ✓/−/× hoặc vạch chia ghế đôi. Ghế đôi rộng hơn; chi tiết ghi 2 chỗ/tính theo cặp. Định vị dùng vòng cobalt; vòng này khác trạng thái đang chọn. Mã ghế luôn hiện, chữ đậm 11,2 world (80% cỡ 14 trước đó), căn giữa theo font và tự thu nếu vượt đệm ngang 8 world. Ở toàn phòng chữ nhỏ theo kích thước ô, hỗ trợ pinch để đọc/chọn chi tiết.

## Bố cục và điều hướng

- Dọc: bộ lọc ở trên; Canvas ở giữa, Legend ngay trên Sheet; Sheet thu/mở luôn để lại vùng map tương tác. Màn chiếu cong nằm ở đầu phòng.
- Ngang: Canvas/Legend 64% chiều rộng bên trái; Sheet 36% bên phải. Mở danh sách không ép chiều cao map thành dải nhỏ.
- Nút Toàn phòng là biểu tượng 48dp có tooltip/contentDescription ở góc trái vùng map. Minimap góc phải hiện khi pan hoặc zoom; khung navy biểu diễn viewport và chạm để reset.
- Header: `Rạp HCMUS · Phòng 01 · 19:00`. Số ghế/chỗ bên trái, tiền đậm bên phải; ghế đang xem và Xem ghế/Thu gọn ở dòng dưới. Nhãn nút phản ánh đúng nấc sau xoay.
- Legend cuộn ngang có nút Chi tiết cố định. Dialog giải thích toàn bộ loại/trạng thái, ghế mờ, định vị, lối đi và ghế đôi; bôi đậm các ý chính.

## Luồng chọn/lọc/Sheet

1. Chạm Trống để chọn; chạm lại để bỏ. Đã đặt/Không khả dụng chỉ hiện thông tin. StateFlow cập nhật nền, số lượng và giá.
2. Lọc bằng hai nút Loại/Trạng thái, mỗi nút mở dialog chọn một giá trị. AND giữa hai tiêu chí; nhãn nút luôn thể hiện điều kiện. Có Xóa lọc chung; không có chip xóa riêng mỗi điều kiện.
3. Ngoài lọc: mờ/khóa chạm ở Canvas, lựa chọn vẫn nằm trong Sheet kèm nhãn Ngoài bộ lọc. Không đột ngột xóa vé của người dùng.
4. Không có kết quả: hiện thông báo và hướng dẫn Xóa lọc. Không hiển thị bộ đếm 196/196 theo yêu cầu sinh viên.
5. Mở Sheet: danh sách cuộn, dòng ghế định vị và đổi inspectedSeat; Xóa bỏ riêng ghế đó, cập nhật tạm tính ngay. Reset chỉ đổi camera, giữ lọc/lựa chọn/nấc Sheet.
6. Xoay dọc/ngang giữ trạng thái và góc nhìn tương đối trong biên mới. Insets chừa thanh hệ thống; ngoài Canvas, điều khiển dùng dp/sp.

## Xem trước 3D

Nút 3D trong dòng mở đúng ghế đó; nút chung mở inspectedSeat. Xem 3D không đổi vé, bộ lọc hoặc giá. Kéo một ngón nhìn quanh 360°, Nhìn màn chiếu reset hướng, Đổi ghế xem chọn hàng/ID. Ghế đôi chỉ **Trái/Phải**, mặc định Trái; không có vị trí Giữa.

Minimap 3D mở Canvas lớn có pan/zoom để chọn chính xác. A/B ghim hai vị trí và tự pause phim, cùng FOV/hướng màn chiếu; giữ nút để ghim lại. Menu có toàn màn hình, cấu hình phòng và xóa ghim. Back thoát toàn màn hình trước khi rời cảnh. Quay lại 2D định vị ID xem cuối và cập nhật thông tin/nút chung.

Video lửa trại 5 giây tự tạo, phát offline, lặp, không âm thanh; có Phát/Tạm dừng. Phòng 3D ghi rõ giả lập, không cam kết góc nhìn của rạp thật. [Chi tiết](07-preview-3d.md), [nguồn video](08-trailer.md).

## Giới hạn UX và ý tưởng chưa triển khai

TalkBack có mô tả cho điều khiển và dòng Sheet, nhưng Canvas chưa có accessibility node riêng cho từng ghế. Màu trạng thái được dùng theo yêu cầu sinh viên; tên trạng thái vẫn có trong chi tiết/Legend. Chưa cam kết hỗ trợ hoàn chỉnh mọi dạng rối loạn sắc giác. Kiểm tra font lớn, màn nhỏ và Samsung thật tiếp tục nằm trong phần sinh viên dùng thử.

Chưa triển khai: tìm nhanh ID, nút +/- một ngón, xác nhận đặt vé/thanh toán và backend. Đề cho phép dùng **lọc hoặc tìm kiếm**, mock data và không yêu cầu thanh toán, nên đây là cải tiến tùy chọn. Không mở rộng các phần này để thay thế video demo và kiểm chứng các chức năng bắt buộc.
