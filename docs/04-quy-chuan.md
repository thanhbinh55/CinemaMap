# Nguyên tắc thiết kế và phương pháp thực hiện

## Phạm vi học phần

Dự án thực hiện đề tài sơ đồ phòng chiếu rạp phim trong đề giữa kỳ cá nhân của học phần Phát triển phần mềm cho thiết bị di động. Dữ liệu giả lập được sử dụng theo phạm vi cho phép của đề. Các yêu cầu bắt buộc được truy vết trong [đặc tả](01-yeu-cau.md); phần 3D là chức năng mở rộng.

## Phân tách trách nhiệm

Dữ liệu ghế được quản lý độc lập với lớp vẽ. ViewModel xử lý lựa chọn, điều kiện lọc và các tổng dẫn xuất. Canvas và Bottom Sheet cùng nhận trạng thái từ ViewModel; các thao tác giao diện gửi sự kiện theo ID ghế. Cách tổ chức này tránh việc mỗi thành phần duy trì một danh sách lựa chọn riêng.

Hình học dùng tọa độ world cố định. Sự kiện chạm dùng pixel cục bộ của View và được chuyển ngược qua camera trước hit-testing. Cùng một phép biến đổi được sử dụng cho vẽ và kiểm tra vùng chạm. Dọc và ngang sử dụng chung dữ liệu, quy tắc trạng thái và hệ màu.

## Quy ước mã nguồn

- Tên lớp, hàm và biến thể hiện trách nhiệm bằng tiếng Anh; chuỗi giao diện tiếng Việt đặt trong tài nguyên Android.
- Kích thước giao diện dùng dp/sp; hình học sơ đồ dùng đơn vị world; sự kiện chạm và viewport dùng pixel.
- Paint, Path, RectF và hình học được tái sử dụng. Thông số nhãn ghế được tính trước khi vẽ.
- Ghi chú mã tập trung vào phép toán, vòng đời và trường hợp biên cần giải thích.
- Dữ liệu giả lập cố định phục vụ tái hiện các ca chọn, lọc và tính tiền.

## Phương pháp thực hiện và học tập

Quá trình thực hiện gồm phân tích đề, tìm hiểu khái niệm, cài đặt từng nhóm chức năng, chạy thử và điều chỉnh. Sinh viên sử dụng giải thích của AI để đọc luồng giữa các lớp và tự viết lại một số đoạn cơ bản. Những phần tiếp theo được hoàn thiện theo từng chức năng, thay vì coi bản kế hoạch ban đầu là sản phẩm hoàn chỉnh.

Các phản hồi giao diện tập trung vào khả năng quan sát sơ đồ, phân biệt trạng thái, bố cục ngang và thao tác bằng một hoặc hai ngón. UI/UX của MoMo và CGV được sinh viên tìm hiểu để tham khảo; giao diện, dữ liệu và tài nguyên trong dự án được triển khai riêng.

## Tài liệu và kiểm chứng

README trình bày các nội dung theo mục 5.4 của đề và có nhật ký AI theo mục 5.5. Mỗi phát biểu về chức năng gắn với mã nguồn, ca kiểm thử hoặc hình ảnh/video. Kết quả kỳ vọng, kết quả đã chạy và hạn chế được phân biệt trong [báo cáo kiểm chứng](09-kiem-chung.md). Tài liệu không sử dụng điểm tự chấm để thay kết luận của giảng viên.

Lịch sử Git được giữ theo các mốc phát triển thực tế. Đề yêu cầu tối thiểu 05–07 commit có ý nghĩa; không yêu cầu chia đều commit theo từng ngày. Tệp build, SDK theo máy và bản quay/video hậu kỳ không thuộc mã nguồn được theo dõi. Video minh họa được chia sẻ bằng liên kết riêng.

## Sử dụng AI và nguồn tài nguyên

Codex hỗ trợ định hướng, giải thích, sinh/chỉnh mã, kiểm thử và biên tập. Phạm vi đóng góp và cách kiểm tra được ghi trong [nhật ký AI tại README](../README.md#nhật-ký-sử-dụng-ai-agent) và [tài liệu sử dụng AI](06-su-dung-ai.md). Sinh viên chịu trách nhiệm về nội dung nộp theo quy định của đề.

Clip lửa trại được tạo bằng mã; ảnh tham khảo không được đóng gói trong APK. Video demo được ghi từ ứng dụng, sau đó bổ sung chú thích, con trỏ và giọng đọc tổng hợp tiếng Việt. Hai loại video có mục đích riêng: tài nguyên trình chiếu trong app và minh họa chức năng của bài làm.
