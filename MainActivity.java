package com.example.myapps; // ← ご自身のプロジェクトのパッケージ名に書き換えてください

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private static final int FILE_CHOOSER_RESULT_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. WebViewの初期化
        webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        
        // JavaScriptを有効にする
        webSettings.setJavaScriptEnabled(true);
        // ローカルファイルへのアクセスを許可
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDomStorageEnabled(true);

        // ページ遷移をWebView内で完結させる
        webView.setWebViewClient(new WebViewClient());

        // 2. JavaScriptとの「橋渡し」を設定
        // これによりHTML側から "AndroidInterface.openNativeFilePicker()" が呼べるようになります
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        // 3. HTMLファイルの読み込み (assetsフォルダ内の video_player-44.html)
        webView.loadUrl("file:///android_asset/video_player-44.html");
    }

    /**
     * JavaScriptから呼び出されるメソッドを定義するクラス
     */
    public class WebAppInterface {
        @JavascriptInterface
        public void openNativeFilePicker() {
            // 音声認識で「ファイルを開く」系の言葉を検知したときに実行される
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*"); // 動画ファイルのみ
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            
            try {
                startActivityForResult(Intent.createChooser(intent, "動画を選択"), FILE_CHOOSER_RESULT_CODE);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(MainActivity.this, "ファイルマネージャーが見つかりません", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * ファイル選択画面から戻ってきた時の処理
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_RESULT_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri videoUri = data.getData();
                
                // 4. 選択された動画のURIをHTML側のJavaScript関数「setVideoSource」へ渡す
                // UIスレッドで実行する必要があります
                webView.post(() -> {
                    String script = "javascript:setVideoSource('" + videoUri.toString() + "')";
                    webView.loadUrl(script);
                });
            }
        }
    }
}
