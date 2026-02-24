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

## 🚀 Resilient Parallel Processing (High Throughput)

TradeFlow, birden fazla varlık verisini toplarken **"Biri hata verirse hepsi durmasın"** (Partial Failure Management) prensibiyle çalışır.

### Teknik Mimari:
1. **Virtual Thread Per Task:** Her bir sembol sorgusu için  Sanal bir Thread başlatılır. Bu, I/O bloklamalarını (API yanıt sürelerini) sistem kaynaklarını tüketmeden paralel olarak yönetmemizi sağlar.
2. **Exception-to-Data Pattern:** `CompletableFuture.handle()` kullanılarak istisnalar (Exceptions), tip güvenli `ErrorAsset` nesnelerine dönüştürülür. Bu sayede hata durumları "beklenmedik bir çöküş" değil, "işlenebilir bir veri" haline gelir.
3. **Exhaustive Switch Handling:** Java 21'in `sealed interface` yapısı sayesinde, rapor oluşturulurken tüm varlık tipleri (Crypto, Stock, Fiat ve Hata) derleme zamanı güvenliğiyle işlenir.

### Bulk Sorgu Örneği:
`GET /api/market/bulk?symbols=BTCUSDT,INVALID_COIN,ETHUSDT`

**Yanıt Stratejisi:**
- ✅ **BTCUSDT:** Başarılı fiyat bilgisi.
- ⚠️ **INVALID_COIN:** `ErrorAsset` içinde hata detayı ("Varlık bulunamadı").
- ✅ **ETHUSDT:** Başarılı fiyat bilgisi.
- 📊 **Total Value:** Sadece başarılı olanların toplamı. 



Bu ek geliştirme (Sınıfların ayrılması veya Proxy kullanımı), Spring Framework’ün çalışma mantığındaki en kritik konulardan biri olan **"Self-Invocation" (Kendi Kendini Çağırma)** problemini çözmek içindir. Senior bir geliştirici, anotasyonların sihirli bir değnek olmadığını, arkada bir **AOP Proxy** mekanizması olduğunu bilir.

İşte `README.md` dosyana ekleyebileceğin, teknik derinliği yüksek bir açıklama metni:

---

### 📝 README.md'ye Eklenecek Bölüm


## 🏗️ Mimari Refactoring: Spring AOP Proxy & Self-Invocation Çözümü

Projenin geliştirme sürecinde, tekli varlık sorgulamalarında çalışan önbellek (Cache) mekanizmasının, toplu sorgulamalarda (Bulk Requests) devre dışı kaldığı tespit edilmiştir. Bu durum, Spring Framework'ün **AOP (Aspect Oriented Programming)** tabanlı Proxy mimarisinden kaynaklanmaktadır.

### 🔍 Problem: Self-Invocation Nedir?
Spring'de `@Cacheable`, `@Transactional` veya `@Async` gibi anotasyonlar, ilgili sınıfın bir **Vekil (Proxy)** nesnesi üzerinden çağrılmasıyla aktif olur. 
- **Normal Akış:** `Controller` -> `Service (Proxy)` -> `Cache Check` -> `Actual Service Method`.
- **Hatalı Akış (Self-Invocation):** Sınıf içindeki bir metodun (`getBulkPrices`), aynı sınıf içindeki başka bir metodu (`getCryptoPrice`) doğrudan çağırması durumunda, çağrı Proxy üzerinden geçmez. Bu nedenle Java doğrudan hedef metoda gider ve `@Cacheable` anotasyonu (ve önbellek kontrolü) tamamen baypas edilir.

### 🛠️ Çözüm: Sorumlulukların Ayrılması (Separation of Concerns)
Bu sorunu aşmak ve **Virtual Threads** ile yapılan paralel sorguların her birinin önbellekten faydalanmasını sağlamak için mimari bir iyileştirme yapılmıştır:


### 🛠️ Uygulanan Çözüm: Dynamic Proxy Resolution via ApplicationContext

Self-invocation sorununu aşmak ve döngüsel bağımlılık (circular dependency) risklerini sıfıra indirmek için **Dynamic Proxy Resolution** yöntemi tercih edilmiştir:

- **Runtime Bean Retrieval:** `MarketService` nesnesi, `ApplicationContext` aracılığıyla çalışma zamanında kendi Proxy referansını elde eder.
- **Breaking the Cycle:** Bu yöntem, `@Lazy` enjeksiyonun aksine Spring'in katı döngüsel bağımlılık kontrollerine takılmadan, metod çağrılarının her zaman AOP Proxy üzerinden geçmesini garanti eder.
- **Thread Safety:** Virtual Threads (Sanal Thread'ler) altında yapılan paralel çağrılar, bu dinamik referans üzerinden güvenli bir şekilde `@Cacheable` mekanizmasına yönlendirilir.

//Yapılabilirdi.
1. **PriceProvider Sınıfı:** Sadece dış API (Binance) ile konuşan ve `@Cacheable` notasyonu ile önbellek yönetiminden sorumlu olan izole bir katman oluşturuldu.
2. **MarketService Sınıfı:** İş mantığını ve Sanal Thread (Virtual Threads) yönetimini üstlenen koordinatör katman olarak kurgulandı.

Bu sayede `MarketService`, `PriceProvider` nesnesini çağırdığında Spring'in Proxy mekanizması araya girer ve:
- ✅ **Performans:** Aynı varlık için mükerrer API çağrıları önlenir.
- ✅ **Resilience:** Dış servis (Binance) üzerindeki trafik yükü ve "Rate Limit" riskleri azaltılır.
- ✅ **Clean Code:** İş mantığı (Concurrency) ile veri sağlama (Caching) sorumlulukları birbirinden ayrılmıştır.

### 🚀 Teknik Kazanım
Sanal thread'ler (Virtual Threads) I/O bloklamalarını çözerek eşzamanlılığı artırırken, bu mimari refactoring ile gereksiz I/O operasyonları da önbellek seviyesinde durdurulmuştur. Sonuç olarak; **yüksek eşzamanlılık + düşük gecikme (latency)** hedefine tam uyum sağlanmıştır.




**GitHub commiti için öneri:**
`docs: explain architectural refactoring for spring aop proxy and self-invocation issue`

Bu dökümantasyonla birlikte 1. hafta serüvenini **"Teknik Mimar"** seviyesinde tamamlamış oluyoruz. Sırada ne var? Başka bir özelliğe mi geçelim yoksa 2. haftanın yeni konusuna (Docker/Containerization) merhaba mı diyelim?



NOT: StructuredTaskScope java 21 de preview aşamasında daha sonra bakılacak. 

Standardizasyon: ProblemDetail kullanarak Google, Microsoft gibi devlerin kullandığı hata formatına uymuş oldun.
Temiz Kod: Controller içinde try-catch kalabalığı bitti. Hatalar fırlatılır (throw), merkezi bir yer onları yakalar.
Gözlemlenebilirlik: Hata mesajlarına timestamp ve type (hata döküman linki) ekleyerek debug sürecini kolaylaştırdın.
Virtual Thread Dostu: Bu hata yapısı asenkron ve sanal thread akışlarında bile thread-safe çalışır.