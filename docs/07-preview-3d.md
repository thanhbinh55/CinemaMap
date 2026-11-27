# Góc nhìn từ ghế 3D

Đây là cải tiến bổ sung của CinemaMap. Sơ đồ 2D bắt buộc tiếp tục được vẽ trên một Canvas. Sau bản cảnh cố định bước 1, sinh viên yêu cầu triển khai tiếp các bước đến luồng hoàn chỉnh; phạm vi hiện tại là phòng 3D giả lập và xem từ từng ghế.

## Chạy và trải nghiệm

1. Chạm một ghế trên sơ đồ, mở **Xem ghế**, chọn **Xem góc nhìn 3D · mã ghế**. Ghế đã đặt/không khả dụng cũng có thể xem khi ghế đó đang được kiểm tra thông tin.
2. Chạm tên một ghế đã chọn trong danh sách để định vị và xem lại thông tin: nút 3D chung đổi theo đúng ghế đó, không bỏ chọn ghế. Hoặc chọn nút **3D** ngay trong một dòng, kể cả dòng nằm ngoài bộ lọc.
3. Màn hình 3D hiển thị đúng ID, loại, số chỗ và trạng thái ghế. Ghế đơn đặt camera ở tâm chỗ ngồi, hướng về màn chiếu. Ghế đôi chỉ có **Trái / Phải**, mặc định Trái; đổi chỗ trả hướng nhìn về màn chiếu.
4. Kéo một ngón để nhìn trái/phải 360° hoặc lên/xuống trong giới hạn. Thao tác chỉ đổi hướng nhìn, không di chuyển chỗ ngồi và không phóng to.
5. **Nhìn màn chiếu** đặt lại hướng nhìn tại cùng ghế. Nút bị vô hiệu khi đang ở hướng mặc định.
6. **Đổi ghế xem** → chọn hàng → chọn ghế để so sánh. Có đủ 196 ID, gồm ghế đôi/đã đặt/không khả dụng. Đổi ghế xem không chọn/bỏ ghế đặt vé, không đổi bộ lọc hoặc tổng tiền; hướng nhìn trở lại màn chiếu tại ghế mới.
7. Xoay dọc/ngang hoặc Home/mở lại. ID đang xem và góc nhìn được giữ khi Activity dựng lại; shader và buffer được tạo lại nếu context GL bị mất.
8. **Về sơ đồ** hoặc Back hệ thống trả về phần đặt ghế. Ghế xem cuối cùng trong 3D trở thành ghế đang xem thông tin, cập nhật nhãn nút 3D chung và được định vị trên Canvas. Lựa chọn, bộ lọc và giá không đổi, kể cả khi ghế xem nằm ngoài bộ lọc. Camera 2D qua xoay Activity vẫn theo giới hạn hiện có của phần 2D.
9. Minimap góc phải thể hiện vị trí mắt và hướng nhìn. Chạm minimap mở sơ đồ lớn, kéo/phóng to và chạm ghế để đổi góc nhìn; không thay lựa chọn đặt vé. Cách chọn hàng/ghế bằng dialog vẫn dùng được.
10. **Ghim A/B** lưu ID ghế và chỗ trái/phải, tự tạm dừng trailer và trả hướng về màn chiếu. Chạm A/B để chuyển giữa hai vị trí, giữ nút để ghim lại. Menu ⋮ có **Xoá ghim A/B**. Ghế đang xem/pins/pause được giữ qua dựng lại Activity.
11. Menu ⋮ → **Toàn màn hình** ẩn thanh tiêu đề và panel điều khiển, giữ minimap. **Hiện điều khiển** hoặc Back đưa lại bố cục thường; Back lần tiếp theo mới rời 3D. Xoay khi toàn màn hình vẫn giữ chế độ và camera. Android có thể hiện hướng dẫn immersive lần đầu sử dụng.
12. Menu ⋮ → **Cấu hình phòng giả lập** chỉnh thông số bằng thanh trượt, chọn bật/tắt hiệu ứng ánh sáng. Huỷ giữ nguyên, Áp dụng dựng lại phòng với thông số mới, Mặc định phục hồi phòng mẫu. Thông số được lưu trên máy, giữ ghế/pins/video khi dựng lại.

Hướng dọc đặt thông tin và các nút bên dưới cảnh. Hướng ngang dành 70% chiều rộng cho cảnh, 30% cho thông tin/nút ở bên phải; panel cuộn được khi chữ lớn. Cảnh giữ toàn bộ chiều cao dưới thanh tiêu đề, không bị phần điều khiển che mất các hàng ghế.

