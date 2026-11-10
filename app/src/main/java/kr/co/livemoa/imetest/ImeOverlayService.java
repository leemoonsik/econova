package kr.co.livemoa.imetest;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.TextView;
import java.util.List;

/**
 * Proof-of-concept. Reads accessibility WINDOW metadata ONLY, never view text.
 * Places a small button at the IME window's top boundary.
 */
public class ImeOverlayService extends AccessibilityService {
    static volatile boolean connected = false;
    static volatile int eventCount = 0;
    static volatile String lastInfo = "아직 이벤트 없음";
    private WindowManager wm;
    private TextView button;
    private WindowManager.LayoutParams params;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = this::refreshOverlay;
    private final int margin = 6;
    private int dp(float value) { return Math.round(getResources().getDisplayMetrics().density * value); }

    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        connected = true;
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        scheduleUpdate();
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { eventCount++; scheduleUpdate(); }
    @Override public boolean onUnbind(Intent intent) { connected = false; return super.onUnbind(intent); }
    @Override public void onInterrupt() { hide(); }
    @Override public void onDestroy() { handler.removeCallbacks(refreshRunnable); hide(); super.onDestroy(); }
    private void scheduleUpdate() { handler.removeCallbacks(refreshRunnable); handler.postDelayed(refreshRunnable, 120); }

    private void refreshOverlay() {
        List<AccessibilityWindowInfo> windows = getWindows();
        Rect imeBounds = null;
        StringBuilder info = new StringBuilder();
        info.append("창 개수: ").append(windows == null ? "null" : windows.size());
        if (windows != null) {
            for (AccessibilityWindowInfo w : windows) {
                Rect r0 = new Rect();
                w.getBoundsInScreen(r0);
                info.append("\n - 종류 ").append(w.getType()).append(" ").append(r0.toShortString());
                if (w.getType() == AccessibilityWindowInfo.TYPE_INPUT_METHOD) {
                    Rect rect = new Rect();
                    w.getBoundsInScreen(rect);
                    // Ignore empty / non-visible IME candidates
                    if (rect.width() > dp(120) && rect.height() > dp(60)) {
                        imeBounds = rect;
                        break;
                    }
                }
            }
        }
        info.append("\n키보드 창: ").append(imeBounds == null ? "못 찾음" : imeBounds.toShortString());
        lastInfo = info.toString();
        if (imeBounds == null) { hide(); return; }
        show(imeBounds);
    }

    private void show(Rect ime) {
        try {
            if (button == null) {
                button = new TextView(this);
                button.setText("🛒 주문");
                button.setTextSize(15);
                button.setTextColor(Color.WHITE);
                button.setGravity(Gravity.CENTER);
                GradientDrawable background = new GradientDrawable();
                background.setColor(Color.rgb(31, 98, 200));
                background.setCornerRadius(dp(16));
                button.setBackground(background);
                button.setElevation(dp(4));
                button.setOnClickListener(v -> {
                    hide();
                    Intent intent = new Intent(this, OrderTestActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                });
            }
            int width = dp(96), height = dp(42);
            // Accessibility overlays use screen coordinates with TOP|LEFT gravity.
            int x = Math.max(dp(6), ime.right - width - dp(12));
            int y = Math.max(dp(24), ime.top - height - dp(margin));
            if (params == null) {
                params = new WindowManager.LayoutParams(
                    width, height, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
                params.gravity = Gravity.TOP | Gravity.LEFT;
                params.x = x; params.y = y;
                wm.addView(button, params);
            } else {
                if (params.x != x || params.y != y) {
                    params.x = x; params.y = y;
                    wm.updateViewLayout(button, params);
                }
            }
        } catch (Exception e) {
            lastInfo = lastInfo + "\n오버레이 오류: " + e;
            hide();
        }
    }
    private void hide() {
        if (button != null && button.getParent() != null && wm != null) {
            try { wm.removeViewImmediate(button); } catch (Exception ignored) {}
        }
        params = null;
    }
}
