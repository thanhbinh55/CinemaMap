# Thiết kế và trải nghiệm chọn ghế

## Tham khảo từ ảnh MoMo

Ảnh do sinh viên cung cấp cho thấy thứ bậc dễ theo dõi: tên rạp, hướng màn chiếu, sơ đồ ghế, chú thích rồi thông tin suất chiếu và tạm tính ở đáy. Dùng nguyên tắc định hướng đó cho rạp và phim hư cấu. Thiết kế riêng với dạng dấu ghế hình chữ nhật, nhãn trực tiếp cho từng khu, thanh lọc và nút Toàn phòng, cùng Sheet hai nấc phục vụ bài thi. Dùng biểu tượng hình học tự vẽ cho trạng thái ghế.

## Ba hướng màu

| Tone | Nền app / bề mặt | Chữ chính / chữ phụ | Nhấn / chữ trên nhấn | Thường / VIP / Đôi | Cảm nhận |
| --- | --- | --- | --- | --- | --- |
| **Kem – Chàm (đề xuất)** | `#F7F4ED` / `#FFFFFF` | `#181A2A` / `#514C61` | `#3C3593` / `#FFFFFF` | `#E1E5F7` / `#F7DCB7` / `#D6EFE6` | Sáng, dễ đọc khi nhìn tổng quan |
| Bạc hà – Xanh rừng | `#F0FAF6` / `#FFFFFF` | `#142922` / `#3B534A` | `#075949` / `#FFFFFF` | `#D5EDE8` / `#F8E2AB` / `#E8DEF8` | Dịu, sạch, phù hợp demo ngoài trời |
| Than – Hổ phách | `#141820` / `#202632` | `#F2F5FA` / `#BAC5D5` | `#FFCA66` / `#241800` | `#33445D` / `#664834` / `#325B54` | Tương phản theo chất liệu phòng chiếu |

### Mã hóa trạng thái

| Trạng thái | Dấu hiệu | Chạm |
| --- | --- | --- |
| Trống | Nền theo loại + viền. Ghế đôi có nét chia và ký hiệu 2 chỗ | Chọn ghế hoặc cặp |
| Đang chọn | Viền đậm và dấu ✓, giữ nhận diện loại bằng nền hoặc dấu chia | Bỏ chọn |
| Đã đặt | Nền trung tính và vạch ngang | Xem mã, loại và trạng thái |
| Không khả dụng | Nền trung tính và dấu × hoặc nét chéo | Xem mã, loại và trạng thái |
| Ngoài bộ lọc | Mờ hơn, kèm nhãn giải thích trong chú thích. Vòng định vị tạm thời nếu yêu cầu từ Sheet | Vùng mờ bỏ qua thao tác chạm |

Dùng font hệ thống với kích thước tham khảo 20sp cho tiêu đề, 14–16sp cho văn bản thao tác và 12–14sp cho metadata. Dùng lưới 4dp, khoảng cách chính 8/16/24dp, mục tiêu vùng chạm 48dp cho điều khiển ngoài Canvas. Viền ghế, ký hiệu và chữ được kiểm tra tương phản trên theme chọn và trong điều kiện sáng khác nhau. Các dấu ghế nhỏ ở góc tổng quan, hỗ trợ phóng to trước khi đọc/chọn ghế chi tiết. Kiểm tra font scale lớn, focus, mô tả nút và insets. Ghế không chứa nội dung trang trí theo phim.

## Bố cục

```text
DỌC — thu gọn                                  NGANG — mở rộng
┌────────────────────────────┐                 ┌────────────────────────────────────────────┐
│ Rạp Ánh Đăng · Phòng 01    │                 │ Phòng 01     Lọc     Chú thích   Toàn phòng │
│ Lọc   Toàn phòng  Chú thích│                 ├────────────────────────────────────────────┤
│ [VIP ×]        108 kết quả │                 │           MÀN CHIẾU   ↔                    │
├────────────────────────────┤                 │   THƯỜNG   ▫ ▫ ▫    ▫ ▫ ▫                  │
│         MÀN CHIẾU          │                 │   VIP      ▫ ▫ ✓    ▫ ▫ ▫                  │
│ A  ▫ ▫ ▫    ▫ ▫ ▫  Thường │                 │   ĐÔI       ▭ ▭      ▭ ▭                   │
│ E  ▫ ▫ ✓    ▫ ▫ ▫  VIP    │                 ├────────────────────────────────────────────┤
│ K   ▭ ▭      ▭ ▭   Đôi     │                 │  ━   1 ghế · 1 chỗ      110.000 đ          │
├────────────────────────────┤                 │  E08  VIP   110.000 đ     Định vị   Xóa    │
│            ━               │                 │  Danh sách có thể cuộn                     │
│ 1 ghế · 1 chỗ    110.000 đ  │                 └────────────────────────────────────────────┘
│ Xem ghế đã chọn            │
└────────────────────────────┘
```

