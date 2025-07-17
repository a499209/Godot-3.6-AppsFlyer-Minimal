extends Node

signal appsflyer_init_success
signal appsflyer_init_failed(error_message)

signal purchase_validation_success
signal purchase_validation_failed(error_message)

signal event_log_success(event_name)
signal event_log_failed(error_message)

signal test_signal(message)

var appsflyer_plugin

func _ready():
	if Engine.has_singleton("AppsFlyerHelper"):
		appsflyer_plugin = Engine.get_singleton("AppsFlyerHelper")
		connect_signals()
		init_appsflyer("YOUR_DEV_KEY", "YOUR_APP_ID") # <- You need to paste dev key and app id here!!!


func connect_signals():
	if appsflyer_plugin:
		
		var signals = [
			"appsflyer_init_success",
			"appsflyer_init_failed",
			"purchase_validation_success",
			"purchase_validation_failed",
			"event_log_success",
			"event_log_failed",
			"test_signal"
			]
		
		for s in signals:
			if appsflyer_plugin.has_signal(s):
				appsflyer_plugin.connect(s, self, s)
			else:
				print("[AppsFlyerManager] signal not found: ", s)


func init_appsflyer(dev_key: String, app_id: String):
	if appsflyer_plugin:
		print("[AppsFlyerManager] Initializing AppsFlyer with key: ", dev_key)
		appsflyer_plugin.init(dev_key, app_id)
	else:
		print("[AppsFlyerManager] Plugin not available")
		emit_signal("appsflyer_init_failed", "Plugin not available")


func validate_purchase(public_key: String, signature: String, purchase_data: String, price: float, currency: String):
	if appsflyer_plugin:
		print("[AppsFlyerManager] Validating purchase: ", price, " ", currency)
		appsflyer_plugin.validatePurchase(public_key, signature, purchase_data, price, currency)
	else:
		print("[AppsFlyerManager] Plugin not available")
		emit_signal("purchase_validation_failed", "Plugin not available")

func log_event(event_name: String, event_value: String = ""):
	if appsflyer_plugin:
		print("[AppsFlyerManager] Logging event: ", event_name)
		appsflyer_plugin.logEvent(event_name, event_value)
	else:
		print("[AppsFlyerManager] Plugin not available")
		emit_signal("event_log_failed", "Plugin not available")

func test_plugin():
	if appsflyer_plugin:
		print("[AppsFlyerManager] Testing plugin...")
		appsflyer_plugin.test()
	else:
		print("[AppsFlyerManager] Plugin not available")


func appsflyer_init_success():
	print("[AppsFlyerManager] AppsFlyer initialized successfully")
	emit_signal("appsflyer_init_success")

func appsflyer_init_failed(error_message):
	print("[AppsFlyerManager] AppsFlyer initialization failed: ", error_message)
	emit_signal("appsflyer_init_failed", error_message)

func purchase_validation_success(): 
	print("[AppsFlyerManager] Purchase validation successful")
	emit_signal("purchase_validation_success")
	GlobalStats.remote_log(str("iap_validate_success!!!!!"))

func purchase_validation_failed(error_message):
	print("[AppsFlyerManager] Purchase validation failed: ", error_message)
	emit_signal("purchase_validation_failed", error_message)
	GlobalStats.remote_log(str("iap_validate_faled: ", error_message))

func event_log_success(event_name):
	print("[AppsFlyerManager] Event logged successfully: ", event_name)
	emit_signal("event_log_success", event_name)

func event_log_failed(error_message):
	print("[AppsFlyerManager] Event logging failed: ", error_message)
	emit_signal("event_log_failed", error_message)

func test_signal(message):
	print("[AppsFlyerManager] Test signal received: ", message)
	emit_signal("test_signal", message)

func is_plugin_available() -> bool:
	return appsflyer_plugin != null
