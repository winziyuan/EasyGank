/*
 * {EasyGank}  Copyright (C) {2015}  {CaMnter}
 *
 * This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
 * This is free software, and you are welcome to redistribute it
 * under certain conditions; type `show c' for details.
 *
 * The hypothetical commands `show w' and `show c' should show the appropriate
 * parts of the General Public License.  Of course, your program's commands
 * might be different; for a GUI interface, you would use an "about box".
 *
 * You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU GPL, see
 * <http://www.gnu.org/licenses/>.
 *
 * The GNU General Public License does not permit incorporating your program
 * into proprietary programs.  If your program is a subroutine library, you
 * may consider it more useful to permit linking proprietary applications with
 * the library.  If this is what you want to do, use the GNU Lesser General
 * Public License instead of this License.  But first, please read
 * <http://www.gnu.org/philosophy/why-not-lgpl.html>.
 */

package com.camnter.easygank.views;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.camnter.easygank.R;
import com.camnter.easygank.core.BaseToolbarActivity;
import com.camnter.easygank.gank.GankType;
import com.camnter.easygank.gank.GankTypeDict;
import com.camnter.easygank.utils.IntentUtils;
import com.camnter.easygank.utils.ToastUtils;
import com.camnter.easygank.utils.WebViewUtils;

/**
 * Description：EasyWebViewActivity
 * Created by：CaMnter
 * Time：2016-01-08 16:43
 */
public class EasyWebViewActivity extends BaseToolbarActivity {

    private static final String EXTRA_URL = "com.camnter.easygank.EXTRA_URL";
    private static final String EXTRA_TITLE = "com.camnter.easygank.EXTRA_TITLE";

    // For gank api
    private static final String EXTRA_GANK_TYPE = "com.camnter.easygank.EXTRA_GANK_TYPE";

    private static final int PROGRESS_RATIO = 1000;

    private WebView mWebView;
    private ProgressBar mProgressBar;

