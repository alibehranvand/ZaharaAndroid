# Zahara Android

اپلیکیشن اندرویدی زاهارا برای وب‌اپلیکیشن:

`https://behranvand.ir/zahara/`

- نام برنامه: زاهارا
- Package: `ir.behranvand.zahara`
- معماری: WebView امن روی HTTPS؛ بک‌اند PHP/SQLite روی هاست باقی می‌ماند.
- پشتیبانی از JavaScript، DOM Storage، Session Cookie، انتخاب فایل Excel، دانلود، دکمه Back، Pull-to-Refresh و لینک‌های خارجی.

## ساخت APK بدون Android Studio

این پروژه برای GitHub Actions آماده شده است.

1. در GitHub یک Repository جدید بسازید، مثلاً `ZaharaAndroid`.
2. تمام فایل‌های این پوشه را داخل Repository آپلود کنید؛ پوشه `.github/workflows` را هم حتماً آپلود کنید.
3. بعد از Push به شاخه `main`، GitHub به‌صورت خودکار APK را می‌سازد.
4. در GitHub وارد تب **Actions** شوید و اجرای **Build Zahara APK** را باز کنید.
5. وقتی کار تمام شد، در پایین صفحه بخش **Artifacts**، فایل `Zahara-debug-apk` را دانلود کنید.
6. فایل ZIP دانلودشده را باز کنید و `app-debug.apk` را روی گوشی نصب کنید.

همچنین می‌توانید از Actions گزینه **Run workflow** را بزنید تا Build را دستی اجرا کنید.
