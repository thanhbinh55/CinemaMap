# Nghiệm thu và chuẩn bị nộp bài

Đây là checklist của bản hiện tại, không phải cam kết điểm số. Phân biệt triển khai, kiểm thử tự động, dùng thử của sinh viên và hồ sơ nộp. [Kết quả chạy/lệnh/minh chứng](09-kiem-chung.md) là nguồn trạng thái kiểm chứng; ảnh/log cũ giữ làm lịch sử.

## Checklist còn lại

- [x] Sơ đồ 196 đối tượng trên một Canvas, màn chiếu/lối đi/nhãn khu, ba loại ghế và bốn trạng thái.
- [x] Pan có biên, pinch có điểm neo và min/max; chuyển đổi tọa độ và kiểm tra mép ghế.
- [x] Chọn/bỏ chọn, giá theo đối tượng, chặn chọn ghế đã đặt/không khả dụng.
- [x] Legend, lọc AND, điều kiện hiện hành/xóa/rỗng; giữ lựa chọn ngoài lọc.
- [x] Sheet thu/mở, danh sách cuộn, định vị/xóa và đồng bộ hai chiều.
- [x] Sửa nhãn Sheet sau phục hồi; lưu camera theo zoomRatio/tâm world; SavedStateHandle cho ID/lọc/ghế xem.
- [x] Phần mở rộng 3D và video procedural có mã nguồn tài nguyên, vòng đời và kiểm thử.
- [x] README trình bày trực tiếp dữ liệu/kiến trúc/tọa độ/hit-test/state/hiệu năng; giữ nhật ký AI.
- [ ] Sinh viên tự dùng thử bản APK mới trên Samsung và xác nhận hiểu phần code nộp; không thay bằng kết quả Agent.
- [ ] **Sinh viên quay video demo 1–5 phút sau**, thêm URL vào README và kiểm tra quyền truy cập.
- [ ] Kiểm tra quyền truy cập repo/video bằng tài khoản người chấm hoặc phiên độc lập trước nộp.

Build/test/lint, kiểm tra nguồn sạch và phép đo hiệu năng có kết quả chi tiết ở tài liệu 09; không suy diễn là đã chạy mọi ca thủ công T01–T16 trên mọi thiết bị. Kiểm thử handle mới không đồng nghĩa force-stop phải giữ phiên. Xoay/recreate là một phép thử khác hủy tiến trình thật.

## Ca nghiệm thu và tiêu chí tái hiện

