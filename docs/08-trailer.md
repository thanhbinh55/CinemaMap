# Video lửa trại tự tạo trên màn chiếu 3D

## Nội dung và nguồn tài nguyên

**Lửa trại** là clip mô phỏng dài 5 giây: củi dựng hình chóp, ngọn lửa cam/vàng chuyển động, than hồng và tàn lửa bay lên trên nền đêm tối. Toàn bộ hình ảnh được tạo bằng mã trong `tools/generate_campfire.py`. Ảnh `IMG_5257.HEIC` do sinh viên cung cấp được dùng để tham khảo bố cục/màu; không đưa pixel ảnh, người xuất hiện trong ảnh hoặc tệp HEIC vào APK.

Không tải footage, nhân vật, mô hình, nhạc, font hay logo bên ngoài. Clip không có âm thanh, không chèn chữ. Đây là hình ảnh mô phỏng bằng mã do Codex tạo theo yêu cầu sinh viên, không phải video quay trực tiếp. Clip này thay nội dung Sao Nhỏ trước đó trên màn chiếu; script cũ `tools/generate_trailer.py` được giữ làm lịch sử nguồn, MP4 cũ đã bỏ khỏi tài nguyên đóng gói.

Đây là nội dung chiếu bên trong app, không thay thế video demo 1–5 phút của hồ sơ nộp bài.

- Tệp đóng gói: `app/src/main/res/raw/campfire_loop.mp4`.
- Thời lượng: 5.000 ms, 120 frame ở 24 fps.
- Kích thước: 1008 × 432, tỷ lệ 21:9, khớp màn chiếu 12,6 × 5,4 của phòng giả lập.
- Codec: H.264 Baseline, yuv420p, MP4; không có track âm thanh.
- Dung lượng: 612.826 byte.
- Android đọc tài nguyên raw trong APK; không cần mạng hoặc quyền truy cập bộ nhớ dùng chung.

## Tạo lại clip

Cần Python 3, NumPy, Pillow và ffmpeg có encoder libx264 trên máy phát triển. Các công cụ này không phải phụ thuộc runtime của app. Chạy từ gốc dự án:

```sh
python3 tools/generate_campfire.py
ffprobe -v error -show_entries format=duration:stream=codec_name,width,height,r_frame_rate -of json app/src/main/res/raw/campfire_loop.mp4
```

Script render 120 frame, đưa RGB qua pipe cho ffmpeg rồi mã hóa MP4. Trường lửa gồm các cột lửa uốn theo chiều cao và noise nhiều lớp chuyển lên trên; màu chuyển từ đỏ/cam sang vàng ở vùng nóng. Glow được làm mờ và cộng vào nền, củi/than/tàn lửa vẽ riêng. Noise dịch đúng một số chu kỳ trong 5 giây; tàn lửa mờ dần trước khi quay về điểm sinh. Seed cố định giúp tái tạo kết quả. Không cần tải tài nguyên ngoài. Có thể dùng `--preview /tmp/campfire.png` để xuất một frame kiểm tra.

## Hành vi trong app

- Khi mở góc nhìn 3D, video lửa trại tự phát và lặp lại mỗi 5 giây.
- Nút **Tạm dừng phim / Phát video** cho phép so sánh các ghế bằng cùng một khung hình.
- Đổi ghế hoặc kéo nhìn quanh không tạo lại MediaPlayer và không đưa clip về đầu.
- Ra nền lưu timestamp rồi giải phóng decoder, bỏ Surface đang gắn và vô hiệu điều khiển tới khi sẵn sàng. Quay lại, GL thông báo Surface hợp lệ; tạo decoder mới, seek về mốc lưu rồi áp ý định phát/pause. Bỏ callback từ decoder cũ.
- Xoay/dựng lại Activity lưu vị trí phát và ý định phát/tạm dừng trong Bundle. Clip được seek về vị trí lưu; việc khôi phục có thể lệch nhẹ theo frame giải mã.
- Đóng màn hình 3D giải phóng MediaPlayer và SurfaceTexture/Surface. Nếu video không tải được, mặt màn chiếu gradient vẫn còn và nút báo chưa phát được phim; điều hướng ghế 3D tiếp tục dùng được.
- Trailer không chọn/bỏ chọn vé, không đổi bộ lọc hoặc tổng tiền.
- Ghim/chuyển A/B tự tạm dừng video để giữ cùng khung hình khi so sánh. Không tự phát lại khi xoá ghim; nút Phát video cho người dùng tiếp tục. Mở toàn màn hình giữ trạng thái phát, đổi cấu hình phòng khôi phục tiến trình và ý định phát/pause qua dựng lại Activity.

## Luồng code

```text
raw/campfire_loop.mp4
    → CinemaTrailerPlayer / MediaPlayer
    → Surface do CinemaVideoScreen cung cấp
    → SurfaceTexture / texture GL_TEXTURE_EXTERNAL_OES
    → shader video + ma trận texture
    → mặt màn chiếu trong không gian 3D
    → camera/phối cảnh của ghế hiện tại
```