Màn chiếu có [video lửa trại tự tạo](08-trailer.md), phát offline, lặp lại mỗi 5 giây, không âm thanh. Nút thứ ba **Tạm dừng phim / Phát video** điều khiển video; đổi ghế không đưa phim về đầu. Tạm dừng giúp so sánh các ghế bằng cùng khung hình.

## Phòng giả lập và quy tắc hình học

- `CinemaSceneGeometry` nhận cùng `MockCinemaData.rows` và dùng `MapGeometry` làm nguồn bố cục. Không khai báo thêm danh sách ID ghế riêng.
- X trái/phải, Y hướng lên, Z từ màn chiếu đến cuối phòng. Đơn vị 3D được quy ước là mét giả lập.
- `CinemaRoomSpec` mặc định: rộng phòng 19,12 m, rộng màn chiếu 12,6 m, hàng đầu cách 4 m, khoảng hàng 1,12 m, bậc 0,18 m, độ cao mắt 1,15 m. Có kiểm tra miền giá trị trước khi đưa vào renderer.
- Tâm X ghế: `(left + width / 2 - centerX) × xScale`, với `xScale = (roomWidth - 2) / map.width`. Chiều rộng ghế dùng `width × xScale`. Mặc định xScale = 0,02: ghế thường/VIP 0,72 m, ghế đôi 1,56 m, lối giữa 0,88 m.
- Tâm Z hàng: `firstRowDistance + (row.top - firstRow.top) / 40 × rowSpacing`, giữ khoảng tách khu của sơ đồ. Mặc định hàng A ở Z = 4; hàng L ở khoảng Z = 17,328.
- Cao độ hàng: `0,08 + rowIndex × rowRise`. Sàn thành các bậc liên tiếp, giữa hai tâm hàng là ranh giới bậc.
- Camera: ghế đơn dùng X/Z ở tâm ghế, Y bằng cao độ hàng + eyeHeight. Ghế đôi chỉ dùng hai chỗ Trái/Phải lệch X bằng `±seat.width/4` so với tâm cả cặp, giữ Y/Z; mặc định Trái. Ghế đơn bỏ qua lựa chọn side. Mỗi vị trí mắt tự tính hướng về tâm màn chiếu.
- Màn chiếu giữ tỷ lệ 21:9 để video không méo: `height = width × 3/7`; mép dưới cao 1,3 m. Mặc định màn chiếu cao 5,4 m, tâm `(0; 4; 0,12)`, phòng sâu khoảng 19,528 m và cao 7,6 m. Chiều cao phòng tăng nếu cần chứa màn chiếu lớn.
- Màu ghế lấy cùng tài nguyên với sơ đồ 2D; snapshot ID đã chọn được gửi qua Intent. Ghế đang đứng được ẩn mô hình của chính nó để tránh camera nhìn xuyên tựa lưng. Các ghế khác giữ đúng vị trí và trạng thái.

Kích thước là giả định phục vụ demo, chưa được hiệu chuẩn với rạp thật. Không suy ra tầm nhìn, độ che khuất của khán giả hoặc chất lượng ghế thực tế từ mô hình này. Không có mô hình người ngồi.

## Camera và góc nhìn

Khi vào hoặc đổi ghế, lấy vector từ mắt đến tâm màn chiếu. Từ đó tính yaw/pitch mặc định. Thao tác kéo cộng góc tương đối, giữ nguyên vị trí mắt.

- Yaw xoay đủ 360°, chuẩn hóa trong khoảng `[-180°, 180°)`.
- Pitch tương đối giới hạn ±45°; hướng cuối cùng không vượt ±75° để tránh lật camera.
- Trường nhìn ngang cố định 75° cho mọi ghế và hai hướng máy. Trường nhìn dọc tính từ tỷ lệ viewport; không kéo méo khung hình.
- Không tự fit màn chiếu theo từng ghế, vì như vậy sẽ làm mất khác biệt gần/xa. Ghế sát màn chiếu có thể thấy màn chiếu vượt khỏi vùng nhìn; kéo để quan sát các phía.
- Nút reset chỉ trả yaw/pitch về 0; đổi ghế tạo trạng thái camera mới với ID mới và góc 0.
- Trạng thái cũ lưu `CENTER` hoặc chưa có side được `SeatEye.fromSavedValue()` chuyển sang Trái; chỗ Phải vẫn được giữ khi khôi phục. Tâm cả cặp chỉ phục vụ phép tính hình học, không là lựa chọn UI.
- A/B lưu vị trí ghế và side với yaw/pitch bằng 0. Hai vị trí cùng FOV, cùng phòng và video đang tạm dừng; không mở hai renderer cùng lúc. Sau khi người dùng chủ động phát trailer, lần chuyển A/B tiếp theo lại tạm dừng.

