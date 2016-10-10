package fr.bmartel.youtubetv;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Youtuve TV custom view.
 *
 * @author Bertrand Martel
 */
public class YoutubeTvView extends WebView {

    private String mVideoId = "EZcJEvXmjfY";

    private final static String TAG = YoutubeTvView.class.getSimpleName();

    //https://developers.google.com/youtube/iframe_api_reference#Playback_quality
    private VideoQuality mVideoQuality = VideoQuality.HD_1080;

    private int mPlayerHeight = 1080;

    private int mPlayerWidth = 1920;

    //check https://developers.google.com/youtube/player_parameters?playerVersion=HTML5

    private int mShowRelatedVideos = 0;

    private int mShowVideoInfo = 0;

    private VideoControls mShowControls = VideoControls.NONE;

    private VideoAutoHide mAutohide = VideoAutoHide.DEFAULT;

    private int mClosedCaptions = 1;

    private int mVideoAnnotation = 1;

    public YoutubeTvView(Context context) {
        super(context);
        init(context);
    }

    public YoutubeTvView(Context context, AttributeSet attrs) {
        super(context, attrs);
        processAttr(context, attrs);
        init(context);
    }

    public YoutubeTvView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        processAttr(context, attrs);
        init(context);
    }

    private void processAttr(final Context context, AttributeSet attrs) {

        TypedArray styledAttr = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.YoutubeTvView,
                0, 0);

        try {
            mVideoId = styledAttr.getString(R.styleable.YoutubeTvView_videoId);
            mVideoQuality = VideoQuality.getVideoQuality(styledAttr.getInteger(R.styleable.YoutubeTvView_videoQuality, VideoQuality.HD_720.getIndex()));
            mPlayerHeight = styledAttr.getInteger(R.styleable.YoutubeTvView_playerHeight, 1080);
            mPlayerWidth = styledAttr.getInteger(R.styleable.YoutubeTvView_playerWidth, 1920);
            mShowRelatedVideos = styledAttr.getBoolean(R.styleable.YoutubeTvView_showRelatedVideos, false) ? 1 : 0;
            mShowVideoInfo = styledAttr.getBoolean(R.styleable.YoutubeTvView_showVideoInfo, false) ? 1 : 0;
            mShowControls = VideoControls.getVideoControls(styledAttr.getInteger(R.styleable.YoutubeTvView_showControls, VideoControls.NONE.getIndex()));
            mClosedCaptions = styledAttr.getBoolean(R.styleable.YoutubeTvView_closedCaptions, false) ? 1 : 0;
            mVideoAnnotation = styledAttr.getBoolean(R.styleable.YoutubeTvView_videoAnnotation, false) ? 1 : 3;
            mAutohide = VideoAutoHide.getVideoControls(styledAttr.getInteger(R.styleable.YoutubeTvView_autoHide, VideoAutoHide.DEFAULT.getIndex()));
        } finally {
            styledAttr.recycle();
        }
    }

    private void init(final Context context) {

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        final WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        setWebChromeClient(new WebChromeClient());
        setPadding(0, 0, 0, 0);
        setInitialScale(getWebviewScale(display));

        JavascriptInterface jsInterface = new JavascriptInterface(this);
        addJavascriptInterface(jsInterface, "JSInterface");

        loadUrl("file:///android_asset/youtube.html" +
                "?videoId=" + mVideoId +
                "&videoQuality=" + mVideoQuality.getValue() +
                "&playerHeight=" + mPlayerHeight +
                "&playerWidth=" + mPlayerWidth +
                "&rel=" + mShowRelatedVideos +
                "&showinfo=" + mShowVideoInfo +
                "&controls=" + mShowControls.getIndex() +
                "&autohide=" + mAutohide.getIndex() +
                "&cc_load_policy=" + mClosedCaptions +
                "&iv_load_policy=" + mVideoAnnotation);
    }

    private int getWebviewScale(final Display display) {
        int width = display.getWidth();
        Double val = new Double(width) / new Double(1920);
        val = val * 100d;
        return val.intValue();
    }

    private void callJavaScript(String methodName, Object... params) {

        StringBuilder stringBuilder = new StringBuilder();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            stringBuilder.append("javascript:try{");
        }
        stringBuilder.append(methodName);
        stringBuilder.append("(");
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if (param instanceof String) {
                stringBuilder.append("'");
                stringBuilder.append(param);
                stringBuilder.append("'");
            }
            if (i < params.length - 1) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append(")}catch(error){Android.onError(error.message);}");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(stringBuilder.toString(), new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String result) {
                    Log.i(TAG, "received : " + result);
                }
            });
        } else {
            loadUrl(stringBuilder.toString());
        }

        loadUrl(stringBuilder.toString());
    }
}