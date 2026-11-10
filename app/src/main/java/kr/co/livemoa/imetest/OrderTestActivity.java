package kr.co.livemoa.imetest;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

public class OrderTestActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(45, 70, 45, 45);
        root.setBackgroundColor(Color.WHITE);
        TextView title = new TextView(this);
        title.setText("라이브모아 주문창 열기 성공");
        title.setTextSize(24);
        title.setTextColor(Color.BLACK);
        root.addView(title);
        TextView details = new TextView(this);
        details.setText("테스트 성공: 키보드 위 아이콘을 눌러 앱 화면을 실행했습니다.\n\n실제 상품 / 주문 / 결제 기능은 연결하지 않았습니다.");
        details.setTextSize(17);
        details.setPadding(0,35,0,25);
        root.addView(details);
        Button back = new Button(this);
        back.setText("돌아가기");
        back.setOnClickListener(v -> finish());
        root.addView(back);
        setContentView(root);
    }
}