## Đọc code theo luồng

| Tệp/hàm | Vai trò |
| --- | --- |
| `BookingViewModel.inspectSeat()` | Chỉ thay ghế đang xem, giữ lựa chọn/lọc/giá; dùng cho dòng danh sách và kết quả xem 3D |
| `MainActivity.openSeatPreview()` | Đồng bộ ghế đang xem, gửi ID và snapshot ID đã chọn qua Activity Result Launcher |
| `MainActivity.previewLauncher` | Nhận ID xem cuối từ 3D, đối chiếu fixture, cập nhật ghế đang xem thông tin và định vị sau layout |
| `SelectedSeatAdapter.onPreview` | Nút 3D của từng dòng gửi đúng ID, độc lập với ghế vừa được chạm trên Canvas |
| `CinemaPreviewActivity.onCreate()` | Khôi phục ID/góc từ Bundle, kiểm tra ID và OpenGL ES, gắn surface và các nút |
| `showRowChooser()` | Chọn hàng/ghế xem trong dialog; không gọi BookingViewModel |
| `updateSeatSummary()/setResult()` | Ghi ID xem hiện tại vào kết quả trả về, áp dụng cả nút Về sơ đồ và Back hệ thống |
| `onSaveInstanceState()` | Lưu ID, side, yaw/pitch, pins A/B, toàn màn hình, thông số phòng và video; ưu tiên hơn Intent khi dựng lại |
| `CinemaSceneGeometry` | Ánh xạ bố cục 2D → vị trí ghế 3D, vị trí mắt và hướng nhìn |
| `CinemaRoomSpec` / `CinemaRoomSettings` | Cấu hình hình học, xác thực giá trị, thanh trượt, SharedPreferences và Bundle |
| `CinemaMiniMapView.updateCamera()/onDraw()` | Đồng bộ vị trí mắt/nón nhìn trên sơ đồ nhỏ; chỉ báo điều hướng, chạm mở map lớn |
| `showMapPicker()` | Dùng lại Canvas 2D với highlight ghế hiện tại; callback đổi ID xem rồi đóng dialog |
| `SeatComparison.pin()` / `compare()` | Lưu hai vị trí, đặt góc về 0 và tạm dừng phim trước khi đổi camera |
| `setFullscreen()` | Áp ConstraintSet cho vùng cảnh, ẩn/hiện panel và system bars, giữ renderer/camera |
| `SeatCameraState.lookBy()` | Trạng thái camera bất biến, chuẩn hóa góc và xử lý số không hữu hạn |
| `CinemaSceneMesh.room()/chairs()/shadows()` | Dựng buffer phòng/bậc/ghế/bóng tiếp xúc; hiệu ứng bổ sung tắt được bằng cấu hình |
| `CinemaSceneView.onTouchEvent()` | Chuyển kéo một ngón qua GestureDetector; hủy chuỗi kéo khi có nhiều ngón để tránh nhảy góc |
| `setCamera()` | Cập nhật state trên UI, đưa snapshot sang luồng GL bằng queueEvent rồi requestRender |
| `CinemaSceneRenderer.onSurfaceCreated()` | Tạo shader, phòng và ghế lại theo context GL hiện tại |
| `updateCamera()` | Đổi view matrix; chỉ tạo lại buffer ghế khi đổi ID xem, không dựng lại mô hình khi kéo |
| `onSurfaceChanged()` | Cập nhật viewport và phối cảnh, nhân projection × view |
| `onDrawFrame()` | Xóa color/depth buffer rồi vẽ tam giác của phòng và ghế; báo sẵn sàng/lỗi |
| `Activity.onPause()/onResume()` | Dừng/khôi phục surface; không để GL chạy liên tục khi app ở nền |

Vertex của phòng/ghế chứa XYZ và RGB. Vertex shader nhân đỉnh với ma trận camera/phối cảnh, thêm sắc sáng theo khoảng cách đến màn chiếu khi bật enhancedLighting. Fragment shader gán màu nội suy. Depth buffer quyết định bề mặt gần che bề mặt xa. Khối ghế có độ sáng khác nhau theo mặt; có thêm miếng tựa đầu và ellipse màu chuyển nhẹ trên sàn để gợi bóng tiếp xúc. Renderer vẽ phòng → bóng với polygon offset → video → ghế. Không có shadow map, bóng người, PBR hay ánh sáng động theo nội dung trailer. Shader video có thêm tọa độ UV và ma trận từ SurfaceTexture; chi tiết tại tài liệu trailer.

