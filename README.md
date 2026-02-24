
# 🚀 TradeFlow: Modern Market & Crypto Aggregator

**TradeFlow**, Java 21'in devrimsel yeteneklerini ve Spring Boot 4'ün modern mimarisini harmanlayan, yüksek performanslı bir finansal veri agregatörüdür. Bu proje, geleneksel "Thread-per-request" modelinin kısıtlamalarını aşarak **Virtual Threads (Project Loom)** ile ölçeklenebilirliği maksimize etmeyi hedefler.

## 🛠️ Teknik Stack

- **Dil:** Java 21 (LTS)
- **Framework:** Spring Boot 4.0.x
- **Eşzamanlılık:** Virtual Threads (Project Loom)
- **Networking:** Spring RestClient (Fluent & Modern)
- **Önbellek:** Caffeine Cache (L1 Cache)
- **Güvenlik:** Spring Security 6 (Lambda DSL)
- **Gözlemlenebilirlik:** Spring Boot Actuator & RFC 9457

---

## 💎 Java 21 & Modernizasyon Odak Noktaları

### 🧵 1. Virtual Threads
Geleneksel platform thread'lerinin (1MB RAM/thread) aksine, TradeFlow her bir I/O işlemini (Binance API çağrıları vb.) **Sanal Thread'ler** üzerinden yürütür.
- **Verimlilik:** Binlerce eşzamanlı istekte bile işletim sistemi thread'lerini bloklamaz, "Mount/Unmount" mekanizmasıyla CPU'yu %100 verimle kullanır.
- **Konfigürasyon:** `spring.threads.virtual.enabled=true`

### 📦 2. Records & Veri Modelleme
DTO ve veri taşıyıcılar için `record` yapısı kullanılarak **Immutability** (değişmezlik) garanti altına alınmış ve boilerplate kod yükü (Getter/Setter/ToString) temizlenmiştir.
- *Örnek:* `public record CryptoAsset(String symbol, double price, String network) implements Asset {}`

### 🔒 3. Sealed Classes & Interfaces
Varlık (Asset) hiyerarşisi `sealed` anahtar kelimesi ile kilitlenmiştir. Bu, mimari sınırların dışına çıkılmasını engeller ve domain güvenliğini sağlar.
- *Hiyerarşi:* `Asset` -> `permits CryptoAsset, StockAsset, FiatAsset, ErrorAsset`

### 🎯 4. Pattern Matching & Switch Expressions
Karmaşık `if-else` yığınları yerine, Java 21'in geliştirilmiş `switch` yapısı ile tip bazlı **Deconstruction** yapılmıştır.
```java
return switch (asset) {
    case CryptoAsset c -> "🚀 Kripto: " + c.symbol();
    case ErrorAsset e  -> "⚠️ Hata: " + e.message();
    default -> "Diğer Varlık";
};
```

---

## 🛡️ Esnek Hata Yönetimi (RFC 9457)

Uygulama, modern API standartı olan **RFC 9457 (Problem Details for HTTP APIs)** protokolünü tam kapsamlı olarak uygular.

- **Standardizasyon:** Hatalar `application/problem+json` formatında dönülerek Google ve Microsoft gibi devlerin API standartlarına uyum sağlanmıştır.
- **Defensive RestClient:** `.onStatus()` metodolojisi ile dış servis hataları yakalanarak domain bazlı özel istisnalara (`AssetNotFoundException`) dönüştürülür.
- **Gözlemlenebilirlik:** Hata mesajlarına `timestamp`, `path` ve hata dökümantasyonuna yönlendiren `type` URI'leri eklenmiştir.

---

## ⚡ Performans Benchmark: Virtual vs. Platform Threads

TradeFlow, Sanal Thread teknolojisinin geleneksel thread havuzlarına karşı üstünlüğünü kanıtlayan yerleşik bir ölçüm mekanizmasına sahiptir.

### 🧪 Test Senaryosu (1.000 Paralel Görev - 1s Gecikme)
| Thread Tipi | Havuz Boyutu | Toplam Süre | Sonuç |
| :--- | :---: | :---: | :--- |
| **Platform Threads** | 100 (Fixed) | **~10,101 ms** | Darboğaza takılır (10 turda tamamlar). |
| **Virtual Threads** | Sınırsız | **~1,015 ms** | **%100 Paralel (Tek turda tamamlar).** |

---

## 🏗️ Mimari Refactoring: Self-Invocation Çözümü

Proje geliştirme sürecinde, Spring'in **AOP Proxy** mekanizmasından kaynaklanan "Self-Invocation" (kendi kendini çağırma) problemi analiz edilmiş ve mimari seviyede çözülmüştür.

### 🔍 Problem
Bir servis metodunun aynı sınıf içindeki `@Cacheable` işaretli başka bir metodu çağırması durumunda, çağrı Proxy üzerinden geçmediği için önbellek mekanizması baypas ediliyordu.

### 🛠️ Çözüm: Dynamic Proxy Resolution
`ApplicationContext` kullanılarak **Dynamic Proxy Resolution** yöntemi tercih edilmiştir:
- **Runtime Bean Retrieval:** `MarketService`, çalışma zamanında kendi Proxy referansını context üzerinden elde eder.
- **Thread Safety:** Sanal thread'ler altındaki paralel çağrılar, bu dinamik referans üzerinden güvenli bir şekilde önbelleğe yönlendirilir.

---

## 🚀 Resilient Parallel Processing (Bulk Request)

TradeFlow, birden fazla varlık verisini toplarken **"Partial Failure Management"** (Kısmi Hata Yönetimi) prensibiyle çalışır:
1. **Parallel Fetching:** Her sembol için ayrı bir Sanal Thread başlatılır.
2. **Exception-to-Data:** `CompletableFuture.handle()` ile hatalar `ErrorAsset` nesnelerine dönüştürülür; böylece bir varlığın hata vermesi tüm raporun çökmesine neden olmaz.

---

## 🏁 Başlangıç

### Gereksinimler
- JDK 21+
- Maven 3.9+

### Kurulum ve Çalıştırma
```bash
git clone https://github.com/zaferuzun/tradeflow.git
cd tradeflow
mvn clean install
mvn spring-boot:run
```

### Önemli Test Endpoint'leri
- **Bulk Price:** `/api/market/bulk?symbols=BTCUSDT,ETHUSDT`
- **Cache Inspect:** `/actuator/cacheContents`
- **Benchmark:** `/test/benchmark/virtual?tasks=1000`

---
