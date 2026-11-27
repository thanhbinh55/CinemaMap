# Kiểm chứng đợt chốt mã 27–28/11/2026

Tài liệu ghi kết quả thật của Agent, không thay cho xác nhận hiểu mã/dùng thử của sinh viên hoặc điểm do giảng viên chấm. Bản nguồn: [`f3e32f9`](https://github.com/thanhbinh55/CinemaMap/commit/f3e32f91348b21418246b3bdb857787f3988ef7c). Commit tài liệu sau đó không đổi mã app. Log nguồn sạch bên dưới ghi cùng SHA. Video demo nộp bài được sinh viên thực hiện sau.

## Môi trường và kết quả

- macOS Apple Silicon; Android Studio JBR/JDK 25; Gradle 9.6.0; AGP 9.4.1; SDK 37.
- Pixel_7 emulator, `sdk_gphone16k_arm64`, Android API 37. Đây không phải kết quả trên Samsung thật hay mọi API từ 26 trở lên.
- Build Debug + APK test: đạt. Unit test: **20 ca đạt**, gồm 19 ca nghiệp vụ/hình học và 1 ca template. [Log unit test](evidence/2026-11-27-final/unit-tests.txt).
- Lint Debug: **0 lỗi, 15 cảnh báo**. Nhóm cảnh báo: gợi ý dependency/toolchain/KTX, overdraw/unused resource, accessibility của Canvas và ID panel khác nhau có chủ đích ở hai hướng. Không coi “0 lỗi” là “0 cảnh báo”. [Danh sách cảnh báo](evidence/2026-11-27-final/lint.txt).
- Kiểm thử thiết bị toàn bộ: **21 ca đạt**, gồm 20 ca chức năng và 1 ca template. [Log thiết bị](evidence/2026-11-27-final/device-tests.txt).
- Build từ clone nguồn sạch: **đạt** từ clone local `--no-hardlinks` của commit mã nguồn, không sao chép build/cache hoặc local.properties; 79 task thực thi. Build APK app/test + unit test + lint đều thành công. [Log clone sạch](evidence/2026-11-27-final/clean-build.txt).
- Đo mẫu pan: **đã đo**, 120 frame; jank theo gfxinfo **114/120 (95%)**; P50 **57 ms**, P95 **150 ms**, P99 **200 ms**. Khoảng DrawStart→SyncQueued P50 **1.852 ms** (không phải toàn bộ frame). [Raw gfxinfo](evidence/2026-11-27-final/pan-gfxinfo.txt), [ảnh sau pan](evidence/2026-11-27-final/pan-map.png). **Chưa đạt mục tiêu nội bộ 60 Hz ở mẫu Debug/emulator này**; không tự chốt yêu cầu độ mượt là đạt. Cần dùng thử/trace trên Samsung trước nộp nếu còn giật.

[Log build workspace](evidence/2026-11-27-final/build.txt).

## Lệnh và minh chứng

```sh
./gradlew --offline :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest :app:lintDebug
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5554 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s emulator-5554 shell am instrument -w vn.edu.midterm.cinemamap.test/androidx.test.runner.AndroidJUnitRunner
```

`--offline` chỉ dùng vì môi trường này có cache. Báo cáo Gradle mặc định ở `app/build/reports/`, không đưa thư mục build lên Git. Các log rút gọn và ảnh cần thiết được lưu trong `docs/evidence/2026-11-27-final/` sau khi chạy xong. Test runner ADB phải ghi `OK`, không lấy riêng exit code ADB để kết luận.

## Phạm vi kiểm thử tự động

| Nhóm | Ca và phạm vi |
| --- | --- |
| Fixture/hit-test | `MapGeometryTest`: đủ 196 ID duy nhất/212 chỗ, số ghế theo loại; cạnh trái/trên nhận, phải/dưới loại; tâm và sát trong/ngoài bốn cạnh của mọi ghế qua 3 camera có scale/offset khác nhau; khe/lối giữa/màn chiếu không trúng |
| Chọn/lọc/tổng | `BookingViewModelTest`, `SeatFilterTest`: chọn/bỏ, giá cặp, chặn Đã đặt, lọc AND/rỗng, lựa chọn ngoài lọc, inspect độc lập, phục hồi handle mới, loại ID lạ/trùng/Đã đặt/Không khả dụng và enum lạ |
| 2D tích hợp | `BookingSheetTest` 4 ca + `MapNavigationTest` 2 ca: Sheet/danh sách cuộn, định vị/xóa/tổng, bộ lọc/reset/recreate, Legend và bố cục ngang, pan/minimap |
| Gesture/xoay | `MapInteractionTest` 3 ca: kiểm tra pixel thật sau chọn/xóa/lọc/zoom để phát hiện cache cũ; MotionEvent hai ngón thật qua detector, neo lệch tâm, min/max, không phát tap; pan tới bốn góc vẫn còn sơ đồ. Dọc → ngang → dọc giữ zoomRatio 2,5, lựa chọn/lọc/tiền/Sheet và nhãn Thu gọn |
| 3D | `CinemaSceneGeometryTest` 9 unit: ánh xạ ID, camera/hướng nhìn, ghế đôi, cấu hình và A/B. `CinemaPreviewTest` 5 ca + `CinemaPreviewUpgradeTest` 4 ca: cảnh thật/điểm ảnh, đổi ghế, quay về, xoay, minimap/picker, A/B, side, fullscreen và cấu hình |
| Video | `CinemaTrailerTest` 2 ca: metadata 5 giây/không âm thanh, điểm ảnh chuyển động/pause, recreate, loop, timestamp sau resume và thêm chu kỳ ra nền thứ hai |
| Template | 1 unit test và 1 instrumentation test mẫu còn trong template; không dùng chúng thay cho ca chức năng |

Unit test hình học dùng tọa độ và bộ tính thật, không so ảnh đoán ID. Ca UI pan biên gọi hàm pan của View để kiểm tra mọi góc cực trị; ca kéo một ngón qua MotionEvent nằm ở MapNavigationTest. Kết quả không có nghĩa đã thử mọi chuỗi ba ngón, mọi thiết bị hoặc mọi font scale.

## Các lỗi đã phát hiện và xử lý

1. **Nhãn Sheet sai sau xoay/recreate:** Behavior đã mở nhưng nút còn Xem ghế. Activity đồng bộ nhãn sau layout và khi Behavior đổi nấc. Ca hồi quy kiểm tra cả nấc thực và nhãn.
2. **Camera 2D về fit khi tạo lại View:** SavedState của View lưu zoomRatio/tâm world/cờ fit, áp sau khi viewport hợp lệ và clamp. Ca xoay xác minh tỷ lệ không bị reset.
3. **Chưa có saved state cho phiên đặt ghế:** SavedStateHandle lưu primitive/ID, phục hồi theo fixture và tính lại tổng. Unit test dựng handle mới từ snapshot. Đây không phải cơ sở dữ liệu lâu dài và chưa được gọi là một ca OS kill thực tế.
4. **Video có lần đứng sau vòng đời:** audit trước sửa đạt 16/17; chạy riêng lại đạt nên không tự coi ổn định. Player nay release decoder khi ra nền, tạo lại theo Surface hợp lệ và seek; bỏ callback decoder đã thay. Ca hồi quy kiểm tra timestamp lẫn ảnh thật sau loop và hai lần resume, không hạ ngưỡng chuyển động.
5. **Kiểm thử Sheet đọc khi animation còn SETTLING:** lượt đầu sau sửa có 18/19 ca đạt, lỗi mong EXPANDED nhưng đang SETTLING. Thay sleep cố định bằng chờ Behavior thực sự EXPANDED với timeout; không bỏ assertion hay cưỡng ép trạng thái app.

6. **Tối ưu đường vẽ:** thử cache Picture/RenderNode nhưng không có bằng chứng cải thiện frame time trong emulator, nên không giữ trong bản chốt. Ảnh QA từng phát hiện bounds 0 của bản thử; bản thử không được commit và số đo ảnh trống không được dùng. Bản chốt chỉ cache thông số chữ và cull ghế ngoài viewport, dùng Canvas trực tiếp. Ca pixel màn hình thật xác minh chọn/xóa/lọc vẫn cập nhật đúng màu.

## Phạm vi đo hiệu năng

`tools/profile_map.py --serial emulator-5554` khởi động lại task ở hướng dọc, reset gfxinfo và gửi 16 swipe một ngón (4 hướng, 240 ms/lượt) ở mức fit với 196 ghế. Script in thông tin thiết bị, số frame và thống kê `dumpsys gfxinfo ... framestats`; báo lỗi nếu không có frame. Không chạy cùng instrumentation. Nếu ADB không ở PATH, truyền `--adb /đường/dẫn/adb`.

Đây là mẫu render của **Debug trên emulator (SkiaGL, GPU mode auto)**, gồm chi phí Android/UI và lệnh input; không phải benchmark GPU cô lập, không chứng minh pinch/3D đạt cùng frame time hoặc 60 FPS trên Samsung. Yêu cầu đề là tránh giật rõ, không đưa ngưỡng FPS cụ thể. Xem kết quả thực ở phần đầu, ghi giới hạn thay vì tự công nhận mọi thiết bị đạt.

## Đối chiếu T01–T16 và phần còn lại

- T01–T02: fixture/hình học, logic chọn và ca UI Sheet đã kiểm tra; hình thức bố cục xem ảnh trong minh chứng.
- T03–T05: kiểm tra tự động pan/minimap, min/max/neo pinch và mép 196 ghế sau transform. Sự kiện pinch không tạo tap. Tổ hợp thao tác bằng tay trên Samsung vẫn do sinh viên dùng thử.
- T06–T10: reset/giữ lọc-lựa chọn, Legend, rỗng/xóa lọc, Sheet cuộn/xóa/định vị có ca tự động. Tương tác kéo Sheet bằng tay, font lớn và trình đọc màn hình chưa có chứng nhận đầy đủ.
- T11: xoay dọc/ngang/dọc và recreate giữ state/camera/Sheet/nhãn có ca hồi quy.
- T12: Home/resume 3D và handle mới đã kiểm tra; không đánh đồng recreate với hủy tiến trình Android thật. Chưa cam kết giữ phiên sau force-stop/xóa app.
- T13–T14: soát cache đồ họa, đo mẫu pan và build nguồn sạch theo kết quả liên kết.
- T15: README/nhật ký AI đã cập nhật; sinh viên cần tự kiểm tra và giải thích code, Agent không xác nhận thay.
- T16: **chưa thực hiện, sinh viên quay video 1–5 phút và thêm URL sau**; kiểm tra quyền truy cập repo/video trước nộp.

## Giới hạn không được ghi thành chức năng hoàn tất

Chưa có backend/thanh toán, iOS, phục hồi đặt vé lâu dài, vị trí 3D hiệu chuẩn rạp thật, ánh sáng vật lý hay accessibility node riêng từng ghế. Đây không phải các yêu cầu chức năng bắt buộc của đề hiện tại. Video demo và trách nhiệm kiểm tra/hiểu mã của sinh viên vẫn là yêu cầu phải hoàn thiện trước nộp.

### Diễn giải số đo

Không dùng các lần thử Picture/RenderNode để công bố phần trăm tăng tốc: môi trường có tải thay đổi và một bản thử có ảnh trống. Bản chốt loại bỏ các cache đó, chỉ giữ cache metrics chữ và culling ô ngoài viewport. Gfxinfo cho thấy frame end-to-end còn trễ, dù giai đoạn ghi draw trên UI ngắn hơn nhiều so với toàn frame; cần trace sâu hơn trên máy thật để kết luận nguyên nhân CPU/GPU/driver. Kiểm thử chức năng đạt không đồng nghĩa hiệu năng đã đạt trên mọi máy.
