# Thiết kế chức năng xem trước góc nhìn 3D

## Mục tiêu và phạm vi

Chức năng mở rộng cho phép quan sát màn chiếu từ một vị trí ghế trong phòng mô phỏng. Dữ liệu ID và phân bố ngang được lấy từ sơ đồ 2D. Hoạt động xem trước độc lập với chọn vé: đổi ghế xem không làm thay đổi danh sách lựa chọn, bộ lọc hoặc tổng tiền.

## Thành phần

| Thành phần | Trách nhiệm |
| --- | --- |
| CinemaPreviewActivity | Giao diện điều khiển, trạng thái ghế xem, cấu hình, A/B và vòng đời |
| CinemaRoomSpec / CinemaRoomSettings | Kích thước phòng mô phỏng và lưu cấu hình |
| CinemaSceneGeometry | Ánh xạ ghế sang tọa độ 3D và vị trí mắt |
| SeatCameraState / SeatComparison | Hướng nhìn, bên ghế đôi và hai vị trí so sánh |
| CinemaSceneMesh | Buffer tam giác của phòng, ghế và hiệu ứng bóng tiếp xúc |
| CinemaSceneRenderer | Shader, ma trận camera/phối cảnh, thứ tự vẽ |
| CinemaSceneView | GLSurfaceView, thao tác kéo và yêu cầu render |
| CinemaMiniMapView | Vị trí ghế và hướng nhìn trên sơ đồ nhỏ |
| CinemaTrailerPlayer / CinemaVideoScreen | Decoder và texture video trên màn chiếu |

## Hình học và camera

Mặc định phòng rộng 19,12 m; màn chiếu rộng 12,6 m, tỷ lệ 21:9; hàng đầu cách màn chiếu 4 m; khoảng hàng 1,12 m; độ nâng mỗi hàng 0,18 m; chiều cao mắt so với sàn hàng 1,15 m. Đây là các tham số mô phỏng, không phải số đo rạp thực tế.

Tọa độ ngang được quy đổi từ tâm ghế 2D; độ sâu và cao độ phụ thuộc hàng. Mỗi ID ánh xạ tới một ghế 3D. Ghế đơn đặt mắt tại tâm; ghế đôi đặt mắt lệch trái hoặc phải một phần tư chiều rộng cả cặp. Ghế đôi chỉ có hai lựa chọn Trái/Phải, mặc định Trái.

Hướng mặc định trỏ từ mắt đến tâm màn chiếu. Kéo một ngón thay đổi yaw/pitch, với góc ngang được chuẩn hóa và góc dọc được giới hạn. Nút Nhìn màn chiếu đặt lại hướng của ghế hiện tại. Camera sử dụng ma trận view và projection, với FOV ngang 75 độ.

## Luồng tương tác

1. Nút 3D trên một dòng danh sách mở đúng ghế của dòng đó; nút chung mở ghế đang được xem thông tin.
2. Người dùng đổi hàng/ghế bằng danh sách hoặc mở sơ đồ từ minimap. Các ghế đã đặt/không khả dụng vẫn có thể được xem góc nhìn mà không được chọn vé.
3. A/B lưu hai camera để so sánh. Chuyển A/B đưa hướng về màn chiếu và tạm dừng video; giữ nút để thay vị trí ghim.
4. Toàn màn hình ẩn bảng điều khiển và mở rộng vùng cảnh. Back thoát toàn màn hình trước khi rời màn hình xem trước.
5. Cấu hình phòng điều chỉnh kích thước và hiệu ứng ánh sáng/bóng mô phỏng. Cấu hình được lưu cục bộ.
6. Khi quay về 2D, Activity trả ID ghế đang xem để cập nhật thông tin và định vị trên Canvas. Lựa chọn và tạm tính được giữ.

## Renderer và vòng đời

Renderer sử dụng OpenGL ES 2.0, buffer tam giác và màu theo đỉnh. Depth buffer xác định quan hệ che khuất. Phòng và ghế là mô hình hình học; bóng tiếp xúc và sắc sáng là hiệu ứng nhẹ, không phải mô phỏng chiếu sáng vật lý.

Buffer/ma trận được tái sử dụng. Buffer ghế được cập nhật khi đổi ID xem; kéo hướng nhìn chủ yếu cập nhật camera. `RENDERMODE_WHEN_DIRTY` nhận yêu cầu vẽ từ thay đổi trạng thái hoặc frame video mới. Các thay đổi gửi qua `queueEvent()` được thực hiện trên luồng GL, tách khỏi luồng giao diện.

Khi Activity ra nền, player lưu mốc phát và giải phóng decoder. Khi trở lại, Surface hợp lệ được công bố từ GL, decoder được tạo lại và seek về vị trí lưu. Camera và trạng thái phát được lưu qua dựng lại Activity. Chi tiết phát video nằm trong [tài liệu tài nguyên](08-trailer.md).

## Kiểm chứng và giới hạn

`CinemaSceneGeometryTest` kiểm tra ánh xạ, hướng camera, ghế đôi, cấu hình và A/B. `CinemaPreviewTest` và `CinemaPreviewUpgradeTest` kiểm tra thao tác, hình ảnh thật, xoay, quay về, minimap, so sánh và toàn màn hình. Báo cáo kết quả mới nhất, bao gồm ca chưa đạt nếu có, được ghi tại [tài liệu 09](09-kiem-chung.md).

Cảnh 3D chưa được hiệu chuẩn với rạp thật; không mô phỏng người ngồi hoặc ánh sáng động từ nội dung phim. Số đo hiệu năng 2D không được suy ra thành hiệu năng 3D. Trạng thái sẵn sàng của renderer, của decoder và khung hình đã hiện trên màn hình là các mốc khác nhau trong kiểm thử bất đồng bộ.

## Tham khảo API

- [GLSurfaceView](https://developer.android.com/reference/android/opengl/GLSurfaceView).
- [OpenGL ES drawing](https://developer.android.com/develop/ui/views/graphics/opengl/draw).
- [Matrix](https://developer.android.com/reference/android/opengl/Matrix).
- [Activity Result API](https://developer.android.com/training/basics/intents/result).
