#!/bin/bash
# اسکریپت خودکار راه‌اندازی و Push ریپازیتوری استودیو شخصی سیاوش
# مخصوص آقای اکبر علی‌میری (سیاوش) در Termux روی شیائومی ردمی نوت ۸

set -e

REPO_NAME="siavash-personal-studio"
echo "==========================================================="
echo "  آغاز راه‌اندازی ریپازیتوری: $REPO_NAME"
echo "  اکوسیستم آفرینش آفرینا (توانا سیتی)"
echo "==========================================================="

if [ ! -d ".git" ]; then
    echo "• در حال مقداردهی اولیه Git..."
    git init
    git branch -M main
fi

git config user.name "Siavash Ali-Miri" || true
git config user.email "siavashhamiri@gmail.com" || true

echo "• افزودن تمام فایل‌های پروژه..."
git add .

echo "• ثبت کامیت اول..."
git commit -m "feat: Initial release of SIAVASH MUSIC CREATOR (MVP)" || echo "تغییر جدیدی برای کامیت یافت نشد."

echo "-----------------------------------------------------------"
echo "✅ ریپازیتوری محلی با موفقیت آماده شد!"
echo "جهت ارسال به اکانت گیت‌هاب، دستور زیر را با نام‌کاربری خود اجرا کنید:"
echo "git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/$REPO_NAME.git"
echo "git push -u origin main"
echo "==========================================================="
