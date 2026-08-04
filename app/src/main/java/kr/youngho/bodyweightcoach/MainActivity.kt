package kr.youngho.bodyweightcoach

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebViewAssetLoader
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var repository: HealthConnectRepository
    private var pendingBackup: Pair<String, String>? = null
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val permissionLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        val all = granted.containsAll(HealthConnectRepository.REQUIRED_PERMISSIONS)
        sendStatus(if (all) "권한이 연결되었습니다." else "일부 권한이 허용되지 않았습니다.")
        if (all) syncWorkouts(30)
    }

    private val createBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val data = pendingBackup
        pendingBackup = null
        if (uri != null && data != null) {
            runCatching {
                contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(data.first) }
            }.onSuccess { notifyJs("onNativeMessage", JSONObject().put("message", "백업 파일을 저장했습니다.").toString()) }
             .onFailure { sendError("백업 저장 실패: ${it.localizedMessage}") }
        }
    }

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        filePathCallback?.onReceiveValue(data)
        filePathCallback = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repository = HealthConnectRepository(this)
        webView = findViewById(R.id.webView)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = true
            setSupportZoom(false)
            mediaPlaybackRequiresUserGesture = true
        }
        webView.addJavascriptInterface(AndroidHealthBridge(), "AndroidHealth")
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                assetLoader.shouldInterceptRequest(request.url)

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return if (request.url.host == "appassets.androidplatform.net") false
                else {
                    startActivity(Intent(Intent.ACTION_VIEW, request.url))
                    true
                }
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?,
            ): Boolean {
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback
                val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "application/json"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                return runCatching { fileChooserLauncher.launch(intent); true }.getOrElse {
                    this@MainActivity.filePathCallback = null
                    false
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) sendStatus()
    }

    private inner class AndroidHealthBridge {
        @JavascriptInterface fun requestStatus() = runOnUiThread { sendStatus() }
        @JavascriptInterface fun requestPermissions() = runOnUiThread {
            when (repository.sdkStatus()) {
                HealthConnectClient.SDK_AVAILABLE -> permissionLauncher.launch(HealthConnectRepository.REQUIRED_PERMISSIONS)
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> openProviderStore()
                else -> sendError("이 기기에서는 Health Connect를 사용할 수 없습니다.")
            }
        }
        @JavascriptInterface fun syncWorkouts(days: Int) = runOnUiThread { this@MainActivity.syncWorkouts(days) }
        @JavascriptInterface fun openHealthConnectSettings() = runOnUiThread { openHealthSettings() }
        @JavascriptInterface fun saveBackup(json: String, fileName: String) = runOnUiThread {
            pendingBackup = json to fileName
            createBackupLauncher.launch(fileName)
        }
    }

    private fun sendStatus(messageOverride: String? = null) {
        lifecycleScope.launch {
            val status = repository.sdkStatus()
            val result = JSONObject()
            when (status) {
                HealthConnectClient.SDK_AVAILABLE -> {
                    val granted = runCatching { repository.hasAllPermissions() }.getOrDefault(false)
                    result.put("status", "available")
                    result.put("permissionsGranted", granted)
                    result.put("message", messageOverride ?: if (granted) "운동·거리·심박수·칼로리 읽기 권한이 연결되어 있습니다." else "Health Connect 권한 연결이 필요합니다.")
                }
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    result.put("status", "update_required")
                    result.put("permissionsGranted", false)
                    result.put("message", messageOverride ?: "Health Connect 설치 또는 업데이트가 필요합니다.")
                }
                else -> {
                    result.put("status", "unavailable")
                    result.put("permissionsGranted", false)
                    result.put("message", messageOverride ?: "이 기기에서는 Health Connect를 사용할 수 없습니다.")
                }
            }
            notifyJs("onHealthStatus", result.toString())
        }
    }

    private fun syncWorkouts(days: Int) {
        lifecycleScope.launch {
            runCatching { repository.readRunningAndSwimming(days.coerceIn(1, 30)) }
                .onSuccess { workouts ->
                    val array = JSONArray()
                    workouts.forEach { array.put(it.toJson()) }
                    notifyJs("onHealthWorkouts", JSONObject().put("workouts", array).toString())
                }
                .onFailure { sendError("동기화 실패: ${it.localizedMessage ?: it.javaClass.simpleName}") }
        }
    }

    private fun notifyJs(function: String, payload: String) {
        webView.post {
            val quoted = JSONObject.quote(payload)
            webView.evaluateJavascript("window.$function && window.$function($quoted);", null)
        }
    }

    private fun sendError(message: String) {
        notifyJs("onHealthError", JSONObject().put("message", message).toString())
    }

    private fun openProviderStore() {
        val market = Uri.parse("market://details?id=${HealthConnectRepository.PROVIDER_PACKAGE}")
        val web = Uri.parse("https://play.google.com/store/apps/details?id=${HealthConnectRepository.PROVIDER_PACKAGE}")
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, market)) }
            .recoverCatching { startActivity(Intent(Intent.ACTION_VIEW, web)) }
    }

    private fun openHealthSettings() {
        val intent = HealthConnectClient.getHealthConnectManageDataIntent(this)
        runCatching { startActivity(intent) }.onFailure { openProviderStore() }
    }
}
