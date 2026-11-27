# CinemaMap — Sơ đồ phòng chiếu rạp phim

Bài giữa kỳ cá nhân học phần **Phát triển phần mềm cho thiết bị di động**, đề tài 2: sơ đồ phòng chiếu. Android Kotlin, Views/XML; toàn bộ sơ đồ chính tự vẽ trên **một Canvas**. Dữ liệu giả lập cục bộ, không cần tài khoản, API, cơ sở dữ liệu hay mạng khi sử dụng.

- Repository: https://github.com/thanhbinh55/CinemaMap
- **Video demo nộp bài: CHƯA CÓ — sinh viên sẽ quay và bổ sung liên kết sau.** Video cần dài 1–5 phút theo [kịch bản](docs/05-nghiem-thu.md#video-demo-do-sinh-viên-thực-hiện-sau).
- Video lửa trại 5 giây trong cảnh 3D là tài nguyên ứng dụng, **không phải** video demo nộp bài.
- Phạm vi kiểm chứng và việc còn lại: [nghiệm thu](docs/05-nghiem-thu.md), [kết quả kiểm tra hiện tại](docs/09-kiem-chung.md). Không coi bộ hồ sơ đã hoàn tất khi chưa có video và quyền truy cập liên kết.

## Chạy dự án

1. Clone repo, mở **thư mục `CinemaMap`** trong Android Studio và chờ Gradle Sync.
2. Cài Android SDK Platform **37**, chấp nhận SDK licenses. Dùng **JDK 25** cho Gradle; không nhầm với mức Java source/target **11** của app.
3. Toolchain đã dùng: Gradle Wrapper **9.6.0**, Android Gradle Plugin **9.4.1**, compile/target SDK **37**, min SDK **26** (Android 8.0 trở lên).
4. Chọn emulator hoặc điện thoại Android API 26+ rồi Run cấu hình `app`. Môi trường kiểm chứng chính là Pixel_7 emulator, API 37; Samsung thật còn cần sinh viên dùng thử.

```sh
git clone https://github.com/thanhbinh55/CinemaMap.git
cd CinemaMap
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Trên macOS có Android Studio, nếu terminal chưa chọn JDK:

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew --version
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
```

Lần Sync/build đầu cần Internet để tải Gradle và thư viện. Chỉ thêm `--offline` khi đã có cache. Android Studio tạo `local.properties` chỉ SDK theo máy; tệp này không đưa lên Git. Nếu dùng terminal, cấu hình SDK qua `ANDROID_HOME` hoặc `local.properties` của chính máy đó.

APK Debug: `app/build/outputs/apk/debug/app-debug.apk`. Với điện thoại Samsung: bật Developer options → USB debugging, kết nối USB, chấp nhận khóa máy tính, chọn thiết bị trong Android Studio rồi Run. Có thể cài APK bằng `adb install -r app/build/outputs/apk/debug/app-debug.apk`. Đây là dự án Android, không cài trực tiếp APK trên iPhone.

## Sử dụng và chức năng bắt buộc

| Chức năng | Hành vi hiện tại |
| --- | --- |
| Sơ đồ | Màn chiếu cong ở trên, nhãn hàng A–L, khu Thường/VIP/Ghế đôi, lối giữa hai khối |
| Trạng thái | Trống, đang chọn, đã đặt, không khả dụng; phân biệt bằng nền màu và chữ, ô bo góc không viền |
| Pan | Kéo một ngón theo hai trục, có giới hạn và đệm 15% viewport; không kéo mất toàn bộ sơ đồ |
| Pinch | Hai ngón, giữ điểm neo dưới tâm khi biên cho phép; giới hạn 0,8–5 lần tỷ lệ vừa phòng; không phát tap sau pinch |
| Chạm | Ghế trống: chọn/bỏ chọn. Ghế đã đặt/không khả dụng: chỉ xem thông tin. Chạm lối đi/khe ghế không chọn |
| Bộ lọc | Loại và trạng thái kết hợp AND; điều kiện hiện trên nút, có Xóa lọc và thông báo khi không có kết quả |
| Lựa chọn ngoài lọc | Ghế mờ và khóa chạm trên map; vẫn giữ trong Sheet với nhãn Ngoài bộ lọc, có thể định vị/xóa |
| Legend | Thanh cuộn ngang và nút Chi tiết; giải thích loại, trạng thái, ghế đôi, lối đi và ghế mờ |
| Reset View | Biểu tượng Toàn phòng hoặc chạm minimap; hoạt ảnh 220 ms về fit, giữ lựa chọn/lọc/Sheet; gesture mới hủy hoạt ảnh |
| Bottom Sheet | Thu gọn: số ghế/chỗ, tiền và ghế đang xem. Mở rộng: danh sách cuộn, định vị/xóa từng mục, cập nhật Canvas ngay |
| Xoay | Layout dọc và ngang riêng; giữ lựa chọn, lọc, tiền, nấc Sheet, nhãn nút và camera tương đối trong biên mới |

Hướng ngang dành 64% chiều rộng cho Canvas/Legend và 36% cho Sheet. Hướng dọc Sheet mở vẫn để lại vùng Canvas. Header hiển thị `Rạp HCMUS · Phòng 01 · 19:00`, tổng tiền nằm bên phải. Minimap trên Canvas xuất hiện khi pan/zoom để chỉ phần phòng đang nhìn.

## Tổ chức mã nguồn và luồng dữ liệu

```text
app/src/main/java/vn/edu/midterm/cinemamap/
├── data/MockCinemaData.kt       # Fixture bất biến
├── model/Seat.kt                # ID, loại, sức chứa, giá, trạng thái gốc, hàng
├── map/MapGeometry.kt           # Hình học world và hit-testing
├── map/CameraTransform.kt       # Chuyển đổi world ↔ View
├── map/CinemaMapView.kt         # Canvas, gesture, camera và SavedState của View
├── ui/BookingUiState.kt         # Snapshot trạng thái UI
├── ui/BookingViewModel.kt       # Chọn/lọc/tổng tiền, SavedStateHandle
├── ui/SeatFilter.kt             # Predicate AND theo trạng thái hiển thị
├── ui/SelectedSeatAdapter.kt    # RecyclerView của Sheet, DiffUtil theo ID
├── MainActivity.kt              # Nối sự kiện, collect state, layout/Sheet/Legend
└── preview/                     # Cải tiến 3D và video; tách khỏi Canvas bắt buộc
```

Luồng chính: **MotionEvent → detector → camera.viewToWorld → findSeatAt → onSeatClickListener → ViewModel → StateFlow → Activity → Canvas + bộ lọc + Sheet**. Chiều ngược: nút Xóa gọi `removeSeat`, dòng ghế gọi `inspectSeat` và `locateSeat`. Canvas không tự sửa trạng thái đặt vé; RecyclerView không tạo View thay cho 196 ghế trên sơ đồ.

## Dữ liệu

| Khu | Hàng | Đối tượng | Chỗ | Giá mỗi đối tượng |
| --- | --- | ---: | ---: | ---: |
| Thường | A–D, 18 ghế/hàng | 72 | 72 | 85.000 đ |
| VIP | E–J, 18 ghế/hàng | 108 | 108 | 110.000 đ |
| Ghế đôi | K–L, 8 khối/hàng | 16 | 32 | 190.000 đ/cặp |
| **Tổng** | **A–L** | **196** | **212** | |

ID duy nhất A01–J18, K01–K08, L01–L08. Fixture có 11 ghế đã đặt, 4 không khả dụng, 181 ghế trống. Đang chọn là trạng thái dẫn xuất từ tập lựa chọn, không ghi đè trạng thái gốc. Ghế đôi là **một đối tượng có hai chỗ**, chọn/xóa/tính tiền theo cặp. Giá dùng số nguyên VND.

Ca mẫu: chọn **A05 + E08 + K03 → 3 ghế, 4 chỗ, 385.000 đ**; bỏ E08 → 2 ghế, 3 chỗ, 275.000 đ. Bộ lọc Trống cập nhật ngay sau khi một ghế chuyển thành Đang chọn.

## Tọa độ, camera và hit-testing

Sơ đồ có hệ world **856 × 636**, ghế đơn 36 × 32, ghế đôi 78 × 32, khe ngang 6, khe hàng 8 và lối giữa 44. Tách khu bằng khoảng dọc 18. Pixel View phụ thuộc màn hình; world không đổi khi xoay.

```text
viewX = worldX × scale + offsetX
viewY = worldY × scale + offsetY
worldX = (viewX − offsetX) / scale
worldY = (viewY − offsetY) / scale
fitScale = min(viewWidth / 856, viewHeight / 636)
```

Canvas gọi `translate(offsetX, offsetY)` rồi `scale(scale, scale)`. Hiệu quả lên một điểm là nhân scale rồi cộng offset. Khi tap, dùng phép nghịch đảo trên đúng snapshot camera hiện tại; không lấy tọa độ chạm đem so trực tiếp với tọa độ world.

Pinch: lấy `anchor = oldCamera.viewToWorld(previousFocus)`, giới hạn `newScale`, tính `newOffset = currentFocus − anchor × newScale`, rồi giới hạn pan. Khi bị chặn biên, điểm neo có thể dịch để thỏa giới hạn. Pan dùng delta pixel, không chia scale lần nữa. Khi nhấc một trong hai ngón, lưu ID/vị trí ngón còn lại để kéo tiếp mà không nhảy.

`findSeatAt` duyệt tối đa 196 hình chữ nhật, nhận điểm thỏa **left ≤ x < right, top ≤ y < bottom**. Cạnh phải/dưới bị loại trừ; khe và lối đi không thuộc ghế. Vùng chạm là bounds chữ nhật của ô bo nhẹ; vòng định vị không mở rộng vùng chạm. Ghế ngoài lọc không phát callback nghiệp vụ. Xem [kiến trúc chi tiết](docs/02-kien-truc.md).

## Quản lý và phục hồi trạng thái

- `BookingViewModel` giữ trạng thái qua xoay. `SavedStateHandle` lưu **ID đã chọn, ID đang xem và tên enum của bộ lọc**, hỗ trợ task được Android phục hồi sau hủy tiến trình; không lưu toàn bộ mô hình ghế hoặc tổng tiền.
- Khi phục hồi, đối chiếu fixture, bỏ ID lạ/trùng/ghế không còn khả dụng và tính lại số ghế/chỗ/giá. Unit test dựng handle mới từ snapshot để kiểm tra đường phục hồi.
- `CinemaMapView` lưu tỷ lệ zoom so với fit, tâm world và cờ góc mặc định bằng SavedState của View; đợi viewport hợp lệ rồi áp camera và clamp theo kích thước mới. Nấc Sheet do `BottomSheetBehavior` phục hồi; nhãn nút được đồng bộ sau layout và khi đổi nấc.
- Đây là trạng thái của phiên/task, **không phải lưu đặt vé lâu dài**. Force-stop, xóa dữ liệu, hoặc mở phiên mới không được coi là cam kết giữ phiên cũ.

## Hiệu năng và độ ổn định

Paint, Path, RectF của minimap, hình học, màu và nhãn được tạo/tái sử dụng ngoài `onDraw`. Khi vẽ ghế không cấp phát tài nguyên đồ họa nặng. Chỉ `invalidate` khi camera, dữ liệu hiển thị, tương tác hoặc kích thước đổi; không có vòng vẽ 2D liên tục khi đứng yên. Thông số chữ (cỡ và baseline) tính một lần theo ghế; khi pan/zoom bỏ qua các ô nằm ngoài viewport. Vòng định vị vẫn được xét riêng để không bị cắt mất. Hit-test tuyến tính phù hợp 196 đối tượng. Danh sách Sheet dùng RecyclerView/ListAdapter/DiffUtil.

Cảnh 3D dùng buffer/ma trận tái sử dụng, `RENDERMODE_WHEN_DIRTY`; frame phim hoặc đổi camera yêu cầu render. Ra nền lưu mốc phát và giải phóng decoder, quay lại tạo decoder cho Surface hiện tại, seek rồi tiếp tục theo ý định phát/pause. Chi tiết và giới hạn đo nằm trong [báo cáo kiểm chứng](docs/09-kiem-chung.md); không cam kết 60 FPS trên mọi thiết bị.

## Kiểm thử

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w vn.edu.midterm.cinemamap.test/androidx.test.runner.AndroidJUnitRunner
```

Có thể dùng `./gradlew :app:connectedDebugAndroidTest` khi môi trường tải đủ bộ Android test runner của Gradle. Lệnh ADB ở trên được dùng để kiểm tra trên emulator khi cache UTP chưa đủ. Chọn đúng thiết bị bằng `adb -s SERIAL` khi có nhiều máy. Kết quả test runner phải kết thúc bằng `OK`; không chỉ dựa vào exit code của ADB.

Unit test bao phủ fixture, mép ghế sau transform, chọn/lọc/tính tiền, phục hồi SavedState và hình học/camera 3D. Instrumentation bao phủ Sheet/Legend/filter, pan/minimap, pinch/rotation, 3D/A-B/minimap/fullscreen và điểm ảnh video thực tế. Log kết quả thuộc bản hiện tại được liên kết trong [báo cáo kiểm chứng](docs/09-kiem-chung.md); ảnh/log cũ là lịch sử, không tự chứng minh bản mới.

## Đối chiếu chấm điểm

| Hạng mục của đề | Tỷ trọng | Phần đáp ứng |
| --- | ---: | --- |
| Canvas, bố cục và hiệu năng | 25% | 196 đối tượng/một Canvas; phân khu, lối đi, nhãn; tái sử dụng tài nguyên |
| Tương tác và xử lý tọa độ | 25% | Pan có biên, pinch có tâm/giới hạn, transform thuận-nghịch, hit-test sau pan/zoom |
| Chức năng và UX | 20% | Legend, bộ lọc/rỗng/xóa, reset mượt, Sheet cuộn/định vị/xóa và đồng bộ hai chiều |
| Kiến trúc và trạng thái | 15% | Tách model/state/render, giữ lựa chọn và UI ở hai hướng, phục hồi state |
| Mã nguồn và độ ổn định | 10% | Kiểm thử logic/tích hợp/biên, build/lint và kiểm tra bản nguồn sạch |
| Hồ sơ và quy trình | 5% | Lịch sử Git, README và nhật ký AI; **video demo sinh viên bổ sung sau** |

Đề phần chức năng ghi 50–250 đối tượng, bảng điểm ghi 150–250; 196 thỏa cả hai. Không dùng đúng Canvas: mục đó tối đa 5/25 theo đề. Hit-test sai sau pan/zoom: phần hit-test không tính điểm. Không build/khởi chạy được: tổng điểm tối đa 5/10. Chức năng mở rộng không thay thế bắt buộc. Các tỷ trọng trên không phải điểm tự chấm hay bảo đảm điểm số. [Ma trận R01–R18](docs/01-yeu-cau.md).

Lịch sử Git có các commit có ý nghĩa trong thời gian thực hiện; không tạo lịch sử giả hoặc chia bắt buộc mỗi ngày một commit. Sinh viên chịu trách nhiệm mã nộp, kiểm tra/điều chỉnh đầu ra AI và giải thích được luồng tọa độ, gesture, state và Sheet.

## Cải tiến 3D và tài nguyên tự tạo

Xem từ một trong 196 ghế, nhìn quanh 360°, đổi hàng/ghế, ghế đôi Trái/Phải, minimap chọn ghế, so sánh A/B, toàn màn hình và cấu hình phòng giả lập. Xem 3D không chọn/bỏ vé, không thay bộ lọc/tổng tiền; quay về trả ID ghế đang xem để định vị trên 2D. Màn chiếu phát **Lửa trại**, MP4 5 giây tạo bằng mã, offline, không âm thanh.

Phòng 3D chưa hiệu chuẩn với rạp thật; ánh sáng/bóng là mô phỏng nhẹ, không PBR hay bóng người. Không dùng cảnh phim, nhạc hoặc nhân vật bên thứ ba. [Mô hình/luồng 3D](docs/07-preview-3d.md), [nguồn video và cách tái tạo](docs/08-trailer.md).

## Tài liệu dự án

| Tài liệu | Nội dung |
| --- | --- |
| [01 — Yêu cầu](docs/01-yeu-cau.md) | Truy vết đề và rubric |
| [02 — Kiến trúc](docs/02-kien-truc.md) | Cấu trúc thực tế, hình học, camera và state |
| [03 — UI/UX](docs/03-ui-ux.md) | Màu, trạng thái, bố cục và luồng sử dụng hiện tại |
| [04 — Quy chuẩn](docs/04-quy-chuan.md) | Làm cá nhân, học từng phần, tài liệu và ghi chú AI |
| [05 — Nghiệm thu/nộp](docs/05-nghiem-thu.md) | Ca T01–T16, checklist và kịch bản quay demo |
| [06 — Làm việc với Codex](docs/06-prompt-codex.md) | Prompt tiếp nối, giới hạn phạm vi được giao |
| [07 — 3D](docs/07-preview-3d.md) | Hình học, renderer, camera và cải tiến |
| [08 — Video trong app](docs/08-trailer.md) | Nguồn procedural, decoder/texture/lifecycle |
| [09 — Kiểm chứng](docs/09-kiem-chung.md) | Lệnh, kết quả và giới hạn của lần chốt mã |

## Nhật ký sử dụng AI Agent

| Ngày | Agent và nhiệm vụ | Tệp hoặc phần được tạo/chỉnh | Hạn chế và cách sinh viên kiểm tra |
| --- | --- | --- | --- |
| 2026-11-23 | Codex đọc 5 trang đề, đối chiếu rubric, lập đặc tả, phương án UI/UX, ca nghiệm thu và prompt bàn giao | README này và 6 tài liệu trong `docs/` | Các quyết định kỹ thuật và UI là đề xuất. Sinh viên đối chiếu mã yêu cầu với PDF, duyệt cách tính ghế đôi và hành vi lọc, sau đó kiểm tra bằng build và demo khi có ứng dụng. Chờ sinh viên xác nhận. |
| 2026-11-23 | Codex cập nhật kế hoạch theo quy trình học do sinh viên mô tả | README và các mục lộ trình, Git, thiết lập, quy chuẩn phối hợp và prompt làm từng phần | Sinh viên đã phản hồi về lịch commit và phạm vi tự động hóa. Bản cập nhật ghi lại quy trình làm từng phần. Việc xác nhận nội dung cập nhật đang chờ. |
| 2026-11-23 | Codex cập nhật hướng dẫn theo lựa chọn tiếp tục dùng Codex | README và `docs/06-prompt-codex.md` | Hướng dẫn giao từng việc trong chat hiện tại và khởi động lại ngữ cảnh khi mở chat mới. |
| 2026-11-23 | Codex thiết lập nền dự án và nối dữ liệu phòng thật vào Canvas | Git local, cấu hình bỏ qua tệp máy, mô hình ghế, dữ liệu giả lập, hình học và Canvas tổng quan | Gradle build và unit test của dự án chạy thành công. Kiểm tra riêng trên các lớp đã biên dịch xác nhận 196 ghế, 212 chỗ, mã duy nhất và lối đi giữa. Kiểm chứng UI trên Pixel_7 ở hai hướng đang chờ. Camera tương tác và chọn ghế sẽ triển khai ở phần kế tiếp. |
| 2026-11-24 | Codex nới giới hạn zoom và sửa chuyển từ pinch sang kéo bằng ngón còn lại | Camera trong `CinemaMapView.kt` và giới hạn zoom trong tài liệu | Build và unit test thành công. Phép thử tạm gửi sự kiện vào View trên Pixel_7 xác nhận kéo trái/phải sau zoom và kéo tiếp sau khi nhấc một ngón. Sinh viên kiểm tra lại bằng thao tác trực tiếp. |
| 2026-11-24 | Codex triển khai Reset View bằng nút Toàn phòng | Layout, MainActivity và camera trong `CinemaMapView.kt` | Build và unit test đã qua. Phép thử trên Pixel_7 xác nhận nút đưa camera về đúng tọa độ và độ phóng mặc định, và chạm vào sơ đồ dừng hoạt ảnh. T06 tích hợp sẽ kiểm tra lại khi có lựa chọn, lọc và Sheet. |
| 2026-11-24 | Antigravity cùng sinh viên triển khai BookingViewModel và chọn ghế trên Canvas | `BookingUiState.kt`, `BookingViewModel.kt`, `BookingViewModelTest.kt`, `CinemaMapView.kt`, `MainActivity.kt` | Build và unit test bao phủ phần logic T02 đã qua: chọn/bỏ chọn, tổng số ghế/chỗ, giá theo từng đối tượng và chặn ghế đã đặt. Nhánh không khả dụng đã có trong mã, còn chờ test riêng. Hiển thị thông tin và tạm tính qua Bottom Sheet cùng nghiệm thu T02 trên UI đang chờ. |
| 2026-11-27 | Codex đồng nhất tiến độ README và checklist với mã nguồn 915b1a7 | README.md và docs/05-nghiem-thu.md | Đối chiếu ViewModel, luồng Activity–Canvas và unit test hiện có. Ghi rõ phạm vi logic T02 đã có bằng chứng, các ca UI và xoay đang chờ. |
| 2026-11-27 | Codex triển khai Bottom Sheet hai nấc và nối với state hiện có | MainActivity, layout, SelectedSeatAdapter, locateSeat và kiểm thử BookingSheetTest | Build/unit test đã qua. Hai ca tích hợp trên Pixel_7 kiểm tra tổng tiền, xóa/định vị, dựng lại Activity và vùng map/danh sách cuộn ở hướng ngang. Bộ lọc và khôi phục sau khi hệ thống hủy tiến trình còn chờ. |
| 2026-11-27 | Codex triển khai Legend trực tiếp theo tham khảo UX của sinh viên | view_seat_legend.xml, item_seat_legend.xml, setupLegend, màu dùng chung và BookingSheetTest | Build/unit test và ba ca thiết bị đã qua, gồm vùng nhìn dọc/ngang và mở/đóng Chi tiết giữ lựa chọn. Cỡ chữ lớn và bộ lọc cần kiểm tra thêm. |
| 2026-11-27 | Codex triển khai lọc loại/trạng thái và đồng bộ Canvas–Sheet | SeatFilter, state/ViewModel, Activity, Canvas, adapter, layout và kiểm thử | Build, 3 ca unit test bộ lọc và 4 ca tích hợp Sheet trên Pixel_7 đã qua. Xác nhận AND, rỗng, xóa lọc giữ lựa chọn, cập nhật trạng thái sau chọn, nhãn ngoài lọc, Reset View và dựng lại Activity. Ca chữ lớn và hủy tiến trình còn chờ. |
| 2026-11-27 | Codex tối ưu bố cục dọc/ngang và điều hướng theo ảnh tham khảo | Layout ngang, layout Sheet dùng chung, Canvas/minimap, màu, legend và MapNavigationTest | Build/unit test và 6 ca thiết bị đã qua. Đã xem ảnh ngang để xác minh vùng Canvas. Cỡ chữ lớn còn chờ kiểm tra. Góc nhìn 3D được ghi là ý tưởng mở rộng. |
| 2026-11-27 | Codex đồng bộ mẫu chú thích bo góc và chuyển trạng thái ghế sang màu theo yêu cầu sinh viên | MainActivity, CinemaMapView, màu, chuỗi và tài liệu UI | Build và unit test đã qua trước khi commit bản UI/UX. Chưa áp dụng các bảng màu tham khảo; sinh viên chọn bảng màu và kiểm tra trực tiếp sau. |
| 2026-11-27 | Codex commit bản 2D tại 4feba58 trước khi triển khai 3D bước 1 theo yêu cầu làm từng bước | preview/CinemaPreviewActivity, CinemaSceneView, CinemaSceneRenderer, layout, manifest, nút trong Sheet và CinemaPreviewTest | Build/unit test và 7 ca thiết bị trên Pixel_7 đã qua, gồm dựng cảnh, xoay, Home/mở lại và quay lại giữ ghế/lọc/giá. Camera cố định, chưa có ghế 3D; chờ sinh viên dùng thử trước bước 2. |
| 2026-11-27 | Codex hoàn thiện các bước 3D còn lại theo yêu cầu sinh viên tiếp tục đến kết quả cuối | CinemaSceneGeometry, CinemaSceneMesh, camera/renderer/view, Activity, dòng ghế, bố cục ngang riêng, tài liệu và kiểm thử | Build, 9 unit test, lint không có lỗi và 10 ca thiết bị đã qua trên Pixel_7 API 37; đã xem ảnh gần/xa và ngang. QA phát hiện lỗi SavedState khi xoay do cùng ID khác loại View; đã tách ID và chạy lại 4 ca 3D thành công. Phòng giả lập, chưa mô phỏng người/ánh sáng vật lý. Sinh viên chưa nghiệm thu trực tiếp; phần 3D chưa commit. |
| 2026-11-27 | Codex sửa nút 3D chung theo ghế đang xem thay vì chỉ ghế vừa chọn trên sơ đồ | BookingViewModel.inspectSeat, listener dòng danh sách, Activity Result và kiểm thử/tài liệu | Build, 10 unit test, lint và 11 ca thiết bị đã qua. Kiểm tra A01/F08, lọc VIP, xem lại A01 không bỏ chọn; đổi ghế trong 3D rồi quay về đồng bộ thông tin/nút, giữ lựa chọn/lọc/giá. APK đã build lại; sinh viên kiểm tra trên Samsung bằng bản mới. |
| 2026-11-27 | Codex tự dựng trailer hoạt hình khoảng 5 giây và phát trên màn chiếu 3D theo yêu cầu sinh viên | tools/generate_trailer.py, raw/sao_nho_trailer.mp4, CinemaTrailerPlayer, CinemaVideoScreen, GlProgram, renderer/view/Activity và CinemaTrailerTest | Clip tự vẽ bằng hình học/chữ pixel, không lấy cảnh phim/nhạc/nhân vật có sẵn. Đã xác minh 5.000 ms, 1008×432, 24 fps, không âm thanh; build, 10 unit test, lint và 7 ca thiết bị video/3D đã qua. Đã sửa nhiễu coplanar bằng polygon offset, xem ảnh thực tế; còn sinh viên thử trên Samsung. |
| 2026-11-27 | Codex làm màn chiếu cong trên map và triển khai các nâng cấp 3D đã đề xuất theo yêu cầu sinh viên | CinemaMapView; CinemaRoomSpec/Settings; CinemaMiniMapView; SeatComparison; geometry/mesh/renderer/view/Activity; layout/strings; định vị kết quả trong MainActivity; unit/UI test và tài liệu | Build, 14 unit test và 17 ca thiết bị trên Pixel_7 API 37 đã qua; lint 0 lỗi/15 cảnh báo. Đã xem ảnh picker, ghế đôi và toàn màn hình, chỉnh nền vùng an toàn cạnh camera khuyết. Minimap mở map lớn để chọn chính xác; A/B tự pause; cấu hình phòng chỉ là giả lập; bóng/ánh sáng là hiệu ứng nhẹ. Chưa đo hiệu năng trên Samsung; sinh viên chưa nghiệm thu trực tiếp. |
| 2026-11-27 | Codex thay nội dung màn chiếu bằng video lửa trại theo ảnh tham khảo sinh viên cung cấp | tools/generate_campfire.py, raw/campfire_loop.mp4, nguồn raw trong CinemaTrailerPlayer, nhãn phát/đọc màn hình, kiểm thử video/điểm ảnh và tài liệu | Video mô phỏng tự tạo 5 giây, 24 fps, 1008×432, không âm thanh, 612.826 byte; không sao chép pixel HEIC vào APK. Build, 14 unit test, lint 0 lỗi/15 cảnh báo và 11 ca video/3D đã qua trên Pixel_7 API 37. Ca resume ban đầu gặp phiên UI cũ; đã cô lập task và bổ sung kiểm tra trạng thái/ảnh/vị trí phát, chạy lại đạt. Đã xem ảnh thực tế trên màn chiếu; sinh viên còn thử bản APK mới trên Samsung. |
| 2026-11-27 | Codex sửa ghế đôi chỉ có Trái/Phải theo phản hồi sinh viên | SeatEye, hình học/default camera, Activity/SceneView, layout hai nút, nhãn A/B, phục hồi Bundle cũ và kiểm thử | Đã bỏ tâm cặp khỏi lựa chọn UI, mặc định Trái; trạng thái CENTER cũ chuyển Trái, Phải vẫn được phục hồi. Build, 15 unit test và 9 ca 3D trên Pixel_7 API 37 đã qua. Đã xem ảnh hai nút. Sinh viên còn kiểm tra APK trên Samsung. |
| 2026-11-27 | Codex bôi đậm các ý chính trong chú thích và bỏ dòng đếm ghế phù hợp theo yêu cầu sinh viên | strings.xml với span chữ đậm, getText trong dialog, view_seat_filters, collector MainActivity, BookingSheetTest và tài liệu | Đã bỏ dòng kết quả dạng 196/196; giữ thông báo khi không có ghế khớp và ẩn khi có kết quả. Build, 15 unit test, lint không lỗi và 6 ca Sheet/điều hướng đã qua trên Pixel_7 API 37. Đã xem ảnh tiêu đề/tên loại/trạng thái và quy tắc ghế đôi được bôi đậm. Sinh viên còn dùng thử trực tiếp. |
| 2026-11-27 | Codex áp dụng nền trắng, navy chủ đạo và ghế chọn nổi bật theo ảnh bảng màu sinh viên cung cấp | colors/themes, Canvas và minimap, màu chữ ghế, system bars hai Activity, nội dung Legend, ảnh QA và tài liệu | Nền trắng, navy #002366, ghế chọn cobalt #0057FF/chữ trắng; các loại ghế còn lại pastel, đã đặt/không khả dụng xám nhạt. Chữ trên ghế chọn tương phản 5,52:1. Build, 15 unit test, lint 0 lỗi/15 cảnh báo và 10 ca UI trên Pixel_7 API 37 đã qua. Đã xem ảnh Sheet, ghế chọn và dialog trắng–navy. Còn sinh viên kiểm tra trên Samsung. |
| 2026-11-27 | Codex chuyển tổng tiền sang phải, đổi tên thành Rạp HCMUS và bỏ chữ Suất giả lập khỏi dòng thông tin | view_booking_sheet, chuỗi, collector MainActivity tách số ghế/chỗ và tổng tiền, cập nhật kiểm thử Sheet và tài liệu | Build, 15 unit test, lint không lỗi và 6 ca Sheet/điều hướng trên Pixel_7 API 37 đã qua. Đã xem ảnh số ghế/chỗ ở trái, tiền ở phải, thông tin HCMUS và nút Xem ghế ở hàng dưới. Dữ liệu nghiệp vụ vẫn là fixture bài tập; sinh viên còn dùng thử APK. |
| 2026-11-27 | Codex tăng nhẹ độ đậm các nền ghế theo ảnh phản hồi sinh viên | colors.xml và bảng màu/UI docs | Thường #CDDFF8, VIP #D5C8EC, Đôi #BFE3DB, Đã đặt #ADBBD0, Không khả dụng #D9E1EC; màu chọn vẫn cobalt #0057FF. Build/lint đã qua, ca Sheet có sẵn chạy thành công trên Pixel_7 API 37; đã xem ảnh các ghế rõ hơn trên nền trắng. Không thêm test cho thay đổi màu; sinh viên còn dùng thử trên Samsung. |
| 2026-11-27 | Codex phân biệt rõ Đã đặt và Không khả dụng theo phản hồi sinh viên | colors.xml, mô tả Legend và bảng màu UI | Đã đặt đổi sang xám đậm trung tính #9CA8B8, Không khả dụng sang cam đất nhạt #F3C9AD, chữ navy đạt tương phản lần lượt 5,89:1 và 9,32:1. Màu dùng chung cho Canvas/Legend/minimap/3D. Build/lint và ca Sheet hiện có đã qua, đã xem ảnh trạng thái khác sắc rõ. Chưa thử trực tiếp Samsung. |
| 2026-11-27 | Codex cho mã ghế luôn hiện, giảm nhẹ cỡ chữ và dùng chữ cùng tông nền đậm hơn theo yêu cầu sinh viên | drawSeat/Paint trong CinemaMapView, 5 màu chữ tài nguyên và tài liệu | Bỏ ngưỡng zoom ẩn mã; chữ cỡ 14 world, tự vừa bề rộng và căn giữa theo font. Thường/VIP/Đôi/Đã đặt/Không khả dụng có màu chữ theo tông riêng, tương phản khoảng 4,63–5,02:1; ghế chọn giữ chữ trắng trên cobalt. Build/lint và 3 ca UI hiện có đã qua trên Pixel_7 API 37; đã xem ảnh dọc toàn phòng và ngang định vị. Còn sinh viên dùng thử trên Samsung. |
| 2026-11-27 | Codex giảm cỡ chữ trong ô ghế xuống 80% mức hiện tại theo yêu cầu sinh viên | CinemaMapView và tài liệu UI/kiến trúc | Cỡ chữ giảm 14 → 11,2 world; giữ màu theo từng tông, căn giữa và luôn hiện mã. Build/lint cùng ca Sheet có sẵn đã qua trên Pixel_7 API 37; đã xem ảnh cỡ chữ mới. Không thêm test cho thay đổi cỡ chữ; còn sinh viên dùng thử thiết bị thật. |
| 2026-11-27 | Codex bỏ viền ô ghế theo phản hồi sinh viên | CinemaMapView.drawSeat, mẫu màu setupLegend, mô tả chú thích và tài liệu UI | Ô ghế và mẫu chú thích chỉ tô nền bo góc, giữ màu/chữ và vòng định vị. Build/lint đã qua (0 lỗi/15 cảnh báo), 5 ca thiết bị gồm 3 phép nghiệm thu 2D tạm ngoài repo và 2 ca Sheet/chú thích có sẵn đã qua trên Pixel_7 API 37. Đã xem ảnh bản mới; sinh viên còn dùng thử trực tiếp. |
| 2026-11-28 | Codex sửa các điểm audit, làm gọn mã/comment, tối ưu trước commit và hoàn thiện tài liệu theo đề | MainActivity, CinemaMapView, BookingViewModel, CinemaTrailerPlayer, test hình học/state/gesture/pixel/video, tools/profile_map.py, README và docs/01–09; mã chốt f3e32f9 | 20 unit test và 21 ca thiết bị đạt; lint 0 lỗi/15 cảnh báo; build APK app/test, unit test và lint từ clone sạch đều đạt. Cache metrics chữ/cull ngoài viewport được giữ; bỏ thử nghiệm Picture/RenderNode vì không có bằng chứng tốt hơn. Mẫu gfxinfo emulator vẫn trễ nhiều frame, chưa tự chốt độ mượt; kết quả/log tại docs/09. Video demo và dùng thử Samsung do sinh viên thực hiện sau; chưa có xác nhận thay cho sinh viên. |

Nhật ký trên giữ lịch sử theo từng mốc; kết quả mới nhất và giới hạn nằm trong tài liệu 09. “Sinh viên còn dùng thử” không được thay bằng xác nhận kiểm tra của sinh viên nếu chưa có phản hồi thực tế.

## Tài liệu đầu vào

Đề thi giữa kỳ 5 trang `De_thi_giua_ky_CNTN2024.pdf` do sinh viên cung cấp, trọng tâm mục 2–3, 5–8. Ảnh chọn ghế MoMo và một app đặt vé khác chỉ tham khảo cấu trúc UI/UX; tài nguyên và dữ liệu của CinemaMap tự tạo. Ảnh lửa trại HEIC chỉ tham khảo bố cục/màu, không đóng gói ảnh vào APK.
