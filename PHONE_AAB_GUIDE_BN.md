# ফোন দিয়ে AAB বানানোর সম্পূর্ণ ধাপ

## যা লাগবে
- Android ফোন
- GitHub account
- Termux (F-Droid থেকে নেওয়া ভালো)
- এই project-এর ZIP

## 1) Project GitHub-এ তুলুন
GitHub app/browser → New repository → নাম দিন `my-music-app` → Create repository.
Repository খুলে Add file → Upload files দিয়ে ZIP extract করার পর **ভেতরের সব ফাইল/ফোল্ডার** upload করুন।
`.github/workflows/build-aab.yml`-সহ সব ফাইল থাকতে হবে।

## 2) ফোনে signing key তৈরি করুন
Termux খুলে:
```bash
pkg update
pkg install openjdk-17
keytool -genkeypair -v -keystore release-keystore.jks -alias mymusic -keyalg RSA -keysize 2048 -validity 10000
```
প্রশ্ন এলে একটি শক্ত password দিন এবং মনে রাখুন। Key password চাইলে একই password দিতে পারেন।

## 3) Keystore-এর Base64 তৈরি করুন
Termux-এ:
```bash
base64 -w 0 release-keystore.jks
```
যদি `-w 0` কাজ না করে:
```bash
base64 release-keystore.jks | tr -d '\n'
```
যে লম্বা text পাবেন সেটি copy করুন।

## 4) GitHub Secrets দিন
Repository → Settings → Secrets and variables → Actions → New repository secret.

এই 4টি secret তৈরি করুন:
- `KEYSTORE_B64` = উপরের Base64 text
- `KEYSTORE_PASSWORD` = আপনার keystore password
- `KEY_ALIAS` = `mymusic`
- `KEY_PASSWORD` = আপনার key password

## 5) AAB Build চালান
GitHub repository → Actions → **Build signed AAB** → **Run workflow**.

Build সবুজ হলে workflow-এর নিচে **Artifacts** থেকে:
`my-music-release-aab`
download করুন।

এর ভিতরে থাকবে:
`app-release.aab`

## 6) খুব গুরুত্বপূর্ণ
`release-keystore.jks` এবং password নিরাপদে সংরক্ষণ করুন। এগুলো হারালে ভবিষ্যৎ app update প্রকাশে সমস্যা হবে।

## 7) Play Console
Play Console → Create app → Store listing/App content/Data safety/Content rating পূরণ করুন → Testing track-এ AAB upload করুন → প্রয়োজনীয় testing শেষ করে Production access নিন।

## Music copyright
শুধু নিজের বা লাইসেন্সকৃত গান ও artwork ব্যবহার করবেন। Spotify-এর গান/লোগো/কভার অনুমতি ছাড়া ব্যবহার করবেন না।

## Before build
`MainActivity.kt`-এর `YOUR-DOMAIN.com` MP3 URL-গুলো বৈধ HTTPS audio URL দিয়ে বদলাতে হবে।
