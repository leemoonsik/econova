package kr.co.livemoa.imetest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 시험용: 앱 안(WebView)에서 라이브 주소를 열고, 키보드가 뜨면 키보드 바로 위에 +Live 버튼을 띄운다.
 * 버튼 위치는 접근성 서비스가 아니라 이 앱 화면 배치(adjustResize)로 정해진다.
 */
public class LiveWebActivity extends Activity {
    private static final String UA_CHROME = "Mozilla/5.0 (Linux; Android 13; SM-S918N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
    private WebView web;
    private EditText urlBox;
    private TextView plus, state;
    private FrameLayout root;
    private boolean kbVisible = false;
    private int kbHeight = 0;
    private String lastBlocked = "-";
    private String lastError = "-";
    private boolean chromeUa = false;

    private int dp(float v) { return Math.round(getResources().getDisplayMetrics().density * v); }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setPadding(dp(6), dp(30), dp(6), dp(2));
        urlBox = new EditText(this);
        urlBox.setSingleLine(true);
        urlBox.setTextSize(13);
        urlBox.setText("https://www.tiktok.com/live");
        urlBox.setImeOptions(EditorInfo.IME_ACTION_GO);
        urlBox.setOnEditorActionListener((v, a, e) -> { go(); return true; });
        bar.addView(urlBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        Button goBtn = new Button(this);
        goBtn.setText("이동");
        goBtn.setOnClickListener(v -> go());
        bar.addView(goBtn);
        Button uaBtn = new Button(this);
        uaBtn.setText("UA");
        uaBtn.setOnClickListener(v -> { chromeUa = !chromeUa; applyUa(); web.reload(); updateState(); });
        bar.addView(uaBtn);
        col.addView(bar);

        state = new TextView(this);
        state.setTextSize(11);
        state.setPadding(dp(8), 0, dp(8), dp(2));
        col.addView(state);

        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, true);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                String u = r.getUrl().toString();
                if (u.startsWith("http://") || u.startsWith("https://")) return false;
                lastBlocked = u.length() > 60 ? u.substring(0, 60) + "…" : u; // 앱 열기(intent://, snssdk:// 등) 시도는 막고 기록
                updateState();
                return true;
            }
            @Override public void onPageFinished(WebView v, String url) { updateState(); }
            @Override public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                if (r.isForMainFrame()) { lastError = e.getErrorCode() + " " + e.getDescription(); updateState(); }
            }
        });
        col.addView(web, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(col, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        plus = new TextView(this);
        plus.setText("+Live");
        plus.setTextSize(15);
        plus.setTextColor(Color.WHITE);
        plus.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(31, 98, 200));
        bg.setCornerRadius(dp(16));
        plus.setBackground(bg);
        plus.setElevation(dp(6));
        plus.setVisibility(android.view.View.GONE);
        plus.setOnClickListener(v -> {
            Intent i = new Intent(this, OrderTestActivity.class);
            i.putExtra("seller", web.getUrl());
            startActivity(i);
        });
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(96), dp(42), Gravity.BOTTOM | Gravity.END);
        lp.setMargins(0, 0, dp(12), dp(8)); // 이 앱 화면의 맨 아래 = 키보드 바로 위
        root.addView(plus, lp);

        root.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            root.getWindowVisibleDisplayFrame(r);
            int screenH = root.getRootView().getHeight();
            int h = screenH - r.bottom;
            boolean vis = h > screenH * 0.15f;
            if (vis != kbVisible || Math.abs(h - kbHeight) > 20) {
                kbVisible = vis; kbHeight = vis ? h : 0;
                plus.setVisibility(vis ? android.view.View.VISIBLE : android.view.View.GONE);
                updateState();
            }
        });

        setContentView(root);
        applyUa();
        updateState();
        go();
    }

    private void applyUa() { web.getSettings().setUserAgentString(chromeUa ? UA_CHROME : null); }

    private void go() {
        String u = urlBox.getText().toString().trim();
        if (u.isEmpty()) return;
        if (!u.startsWith("http")) u = "https://" + u;
        web.loadUrl(u);
        urlBox.clearFocus();
        updateState();
    }

    private void updateState() {
        state.setText("키보드: " + (kbVisible ? "보임(" + kbHeight + "px)" : "숨김")
            + " | UA: " + (chromeUa ? "크롬형" : "기본")
            + "\n막은 앱열기: " + lastBlocked + " | 오류: " + lastError
            + "\n주소: " + (web == null ? "" : String.valueOf(web.getUrl())));
    }

    @Override public boolean onKeyDown(int code, KeyEvent e) {
        if (code == KeyEvent.KEYCODE_BACK && web.canGoBack()) { web.goBack(); return true; }
        return super.onKeyDown(code, e);
    }
}
