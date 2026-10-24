# JISAN MOBILE - Native Android E-Commerce Application

**Bengali Brand Line:** "মোবাইল মানেই জিসান মোবাইল"  
**Shop Location:** Baromasia Hajir Hut, Sagar Para, Murshidabad, West Bengal, India  
**Customer Contact & WhatsApp:** +91 9382302242  
**Secondary Contact:** +91 8918933017  
**Currency:** Indian Rupees (₹)  
**Default Language:** Bengali (বাংলা) with seamless English toggle  

---

## 1. Overview & Architecture

JISAN MOBILE is a native Android e-commerce application built with **Kotlin** and **Jetpack Compose (Material 3)**. The application is tailored for high-speed browsing, low RAM usage, and offline resilience on Indian smartphone networks.

### Architecture Patterns
- **Clean Architecture & MVVM**: `ShopViewModel` exposes reactive `StateFlow` streams.
- **Local Database Persistence**: Powered by Android Room (`JisanDatabase`), ensuring instant loading, zero data loss, offline cart caching, and atomic stock updates.
- **Dual Language Engine**: Live English/Bengali localization (`AppLanguage`) switchable anywhere in the app with immediate UI updates.
- **Meesho-Inspired Clean Layout**: Rounded cards, contrast badges, compact vertical rhythm, 48dp touch targets, and clear action hierarchies.

---

## 2. Implemented Features

| Feature | Details | Status |
|---|---|---|
| **Home Screen** | JISAN MOBILE branding, location, search bar, admin-managed promotional banners carousel, category shortcuts, sections for New Smartphones, Second-Hand Phones, Best Sellers, and Mobile Accessories. | ✅ Complete |
| **Bengali & English Toggle** | Instant switch between Bengali and English across all buttons, titles, forms, order statuses, and condition badges. | ✅ Complete |
| **Catalog & Search** | Full-text search by model/name/brand, filter by condition (New / Second-Hand), price sorting (low-high, high-low, newest), and category chips. | ✅ Complete |
| **Product Details** | Full technical specifications, MRP vs selling price, discount percentage badge, color & storage variants picker, stock indicator, and shop guarantee terms. | ✅ Complete |
| **Second-Hand Phone Inspection** | Dedicated certified badge, battery health percentage (e.g., ⚡91%), cosmetic grading (e.g., "9/10 Like New"), included box accessories, and 3-month shop warranty. | ✅ Complete |
| **Persistent Cart** | Room-backed offline cart. Real-time subtotal, free delivery threshold check (Free over ₹499), quantity increments/decrements, and out-of-stock guards. | ✅ Complete |
| **Checkout & Order Validation** | Customer name, validated 10-digit Indian phone number, delivery address, PIN code, Cash on Delivery (COD) payment method, and price/stock revalidation. | ✅ Complete |
| **Unique Order Tracking** | Formats order numbers as `JM-YYYYMMDD-XXXX`. Order tracking states (`PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`). | ✅ Complete |
| **WhatsApp Direct Assist** | One-tap WhatsApp receipt generator for +91 9382302242, allowing customers to send formatted order receipts directly to the shop. | ✅ Complete |
| **Admin Management Console** | PIN-protected console (`7860`). Real-time sales metrics, incoming order management, stock updates, product CRUD, and 1-tap demo product cleanup. | ✅ Complete |
| **Demo Inventory** | Pre-seeded with 8 sample products clearly labelled `[DEMO]`. Removable in 1-tap via the Admin Panel. | ✅ Complete |

---

## 3. External Configurations & Integrations

The core app operates fully with local Room persistence out of the box. To connect live cloud backends:

### A. Firebase Firestore & Google Sign-In Setup
1. Open your Firebase Console and create a project.
2. Register the Android package `com.aistudio.jisanmobile.shop` with your SHA-1/SHA-256 fingerprint.
3. Download `google-services.json` and place it in the `/app` directory.
4. Uncomment the Firebase dependencies in `/app/build.gradle.kts`:
   - `implementation(libs.firebase.firestore)`
   - `implementation(libs.firebase.auth)`
   - `implementation(libs.androidx.credentials)`
   - `implementation(libs.googleid)`
5. Deploy the provided `firestore.rules` located in the project root:
   ```bash
   firebase deploy --only firestore:rules
   ```

### B. Setting Up First Admin Custom Claims
To grant cloud admin access in Firebase Auth, execute this Firebase Admin SDK script (e.g. in Cloud Functions or local node script):
```javascript
const admin = require('firebase-admin');
admin.initializeApp();

// Set admin custom claim for shop owner email
admin.auth().getUserByEmail('jisanmobileworkpolicy@gmail.com')
  .then((user) => admin.auth().setCustomUserClaims(user.uid, { admin: true }))
  .then(() => console.log('Admin custom claim granted successfully.'));
```

### C. Online Payments (UPI Gateway)
Cash on Delivery (COD) and UPI on Delivery are active by default. For in-app pre-paid UPI / Netbanking, integrate a certified Indian payment gateway (Razorpay or Cashfree) and configure secret keys in the Secrets panel.

---

## 4. Build & Run Steps

1. **Local JVM Testing:**
   ```bash
   gradle :app:testDebugUnitTest
   ```
2. **Build Debug APK:**
   ```bash
   gradle :app:assembleDebug
   ```
   The APK is generated at: `app/build/outputs/apk/debug/app-debug.apk`.
3. **Build Release APK:**
   ```bash
   gradle :app:assembleRelease
   ```