| Ca | Yêu cầu | Thực hiện và kết quả kỳ vọng | Minh chứng dự kiến |
| --- | --- | --- | --- |
| T01 | R01, R13 | Start dọc và ngang, fit đủ 196 ghế/212 chỗ, 72 Thường/108 VIP/16 Đôi, ID duy nhất, màn chiếu và lối đi rõ, một Custom View cho toàn sơ đồ. Kiểm tra màn nhỏ và hai nấc Sheet. | Unit test fixture, Layout Inspector, ảnh dọc/ngang |
| T02 | R02, R05, R09, R12 | Chọn A05, E08, K03. Sheet hiện 3 ghế, 4 chỗ, 385.000 đ. Bỏ E08 còn 2 ghế, 3 chỗ, 275.000 đ; E08 về Trống, các mục khác giữ nguyên. Chạm A03 Đã đặt và B01 Không khả dụng, chỉ hiện chi tiết. | Test ViewModel và video |
| T03 | R03, R13 | Ở mức 3× fit, kéo qua bốn biên và góc, viewport không mất toàn sơ đồ. Reset về fit căn giữa; kéo ở fit được lệch trong khoảng đệm 15% viewport. Thử kéo rồi thả và kiểm tra không sinh lựa chọn. | Unit test giới hạn và demo |
| T04 | R04, R13 | Pinch ở vị trí lệch tâm theo nhiều hướng, gồm dịch tâm hai ngón, theo dõi đúng điểm neo ở trường hợp không chạm giới hạn. Kiểm tra min/max, nhấc một ngón rồi kéo tiếp, hoàn tất mà không phát tap. | Test phép toán và thao tác hai ngón |
| T05 | R05, R13 | Round-trip một số điểm world qua transform. Kiểm tra tap chính giữa và sát trong/ngoài từng cạnh, ở khe ghế, lối giữa và trên ghế đôi ở mặc định, sau pan, sau zoom, sau kết hợp pan+zoom, sau xoay. Vùng dưới mép Sheet không nhận tap bản đồ. | Unit test tham số hóa và kiểm tra trên thiết bị |
| T06 | R08, R12 | Đang zoom/pan và giữ lựa chọn + bộ lọc, bấm Toàn phòng. Camera về fit mượt, lựa chọn, giá, bộ lọc và nấc Sheet giữ nguyên. Chạm hoặc kéo khi đang reset để xác nhận camera tiếp nhận điều khiển ổn định. | Video và kiểm thử UI |
| T07 | R06 | Mở Chú thích từ cả hai hướng. Kiểm đủ loại, bốn trạng thái, lối/khu, ghế đôi và nghĩa ghế mờ. Đóng chú thích và trở lại đúng góc nhìn cùng lựa chọn. Thử cỡ chữ lớn. | Ảnh và checklist thao tác |
| T08 | R07, R12 | Phiên mới lọc Đôi + Đang chọn để thấy 0 và nút xóa. Xóa và kiểm thấy đủ 196 ghế. Chọn A05 rồi lọc VIP, ghế mờ trên map và vẫn nằm trong Sheet có nhãn ngoài lọc. Chọn một ghế VIP trong lọc Trống rồi xác minh trạng thái và độ mờ ghế cập nhật ngay. | Test ViewModel và video |
| T09 | R09, R10 | Thu/mở Sheet bằng kéo và nút. Chọn đủ ghế để danh sách cuộn, thao tác được trên Canvas khi mở ở dọc/ngang. Kiểm tra tương tác giữa cuộn danh sách và kéo Sheet. | Kiểm tra UI cả hai hướng |
| T10 | R11, R12 | Từ Sheet định vị A05 khi đang lọc VIP, thấy vòng định vị, xóa A05 thì tạm tính giảm ngay. Xóa hai lần cùng ID không ảnh hưởng ghế khác. Xóa lọc rồi thấy A05 về Trống. Tap/bỏ tap luân phiên trên Canvas và kiểm tra không có ID trùng trong Sheet. | Test ViewModel và video |
| T11 | R12, R14 | Khi đã zoom, lọc, chọn và mở Sheet, xoay dọc → ngang → dọc. Giữ ghế/giá/lọc/nấc Sheet, không trùng hoặc trống dữ liệu cũ. Camera giữ mức zoom tương đối và tâm trong giới hạn mới. Tap tiếp đúng vào ghế. | Xoay trên thiết bị và test dựng lại Activity |
| T12 | R14 | Kiểm tra khi rời app/quay lại và dựng lại Activity, system bars không che nút, chữ lớn vẫn thao tác được. Kiểm tra phục hồi bằng SavedStateHandle trong phạm vi dự kiến khi hệ thống dựng lại tiến trình. | Ghi môi trường và kết quả thao tác |
| T13 | R15 | Soát `onDraw()` tái sử dụng đồ họa, không có vòng invalidate tự chạy khi đứng yên. Trace pan/pinch/tap ở 196 và 250 ghế, ghi thiết bị, build, refresh rate, frame time/jank và nhận xét. Mục tiêu nội bộ 16,7 ms mỗi frame trên máy 60 Hz khi đo render, điều tra nếu giật rõ rệt. | Mã nguồn và Android Studio System Trace hoặc Perfetto |
| T14 | R17 | Clone sạch, xác minh Gradle Wrapper/JDK/SDK, test/lint/build/cài/chạy theo README, soát tệp sinh tự động, số lượng commit theo mục 5.2, ý nghĩa và phân bố commit. | Log lệnh, commit và cấu hình thiết bị |
| T15 | R16, R18 | Đối chiếu tất cả mục README, nhật ký AI có đóng góp thực tế, lỗi/hạn chế và kết quả kiểm tra/sửa của sinh viên. Sinh viên giải thích được camera, hit-testing, lọc và xoay. | README và tự duyệt |
| T16 | R18 | Video 1–5 phút có đủ thao tác yêu cầu; thử link repository và video bằng quyền người chấm hoặc phiên độc lập. | Link và kết quả kiểm tra quyền |

