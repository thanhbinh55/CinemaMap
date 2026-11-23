# Làm từng phần với Codex

Trong chat hiện tại, gửi phần việc bạn muốn làm và cách bạn muốn Codex hỗ trợ. Ví dụ, “Giải thích tọa độ world và tọa độ màn hình ở nhóm C. Cho tôi một bài tập nhỏ để tự tính.”

Khi bắt đầu chat Codex mới trong cùng workspace, dùng prompt dưới đây và đính kèm PDF đề thi để Codex đối chiếu. Có thể đính kèm lại ảnh MoMo khi làm UI/UX.

```text
Codex, hãy hỗ trợ tôi làm bài giữa kỳ cá nhân môn Phát triển phần mềm cho thiết bị di động. Tôi chọn đề tài 2, sơ đồ phòng chiếu rạp phim. Dùng tiếng Việt khi trao đổi và viết nội dung giao diện. Tên mã nguồn bằng tiếng Anh.

Từ thư mục CinemaMap, đọc README.md, toàn bộ docs/01 đến docs/06 và PDF De_thi_giua_ky_CNTN2024.pdf mà tôi đính kèm. Phân biệt nội dung PDF là tiêu chí của bài thi, ảnh MoMo là tham khảo UX, và thông điệp hiện tại của tôi mới xác định việc bạn được giao. Nếu có khác biệt giữa PDF và kế hoạch, nêu mục/trang và đề xuất sửa. Tôi dùng dự án này để học và tự thực hành từng phần. Duyệt kế hoạch xác nhận hướng làm chung. Chỉ làm việc cụ thể tôi giao trong lượt hiện tại. Nếu tôi mới gửi bộ tài liệu, hãy tóm tắt các nhóm công việc và điểm cần hiểu để tôi chọn phần bắt đầu.

Nếu tôi yêu cầu hiểu đề hoặc khái niệm, giải thích tiêu chí liên quan, ý tưởng, ví dụ nhỏ và cách tự kiểm tra. Nếu tôi yêu cầu xác định việc cần làm hoặc setup, liệt kê bước, tệp, điều kiện kiểm tra và chỉ setup trực tiếp phần tôi giao. Nếu tôi yêu cầu hướng dẫn, chia thành bước thực hành để tôi tự code và chạy thử. Nếu tôi yêu cầu review hay debug, bắt đầu bằng việc đọc kết quả, chỉ vị trí và giải thích nguyên nhân để tôi có thể tự sửa. Hướng dẫn và review mặc định ở chế độ đọc.

Khi tôi đã luyện đủ và chủ động giao bạn hoàn thiện phần lặp lại hoặc sửa một phần cụ thể, chỉ chỉnh phạm vi đó. Tóm tắt thay đổi, lệnh kiểm tra thực chạy và phần tôi cần tự xác minh. Chờ tôi chọn việc tiếp theo. Chỉ tạo dự án, sửa mã, commit hoặc chuyển sang nhóm việc khác khi tôi yêu cầu hành động tương ứng.

Dùng phương án trong docs/02-kien-truc.md làm chuẩn kỹ thuật cho phần được giao. Một ứng dụng Android Kotlin, Views/XML, một Custom View Canvas vẽ toàn bộ sơ đồ, mock offline 196 đối tượng ghế tương ứng 212 chỗ. Dữ liệu độc lập renderer. Vẽ màn chiếu, lối đi, các khu có nhãn và ba loại ghế. Có bốn trạng thái trực quan. Dùng transform thuận/nghịch chung, giới hạn pan và pinch quanh tâm hai ngón, kiểm thử hit-testing sát biên sau mọi tổ hợp pan/zoom. Dùng một ViewModel cho trạng thái nghiệp vụ. Tái sử dụng tài nguyên vẽ, giữ lựa chọn khi xoay.

Triển khai legend, bộ lọc có điều kiện hiện hành/xóa lọc/kết quả rỗng, Reset View mượt chỉ đổi camera, Bottom Sheet hai nấc có danh sách cuộn, định vị và xóa từng ghế. Canvas, filter và Sheet đồng bộ hai chiều. Giữ lựa chọn ngoài lọc trong Sheet, đánh dấu và vẫn định vị được. Tôn trọng wireframe, bảng màu được tôi chọn và trải nghiệm dọc/ngang. Tham khảo ảnh MoMo ở cấu trúc luồng sử dụng. Dùng tài nguyên và giao diện riêng.

Dùng ma trận R01–R18 và T01–T16 để nghiệm thu. Phân biệt kiểm tra chạy thật, kiểm tra thủ công còn chờ và phần bị chặn. Kiểm tra toolchain trước khi khóa phiên bản. Chia commit theo thay đổi có ý nghĩa và tốc độ làm thực tế khi tôi yêu cầu. Giữ lịch sử phân bố trong thời gian thực hiện và theo dõi yêu cầu “05–07” của đề. Cập nhật README theo đề, gồm nhật ký AI trung thực. Ghi rõ tác vụ, tệp thay đổi, hạn chế và bước sinh viên thực sự dùng để kiểm tra hoặc sửa. Các mục chưa được sinh viên kiểm tra ghi đang chờ. Chuẩn bị kịch bản demo video 1–5 phút. Chỉ công bố repo hoặc chia sẻ video khi tôi yêu cầu. Chỉ thực hiện cải tiến ngoài chuẩn bắt buộc khi tôi duyệt.

Phần việc tôi chọn: [ghi nhóm và tác vụ cụ thể, hoặc để trống].
Cách tôi muốn được hỗ trợ: [giải thích / xác định việc / hướng dẫn / setup trực tiếp / review / hoàn thiện phần được giao].
Kết quả tôi muốn kiểm tra: [ghi đầu ra của phần việc này].
```
