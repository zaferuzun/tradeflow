# 🚀 TradeFlow: Modern Market & Crypto Aggregator

**TradeFlow**, Java 21' özelliklerini ve Spring Boot 4'ün modern mimarisini kullanarak yüksek performanslı bir finansal veri takip edilmesi saglayan aday projedir. Java 21 den önce kullanılan "Thread-per-request" modelini geride bırakarak **Virtual Threads** kullanılmıştır.


## 🚀 Getting Started

### Prerequisites
- JDK 21+
- Maven 3.9+
- IDE (IntelliJ IDEA Recommended)


### Installation & Run
```bash
git clone https://github.com/zenon/tradeflow.git
cd tradeflow
mvn clean install
mvn spring-boot:run
```

## 🛠️ Tech Stack & Modernization Baseline

- **Language:** Java 21 (LTS)
- **Framework:** Spring Boot 4.0.x
- **Concurrency:** Virtual Threads
- **Networking:** Spring RestClient
- **Security:** Spring Security 6+ (Lambda DSL)

---


## 💎 Kullanılan Java 21 Özellikleri

### 🧵 1. Virtual Threads 
Geleneksel platform thread'lerinin (1MB RAM/thread) aksine, TradeFlow her bir I/O işlemini (Binance API çağrıları vb.) **Virtual Threads** üzerinden yürütür.
- **Verimlilik:** Binlerce eşzamanlı istekte bile işletim sistemi thread'lerini bloklamaz.
- **Konfigürasyon:** `spring.threads.virtual.enabled=true`

### 📦 2. Records & Data Modeling
Veri taşıyıcılar (DTO) için `record` yapısı kullanılarak hem immutability (değişmezlik) garanti edilmiş hem de boilerplate kod (getter/setter) temizlenmiştir.
- **Example:** `public record CryptoAsset(String symbol, double price, String network) {}`

### 🔒 3. Sealed Classes & Interfaces
Varlık (Asset) hiyerarşisi `sealed` anahtar kelimesi ile kilitlenmiştir. Bu, mimari sınırların dışına çıkılmasını engeller ve domain güvenliğini sağlar.
- **Hierarchy:** `Asset` -> `CryptoAsset`, `StockAsset`, `FiatAsset` 

### 🎯 4. Pattern Matching for Switch
Karmaşık `if-else` blokları yerine, Java 21'in geliştirilmiş `switch` yapısı ile tip bazlı deconstruction yapılmıştır.
```java
return switch (asset) {
    case CryptoAsset c -> "Rocketing Crypto: " + c.symbol();
    case StockAsset s  -> "Steady Stock: " + s.symbol();
    default -> "Traditional Asset";
};
```


## 🛡️ Error Handling (RFC 9457)

Uygulama, modern API standartı olan **RFC 9457 (Problem Details for HTTP APIs)** protokolünü tam kapsamlı olarak uygular.

### Öne Çıkan Özellikler:
- **Global Exception Handling:** `@RestControllerAdvice` ve `ResponseEntityExceptionHandler` kullanılarak merkezi bir hata yönetim katmanı kuruldu.
- **RFC 9457 Compliance:** Hata mesajları artık rastgele formatlar yerine; `type`, `title`, `status`, `detail` ve `instance` alanlarını içeren standart bir JSON yapısında (`application/problem+json`) dönüş yapılır.
- **Defensive RestClient Usage:** `RestClient` katmanında `.onStatus()` metodolojisi kullanılarak dış servis hataları (4xx/5xx) yakalanır ve domain bazlı özel istisnalara (`AssetNotFoundException`, `TradeException`) dönüştürülür.
- **Enhanced Observability:** Her hata mesajına bir `timestamp` ve hatanın dökümantasyonuna yönlendiren bir `type` URI'si eklenerek debug süreçleri kolaylaştırılmıştır.

### Örnek Standart Hata Yanıtı:

Bu Alanlar Ne Anlama Gelir?
- type (URI): Hatanın dokümantasyonuna giden bir link. Senior bir dokunuştur; frontend'ci bu linke tıklayıp hatanın nedenlerini okuyabilir.
- title: Hatanın kısa, insan tarafından okunabilir adı ("Varlık Mevcut Değil").
- status: HTTP durum kodu (404, 500 vb.). Nesnenin içinde de olması, logları okurken kolaylık sağlar.
- detail: Hatanın o ana özel açıklaması.
- instance: Hatanın hangi uç noktada (endpoint) oluştuğu.
- Extra Properties: Standart dışı ama senin eklediğin alanlar (Örn: timestamp, errorCode).

```json
{
  "type": "https://xxx.com/errors/asset-not-found",
  "title": "Varlık Mevcut Değil",
  "status": 404,
  "detail": "Varlık bulunamadı: BTCUSDT",
  "instance": "/api/market/price/BTCUSDT",
  "timestamp": "2024-05-20T14:30:00Z"
}
```

---

## ⚡ Performance Architecture

TradeFlow, dış servislerle (Binance, Exchange API vb.) konuşurken **Spring RestClient** kullanır. Sanal Thread'lerin gücüyle birleşen bu yapı, bloklayan I/O işlemlerini "maliyet" olmaktan çıkarır.

- **Blocking I/O Efficiency:** Bir API cevabı beklenirken gerçek OS thread'i (Carrier Thread) boşa çıkarılır, başka bir işe atanır.
- **Low Memory Footprint:** Platform thread'lerin aksine, sanal thread'ler sadece birkaç yüz byte yer kaplar.

---


