# Đối chiếu đề thi và phương án rạp phim

Nguồn là đề thi PDF 5 trang. Các số mục bên dưới trỏ tới đề gốc. Mã R dùng để liên kết đặc tả, kiểm thử và minh chứng. Tiến độ kiểm chứng được ghi trong tài liệu nghiệm thu.

## Phạm vi và cách hiểu các ngưỡng

- Mục 1 yêu cầu ứng dụng di động có màn hình chính là sơ đồ tự vẽ trên Canvas. Sinh viên làm cá nhân trong một tuần, chọn một trong năm chủ đề. Chủ đề số 2 có màn chiếu, lối đi, ghế thường, VIP, ghế đôi và trạng thái đặt vé.
- Mục 2.1 và checklist mục 8 ghi **50–250 đối tượng**. Rubric mục 6 dùng **150–250** cho hạng mục Canvas. Kế hoạch chọn **196 đối tượng ghế nhìn thấy ở góc mặc định**, trong đó có 16 ghế đôi. Màn chiếu, lối đi và nhãn khu vực là hình vẽ định hướng, nằm ngoài số đếm ghế.
- Đề dùng dữ liệu giả lập và cho phép thực hiện bằng Android Custom View/Canvas hoặc Flutter CustomPainter. Thiết kế đề xuất một suất chiếu offline, trạng thái đặt sẵn trong fixture và nhật ký kiểm tra trong README.
- Mục 5.2 và mục 8 diễn đạt số commit là “05–07”. Lịch sử có các commit có ý nghĩa, phân bố trong thời gian thực hiện. Kiểm tra mốc **05–07** theo đề khi chuẩn bị nộp. Chọn thời điểm commit và cách chia thay đổi theo tiến độ thật.

## Ma trận truy vết

