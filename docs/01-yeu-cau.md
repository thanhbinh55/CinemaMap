# Đặc tả và đối chiếu yêu cầu

## Cơ sở và phạm vi

Tài liệu đối chiếu đề `De_thi_giua_ky_CNTN2024.pdf`, gồm 5 trang. Dự án chọn đề tài 2: sơ đồ phòng chiếu rạp phim. Các mã R01–R18 dùng để truy vết yêu cầu với phần hiện thực và kiểm chứng.

Mục 2.1 và danh mục kiểm tra của đề quy định 50–250 đối tượng; bảng tiêu chí chấm mục 6 quy định 150–250. Dữ liệu CinemaMap gồm **196 đối tượng ghế**, đáp ứng cả hai khoảng. Ghế đôi là một đối tượng có hai chỗ, nên tổng sức chứa là 212 chỗ. Màn chiếu, lối đi và nhãn là thành phần định hướng, không được cộng vào số ghế.

## Ma trận yêu cầu

| Mã | Nguồn trong đề | Yêu cầu | Hiện thực và minh chứng |
| --- | --- | --- | --- |
| R01 | 1, 2.1; trang 1 | Sơ đồ có phân khu, lối đi, nhãn và nhận biết tổng thể ở mức mặc định | Một CinemaMapView vẽ khu Thường/VIP/Đôi, hàng A–L, màn chiếu và hai khối ghế. MockCinemaData, MapGeometryTest, ảnh màn hình chính |
| R02 | 2.1; trang 1–2 | Ít nhất ba trạng thái; phản hồi chọn và bỏ chọn chính xác | Bốn trạng thái Trống/Đang chọn/Đã đặt/Không khả dụng; BookingViewModelTest và kiểm tra điểm ảnh trong MapInteractionTest |
| R03 | 2.1; trang 1 | Pan một ngón theo các hướng, có giới hạn | Camera giới hạn theo viewport với đệm 15%; MapNavigationTest và MapInteractionTest |
| R04 | 2.1; trang 1 | Pinch liên tục quanh tâm hai ngón, có min/max | ScaleGestureDetector, điểm neo world, tỷ lệ 0,8–5 lần fit; MapInteractionTest |
| R05 | 2.1, 3.2; trang 1–2 | Hit-testing đúng sau pan/zoom và sát biên | viewToWorld và kiểm tra hình chữ nhật nửa mở; MapGeometryTest kiểm tra bốn cạnh qua nhiều camera |
| R06 | 2.2; trang 2 | Chú thích đầy đủ, dễ truy cập, ít che sơ đồ | Dải chú thích cuộn ngang và dialog Chi tiết; BookingSheetTest |
| R07 | 2.2; trang 2 | Lọc hoặc tìm kiếm, thể hiện điều kiện, xóa lọc và thông báo rỗng | Lọc AND theo loại/trạng thái, ghế lệch điều kiện làm mờ, giữ lựa chọn trong Sheet; SeatFilterTest, BookingSheetTest và video |
| R08 | 2.2; trang 2 | Reset View mượt về mặc định, giữ lựa chọn và lọc | ValueAnimator 220 ms, chỉ thay camera; BookingSheetTest và video |
| R09 | 2.3; trang 2 | Bottom Sheet thu gọn có thông tin tóm tắt | Số ghế, số chỗ, tạm tính và ghế đang xem; BookingSheetTest |
| R10 | 2.3; trang 2 | Bottom Sheet mở rộng có nội dung cuộn và còn vùng tương tác | RecyclerView và bố cục dọc/ngang riêng; BookingSheetTest |
| R11 | 2.3; trang 2 | Chọn mục để định vị/làm nổi và xóa từng mục | inspectSeat, locateSeat, removeSeat; BookingSheetTest và video |
| R12 | 2.3, 3.3; trang 2–3 | Canvas–lọc–Sheet đồng bộ; không trùng hoặc sai thông tin sau lọc/reset/xoay | BookingUiState/StateFlow; BookingViewModelTest, BookingSheetTest, MapInteractionTest |
| R13 | 3.1–3.2; trang 2 | Một Canvas cho sơ đồ; không dùng từng View thay ghế; dữ liệu độc lập | MockCinemaData → MapGeometry → CinemaMapView; đọc mã nguồn và kiểm thử tọa độ |
| R14 | 3.3; trang 3 | Phân tách trách nhiệm, giữ lựa chọn và hoạt động ở hai hướng | ViewModel, SavedStateHandle, SavedState của View, layout-land; ca xoay trong MapInteractionTest |
| R15 | 3.4; trang 3 | Tái sử dụng tài nguyên, vẽ khi cần và thao tác mượt | Paint/Path/hình học/thông số chữ được tái sử dụng; có culling. Số đo và giới hạn độ mượt tại báo cáo 09 |
| R16 | 4, 5.5; trang 3–4 | Ghi công cụ AI, đóng góp, hạn chế và cách kiểm tra/điều chỉnh | Nhật ký trong README; tài liệu 06 mô tả phạm vi hỗ trợ và hoạt động tự thực hành |
| R17 | 5.1–5.2; trang 3 | Dự án build/chạy được; lịch sử tối thiểu 05–07 commit có ý nghĩa | Log build, kiểm thử thiết bị, build nguồn sạch và lịch sử Git phân bố 23–28/11/2026 |
| R18 | 5.3–5.4, 8; trang 3–5 | Video 1–5 phút, README đầy đủ và liên kết truy cập được | Video 4:49; README có liên kết trực tiếp và thư mục Drive; metadata/quyền truy cập được kiểm tra |

Phạm vi và kết quả cụ thể của từng minh chứng được ghi trong [báo cáo kiểm chứng](09-kiem-chung.md). Ma trận mô tả quan hệ yêu cầu–hiện thực, không gán điểm số cho sản phẩm.

## Tiêu chí chấm

| Hạng mục | Tỷ trọng |
| --- | ---: |
| Canvas, bố cục và hiệu năng | 25% |
| Tương tác và xử lý tọa độ | 25% |
| Chức năng và trải nghiệm người dùng | 20% |
| Kiến trúc và quản lý trạng thái | 15% |
| Chất lượng mã nguồn và độ ổn định | 10% |
| Hồ sơ và quy trình nộp bài | 5% |

Theo mục 7 của đề, chức năng không chạy hoặc không thể kiểm tra không được tính điểm. Sử dụng Canvas không đúng yêu cầu giới hạn hạng mục tương ứng ở 5/25 điểm; hit-testing sai sau pan/zoom không được tính điểm phần hit-testing. Nếu ứng dụng không biên dịch hoặc khởi chạy được, tổng điểm tối đa là 5,0/10,0. Các phần mở rộng không thay thế yêu cầu bắt buộc.

## Phạm vi đánh giá

Kiểm thử chức năng, kiểm tra cấu trúc mã và phép đo hiệu năng là các loại bằng chứng riêng. Kết quả kiểm thử không đủ để suy ra độ mượt trên mọi thiết bị. Yêu cầu sinh viên hiểu và chịu trách nhiệm về mã nguồn thuộc quy định sử dụng AI của đề; khả năng giải trình được giảng viên đánh giá trực tiếp.
