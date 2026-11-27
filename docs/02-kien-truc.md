# Kiến trúc và xử lý tọa độ

## Cấu trúc

Dự án gồm module Android `app`, package `vn.edu.midterm.cinemamap`. Giao diện sử dụng Views/XML và Material Components. Phần 3D sử dụng GLSurfaceView/OpenGL ES 2.0. Cấu hình toolchain và hướng dẫn build được trình bày trong [README](../README.md#hướng-dẫn-chạy).

| Thành phần | Trách nhiệm |
| --- | --- |
| model/Seat.kt | Ghế, hàng ghế, loại, sức chứa, giá và trạng thái gốc |
| data/MockCinemaData.kt | Tập dữ liệu giả lập cố định, ID duy nhất |
| map/MapGeometry.kt | Hình học hàng/ghế, kích thước world và hit-testing |
| map/CameraTransform.kt | Biến đổi thuận/nghịch giữa world và View |
| map/CinemaMapView.kt | Vẽ sơ đồ, gesture, giới hạn camera, reset, định vị và minimap |
| ui/BookingViewModel.kt | Lựa chọn, lọc, xem thông tin, tổng và phục hồi trạng thái |
| ui/BookingUiState.kt | Snapshot trạng thái hiển thị |
| ui/SeatFilter.kt | Điều kiện AND theo loại và trạng thái hiển thị |
| ui/SelectedSeatAdapter.kt | Danh sách lựa chọn, DiffUtil theo ID |
| MainActivity.kt | Kết nối listener, StateFlow, Canvas, Sheet, Legend và kết quả 3D |
| preview/ | Hình học, renderer, camera và phát video trong không gian 3D |

## Hình học sơ đồ

Hệ world có chiều rộng 856 và chiều cao 636. Mỗi hàng được chia thành hai cụm, cách nhau lối đi rộng 44. Ghế đơn rộng 36, ghế đôi rộng 78, chiều cao ghế 32; khe ngang 6, khoảng hàng 8 và khoảng tách khu 18.

```text
seatsPerSide = số ghế trong hàng / 2
blockWidth = seatsPerSide × seatWidth + (seatsPerSide − 1) × 6
leftBlock = 428 − 44/2 − blockWidth
rightBlock = 428 + 44/2
column = index % seatsPerSide
left = blockLeft + column × (seatWidth + 6)
```

`SeatRow` chứa dữ liệu một hàng. `SeatPosition` chứa một ghế và tọa độ đã tính; `RowPosition` nhóm các vị trí cùng hàng. Hình học được tạo từ dữ liệu trước khi vẽ và không phụ thuộc kích thước màn hình.

## Camera

```text
view = world × scale + offset
world = (view − offset) / scale
fitScale = min(viewWidth / worldWidth, viewHeight / worldHeight)
```

`onDraw()` lưu trạng thái Canvas, áp `translate` và `scale`, vẽ sơ đồ rồi khôi phục. Minimap được vẽ theo pixel của View sau phép khôi phục. Thứ tự lệnh trên Canvas tạo phép biến đổi điểm tương đương nhân tỷ lệ rồi cộng độ lệch.

Pinch lấy điểm neo world tại focus trước, nhân tỷ lệ với scaleFactor, giới hạn tỷ lệ rồi tính `offsetNew = focusNew − anchor × scaleNew`. Tỷ lệ tương đối với fit nằm trong khoảng 0,8–5. Độ lệch được giới hạn theo viewport; khi chạm biên, điều kiện giữ sơ đồ trong vùng nhìn được ưu tiên.

Pan dùng trực tiếp độ dịch chuyển pixel. Khoảng đệm bằng 15% viewport cho phép dịch chuyển khi toàn sơ đồ nhỏ hơn vùng nhìn. Cờ chuỗi nhiều ngón ngăn phát sinh tap sau pinch; ID và vị trí ngón còn lại được ghi nhận để kéo tiếp. Reset View sử dụng ValueAnimator 220 ms về camera fit; thao tác mới hủy hoạt ảnh đang chạy.

## Hit-testing

Điểm chạm được chuyển bằng `viewToWorld()` của camera hiện tại. `findSeatAt()` duyệt các vùng ghế với điều kiện `left ≤ x < right`, `top ≤ y < bottom`. Vùng kiểm tra là hình chữ nhật bao ô ghế bo góc; khe, lối đi và màn chiếu không thuộc ghế. Vòng định vị không mở rộng vùng chạm.

Sau khi xác định ghế, View kiểm tra ghế có thuộc điều kiện lọc hay không rồi phát callback nghiệp vụ. `MapGeometryTest` kiểm tra tâm, sát trong/ngoài bốn cạnh của các ghế dưới nhiều phép biến đổi.

## Trạng thái và đồng bộ

```text
MotionEvent → detector → camera.viewToWorld → findSeatAt
    → onSeatClickListener → BookingViewModel → BookingUiState
    → MainActivity → Canvas + bộ lọc + Bottom Sheet
```

ViewModel bật/tắt ghế trống, chỉ xem thông tin đối với ghế đã đặt/không khả dụng và tính lại các tổng. Dòng danh sách gọi `inspectSeat()` và định vị trên Canvas; nút Xóa gọi `removeSeat()`. Lọc không xóa lựa chọn. Các tổng không được duy trì riêng trong Adapter hoặc Canvas.

`StateFlow` được thu thập trong `repeatOnLifecycle(STARTED)`. `SavedStateHandle` lưu ID lựa chọn, ID đang xem và tên enum bộ lọc. Khi phục hồi, ID lạ/trùng hoặc ghế không còn trống được loại; số ghế, số chỗ và tiền được tính lại từ dữ liệu gốc.

Camera lưu zoomRatio, tâm world và cờ fit trong SavedState của View. Camera được áp lại sau khi có kích thước hợp lệ và được giới hạn theo viewport mới. Material BottomSheetBehavior phục hồi nấc; Activity đồng bộ nhãn nút sau layout và khi nấc thay đổi. Các ca xoay và dựng lại Activity kiểm tra đồng thời state, camera, Sheet và nhãn.

## Tài nguyên và đường vẽ

Paint, Path, RectF, hình học, màu và nhãn được tái sử dụng. Cỡ chữ/baseline được tính theo ID ghế; vòng vẽ không đo lại từng nhãn trong mỗi frame. Ghế ngoài viewport được bỏ qua khi vẽ, trong khi hit-testing giữ toàn bộ dữ liệu hình học. Canvas yêu cầu vẽ lại theo thay đổi trạng thái hoặc camera.

RecyclerView sử dụng ListAdapter/DiffUtil cho danh sách Bottom Sheet. Phần 3D giữ buffer/ma trận và chỉ yêu cầu render khi camera, cấu hình hoặc frame video thay đổi. [Kết quả kiểm thử và số đo](09-kiem-chung.md).

## Tham khảo API

- [Custom drawing](https://developer.android.com/develop/ui/views/layout/custom-views/custom-drawing).
- [Drag and scale](https://developer.android.com/develop/ui/views/touch-and-input/gestures/scale).
- [Saving UI state](https://developer.android.com/topic/libraries/architecture/views/saving-states-views).
- [BottomSheetBehavior](https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetBehavior).
