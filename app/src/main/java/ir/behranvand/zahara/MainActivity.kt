package ir.behranvand.zahara

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
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

    private fun setupWebView() {

        val settings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true

        settings.allowFileAccess = true
        settings.allowContentAccess = true

        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false

        settings.useWideViewPort = true
        settings.loadWithOverviewMode = false

        val cookies = CookieManager.getInstance()
        cookies.setAcceptCookie(true)
        cookies.setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {

                return openUrl(request.url.toString())
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(
                view: WebView,
                url: String
            ): Boolean {

                return openUrl(url)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {

            override fun onShowFileChooser(
                webView: WebView,
                filePath: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {

                this@MainActivity.filePathCallback?.onReceiveValue(null)

                this@MainActivity.filePathCallback = filePath

                val intent = Intent(Intent.ACTION_GET_CONTENT)

                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"

                try {
                    startActivityForResult(
                        Intent.createChooser(
                            intent,
                            "انتخاب فایل"
                        ),
                        FILE_CHOOSER_REQUEST
                    )

                    return true

                } catch (e: Exception) {

                    this@MainActivity.filePathCallback = null
                    return false
                }
            }
        }

        webView.setDownloadListener(
            DownloadListener { url, userAgent, contentDisposition, mimeType, _ ->

                try {

                    val request =
                        DownloadManager.Request(Uri.parse(url))

                    request.setMimeType(mimeType)

                    val cookie =
                        CookieManager
                            .getInstance()
                            .getCookie(url)

                    if (!cookie.isNullOrEmpty()) {
                        request.addRequestHeader(
                            "Cookie",
                            cookie
                        )
                    }

                    request.addRequestHeader(
                        "User-Agent",
                        userAgent
                    )

                    val fileName =
                        android.webkit.URLUtil.guessFileName(
                            url,
                            contentDisposition,
                            mimeType
                        )

                    request.setTitle(fileName)
                    request.setDescription("در حال دانلود فایل...")

                    request.setNotificationVisibility(
                        DownloadManager.Request
                            .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    )

                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        fileName
                    )

                    val manager =
                        getSystemService(
                            Context.DOWNLOAD_SERVICE
                        ) as DownloadManager

                    manager.enqueue(request)

                    Toast.makeText(
                        this@MainActivity,
                        "دانلود شروع شد",
                        Toast.LENGTH_SHORT
                    ).show()

                } catch (e: Exception) {

                    Toast.makeText(
                        this@MainActivity,
                        "دانلود انجام نشد",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun openUrl(url: String): Boolean {

        if (
            url.startsWith("https://behranvand.ir") ||
            url.startsWith("http://behranvand.ir")
        ) {
            return false
        }

        if (url.startsWith("tel:")) {

            try {
                startActivity(
                    Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse(url)
                    )
                )
            } catch (e: Exception) {
            }

            return true
        }

        if (url.startsWith("mailto:")) {

            try {
                startActivity(
                    Intent(
                        Intent.ACTION_SENDTO,
                        Uri.parse(url)
                    )
                )
            } catch (e: Exception) {
            }

            return true
        }

        try {

            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )

        } catch (e: Exception) {
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
        webView.destroy()

        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (requestCode != FILE_CHOOSER_REQUEST) {
            return
        }

        val callback = filePathCallback
        filePathCallback = null

        if (callback == null) {
            return
        }

        if (
            resultCode == RESULT_OK &&
            data != null &&
            data.data != null
        ) {

            callback.onReceiveValue(
                arrayOf(data.data!!)
            )

        } else {

            callback.onReceiveValue(null)
        }
    }
}
