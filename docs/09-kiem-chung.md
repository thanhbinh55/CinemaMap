# Báo cáo kiểm chứng

## Phạm vi và phiên bản

Báo cáo tổng hợp kết quả chạy trên mã ứng dụng tại [commit f3e32f9](https://github.com/thanhbinh55/CinemaMap/commit/f3e32f91348b21418246b3bdb857787f3988ef7c) và đợt rà soát ngày 28/11/2026. Đợt biên tập hồ sơ không thay đổi mã runtime của ứng dụng. Kiểm thử ảnh 3D được điều chỉnh cách chờ khung hình; các ngưỡng kiểm tra ảnh và phép so sánh gần/xa được giữ nguyên.

Các kết quả được trình bày theo từng lần chạy. Kết quả kỳ vọng của một ca và kết quả đã thực thi là hai thông tin riêng biệt.

## Môi trường

- macOS Apple Silicon; JDK 25 đi kèm Android Studio.
- Gradle Wrapper 9.6.0; Android Gradle Plugin 9.4.1; Android SDK 37.
- Máy ảo `emulator-5554`, model `sdk_gphone16k_arm64`, Android API 37.
- Mạng và cache công cụ đã được cấu hình tại môi trường kiểm chứng. Tham số `--offline` trong log sử dụng cache có sẵn.

## Kết quả đã thực thi

| Nội dung | Kết quả | Minh chứng |
| --- | --- | --- |
| Build APK Debug và APK kiểm thử | Thành công | [Build](evidence/2026-11-28-submission/build.txt), [build APK kiểm thử sau điều chỉnh](evidence/2026-11-28-submission/test-apk-build.txt) |
| Unit test chạy mới | 20/20 đạt; gồm 19 ca chức năng và 1 ca mẫu | [Tổng hợp XML](evidence/2026-11-28-submission/unit-tests.txt), [log chạy mới](evidence/2026-11-28-submission/checks.txt) |
| Lint Debug | 0 lỗi; 14 mục Warning theo báo cáo XML | [Danh sách](evidence/2026-11-28-submission/lint.txt) |
| Kiểm thử thiết bị, lượt đầu | 20/21 đạt; ca ảnh góc nhìn gần/xa không đạt | [Log đầy đủ](evidence/2026-11-28-submission/device-tests-first.txt) |
| Ca ảnh 3D sau điều chỉnh chờ | Chạy riêng đạt 1/1 | [Log](evidence/2026-11-28-submission/scene-pixel-retest.txt) |
| Kiểm thử thiết bị, lượt tiếp theo | 20/21 đạt; ca so sánh A/B qua dựng lại Activity không đạt | [Log đầy đủ](evidence/2026-11-28-submission/device-tests-final.txt) |
| Ca A/B chạy riêng | Không đạt, 1 ca/1 lỗi | [Log](evidence/2026-11-28-submission/comparison-retest.txt) |
| Build từ nguồn sạch, mã f3e32f9 | Thành công; clone local không sao chép build hoặc local.properties | [Log nguồn sạch](evidence/2026-11-27-final/clean-build.txt) |
| Video minh họa | 289 giây; có giọng nam tiếng Việt; luồng hình giữ nguyên khi ghép âm thanh | [Thông số](evidence/2026-11-28-submission/video.json) |
| Repository | Truy cập API không đăng nhập trả HTTP 200, repository public | [Thông tin kiểm tra](evidence/2026-11-28-submission/repository.json) |

Kết quả 21/21 tại [lần kiểm tra trước](evidence/2026-11-27-final/device-tests.txt) được giữ như bằng chứng lịch sử, không sử dụng thay cho kết quả mới có lỗi. Lượt kiểm tra mới chưa cho phép kết luận toàn bộ bộ tích hợp đạt ổn định.

Các nhóm cảnh báo lint gồm tài nguyên/layout khác nhau giữa hai hướng, phiên bản phụ thuộc, overdraw, gợi ý KTX và accessibility của Custom View. Số 14 ở trên là số mục Warning chính trong XML; không diễn giải build thành công là không có cảnh báo.

## Phạm vi các nhóm kiểm thử

| Nhóm | Nội dung |
| --- | --- |
| MapGeometryTest | 196 ID, sức chứa, hình học, tâm ghế và điểm sát trong/ngoài bốn cạnh dưới nhiều camera; khe/lối đi không trúng |
| BookingViewModelTest / SeatFilterTest | Chọn/bỏ chọn, giá theo loại/cặp, lọc AND/rỗng, giữ lựa chọn ngoài lọc và phục hồi trạng thái hợp lệ |
| BookingSheetTest, 4 ca | Sheet, danh sách, định vị/xóa, bộ lọc, reset và bố cục |
| MapNavigationTest, 2 ca | Pan, minimap và vùng tương tác dọc/ngang |
| MapInteractionTest, 3 ca | Điểm ảnh sau chọn/xóa/lọc, pinch theo tâm và min/max, pan biên, xoay giữ camera/lựa chọn/lọc/Sheet |
| CinemaSceneGeometryTest, 9 ca | Ánh xạ ghế, hướng camera, ghế đôi, cấu hình và A/B |
| CinemaPreviewTest, 5 ca | Hình ảnh cảnh, góc nhìn gần/xa, đổi ghế, dựng lại, xoay và quay về |
| CinemaPreviewUpgradeTest, 4 ca | A/B, ghế đôi, minimap/picker, toàn màn hình và cấu hình; ca A/B còn lỗi trong lượt mới |
| CinemaTrailerTest, 2 ca | Metadata, chuyển động/pause, vòng đời và lặp video |

Các ca 2D thuộc BookingSheetTest, MapInteractionTest và MapNavigationTest đều đạt trong hai lượt toàn bộ mới. Kiểm thử hủy tiến trình Android thực tế, mọi mức chữ lớn và mọi thiết bị API 26 trở lên chưa được thực hiện đầy đủ.

## Điều chỉnh kiểm thử ảnh và lỗi còn tái hiện

Helper ảnh 3D ban đầu chờ cố định một giây rồi kiểm tra một ảnh. Trạng thái Surface/renderer sẵn sàng không đồng nghĩa frame video đã hiển thị. Helper hiện kiểm tra ảnh trong tối đa tám giây và lưu ảnh chẩn đoán; giữ điều kiện số mẫu màu lửa lớn hơn 20, vùng tối lớn hơn 100 và diện tích nhìn gần lớn hơn hai lần nhìn xa.

Sau thay đổi này, ca ảnh chạy riêng đạt và cả năm ca CinemaPreviewTest đạt trong lượt toàn bộ tiếp theo. Tuy nhiên, ca `comparisonAndCoupleSideSurviveRecreationAndCanBeReplaced` không đạt: kỳ vọng camera F09 nhưng nhận L04/Phải. Chạy riêng vẫn tái hiện lỗi. Báo cáo ghi nhận giới hạn của phần mở rộng A/B; chưa xác định nguyên nhân từ các log hiện có và không ghi nhận lỗi đã được sửa.

## Hiệu năng

`tools/profile_map.py` khởi động task mới ở hướng dọc, reset gfxinfo và thực hiện 16 swipe một ngón, mỗi swipe 240 ms, trên sơ đồ 196 ghế ở mức fit. Mẫu dưới đây được thu sau khi build kết thúc, không chạy đồng thời instrumentation:

| Chỉ số từ gfxinfo | Giá trị |
| --- | ---: |
| Tổng frame | 72 |
| Janky frames | 72/72, 100% |
| Phân vị 50 theo thống kê gfxinfo | 150 ms |
| Phân vị 95 | 250 ms |
| Phân vị 99 | 400 ms |

[Log gfxinfo](evidence/2026-11-28-submission/pan-gfxinfo.txt).

Mẫu emulator này có độ trễ cao. Việc tái sử dụng tài nguyên và chỉ vẽ khi cần đã có trong mã, nhưng chưa đủ để kết luận đạt yêu cầu phản hồi mượt của mục 3.4 trong môi trường đo. Không suy ra số đo trên điện thoại thật hoặc hiệu năng 3D từ mẫu này. Video thao tác không thay thế phép đo frame-time của ứng dụng; FPS của file video cũng không phải FPS của app.

## Lệnh tái hiện

```sh
./gradlew --offline :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest :app:lintDebug
./gradlew --offline :app:testDebugUnitTest :app:lintDebug --rerun-tasks
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5554 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s emulator-5554 shell am instrument -w vn.edu.midterm.cinemamap.test/androidx.test.runner.AndroidJUnitRunner
python3 tools/profile_map.py --serial emulator-5554
```

Lệnh đo chạy riêng với kiểm thử thiết bị. Kết luận instrumentation dựa trên nội dung `OK` hoặc `FAILURES` của runner, không chỉ exit code ADB.

## Hồ sơ và giới hạn kết luận

README đã có mô tả đề tài, hướng dẫn chạy, kiến trúc, dữ liệu, tọa độ, hit-testing, trạng thái, tối ưu, video, hình ảnh và nhật ký AI. Lịch sử trước đợt cập nhật này có 12 commit, phân bố ngày 23, 24, 27 và 28/11/2026. Video dài 4 phút 49 giây; metadata Drive cho thấy file được chia sẻ cho người có liên kết ở quyền commenter, bao gồm khả năng xem.

Các chức năng bắt buộc đã có hiện thực và bằng chứng kiểm thử tương ứng. Tuy nhiên, tiêu chí độ mượt còn hạn chế theo mẫu đo, và phần mở rộng A/B còn một ca lỗi tái hiện. Vì vậy, hồ sơ không đưa ra cam kết đáp ứng tuyệt đối mọi tiêu chí hoặc điểm số. Những giới hạn này được công bố cùng kết quả đã đạt.
