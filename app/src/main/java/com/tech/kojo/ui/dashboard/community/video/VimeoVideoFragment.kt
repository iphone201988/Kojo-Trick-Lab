package com.tech.kojo.ui.dashboard.community.video

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetVideoByIdResponse
import com.tech.kojo.data.model.UpdateVideoCountModel
import com.tech.kojo.databinding.FragmentVimeoVideoBinding
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VimeoVideoFragment : BaseFragment<FragmentVimeoVideoBinding>() {

    private val viewModel: VideoFragmentVM by viewModels()

    private lateinit var webView: WebView

    override fun getLayoutResource(): Int {
        return R.layout.fragment_vimeo_video
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initWebView()
        loadVimeoVideo()
        initOnClick()
        val videoId = arguments?.getString("videoId")
        if (videoId!=null){
            val request = HashMap<String, Any>()
            request["timeTaken"]=200
            request["isViewed"]=true
            viewModel.updateVideoViewStatus("${Constants.UPDATE_VIDEO_VIEW}/$videoId",request)
        }
        initObserver()
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivBack -> requireActivity().finish()
            }
        }
    }

    private fun initObserver(){
        viewModel.observeCommon.observe(viewLifecycleOwner){
            when(it?.status){
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    when(it.message){
                        "updateVideoViewStatus"->{
                                runCatching {
                                    val model =
                                        BindingUtils.parseJson<UpdateVideoCountModel>(it.data.toString())
                                    if (model?.success == true && model.data != null) {
                                        Log.d("video count updated","Video Count Updated")
                                    }
                                }.onFailure { e ->
                                    showErrorToast(e.message.toString())
                                }.also {
                                }
                        }
                    }
                }
                Status.ERROR -> {
                    showErrorToast(it.message.toString())
                }

                else -> {

                }
            }
        }
    }

    private fun initWebView() {
        webView = binding.webView

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = true
            allowContentAccess = true
            allowUniversalAccessFromFileURLs = true
            allowFileAccessFromFileURLs = true

            // Set user agent
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

            // Enable mixed content
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                showLoading2()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideLoading2()
                injectVimeoPlayerAPI()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                error?.let {
//                    Log.e("web err","WebView error: ${it.description}")
//                    handleVideoError("Failed to load video: ${it.description}")
                }
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)

                if (errorResponse?.statusCode == 403 || errorResponse?.statusCode == 401) {
                    handleVideoError("This video is domain restricted. Please check if com.tech.kojo is whitelisted in Vimeo.")
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    showLoading2()
                } else {
                    hideLoading2()
                }
            }
        }

        webView.addJavascriptInterface(VimeoJavaScriptInterface(), "Android")
    }

    private fun loadVimeoVideo() {
       val iFrame ="<div style=\"padding:56.25% 0 0 0;position:relative;\"><iframe src=\"https://player.vimeo.com/video/1173393599?badge=0&amp;autopause=0&amp;player_id=0&amp;app_id=58479\" frameborder=\"0\" allow=\"autoplay; fullscreen; picture-in-picture; clipboard-write; encrypted-media; web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" style=\"position:absolute;top:0;left:0;width:100%;height:100%;\" title=\"Aerial Saturn\"></iframe></div><script src=\"https://player.vimeo.com/api/player.js\"></script>"
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        background: black;
                        overflow: hidden;
                        width: 100vw;
                        height: 100vh;
                    }
                    .video-container {
                        position: relative;
                        width: 100%;
                        height: 100%;
                        background: black;
                    }
                    iframe {
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 100%;
                        border: 0;
                    }
                </style>
            </head>
            <body>
            $iFrame
            </body>
            </html>
        """.trimIndent()

        // IMPORTANT: Use your whitelisted domain as base URL
        val baseUrl = "https://com.tech.kojo"

        webView.loadDataWithBaseURL(
            baseUrl,
            htmlContent,
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun injectVimeoPlayerAPI() {
        val script = """
            (function() {
                try {
                    var iframe = document.querySelector('iframe');
                    if (iframe) {
                        var player = new Vimeo.Player(iframe);
                        
                        player.on('play', function() {
                            Android.onVideoEvent('play');
                        });
                        
                        player.on('pause', function() {
                            Android.onVideoEvent('pause');
                        });
                        
                        player.on('ended', function() {
                            Android.onVideoEvent('ended');
                        });
                        
                        player.on('error', function(error) {
                            Android.onVideoError(error.message);
                        });
                        
                        player.ready().then(function() {
                            Android.onVideoEvent('ready');
                        }).catch(function(error) {
                            Android.onVideoError(error.message);
                        });
                    }
                } catch(e) {
                    Android.onVideoError(e.toString());
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(script, null)
    }

    private fun handleVideoError(message: String) {
        Log.e("err","Video Error: $message")
        showError(message)
    }

    inner class VimeoJavaScriptInterface {
        @JavascriptInterface
        fun onVideoEvent(event: String) {
            activity?.runOnUiThread {
                when (event) {
                    "play" -> Log.d("Video playing","Video playing")
                    "pause" -> Log.d("Video paused","Video paused")
                    "ended" -> {
                        Log.d("Video ended","Video ended")
                    }
                    "ready" -> Log.d("Video ready","Video ready")
                }
            }
        }

        @JavascriptInterface
        fun onVideoError(error: String) {
            activity?.runOnUiThread {
                handleVideoError(error)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        webView.evaluateJavascript("""
            (function() {
                var iframe = document.querySelector('iframe');
                if (iframe) {
                    try {
                        var player = new Vimeo.Player(iframe);
                        player.pause();
                    } catch(e) {}
                }
            })();
        """, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.loadUrl("about:blank")
        webView.stopLoading()
        webView.destroy()
    }

    private fun showLoading2() {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE
    }

    private fun hideLoading2() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        // Keep webView visible but might show error overlay
    }
}

//    @SuppressLint("SetJavaScriptEnabled")
//    private fun loadVimeoVideo() {
//
//        val url = "https://player.vimeo.com/video/1172852925?badge=0&autopause=0&player_id=0&app_id=58479&autoplay=1&muted=1&loop=1"
//
//        binding.webView.settings.apply {
//            javaScriptEnabled = true
//            domStorageEnabled = true
//            mediaPlaybackRequiresUserGesture = false // important for autoplay
//        }
//
//        binding.webView.webViewClient = WebViewClient()
//
//        binding.webView.loadUrl(url)
//    }

//    @SuppressLint("SetJavaScriptEnabled")
//    private fun loadVimeoIframe() {
//
//        val html = """
//        <html>
//        <head>
//            <meta name="viewport" content="width=device-width, initial-scale=1.0">
//        </head>
//        <body style="margin:0;padding:0;">
//
//            <div style="padding:56.25% 0 0 0;position:relative;">
//                <iframe
//                    src="https://player.vimeo.com/video/1172852925?badge=0&autopause=0&player_id=0&app_id=58479&autoplay=1&muted=1&loop=1"
//                    frameborder="0"
//                    allow="autoplay; fullscreen; picture-in-picture"
//                    allowfullscreen
//                    style="position:absolute;top:0;left:0;width:100%;height:100%;">
//                </iframe>
//            </div>
//
//        </body>
//        </html>
//    """.trimIndent()
//
//        binding.webView.settings.apply {
//            javaScriptEnabled = true
//            domStorageEnabled = true
//            mediaPlaybackRequiresUserGesture = false
//            loadWithOverviewMode = true
//            useWideViewPort = true
//        }
//
//        binding.webView.webViewClient = WebViewClient()
//
//        binding.webView.loadDataWithBaseURL(
//            null,
//            html,
//            "text/html",
//            "utf-8",
//            null
//        )
//    }

//    @SuppressLint("SetJavaScriptEnabled")
//    fun loadDynamicVimeoIframe(rawHtml: String) {
//
//        // ❌ Remove script tag (can break WebView)
//        val cleanHtml = rawHtml.replace(
//            Regex("<script.*?</script>", RegexOption.DOT_MATCHES_ALL),
//            ""
//        )
//
//        val finalHtml = """
//        <html>
//        <head>
//            <meta name="viewport" content="width=device-width, initial-scale=1.0">
//        </head>
//        <body style="margin:0;padding:0;">
//            $cleanHtml
//        </body>
//        </html>
//    """.trimIndent()
//
//        binding.webView.settings.apply {
//            javaScriptEnabled = true
//            domStorageEnabled = true
//            mediaPlaybackRequiresUserGesture = false
//            loadWithOverviewMode = true
//            useWideViewPort = true
//        }
//
//        binding.webView.webViewClient = WebViewClient()
//
//        binding.webView.loadDataWithBaseURL(
//            null,
//            finalHtml,
//            "text/html",
//            "utf-8",
//            null
//        )
//    }
