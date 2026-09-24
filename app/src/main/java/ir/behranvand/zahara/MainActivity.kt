package ir.behranvand.zahara

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var webView: WebView

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val WEBSITE_URL = "https://behranvand.ir/zahara/"
        private const val FILE_CHOOSER_REQUEST = 1001
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)

        setContentView(webView)

        setupWebView()

        if (savedInstanceState == null) {
            webView.loadUrl(WEBSITE_URL)
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {

        val settings = webView.settings

        // JavaScript
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        // Database / storage
        settings.databaseEnabled = true

        // Zoom
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false

        // Viewport
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = false

        // Cache
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        // File access
        settings.allowFileAccess = true
        settings.allowContentAccess = true

        // Cookies
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        /*
         * WebViewClient
         *
         * مهم:
         * اینجا دیگر هیچ‌وقت null قرار نمی‌دهیم.
         */
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {

                val url = request.url.toString()

                return handleUrl(url)
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(
                view: WebView,
                url: String
            ): Boolean {

                return handleUrl(url)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)

                if (request.isForMainFrame) {
                    Toast.makeText(
                        this@MainActivity,
                        "اتصال به سایت برقرار نشد",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        /*
         * JavaScript dialogs / file chooser
         */
        webView.webChromeClient = object : WebChromeClient() {

            override fun onShowFileChooser(
                webView: WebView?,
                filePath: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {

                filePathCallback?.onReceiveValue(null)

                filePathCallback = filePath

                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(
                        Intent.EXTRA_MIME_TYPES,
                        arrayOf(
                            "application/vnd.ms-excel",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "application/octet-stream"
                        )
                    )
                }

                return try {
                    startActivityForResult(
                        Intent.createChooser(intent, "انتخاب فایل اکسل"),
                        FILE_CHOOSER_REQUEST
                    )
                    true
                } catch (e: Exception) {
                    filePathCallback?.onReceiveValue(null)
                    filePathCallback = null
                    false
                }
            }
        }

        /*
         * Download support
         */
        webView.setDownloadListener(
            DownloadListener { url, userAgent, contentDisposition, mimeType, _ ->

                try {

                    val request = DownloadManager.Request(Uri.parse(url))

                    request.setMimeType(mimeType)

                    val cookies = CookieManager
                        .getInstance()
                        .getCookie(url)

                    if (!cookies.isNullOrEmpty()) {
                        request.addRequestHeader("Cookie", cookies)
                    }

                    request.addRequestHeader("User-Agent", userAgent)

                    request.setDescription("در حال دانلود فایل...")
                    request.setTitle(
                        URLUtilHelper.guessFileName(
                            url,
                            contentDisposition,
                            mimeType
                        )
                    )

                    request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    )

                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        URLUtilHelper.guessFileName(
                            url,
                            contentDisposition,
                            mimeType
                        )
                    )

                    val downloadManager =
                        getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                    downloadManager.enqueue(request)

                    Toast.makeText(
                        this@MainActivity,
                        "دانلود شروع شد",
                        Toast.LENGTH_SHORT
                    ).show()

                } catch (e: Exception) {

                    Toast.makeText(
                        this@MainActivity,
                        "دانلود فایل انجام نشد",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun handleUrl(url: String): Boolean {

        /*
         * لینک‌های سایت داخل WebView باز شوند.
         */
        if (
            url.startsWith("https://behranvand.ir") ||
            url.startsWith("http://behranvand.ir")
        ) {
            webView.loadUrl(url)
            return true
        }

        /*
         * لینک‌های tel:
         */
        if (url.startsWith("tel:")) {
            try {
                startActivity(
                    Intent(Intent.ACTION_DIAL, Uri.parse(url))
                )
            } catch (_: Exception) {
            }

            return true
        }

        /*
         * ایمیل
         */
        if (url.startsWith("mailto:")) {
            try {
                startActivity(
                    Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                )
            } catch (_: Exception) {
            }

            return true
        }

        /*
         * لینک‌های خارجی در مرورگر سیستم
         */
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (_: Exception) {
        }

        return true
    }

    override fun onBackPressed() {

        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {

        filePathCallback?.onReceiveValue(null)
        filePathCallback = null

        webView.stopLoading()
        webView.webChromeClient = null
        webView.webViewClient = WebViewClient()

        webView.destroy()

        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != FILE_CHOOSER_REQUEST) {
            return
        }

        val callback = filePathCallback
        filePathCallback = null

        if (callback == null) {
            return
        }

        if (resultCode == RESULT_OK && data != null) {

            val result = data.data

            if (result != null) {
                callback.onReceiveValue(arrayOf(result))
            } else {
                callback.onReceiveValue(null)
            }

        } else {
            callback.onReceiveValue(null)
        }
    }
}

/*
 * Helper برای نام فایل دانلودی
 */
private object URLUtilHelper {

    fun guessFileName(
        url: String,
        contentDisposition: String?,
        mimeType: String?
    ): String {

        var fileName = "download"

        try {
            val uri = Uri.parse(url)

            uri.lastPathSegment?.let {
                if (it.isNotBlank()) {
                    fileName = it
                }
            }

        } catch (_: Exception) {
        }

        if (
            !fileName.contains(".") &&
            !mimeType.isNullOrBlank()
        ) {
            fileName += when {
                mimeType.contains("spreadsheet") -> ".xlsx"
                mimeType.contains("excel") -> ".xls"
                mimeType.contains("pdf") -> ".pdf"
                mimeType.contains("zip") -> ".zip"
                else -> ""
            }
        }

        return fileName
    }
}