## Hiệu năng và vòng đời

Dùng Android Views/XML, GLSurfaceView và OpenGL ES 2.0; không thêm engine, tải mạng, mô hình hoặc hình ảnh của bên thứ ba. Room FloatBuffer được tạo theo context, chair/shadow buffer theo context và khi đổi ID ghế xem. Kéo hoặc đổi side chỉ tính lại camera bằng các ma trận tái sử dụng. `RENDERMODE_WHEN_DIRTY` vẽ theo thay đổi camera hoặc frame video mới, không có vòng vẽ độc lập; tạm dừng phim thì cảnh đứng yên không vẽ liên tục. Không làm lại nhãn ghế trên UI trong mỗi sự kiện kéo. Chưa đo frame time trên nhiều thiết bị.

Có thông báo và đường quay lại nếu ID không hợp lệ, thiết bị không hỗ trợ hoặc renderer báo lỗi shader/GL. Logcat dùng nhãn `CinemaPreview`. Dữ liệu xem trước không ghi vào state đặt vé. Bundle giữ camera 3D; phần 2D dùng SavedStateHandle để phục hồi ID/lọc/ghế xem và tính lại tổng theo fixture.

## Kiểm chứng

| Ca | Phạm vi tự động |
| --- | --- |
| P01 | Unit test ánh xạ đủ 196 ID, đối xứng trái/phải, lối giữa, ghế đôi, cao độ và chiều sâu |
| P02 | Unit test hướng mặc định của từng camera trong 196 ghế trỏ về tâm màn chiếu |
| P03 | Unit test kéo không đổi mắt, giới hạn pitch, yaw nhiều vòng, số không hữu hạn |
| P04 | Mở A01 khi đang chọn A01/lọc VIP; đổi sang L04 không khả dụng qua dialog |
| P05 | Kéo thật trên surface, xoay giữ ID/góc, bấm reset đúng hướng mặc định |
| P06 | Home/mở lại và quay về vẫn giữ A01, lọc VIP, tổng 85.000đ |
| P07 | So sánh điểm ảnh từ A09 và L04: màn chiếu từ ghế trước lớn hơn rõ rệt; dựng lại Activity giữ ghế/góc |
| P08 | Nút 3D của dòng A01 mở A01 dù ghế đang xem trên sơ đồ là F08 |
| P09 | ID Z99 hiển thị lỗi và nút quay lại, không mở camera sai |
| P10 | Chọn A01/F08, lọc VIP; chạm dòng A01 đổi thông tin/nút 3D sang A01, dựng lại vẫn đúng; mở 3D và quay về giữ cả hai ghế cùng tổng 195.000đ |
| P11 | Đổi A01 → L04 trong 3D, quay về nút chung/thông tin theo L04; lựa chọn A01, lọc VIP và tổng 85.000đ giữ nguyên |
| P12 | Ghim F09 vào A, L04/Phải vào B; chuyển A/B tạm dừng phim, dựng lại giữ pins/side; giữ nút để ghim lại |
| P13 | Chạm minimap mở Canvas lớn, chạm thật vào bounds L04 không khả dụng đổi góc nhìn; chuyển chỗ Trái cập nhật camera/minimap |
| P14 | Toàn màn hình → xoay ngang giữ camera/side và vùng cảnh; Back trả panel vào đúng vùng bên phải |
| P15 | Chỉnh rộng phòng lên 24 m, Áp dụng tạo geometry mới, giữ ID/pin A/pause và lưu cấu hình trên máy |
| P16 | Unit test vị trí mắt ghế đôi đối xứng và nhìn tâm màn chiếu; cấu hình đổi hình học giữ ID/tỷ lệ video/FOV, chặn NaN; A/B chuẩn hóa hướng và thay từng slot |

Bộ thiết bị gồm bốn ca `CinemaPreviewUpgradeTest`, năm ca `CinemaPreviewTest`, hai ca `CinemaTrailerTest`, bốn ca `BookingSheetTest`, hai ca `MapNavigationTest`. Kiểm tra ảnh surface có nội dung màn chiếu và bề mặt phòng, không chỉ kiểm tra TextView sẵn sàng. Với clip lửa trại nền tối, ca gần/xa đếm vùng lửa cam và loại vùng minimap khỏi phép đo, thay cho đếm nền trắng của clip Sao Nhỏ cũ. Sinh viên nên thử thêm chữ lớn, thiết bị thật và các mức cấu hình khác nhau. Không xem đây là chứng nhận trải nghiệm trên mọi thiết bị.

