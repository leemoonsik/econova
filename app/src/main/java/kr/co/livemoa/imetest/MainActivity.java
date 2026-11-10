package kr.co.livemoa.imetest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(45, 65, 45, 45);
        root.setBackgroundColor(Color.WHITE);
        TextView info = new TextView(this);
        info.setText("라이브모아: 기본 키보드 위 아이콘 테스트\n\n1. 아래 버튼으로 접근성 설정을 엽니다.\n2. '라이브모아 키보드 위 아이콘 테스트' 서비스를 켭니다.\n3. 틱톡 / 유튜브 등에서 댓글 입력창을 눌러 봅니다.\n4. 키보드 상단에 주문 아이콘이 나타나는지 확인합니다.\n5. 아이콘을 누르면 테스트 주문창이 열립니다.\n\n※ 실제 라이브모아 앱과 연결하지 않는 검증용 앱입니다.\n※ 화면 내용·댓글·개인정보를 읽거나 저장하지 않습니다.");
        info.setTextSize(17);
        root.addView(info);
        Button settings = new Button(this);
        settings.setText("접근성 서비스 설정 열기");
        settings.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(settings);
        Button test = new Button(this);
        test.setText("주문 테스트 화면 확인");
        test.setOnClickListener(v -> startActivity(new Intent(this, OrderTestActivity.class)));
        root.addView(test);
        setContentView(root);
    }
}