- Màn hình đầu mở trực tiếp sơ đồ với toàn phòng vừa vùng nhìn. Màn chiếu, lối đi, nhãn khu và hàng được vẽ trên Canvas. Toolbar và legend nằm ngoài Canvas. Legend là thanh cuộn ngang nằm giữa Canvas và Bottom Sheet, luôn xuất hiện phía trên mép Sheet. Nút Chi tiết mở phần giải thích đầy đủ, đóng để tiếp tục ở đúng vị trí và lựa chọn hiện tại.
- Bản dọc có tiêu đề, hàng công cụ và dòng điều kiện lọc khi áp dụng. Bản ngang dồn tiêu đề/công cụ vào một hàng khi đủ bề rộng, ưu tiên chiều cao sơ đồ. Mở rộng Sheet định hướng tối đa khoảng 45% vùng nội dung sau thanh công cụ, điều chỉnh theo thiết bị và insets để còn vùng Canvas thao tác. Danh sách cuộn trong Sheet với phần tóm tắt gọn. Dùng thanh kéo và một nút có nhãn đổi Thu gọn/Mở rộng để thao tác được bằng cả chạm nút lẫn kéo.
- Mặc định hiện suất giả lập, 0 ghế, 0 chỗ và 0 đ. Khi chọn, cập nhật số đối tượng, số chỗ và tạm tính ngay, cho xem mã nếu chỉ có một mục. Khi chạm một ghế đã đặt/không khả dụng, tóm tắt hoặc chi tiết ghi mã và trạng thái, giữ số lượng đã chọn. Khi chọn ghế đôi, chỉ rõ 2 chỗ và giá theo cặp.
- Lọc theo loại và trạng thái trong panel gọn. Ô điều kiện hiển thị khi đóng panel, có nút xóa từng điều kiện và Xóa lọc. Khi có 0 kết quả, hiện nội dung “Không có ghế phù hợp” và thao tác xóa bộ lọc, ưu tiên để Canvas và Sheet tiếp tục sử dụng được.
- Chạm dòng hoặc nút Định vị đưa ghế vào vùng nhìn và thêm vòng định vị tạm thời. Xóa bằng nút riêng có nhãn theo mã ghế. Nút Toàn phòng đặt lại camera, giúp khám phá lại toàn sơ đồ. Xoay máy giữ lựa chọn, lọc, Sheet và góc nhìn tương đối theo đặc tả kỹ thuật.

## Cải tiến theo thứ tự ưu tiên

1. Nút phóng to/thu nhỏ lấy tâm viewport, hỗ trợ thao tác bằng một tay. Dùng cùng camera như pinch.
2. Tìm nhanh mã ghế dạng `e8` hoặc `E08`. Dùng chung thao tác định vị, giải thích khi kết quả ngoài bộ lọc.
3. Màn xác nhận đặt vé giả lập, hiển thị mã ghế, số chỗ và tổng tiền. Khi sinh viên yêu cầu triển khai, thao tác xác nhận cập nhật tập ghế đã đặt mô phỏng và xóa các lựa chọn tương ứng trong cùng một lần cập nhật trạng thái. Demo bài thi dùng fixture ổn định, có luồng khôi phục phiên mẫu rõ ràng.
4. Duyệt ghế bằng trình đọc màn hình qua danh sách điều hướng theo hàng hoặc accessibility node ảo, ưu tiên giải pháp ít làm tăng độ phức tạp của Canvas.

Chọn từng cải tiến sau khi đạt các ca bắt buộc. Luồng xác nhận đặt vé là phần mở rộng, còn trạng thái vé Đã đặt đã có ngay trong fixture bắt buộc.
