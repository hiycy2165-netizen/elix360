# ELIX Android

ELIX Android uygulamasının temel hazır projesidir.

## Ne yapıyor?

- ELIX 360 PythonAnywhere sunucusuna bağlanır.
- Mesaj gönderir.
- ELIX cevabını gösterir.
- API anahtarı ayarlanırsa `/api/v1/chatb` kullanır.
- API anahtarı yoksa `/api/guest_chat` üzerinden misafir sohbeti kullanır.
- GitHub Actions otomatik olarak APK üretir.

## GitHub'da APK alma

1. Bu klasörün tamamını bir GitHub repository'sine yükle.
2. GitHub'da **Actions** sekmesine gir.
3. **Build ELIX APK** iş akışını seç.
4. **Run workflow** bas.
5. İşlem bitince **ELIX-debug-APK** adlı artifact'i aç.
6. İçindeki `app-debug.apk` dosyasını telefona indir.

## API anahtarı

Uygulamada sağ üstteki ayarlardan ELIX hesap API anahtarını girebilirsin.

API anahtarı boşsa uygulama misafir endpointini kullanır. Bu mod mevcut ELIX sunucusundaki bilgi sistemini kullanır fakat hesap geçmişi/API hesabı bağlantısı sağlamaz.

API anahtarı girildiğinde:
`POST https://elix360.pythonanywhere.com/api/v1/chatb`

JSON:
```json
{"message":"Selam"}
```

Header:
```text
X-API-Key: SENIN_API_ANAHTARIN
Content-Type: application/json
```

## Sonraki sürüm

Bu temel üzerine:
- kayıt/giriş
- hesap sohbet geçmişi
- eski sohbetleri açma
- kullanıcı belleği
- admin paneli
- ELIX'in web sitesindeki görünümle daha yakın tasarım
- uygulama ikonu ve isimlendirme
eklenebilir.
