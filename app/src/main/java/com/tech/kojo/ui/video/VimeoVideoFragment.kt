package com.tech.kojo.ui.video

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
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

    // Fullscreen variables
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun getLayoutResource(): Int = R.layout.fragment_vimeo_video

    override fun getViewModel(): BaseViewModel = viewModel

    override fun onCreateView(view: View) {
        initWebView()
        loadVimeoVideo()
        initOnClick()
        handleBackPress()

        val videoId = arguments?.getString("videoId")
        if (videoId != null) {
            val request = HashMap<String, Any>()
            request["timeTaken"] = 200
            request["isViewed"] = true
            viewModel.updateVideoViewStatus("${Constants.UPDATE_VIDEO_VIEW}/$videoId", request)
        }
        initObserver()
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (customView != null) {
                    // If video is fullscreen, exit fullscreen
                    webView.webChromeClient?.onHideCustomView()
                } else {
                    // Otherwise, close activity
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivBack -> requireActivity().finish()
            }
        }
    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.SUCCESS -> {
                    if (it.message == "updateVideoViewStatus") {
                        runCatching {
                            val model = BindingUtils.parseJson<UpdateVideoCountModel>(it.data.toString())
                            if (model?.success == true && model.data != null) {
                                Log.d("video count updated", "Video Count Updated")
                            }
                        }.onFailure { e -> showErrorToast(e.message.toString()) }
                    }
                }
                Status.ERROR -> showErrorToast(it.message.toString())
                else -> {}
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
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                showLoading2()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                hideLoading2()
                injectVimeoPlayerAPI()
            }

            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                if (errorResponse?.statusCode == 403 || errorResponse?.statusCode == 401) {
                    handleVideoError("Domain restricted. Whitelist com.tech.kojo in Vimeo.")
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }

                customView = view
                customViewCallback = callback

                // 1. Hide main layout, show fullscreen container
                binding.mainLayout.visibility = View.GONE
                binding.fullscreenContainer.visibility = View.VISIBLE
                binding.fullscreenContainer.addView(view)

                // 2. Force Landscape for video
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                hideSystemUI()
            }

            override fun onHideCustomView() {
                if (customView == null) return

                // 1. Remove video view, restore layout
                binding.fullscreenContainer.removeView(customView)
                binding.fullscreenContainer.visibility = View.GONE
                binding.mainLayout.visibility = View.VISIBLE

                customView = null
                customViewCallback?.onCustomViewHidden()

                // 2. Restore Portrait
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                showSystemUI()
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) showLoading2() else hideLoading2()
            }
        }

        webView.addJavascriptInterface(VimeoJavaScriptInterface(), "Android")
    }

    private fun loadVimeoVideo() {
        val videoUrl = arguments?.getString("videoUrl")
        // Note the addition of "allowfullscreen" and "webkitallowfullscreen"
        val iFrame = """
            <div style="position:relative;padding-top:56.25%;height:0;overflow:hidden;">
                <iframe src="$videoUrl" 
                    style="position:absolute;top:0;left:0;width:100%;height:100%;" 
                    frameborder="0" 
                    allow="autoplay; fullscreen; picture-in-picture" 
                    allowfullscreen 
                    webkitallowfullscreen 
                    mozallowfullscreen>
                </iframe>
            </div>
            <script src="https://player.vimeo.com/api/player.js"></script>
        """.trimIndent()

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>body { margin: 0; background: black; }</style>
            </head>
            <body>$iFrame</body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL("https://com.tech.kojo", htmlContent, "text/html", "UTF-8", null)
    }

    private fun hideSystemUI() {
        activity?.window?.decorView?.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    private fun showSystemUI() {
        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    private fun injectVimeoPlayerAPI() {
        val script = """
            (function() {
                var iframe = document.querySelector('iframe');
                if (iframe) {
                    var player = new Vimeo.Player(iframe);
                    player.on('play', function() { Android.onVideoEvent('play'); });
                    player.on('pause', function() { Android.onVideoEvent('pause'); });
                    player.on('ended', function() { Android.onVideoEvent('ended'); });
                    player.on('error', function(error) { Android.onVideoError(error.message); });
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(script, null)
    }

    private fun handleVideoError(message: String) {
        activity?.runOnUiThread {
            binding.errorText.text = message
            binding.errorText.visibility = View.VISIBLE
        }
    }

    inner class VimeoJavaScriptInterface {
        @JavascriptInterface
        fun onVideoEvent(event: String) {
            activity?.runOnUiThread { Log.d("VimeoEvent", event) }
        }

        @JavascriptInterface
        fun onVideoError(error: String) {
            handleVideoError(error)
        }
    }

    override fun onPause() {
        super.onPause()
        webView.evaluateJavascript("document.querySelector('iframe').contentWindow.postMessage('{\"method\":\"pause\"}', '*');", null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.destroy()
    }

    private fun showLoading2() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading2() {
        binding.progressBar.visibility = View.GONE
    }
}