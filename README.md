Welcome to the Godot AppsFlyer Plugin, a lightweight and flexible integration of AppsFlyer for Godot Engine.
This plugin helps you connect your game to AppsFlyer, track events, and validate in-app purchases — all with minimal effort and maximum love.

This plugin is written for Android. iOS support is not included in this version.

## 🚀 Features

Easy initialization of AppsFlyer SDK

Event tracking (log_event)

Full purchase validation (server-side ready)

Callbacks for success/failure

Works with Godot 3.6+



## 📦 Installation


Copy .aar and .gdap from releases into your android/plugins folder.

Enable the plugin in your Android export settings.

Add GodotAppsFlyer.gd to your project autoload

paste your key and app id to init_appsflyer() function (GodotAppsFlyer.gd)

```xml
   init_appsflyer("YOUR_DEV_KEY", "YOUR_APP_ID")
```
## ✨ Tracking Events

Tracking Events example:
```xml
GodotAppsFlyer.log_event("tutorial_completed", "step_3")
```
## 🧾 Purchase Validation

One of the most crucial parts — and the trickiest.
Here’s how to make sure it works like a charm 🌟

Parameters

When validating purchases, make sure to pass all required variables.
```xml
GodotAppsFlyer.validate_purchase(
    public_key: String,
    signature: String,
    purchase_data: String,
    price: float,
    currency: String,
)
```

| Name                                                            | Type     | Description                                                                                                                                                                                      |
| --------------------------------------------------------------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `public_key`                                                    | `String` | **Your Google Play public key** (Base64-encoded). This is used to verify the authenticity of the purchase. You can find it in the [Google Play Console](https://play.google.com/console/) under:  **Monetization > Monetization setup > Licensing > Public Key**.|                                                                                                                                                                                      |
| `signature`                                                     | `String` | The **signature string** returned by the Google Play Billing API after a purchase is made. It is used along with the public key to validate the purchase.                                        |
| `purchase_data`                                                 | `String` | The **raw purchase data JSON**, as returned by the billing API. This includes information like product ID, purchase time, etc.                                                                   |
| `price`                                                         | `float`  | The **price** of the item purchased, in the standard currency (e.g., `1.99`).                                                                                                                    |
| `currency`                                                      | `String` | The **ISO 4217 currency code** (e.g., `"USD"`, `"EUR"`, `"RSD"`), matching the currency used in the purchase.                                                                                    |

Example:
```xml
var public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A..." # ← Better do not hardcode it in production
var signature = "MEUCIQDVXeU9h..." # from BillingClient
var purchase_data = "{\"orderId\":\"GPA.1234-5678-9012-34567\",...}" # from BillingClient
var price = 2.99
var currency = "USD"

GodotAppsFlyer.validate_purchase(public_key, signature, purchase_data, price, currency)
```
## ⚙️ Android Setup Notes
Make sure your app has the required permissions in AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 🧁 Final Notes
This plugin is made with love (but without java dev experience) to help fellow Godot developers.
If you find bugs, feel free to open an issue or even better — a pull request.
Stay kind, stay indie 
