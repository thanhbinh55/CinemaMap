# Quy chuẩn dự án, môn học và sử dụng AI

## Thiết kế và mã nguồn

- Dùng một Custom View cho sơ đồ. Mọi hình ghế, màn chiếu, lối đi, nhãn khu/hàng được vẽ trong cùng Canvas. RecyclerView phục vụ danh sách Bottom Sheet.
- Giữ quan hệ dữ liệu → trạng thái → UI. Một phép biến đổi camera cho vẽ và hit-testing. UI gửi ý định theo ID, ViewModel thực thi quy tắc chọn/lọc. Thông tin hiển thị dẫn xuất từ state hiện tại.
- Tên lớp, hàm và test bằng tiếng Anh, mô tả ý nghĩa nghiệp vụ. Nội dung hiển thị bằng tiếng Việt, đặt trong tài nguyên chuỗi. Dùng dp/sp cho UI, pixel cục bộ cho sự kiện và world cho hình học. Ghi chú ngắn ở phép toán hoặc đoạn có điều kiện biên.
- Giữ nguyên một tập token màu, chữ, khoảng cách và dấu hiệu trạng thái cho hai hướng. Thêm trạng thái rỗng, phản hồi thao tác và nhãn hỗ trợ đọc màn hình cho các điều khiển. Theo [quy chuẩn UI/UX](03-ui-ux.md) khi duyệt layout.
- Trước mỗi mốc, chạy kiểm tra phù hợp với phần thay đổi và ghi môi trường, lệnh, kết quả thật. Giữ test xác định được kết quả và fixture cố định.

## Quy trình phối hợp với Agent

1. **Hiểu đề và khái niệm.** Chọn một phần nhỏ, đối chiếu mã R và tiêu chí chấm, giải thích khái niệm cùng ví dụ. Chẳng hạn, trước khi viết hit-testing, sinh viên tự đổi một điểm chạm từ pixel về world.
2. **Xác định việc cần làm và setup.** Tách phần đang học thành các bước, nêu đầu vào, tệp liên quan và kết quả có thể kiểm tra. Agent hướng dẫn hoặc trực tiếp setup theo đúng nội dung sinh viên yêu cầu.
3. **Hướng dẫn để sinh viên tự làm.** Đưa một bài thực hành vừa sức, gợi ý hoặc ví dụ nhỏ theo mức sinh viên cần. Sinh viên tự cài đặt, chạy thử và có thể yêu cầu đọc diff, giải thích lỗi hoặc gợi ý bước debug.
4. **Hoàn thiện phần được giao.** Khi sinh viên đã luyện một mẫu và chủ động yêu cầu Agent làm tiếp phần lặp lại, giới hạn sửa đổi vào phần đó. Báo thay đổi, kiểm tra đã chạy và điểm sinh viên cần tự xác minh.
5. **Chốt phần hiện tại.** Đối chiếu ca nghiệm thu, cập nhật nhật ký và chọn thời điểm commit theo thay đổi thực tế. Sinh viên chủ động chọn công việc kế tiếp.

Duyệt kế hoạch xác nhận hướng làm chung. Mỗi lượt hỗ trợ dựa trên phần việc và hành động sinh viên yêu cầu, như giải thích, lập danh sách việc, setup, hướng dẫn, review hoặc sửa trực tiếp. Hướng dẫn và review mặc định thực hiện ở chế độ đọc. Agent chỉ sửa mã, tạo dự án, commit hoặc chuyển sang phần kế khi sinh viên yêu cầu hành động tương ứng. Ghi đóng góp của Agent và phần sinh viên tự làm theo thực tế.

## Tài liệu và quy trình cá nhân

- README sau triển khai có mô tả đề tài, cách tải và chạy, SDK/JDK/Gradle đã kiểm chứng, kiến trúc và dữ liệu, công thức tọa độ, hit-testing, quản lý trạng thái khi xoay, hiệu năng, video và nhật ký AI. Dẫn ra tài liệu chi tiết khi cần. Dùng đường dẫn tương đối trong repo.
- Dùng mã R cho yêu cầu và mã T cho ca kiểm thử. Với mỗi ca, ghi build/commit, thiết bị và hướng, dữ liệu đầu vào, điều mong đợi, kết quả, và đường dẫn minh chứng nếu có. Trạng thái ghi `Chờ triển khai`, `Chưa chạy`, `Đạt` hoặc `Không đạt`.
- Sinh viên duyệt thay đổi, chịu trách nhiệm về toàn bộ mã, chạy demo và giải thích luồng gesture, phép đổi tọa độ và state. Các yêu cầu học thuật của học phần và nhà trường tiếp tục áp dụng. Đề cho phép AI hỗ trợ phân tích, kiến trúc, viết/chỉnh mã, debug, kiểm thử và giải thích lỗi.
- Theo mốc “05–07” của đề, chọn các commit có ý nghĩa và phân bố trong thời gian triển khai thật. [Lộ trình](05-nghiem-thu.md) là các nhóm công việc để chọn theo tiến độ. Mỗi commit mô tả ý nghĩa thay đổi, kiểm tra diff và các tệp được theo dõi. Nộp liên kết repo GitHub và video truy cập được với quyền của người chấm.
- Dùng tên rạp/phim, giá và dữ liệu tự tạo. Tài nguyên bên thứ ba, nếu sử dụng, ghi nguồn và quyền sử dụng rõ. Dữ liệu nhận dạng, tài khoản, khóa, file ký và thông tin SDK theo máy nằm ngoài repo.

## Nhật ký sử dụng AI Agent

Ghi **trong README.md**, sau mỗi đợt có đóng góp đáng kể. Mỗi mục ghi đầy đủ:

| Trường | Nội dung cần lưu |
| --- | --- |
| Ngày và công cụ | Ngày thực tế, tên AI Agent, model nếu biết |
| Nhiệm vụ giao | Mục tiêu, phần yêu cầu, prompt tóm tắt hoặc đường dẫn prompt nội bộ |
| Đóng góp | Tệp/lớp/thuật toán được tạo, sửa hoặc đề xuất và tình trạng tiếp nhận |
| Lỗi hay hạn chế | Lỗi quan sát được và điểm cần xác minh. Nếu đã kiểm tra mà chưa thấy lỗi, ghi phạm vi kiểm tra |
| Kiểm tra của sinh viên | Cách đọc diff, chạy test/đo/dùng thử, kết quả thật và cách sinh viên sửa hoặc quyết định loại bỏ |

Mẫu khi cập nhật hàng nhật ký:

```text
Ngày / AI Agent:
Nhiệm vụ và mã yêu cầu:
Phần đầu ra sử dụng, tệp hoặc commit:
Lỗi, hạn chế, phần còn chờ kiểm tra:
Cách sinh viên thực sự kiểm tra và điều chỉnh:
```

Mục lập kế hoạch bằng Codex đã được ghi trong [README](../README.md). Các mục sau ghi khi có công việc và kiểm tra thực tế.