| Thành phần | Trách nhiệm |
| --- | --- |
| `CinemaTrailerPlayer` | Quản lý decoder trên UI thread, prepare bất đồng bộ, loop, pause, seek và giải phóng |
| `CinemaVideoScreen` | Tạo external texture, nhận frame mới, cập nhật texture trên GL thread và vẽ một mặt chữ nhật |
| `CinemaSceneRenderer` | Vẽ phòng/gradient dự phòng, video rồi ghế. Video nằm đúng tọa độ màn chiếu và dùng cùng ma trận camera |
| `CinemaSceneView` | requestRender khi có frame mới, công bố Surface sau resume/context mới; giải phóng Java resource sau khi GL thread kết thúc |
| `CinemaPreviewActivity` | Nối lifecycle, nút phát/tạm dừng và lưu/khôi phục vị trí phát |
| `GlProgram` | Dùng chung phần biên dịch/link shader và kiểm tra lỗi cho phòng/ghế và video |

Video không phải một VideoView đè lên giao diện. Vì là texture trong thế giới 3D, màn hình nghiêng/xa/gần đúng theo camera, ghế có thể che đúng phần nằm trước nó. Ma trận từ SurfaceTexture xử lý hướng/cắt vùng của frame decoder; cập nhật nó mỗi khi nhận frame.

`RENDERMODE_WHEN_DIRTY` vẫn được giữ: video đang phát cung cấp frame mới để yêu cầu vẽ lại. Khi tạm dừng, không có vòng requestRender tự chạy. Mô hình phòng/ghế không dựng lại ở mỗi frame phim. Callback frame chỉ đặt cờ AtomicBoolean và yêu cầu render, không cập nhật texture trên UI thread.

## Kiểm chứng

`CinemaTrailerTest` kiểm tra metadata 5 giây/độ phân giải/không âm thanh và điểm ảnh thật ở giữa mặt màn chiếu: có chuyển động khi phát, đứng yên khi tạm dừng, giữ trạng thái pause qua dựng lại, phát lại sau ra nền và tiếp tục chuyển động sau một chu kỳ 5 giây. `CinemaPreviewTest` tiếp tục kiểm tra góc nhìn gần/xa, đổi ghế, xoay và luồng đặt ghế.

Mốc Sao Nhỏ trước đó đã qua build, 10 unit test, lint và 7 ca video/3D trên Pixel_7 API 37. QA khi đó phát hiện video/mặt gradient đồng phẳng gây nhiễu depth buffer 16-bit; đã thêm polygon offset cho lớp video. Mốc lửa trại được ghi riêng trong nhật ký AI/kiểm chứng bên dưới.

Giới hạn: lửa là mô phỏng procedural, không phải footage; không có âm thanh hoặc ánh sáng động từ video hắt ra phòng. Cần sinh viên kiểm tra trực tiếp trên Samsung; kiểm thử emulator không thay thế toàn bộ driver/codec thiết bị thật.

### Mốc lửa trại ngày 27/11/2026

- Build Debug, 14 unit test và lint đã qua; lint có 0 lỗi/15 cảnh báo hiện có.
- Pixel_7 API 37: [11 ca thiết bị đã qua](evidence/2026-11-27-campfire/device-tests.txt): 2 video, 5 luồng 3D, 4 nâng cấp 3D. Đã kiểm tra chuyển động thật trên màn chiếu, pause, dựng lại, ra nền/mở lại, loop, A/B, ghế đôi, xoay và cấu hình phòng.
- [Metadata MP4](evidence/2026-11-27-campfire/video-metadata.json) xác nhận 5 giây, 120 frame, 24 fps, 1008×432, H.264 Constrained Baseline, không có track âm thanh. Kích thước 612.826 byte.
- Kiểm tra nguồn render tại 0/5 giây lệch tối đa 1 mức màu do làm tròn; chênh trung bình giữa frame cuối và đầu khoảng 0,73/255 trên toàn ảnh. Các thành phần chuyển động đều tuần hoàn, không có cảnh cắt/tiêu đề ở điểm nối.
- Đã xem [ảnh video đang phát trong app](evidence/2026-11-27-campfire/in-app.png). Ảnh tham khảo HEIC không được đóng gói.
- Ca vòng đời ban đầu chưa đạt: ảnh chụp gặp phiên 3D cũ đang pause, trong khi ca đo một Activity khác. Đã thêm phiên task riêng cho ca video, chờ cả trạng thái mong muốn và vùng cảnh sẵn sàng, giữ ảnh/vị trí phát để chẩn đoán. Ca video và toàn bộ 11 ca liên quan đã được chạy lại thành công. Không hạ ngưỡng đo chuyển động để bỏ qua lỗi.

Tham khảo API: [SurfaceTexture](https://developer.android.com/reference/android/graphics/SurfaceTexture), [MediaPlayer](https://developer.android.com/reference/android/media/MediaPlayer), [GL_TEXTURE_EXTERNAL_OES](https://developer.android.com/reference/android/opengl/GLES11Ext).

## Kiểm chứng bản chốt

Lượt audit trước sửa ghi nhận 16/17 ca đạt; ca video có lần đứng hình sau recreate/ra nền, chạy riêng lại đạt nên không coi là ổn định. Bản chốt đổi sang giải phóng decoder khi ra nền, tạo lại theo Surface hiện tại; giữ vị trí phát và yêu cầu play/pause. Ca hồi quy kiểm tra cả timestamp và điểm ảnh sau loop, thêm chu kỳ ra nền thứ hai. Kết quả chạy mới nhất, thay cho các số lịch sử ở trên, nằm tại [09 — Kiểm chứng](09-kiem-chung.md).