| Mã | Đề | Yêu cầu | Phương án và điều kiện nghiệm thu |
| --- | --- | --- | --- |
| R01 | 1, 2.1, 6, 8 · tr. 1, 4–5 | Phòng chiếu có hình học rõ, đối tượng theo khu, có nhãn, đọc tổng thể ở zoom mặc định | A–L chia Thường/VIP/Đôi, màn chiếu ở đầu phòng, hai khối trái/phải, lối đi giữa và khoảng cách giữa các khu. Mở app thấy toàn phòng và đủ 196 ghế ở cả hai hướng. |
| R02 | 2.1 · tr. 1–2 | Ít nhất ba trạng thái trực quan, phản hồi rõ khi chọn, bỏ chọn trả đúng trạng thái trước | Bốn trạng thái Trống, Đang chọn, Đã đặt, Không khả dụng. Chọn ghế trống cho dấu ✓ và viền. Bỏ chọn trả về Trống, các ghế khác giữ nguyên. Loại ghế và trạng thái có mã hóa riêng. |
| R03 | 2.1 · tr. 1 | Pan một ngón theo các hướng và giới hạn phạm vi | Kéo theo hai trục, chặn ở biên, không đẩy toàn bộ bản đồ ra khỏi viewport. Trục có nội dung nhỏ hơn viewport được căn giữa. |
| R04 | 2.1 · tr. 1 | Pinch liên tục quanh tâm hai ngón, giới hạn min/max | Dùng focus hai ngón và phép đổi tọa độ, tỷ lệ tương đối từ 0,8× đến 5× mức vừa phòng, mặc định 1×. Giữ điểm neo khi biên cho phép. |
| R05 | 2.1, 3.2, 7 · tr. 1–2, 4 | Tap chính xác để chọn/bỏ chọn/xem thông tin, kể cả sát biên sau pan/zoom | Đảo phép biến đổi dùng khi vẽ, hit vùng ghế tại tọa độ world. Chạm mép trong chọn đúng ghế, mép ngoài và lối đi bỏ qua. Ghế đã đặt/không khả dụng cho xem chi tiết, không thêm vào danh sách. |
| R06 | 2.2 · tr. 2 | Legend đầy đủ, dễ mở, ít che sơ đồ | Dải gọn ngay cạnh toolbar/sơ đồ và mục Chú thích xem đủ loại, trạng thái, nhãn khu và ý nghĩa lối đi, ghế đôi, lọc mờ. |
| R07 | 2.2 · tr. 2 | Lọc hoặc tìm kiếm, có điều kiện đang dùng, xóa được, có trạng thái rỗng, lựa chọn nhất quán | Lọc theo loại và trạng thái với nhãn đang áp dụng và đếm kết quả. Ghế lệch điều kiện làm mờ, có thông báo khi 0 kết quả và thao tác xóa lọc. Lựa chọn đang có giữ trong sheet và ghi rõ khi nằm ngoài lọc. |
| R08 | 2.2 · tr. 2 | Reset View mượt về vị trí và tỷ lệ mặc định, giữ bộ lọc/lựa chọn theo quy tắc | Nút Toàn phòng làm hoạt ảnh về camera fit. Bộ lọc, ghế đã chọn và trạng thái sheet được giữ. |
| R09 | 2.3 · tr. 2 | Bottom Sheet thu gọn hiển thị tóm tắt | Hiện thông tin suất giả lập, mã hoặc số ghế đối tượng, số chỗ và tạm tính, kèm thông tin ghế vừa chạm khi cần. |
| R10 | 2.3 · tr. 2 | Bottom Sheet mở rộng hiển thị chi tiết/danh sách, cuộn được, còn vùng tương tác trên sơ đồ | Sheet hai nấc dạng thường trực, có danh sách cuộn và khu vực Canvas tương tác được ở cả dọc/ngang. |
| R11 | 2.3 · tr. 2 | Định vị hoặc làm nổi đối tượng tương ứng, xóa mục khi phù hợp | Chạm dòng ghế để định vị và đánh dấu; nút Xóa của từng dòng cập nhật ghế, số chỗ và giá. Ca đang lọc vẫn định vị được. |
| R12 | 2.3, 3.3 · tr. 2–3 | Đồng bộ hai chiều, đúng sau lọc/reset/xoay, không trùng và không còn dữ liệu lỗi thời | Canvas, bộ lọc và Sheet cùng đọc trạng thái và gửi sự kiện qua ViewModel. Danh sách dựa trên ID duy nhất, chi tiết được dẫn xuất từ trạng thái hiện tại. |
| R13 | 3.1–3.2 · tr. 2 | Canvas duy nhất tự vẽ toàn sơ đồ, không tạo từng View/Widget thay ghế, dữ liệu độc lập lớp vẽ | Một `CinemaMapView`, dữ liệu fixture và hình học tách khỏi renderer. Kiểm tra cây View và đối chiếu transform thuận/nghịch trong mã. |
| R14 | 3.3, 8 · tr. 3–5 | Phân tầng, giữ lựa chọn khi xoay, giao diện ổn định hai hướng | ViewModel giữ trạng thái logic, camera lưu dạng chuẩn hóa để tính lại khi đổi kích thước. Kiểm tra lần lượt dọc → ngang → dọc với filter, sheet và selection đã có. |
| R15 | 3.4, 6 · tr. 3–4 | Tái sử dụng tài nguyên, vẽ khi cần, thao tác mượt | Cache Paint/Path/hình học/nhãn; invalidate theo thay đổi, kiểm tra trace và thao tác ở 196 ghế. Thử fixture 250 ghế để kiểm tra biên hiệu năng. |
| R16 | 4, 5.4–5.5 · tr. 3–4 | Trách nhiệm cá nhân, kiểm tra đầu ra AI và ghi nhật ký trong README | Ghi agent, việc giao, phần mã, hạn chế và cách sinh viên kiểm tra/sửa. Sinh viên có thể giải thích thuật toán và quyết định triển khai. |
| R17 | 5.1–5.2, 6–8 · tr. 3–5 | GitHub repo truy cập được, mã build/run, cấu trúc rõ, lịch sử Git thực | Kiểm tra từ bản clone sạch. Đối chiếu số lượng theo mục 5.2, kiểm tra commit phân bố theo tiến độ thật, thông điệp rõ và tệp theo dõi cần thiết. |
| R18 | 5.3–5.4, 8 · tr. 3–5 | Video 1–5 phút, README đầy đủ, link repo và video truy cập được | Quay đủ thao tác theo kịch bản, README có đủ mục đề yêu cầu, thử quyền truy cập bằng phiên độc lập với tài khoản chủ sở hữu. |

## Rubric và điều kiện giới hạn điểm

| Hạng mục | Trọng số | Bằng chứng |
| --- | ---: | --- |
| Canvas, bố cục và hiệu năng | 25% | R01, R02, R13, R15. Góc nhìn toàn phòng, phép đếm fixture, cây View và trace thao tác |
| Tương tác và xử lý tọa độ | 25% | R03–R05, R13. Demo pinch theo tâm, pan tới biên và test chạm ghế sát mép sau transform |
| Chức năng và UX | 20% | R06–R12. Demo từ bộ lọc và Canvas đến Bottom Sheet rồi thao tác ngược lại |
| Kiến trúc và quản lý trạng thái | 15% | R12–R14. Mã nguồn, sơ đồ phân tầng và xoay thực tế |
| Chất lượng mã và độ ổn định | 10% | R02–R05, R07–R15, R17. Test biên, vòng đời và build từ clone sạch |
| Hồ sơ và quy trình nộp | 5% | R16–R18. Git, README, nhật ký và video truy cập được |

Theo mục 7, tính năng cần chạy và kiểm chứng được để tính điểm. Dùng Canvas sai yêu cầu giới hạn hạng mục tương ứng ở 5/25 điểm theo cách ghi của đề. Hit-testing sai sau pan hoặc zoom sẽ mất điểm phần hit-testing. Ứng dụng không build hoặc khởi chạy được có tổng điểm tối đa 5,0/10,0 và giảng viên chỉ chấm phần xác minh được. Tính năng bổ sung chỉ triển khai sau khi hoàn thành phạm vi bắt buộc. Vi phạm học thuật xử lý theo quy định của học phần và nhà trường.
