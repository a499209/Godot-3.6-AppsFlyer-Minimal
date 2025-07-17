package org.godot.appsflyerhelper;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appsflyer.AppsFlyerLib;
import com.appsflyer.attribution.AppsFlyerRequestListener;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;
import org.godotengine.godot.plugin.SignalInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class AppsFlyerHelper extends GodotPlugin {

    private Godot godot;
    private boolean isInitialized = false;

    public AppsFlyerHelper(Godot godot) {
        super(godot);
        this.godot = godot;
        System.out.println("[AppsFlyerHelper] Plugin created!");
    }

    @Nullable
    @Override
    public View onMainCreate(Activity activity) {
        return null;
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "AppsFlyerHelper";
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new HashSet<>();

        signals.add(new SignalInfo("appsflyer_init_success"));
        signals.add(new SignalInfo("appsflyer_init_failed", String.class));
        signals.add(new SignalInfo("purchase_validation_success"));
        signals.add(new SignalInfo("purchase_validation_failed", String.class));
        signals.add(new SignalInfo("event_log_success", String.class));
        signals.add(new SignalInfo("event_log_failed", String.class));
        signals.add(new SignalInfo("test_signal", String.class));

        return signals;
    }

    // Use the parent class's emitSignal method directly
    private void sendSignal(String signalName, Object... args) {
        godot.runOnUiThread(() -> {
            try {
                System.out.println("[AppsFlyerHelper] Emitting signal: " + signalName);
                emitSignal(signalName, args);
            } catch (Exception e) {
                System.err.println("[AppsFlyerHelper] Error emitting signal " + signalName + ": " + e.getMessage());
            }
        });
    }

    @UsedByGodot
    public void init(String devKey, String appId) {
        System.out.println("[AppsFlyerHelper] init() called with devKey: " + devKey);

        Activity activity = getActivity();
        if (activity == null) {
            System.err.println("[AppsFlyerHelper] Activity is null!");
            sendSignal("appsflyer_init_failed", "Activity is null");
            return;
        }

        Context context = activity.getApplicationContext();

        try {
            AppsFlyerLib.getInstance().setDebugLog(true);
            AppsFlyerLib.getInstance().enableTCFDataCollection(true);

            // Сначала инициализируем SDK
            System.out.println("[AppsFlyerHelper] Initializing AppsFlyer SDK...");
            AppsFlyerLib.getInstance().init(devKey, null, context);

            // Затем запускаем SDK с колбэками
            System.out.println("[AppsFlyerHelper] Starting AppsFlyer SDK...");
            AppsFlyerLib.getInstance().start(context, devKey, new AppsFlyerRequestListener() {
                @Override
                public void onSuccess() {
                    System.out.println("[AppsFlyer] SDK started successfully");
                    isInitialized = true;
                    sendSignal("appsflyer_init_success");
                }

                @Override
                public void onError(int errorCode, String errorMessage) {
                    System.err.println("[AppsFlyer] SDK start failed: " + errorMessage);
                    isInitialized = false;
                    sendSignal("appsflyer_init_failed", errorMessage);
                }
            });

        } catch (Exception e) {
            System.err.println("[AppsFlyerHelper] Exception during init: " + e.getMessage());
            sendSignal("appsflyer_init_failed", "Exception: " + e.getMessage());
        }
    }

    @UsedByGodot
    public void validatePurchase(final String publicKey, final String signature, final String purchaseData, final float price, final String currency) {
        System.out.println("[AppsFlyerHelper] validatePurchase() called");

        // Проверка инициализации AppsFlyer
        if (!isInitialized) {
            System.err.println("[AppsFlyerHelper] AppsFlyer SDK not initialized!");
            sendSignal("purchase_validation_failed", "AppsFlyer SDK not initialized");
            return;
        }

        Activity activity = getActivity();
        if (activity == null) {
            System.err.println("[AppsFlyerHelper] Activity is null!");
            sendSignal("purchase_validation_failed", "Activity is null");
            return;
        }

        Context context = activity.getApplicationContext();

        // Валидация входных параметров
        if (signature == null || signature.isEmpty()) {
            System.err.println("[AppsFlyerHelper] Invalid signature");
            sendSignal("purchase_validation_failed", "Invalid signature");
            return;
        }

        if (purchaseData == null || purchaseData.isEmpty()) {
            System.err.println("[AppsFlyerHelper] Invalid purchase data");
            sendSignal("purchase_validation_failed", "Invalid purchase data");
            return;
        }

        if (price <= 0) {
            System.err.println("[AppsFlyerHelper] Invalid price");
            sendSignal("purchase_validation_failed", "Invalid price");
            return;
        }

        if (currency == null || currency.isEmpty()) {
            System.err.println("[AppsFlyerHelper] Invalid currency");
            sendSignal("purchase_validation_failed", "Invalid currency");
            return;
        }

        try {
            Map<String, Object> eventValues = new HashMap<>();
            eventValues.put("af_revenue", price);
            eventValues.put("af_currency", currency);
            eventValues.put("af_receipt_id", signature);
            eventValues.put("af_purchase_data", purchaseData);
            eventValues.put("af_signature", signature);

            System.out.println("[AppsFlyerHelper] Sending purchase event...");
            AppsFlyerLib.getInstance().logEvent(context, "af_purchase", eventValues, new AppsFlyerRequestListener() {
                @Override
                public void onSuccess() {
                    System.out.println("[AppsFlyerHelper] Purchase event sent successfully");
                    sendSignal("purchase_validation_success");
                }

                @Override
                public void onError(int code, String message) {
                    System.err.println("[AppsFlyerHelper] Purchase event failed: " + message);
                    sendSignal("purchase_validation_failed", message);
                }
            });
        } catch (Exception e) {
            System.err.println("[AppsFlyerHelper] Exception during purchase validation: " + e.getMessage());
            sendSignal("purchase_validation_failed", "Exception: " + e.getMessage());
        }
    }

    @UsedByGodot
    public void logEvent(String eventName, String eventValue) {
        System.out.println("[AppsFlyerHelper] logEvent() called: " + eventName);

        if (!isInitialized) {
            System.err.println("[AppsFlyerHelper] AppsFlyer SDK not initialized!");
            sendSignal("event_log_failed", "AppsFlyer SDK not initialized");
            return;
        }

        Activity activity = getActivity();
        if (activity == null) {
            System.err.println("[AppsFlyerHelper] Activity is null!");
            sendSignal("event_log_failed", "Activity is null");
            return;
        }

        Context context = activity.getApplicationContext();

        try {
            Map<String, Object> eventValues = new HashMap<>();
            eventValues.put("af_content", eventValue);

            AppsFlyerLib.getInstance().logEvent(context, eventName, eventValues, new AppsFlyerRequestListener() {
                @Override
                public void onSuccess() {
                    System.out.println("[AppsFlyerHelper] Event logged successfully: " + eventName);
                    sendSignal("event_log_success", eventName);
                }

                @Override
                public void onError(int code, String message) {
                    System.err.println("[AppsFlyerHelper] Event log failed: " + message);
                    sendSignal("event_log_failed", message);
                }
            });
        } catch (Exception e) {
            System.err.println("[AppsFlyerHelper] Exception during event logging: " + e.getMessage());
            sendSignal("event_log_failed", "Exception: " + e.getMessage());
        }
    }

    @UsedByGodot
    public void test() {
        System.out.println("[AppsFlyerHelper] Test method called!");
        sendSignal("test_signal", "Test message from plugin");
    }
}