Các mô tả T01–T16 là tiêu chí thực hiện/tái hiện. Bằng chứng và phạm vi đã chạy xem tài liệu 09. Thử 250 đối tượng và mục tiêu 16,7 ms ở 60 Hz là mục tiêu nội bộ, không phải một yêu cầu số FPS hay quy mô dữ liệu khác bắt buộc của đề; bản nộp dùng 196 đối tượng.

## Video demo do sinh viên thực hiện sau

Sinh viên chủ động thực hiện sau theo yêu cầu ngày 27/11/2026. Chưa có video/URL, không đánh dấu hồ sơ hoàn tất. Kịch bản tham khảo dài 3 phút 20 giây:

| Thời gian | Thao tác cần thấy |
| --- | --- |
| 0:00–0:20 | Mở app, chỉ màn chiếu, lối giữa, ba khu, 196 đối tượng và legend |
| 0:20–0:55 | Pan một ngón tới biên, pinch quanh tâm lệch giữa, chạm cạnh trong ghế sau pan/zoom, chạm khe giữa để so sánh |
| 0:55–1:25 | Chọn và bỏ chọn ghế thường, VIP và đôi, cho thấy số chỗ/tạm tính đổi; chạm ghế đã đặt để xem chi tiết |
| 1:25–1:55 | Mở Sheet, cuộn danh sách nhiều mục, định vị và xóa một ghế, chỉ Canvas và tạm tính cập nhật |
| 1:55–2:25 | Lọc VIP, chỉ điều kiện, ghế mờ và lựa chọn ngoài lọc. Bấm Toàn phòng để thấy camera fit trong lúc bộ lọc và lựa chọn còn nguyên |
| 2:25–3:00 | Xoay dọc → ngang → dọc khi còn nhóm ghế chọn và bộ lọc, thao tác thêm trong hướng ngang |
| 3:00–3:20 | Xóa các ghế đôi đang chọn nếu có, rồi lọc Đôi + Đang chọn để ra tổ hợp rỗng. Hiện thông báo rỗng, xóa lọc và thấy lại toàn phòng |

Khi quay, chuẩn bị trước các ghế mẫu và tổ hợp lọc theo fixture mặc định. Kết thúc sau khi kiểm tra video đúng thời lượng và liên kết có thể truy cập.

## Lịch sử Git và cách chốt thay đổi

Commit theo phần có ý nghĩa và nhịp làm thật; không bắt buộc một commit mỗi ngày, không tạo lùi ngày hoặc dàn dựng lịch sử. Repo đã có các commit từ 23, 24 và 27/11/2026; đối chiếu lại `git log --date=iso` trước nộp. Khi chốt phải đưa cả source/resource/test mới vào Git, không chỉ file đã được track. Không nộp `.gradle`, `build`, SDK path, khóa ký hay dữ liệu cá nhân. MP4 raw là tài nguyên runtime cần thiết; script procedural được giữ để kiểm chứng nguồn.

## Học và tự giải thích trước khi nộp

Sinh viên cần giải thích được SeatRow/SeatPosition, world/pixel, công thức thuận/nghịch, điểm neo pinch, pointerId sau nhấc ngón, giới hạn pan, nguồn state thống nhất, SavedStateHandle/View SavedState, và callback Canvas ↔ Sheet. Với 3D, hiểu ánh xạ ghế sang vị trí mắt, ma trận view/projection và MediaPlayer → SurfaceTexture. Các bước Agent làm được ghi trong README; phần sinh viên chưa trực tiếp kiểm tra vẫn ghi chưa xác nhận.