    private boolean goBack = false;
    private static final int RESET_GO_BACK_INTERVAL = 2666;
    private static final int MSG_WHAT_RESET_GO_BACK = 206;
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case EasyWebViewActivity.MSG_WHAT_RESET_GO_BACK:
                    EasyWebViewActivity.this.goBack = false;
                    return true;
            }
            return false;
        }
    });


    /**
     * @param context Any context
     * @param url     A valid url to navigate to
     */
    public static void toUrl(Context context, String url) {
        toUrl(context, url, android.R.string.untitled);
    }

    /**
     * @param context    Any context
     * @param url        A valid url to navigate to
     * @param titleResId A String resource to display as the title
     */
    public static void toUrl(Context context, String url, int titleResId) {
        toUrl(context, url, context.getString(titleResId));
    }

    /**
     * @param context Any context
     * @param url     A valid url to navigate to
     * @param title   A title to display
     */
    public static void toUrl(Context context, String url, String title) {
        Intent intent = new Intent(context, EasyWebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_TITLE, title);
        context.startActivity(intent);
    }

    /**
     * For gank api
     *
     * @param context context
     * @param url     url
     * @param title   title
     * @param type    type
     */
    public static void toUrl(Context context, String url, String title, String type) {
        Intent intent = new Intent(context, EasyWebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_GANK_TYPE, type);
        context.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_web_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.web_refresh:
                this.refreshWebView();
                return true;
            case R.id.web_copy:
                ClipData clipData = ClipData.newPlainText(this.getString(R.string.app_name), this.mWebView.getUrl());
                ClipboardManager clipboardManager = (ClipboardManager) this.getSystemService(
                        Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clipData);
                Snackbar.make(this.mWebView, this.getString(R.string.common_copy_success), Snackbar.LENGTH_SHORT).show();
                return true;
            case R.id.web_switch_screen_mode:
                this.switchScreenConfiguration(item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.setAppBarLayoutVisibility(true);
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.setAppBarLayoutVisibility(false);
        }
    }

    public void switchScreenConfiguration(MenuItem item) {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            if (item != null) item.setTitle(this.getString(R.string.menu_web_vertical));
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            if (item != null) item.setTitle(this.getString(R.string.menu_web_horizontal));
        }
    }

    protected void refreshWebView() {
        this.mWebView.reload();
    }

    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_webview;
    }

    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        this.mWebView = this.findView(R.id.webview);
        this.mProgressBar = (ProgressBar) this.findViewById(R.id.webview_pb);
    }

    /**
     * Initialize the View of the listener
     */
    @Override
    protected void initListeners() {

    }

    /**
     * Initialize the Activity data
     */
    @Override
    protected void initData() {
        this.enableJavascript();
        this.enableCaching();
        this.enableCustomClients();
        this.enableAdjust();
        this.zoomedOut();
        this.mWebView.loadUrl(this.getUrl());
        this.showBack();
        this.setTitle(this.getUrlTitle());
        if (GankTypeDict.urlType2TypeDict.get(this.getGankType()) == GankType.video) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
    }


    private void enableCustomClients() {
        this.mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            /**
             * @param view The WebView that is initiating the callback.
             * @param url  The url of the page.
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.contains("www.vmovier.com")) {
                    WebViewUtils.injectCSS(EasyWebViewActivity.this, EasyWebViewActivity.this.mWebView, "vmovier.css");
                } else if (url.contains("video.weibo.com")) {
                    WebViewUtils.injectCSS(EasyWebViewActivity.this, EasyWebViewActivity.this.mWebView, "weibo.css");
                } else if (url.contains("m.miaopai.com")) {
                    WebViewUtils.injectCSS(EasyWebViewActivity.this, EasyWebViewActivity.this.mWebView, "miaopai.css");
                }
            }
        });
        this.mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                EasyWebViewActivity.this.mProgressBar.setProgress(progress);
                setProgress(progress * PROGRESS_RATIO);
                if (progress >= 80) {
                    EasyWebViewActivity.this.mProgressBar.setVisibility(View.GONE);
                } else {
                    EasyWebViewActivity.this.mProgressBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void enableJavascript() {
        this.mWebView.getSettings().setJavaScriptEnabled(true);
        this.mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
    }

    private void enableCaching() {
        this.mWebView.getSettings().setAppCachePath(getFilesDir() + getPackageName() + "/cache");
        this.mWebView.getSettings().setAppCacheEnabled(true);
        this.mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    private void enableAdjust() {
        this.mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        this.mWebView.getSettings().setLoadWithOverviewMode(true);
    }

    private void zoomedOut() {
        this.mWebView.getSettings().setLoadWithOverviewMode(true);
        this.mWebView.getSettings().setUseWideViewPort(true);
        this.mWebView.getSettings().setSupportZoom(true);
    }


    private String getUrl() {
        return IntentUtils.getStringExtra(this.getIntent(), EXTRA_URL);
    }

    private String getUrlTitle() {
        return IntentUtils.getStringExtra(this.getIntent(), EXTRA_TITLE);
    }

    /**
     * For gank api
     *
     * @return tyep
     */
    private String getGankType() {
        return IntentUtils.getStringExtra(this.getIntent(), EXTRA_GANK_TYPE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mWebView != null) this.mWebView.destroy();
    }

    /**
     * Take care of calling onBackPressed() for pre-Eclair platforms.
     *
     * @param keyCode keyCode
     * @param event   event
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (GankTypeDict.urlType2TypeDict.get(this.getGankType()) == GankType.video) {
                if (this.goBack) {
                    this.finish();
                } else {
                    this.goBack = true;
                    Message msg = this.mHandler.obtainMessage();
                    msg.what = MSG_WHAT_RESET_GO_BACK;
                    this.mHandler.sendMessageDelayed(msg, RESET_GO_BACK_INTERVAL);
                    ToastUtils.show(this, this.getString(R.string.common_go_back_tip), ToastUtils.LENGTH_SHORT);
                }
            } else {
                // 横屏优先级高
                if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    this.switchScreenConfiguration(null);
                    return true;
                } else {
                    this.finish();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