Đã chạy trên Pixel_7, Android API 37: 14 unit test (trong đó 8 ca hình học/camera/A-B), 17 ca thiết bị, build và lint Debug. Lint có 0 lỗi, 15 cảnh báo, gồm toolchain/dependency, ID panel khác nhau có chủ đích giữa hai hướng, gợi ý KTX và accessibility Canvas hiện có. Không nâng dependency trong đợt sửa này. Ca 3D còn kiểm tra chuỗi hai ngón rồi nhấc một ngón không làm nhảy camera; chuỗi kéo một ngón tiếp theo vẫn hoạt động. Đã kiểm tra đồng bộ ghế qua dòng danh sách, dựng lại Activity và kết quả trả về từ 3D, kể cả vòng định vị L04 ngoài bộ lọc VIP. Kết quả thuộc workspace ngày 27/11/2026; kiểm tra trực tiếp Samsung còn chờ sinh viên. Kết quả chốt mới nhất xem tài liệu 09.

Minh chứng: [kết quả 17 ca thiết bị](evidence/2026-11-27-3d-upgrade/device-tests.txt), [picker và màn chiếu cong](evidence/2026-11-27-3d-upgrade/3d-upgrade-picker.png), [cảnh toàn màn hình](evidence/2026-11-27-3d-upgrade/3d-upgrade-fullscreen.png). Sau khi xem ảnh, đã chỉnh nền vùng an toàn cạnh camera khuyết cho khớp màu cảnh và [chạy lại ca toàn màn hình/xoay/Back thành công](evidence/2026-11-27-3d-upgrade/fullscreen-final.txt). Ảnh toàn màn hình được chụp lại sau chỉnh sửa cuối này.

Trong QA bố cục ngang, đã phát hiện `ClassCastException` khi dùng cùng ID cho LinearLayout dọc và ScrollView ngang: Android khôi phục SavedState theo ID vào sai loại View. Đã tách ID vùng ScrollView ngang thành `preview_controls_scroll`; các nút/nhãn cùng loại vẫn giữ ID chung. Ca xoay kiểm tra cả state camera và vùng cảnh không bị panel che.

Lệnh build/check: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew --offline :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest :app:lintDebug`. APK test được cài cùng APK app và chạy bằng AndroidJUnitRunner trên emulator. Kết quả kiểm chứng thuộc workspace đang sửa sau mốc 2D `4feba58`; đây là mô tả mốc trước khi chốt commit 3D; kết quả hiện tại xem [09 — Kiểm chứng](09-kiem-chung.md).

Tài liệu tham khảo: [GLSurfaceView](https://developer.android.com/reference/android/opengl/GLSurfaceView), [vẽ hình OpenGL ES](https://developer.android.com/develop/ui/views/graphics/opengl/draw), [ma trận Android](https://developer.android.com/reference/android/opengl/Matrix).

Luồng trả ID ghế dùng [Activity Result API](https://developer.android.com/training/basics/intents/result), chỉ đồng bộ ngữ cảnh xem thông tin.

### Sửa vị trí ghế đôi theo phản hồi sinh viên

Ghế đôi chỉ có hai chỗ Trái/Phải; điểm giữa cả cặp trước đây là tâm hình học và đã được bỏ khỏi lựa chọn camera. `SeatEye` chỉ còn hai giá trị, mặc định Trái. Ghế đơn vẫn dùng tâm của chính nó. Ghim A/B chỉ thêm nhãn chỗ khi ID là ghế đôi. SavedState cũ lưu CENTER hoặc thiếu side được chuyển sang Trái, còn RIGHT được giữ đúng.

Đã qua 15 unit test (thêm ca phục hồi side cũ) và [9 ca 3D](evidence/2026-11-27-readability/couple-tests.txt), gồm mặc định Trái, chuyển Phải rồi Trái, ghim A/B, dựng lại, xoay và quay về sơ đồ. [Ảnh kiểm chứng hai lựa chọn](evidence/2026-11-27-readability/couple-left-right.png).

## Vòng đời tại bản chốt mã

Khi Activity ra nền, bộ phát lưu timestamp/ý định phát, release decoder, bỏ Surface đang gắn và báo chưa sẵn sàng. Quay lại, renderer công bố Surface hợp lệ, player tạo decoder mới, seek và áp ý định phát/pause. Callback lỗi từ decoder đã thay bị bỏ qua. Đây là biện pháp tránh tái sử dụng decoder gắn Surface cũ; kiểm chứng hồi quy và giới hạn nằm trong [09](09-kiem-chung.md).
