Hướng dẫn đẩy và cập nhật dự án lên GitHub
Khởi tạo Git trong thư mục dự án
   git init
   git status
   git add .
   git commit -m "Khởi tạo dự án đầu tiên"

Xóa remote cũ (nếu có)
   git remote remove origin
   git remote -v

Thêm remote mới trỏ tới GitHub của bạn
   git remote add origin https://github.com/Aurora241/MelodyPlayer.git
   git branch -M main
   git push -u origin main

Khi có thay đổi mới, cập nhật code
    git add .
    git commit -m "Cập nhật code mới"
    git push origin main

Nếu muốn lấy code mới nhất từ GitHub về
    git pull origin main
