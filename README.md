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

## ⚡ Performance Showdown: Virtual vs. Platform Threads

TradeFlow, Java 21'in Sanal Thread (Project Loom) teknolojisinin geleneksel Platform Thread'lerine karşı üstünlüğünü kanıtlayan yerleşik bir benchmark mekanizmasına sahiptir.

### 🧪 Test Senaryosu
Aşağıdaki test, her biri **1 saniye (1000ms)** süren (bloklayan I/O simülasyonu) **1.000 adet** bağımsız görevin eşzamanlı olarak çalıştırılmasını kapsar.

- **Platform Threads:** 100 adet sabit thread havuzu (Fixed Thread Pool) kullanır.
- **Virtual Threads:** Her görev için yeni bir Sanal Thread oluşturur (`newVirtualThreadPerTaskExecutor`).

### 📊 Benchmark Sonuçları

| Thread Tipi | Görev Sayısı | Havuz Boyutu | Toplam Süre | Verimlilik |
| :--- | :---: | :---: | :---: | :--- |
| **Platform Threads** | 1,000 | 100 (Fixed) | **~10,000 ms** | Darboğaza takılır (10 turda tamamlar). |
| **Virtual Threads** | 1,000 | Sınırsız (Sanal) | **~1,000 ms** | %100 Paralel (Tek turda tamamlar). |

> **Not:** Görev sayısını 10.000'e çıkardığınızda, Platform Thread'lerin süresi doğrusal olarak artarken (100 saniye), Virtual Thread'ler hala ~1 saniye civarında sonuç vermektedir.

### 🔍 Teknik Analiz (Senior Insight)
- **Blocking Cost:** Platform thread'lerde her "bekleme" (wait/sleep), bir OS thread'ini kilitler ve sistem kaynaklarını (1MB stack) esir alır.
- **Mount/Unmount:** Virtual thread'ler bir I/O beklemeye başladığında, JVM onları taşıyıcı thread'den (Carrier Thread) ayırır. Bu sayede fiziksel kaynaklar boşa çıkar ve diğer görevler için kullanılabilir.
- **Throughput:** Virtual Threads kullanımı, uygulamanın donanım maliyetini değiştirmeden işlem kapasitesini (throughput) 10 katına kadar artırabilir.

### 🚀 Testi Çalıştırın
Uygulamayı başlattıktan sonra aşağıdaki uç noktaları kullanarak farkı kendi gözlerinizle görebilirsiniz:

```bash
# Platform Threads Testi
curl http://localhost:8080/test/benchmark/platform?tasks=1000
#Platform Threads (Pool Size 100) ile 1000 işlem süresi: 10101 ms

# Virtual Threads Testi (Java 21 Power!)
curl http://localhost:8080/test/benchmark/virtual?tasks=1000
#Virtual Threads ile 1000 işlem süresi: 1011 ms (Average of 10 results: 1015)
```

NOT: StructuredTaskScope java 21 de preview aşamasında daha sonra bakılacak. 

Standardizasyon: ProblemDetail kullanarak Google, Microsoft gibi devlerin kullandığı hata formatına uymuş oldun.
Temiz Kod: Controller içinde try-catch kalabalığı bitti. Hatalar fırlatılır (throw), merkezi bir yer onları yakalar.
Gözlemlenebilirlik: Hata mesajlarına timestamp ve type (hata döküman linki) ekleyerek debug sürecini kolaylaştırdın.
Virtual Thread Dostu: Bu hata yapısı asenkron ve sanal thread akışlarında bile thread-safe çalışır.