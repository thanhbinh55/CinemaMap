# Tài nguyên lửa trại và phát video trong cảnh 3D

## Nguồn tài nguyên

Clip lửa trại được tạo bằng mã trong `tools/generate_campfire.py`: củi hình chóp, ngọn lửa cam/vàng, than hồng và tàn lửa trên nền tối. Ảnh HEIC do sinh viên cung cấp được dùng để tham khảo bố cục và màu; không có ảnh HEIC trong tài nguyên APK.

| Thuộc tính | Giá trị |
| --- | --- |
| Tệp đóng gói | app/src/main/res/raw/campfire_loop.mp4 |
| Thời lượng / số frame | 5 giây / 120 frame |
| Tốc độ khung hình | 24 fps |
| Kích thước / tỷ lệ | 1008 × 432 / 21:9 |
| Định dạng | MP4, H.264 Constrained Baseline, yuv420p |
| Âm thanh | Không có |
| Dung lượng | 612.826 byte |

Thông số được đối chiếu bằng [metadata tài nguyên](evidence/2026-11-27-campfire/video-metadata.json). Đây là video trình chiếu bên trong app, độc lập với video minh họa chức năng dài 4 phút 49 giây của bài làm.

## Tạo lại tài nguyên

Công cụ phát triển cần Python 3, NumPy, Pillow và FFmpeg có encoder libx264:

```sh
python3 tools/generate_campfire.py
ffprobe -v error -show_entries format=duration:stream=codec_name,width,height,r_frame_rate -of json app/src/main/res/raw/campfire_loop.mp4
```

Script tạo 120 frame RGB và truyền qua pipe tới FFmpeg. Ngọn lửa sử dụng trường nhiễu chuyển động theo chiều cao; glow, củi, than và tàn lửa được vẽ thành các lớp. Seed cố định và chuyển động tuần hoàn phục vụ tái tạo clip lặp. Các thư viện tạo tài nguyên không phải phụ thuộc runtime Android.

## Phát trên màn chiếu

```text
campfire_loop.mp4 → MediaPlayer → Surface
    → SurfaceTexture / GL_TEXTURE_EXTERNAL_OES
    → shader video → mặt màn chiếu 3D → camera của ghế
```

`CinemaTrailerPlayer` quản lý prepare, phát/lặp, pause, seek và giải phóng decoder trên luồng giao diện. `CinemaVideoScreen` quản lý external texture, nhận thông báo frame và cập nhật texture trên luồng GL. Renderer vẽ video cùng hệ tọa độ và ma trận camera với phòng/ghế; vì vậy màn chiếu có phối cảnh và có thể bị ghế phía trước che khuất.

Callback frame đánh dấu dữ liệu mới và yêu cầu render. `RENDERMODE_WHEN_DIRTY` không tạo vòng vẽ riêng khi phim tạm dừng và camera không thay đổi. Mô hình phòng không được dựng lại trong mỗi frame phim.

## Trạng thái phát và vòng đời

- Video tự phát và lặp. Phát/Tạm dừng thay đổi ý định phát của người dùng.
- Đổi ghế hoặc kéo nhìn quanh giữ tiến trình video.
- So sánh A/B tạm dừng phim để so sánh cùng một thời điểm; nút Phát video cho phép tiếp tục.
- Ra nền lưu vị trí, giải phóng decoder và bỏ Surface đã gắn. Khi trở lại, decoder được tạo theo Surface hiện tại, seek và áp trạng thái phát/pause.
- Xoay hoặc dựng lại Activity lưu vị trí phát và ý định phát. Vị trí khôi phục có thể lệch theo độ chính xác seek của decoder.
- Nếu phát video thất bại, cảnh giữ mặt màn chiếu dự phòng và thông báo trạng thái; điều hướng ghế vẫn độc lập.

## Kiểm chứng

`CinemaTrailerTest` kiểm tra metadata, chuyển động điểm ảnh khi phát, đứng hình khi pause, dựng lại Activity, lặp và các chu kỳ ra nền/trở lại. `CinemaPreviewTest` kiểm tra hình ảnh màn chiếu ở các vị trí khác nhau. Kết quả theo lần chạy và các hạn chế được ghi trong [báo cáo kiểm chứng](09-kiem-chung.md).

## Video minh họa có lời đọc

Sinh viên tự quay quá trình thao tác ứng dụng. Hậu kỳ bổ sung chú thích và con trỏ; lời đọc tiếng Việt được sinh từ nội dung sinh viên đã duyệt, bằng giọng nam `vi-VN-NamMinhNeural`. Lời đọc được căn theo 66 câu/nhóm câu. Khi ghép vào bản 4K, luồng hình được sao chép trực tiếp; hash luồng video trước/sau trùng nhau. Phụ thuộc tạo giọng đọc phục vụ hậu kỳ, không được đưa vào ứng dụng.

Bản được liên kết trực tiếp tại README hiện là video 1080p có lời đọc, thời lượng 4:49. Nguồn quay 720 × 1600; bản xuất 4K không được coi là quay 4K nguyên bản. [Thông số và kiểm tra video minh họa](evidence/2026-11-28-submission/video.json).

## Tham khảo API

- [SurfaceTexture](https://developer.android.com/reference/android/graphics/SurfaceTexture).
- [MediaPlayer](https://developer.android.com/reference/android/media/MediaPlayer).
- [GLES11Ext](https://developer.android.com/reference/android/opengl/GLES11Ext).
