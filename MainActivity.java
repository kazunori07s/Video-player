package com.example.myapps; // あなたのパッケージ名に合わせてください

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JavascriptInterface; // 追加
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.content.Intent; // 追加
import android.net.Uri; // 追加

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // ★重要: JavaScriptから "AndroidInterface" という名前で呼び出せるようにする
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        webView.loadUrl("file:///android_asset/video_player-44.html");
    }

    // ★重要: JavaScriptから命令を受け取るためのクラス
    public class WebAppInterface {
        @JavascriptInterface
        public void openNativeFilePicker() {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*"); // 動画ファイルを選択
            startActivityForResult(Intent.createChooser(intent, "動画を選択"), 100);
        }
    }

    // ★重要: ファイル選択が終わった後に、結果（パス）をJavaScriptに送る
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri videoUri = data.getData();
            // HTML内の setVideoSource 関数を呼び出す
            webView.post(() -> {
                webView.loadUrl("javascript:setVideoSource('" + videoUri.toString() + "')");
            });
        }
    }
}

