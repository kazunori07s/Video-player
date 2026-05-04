package Video-player; // ← ここはご自身のプロジェクト名に合わせてください

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

    // ★重要: ここで webView を変数として宣言します
    private WebView webView;
    private static final int FILE_CHOOSER_RESULT_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // レイアウト(xml)上のWebViewを取得
        webView = findViewById(R.id.webview);
        
        // WebViewの設定
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDomStorageEnabled(true);
        
        // リンククリック時にブラウザに飛ばないようにする
        webView.setWebViewClient(new WebViewClient());

        // JavaScriptとの連携窓口を登録
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        // HTMLファイルを読み込む
        webView.loadUrl("file:///android_asset/video_player-44.html");
    }

    /**
     * JavaScriptから呼び出されるクラス
     */
    public class WebAppInterface {
        @JavascriptInterface
        public void openNativeFilePicker() {
            // 音声認識で「ファイルを開く」と言われたときに実行
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                startActivityForResult(Intent.createChooser(intent, "動画を選択"), FILE_CHOOSER_RESULT_CODE);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "エラーが発生しました", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * ファイル選択後に呼ばれる処理
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_RESULT_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri videoUri = data.getData();
                
                // HTML内のJavaScript関数「setVideoSource」を実行して動画パスを送る
                webView.post(() -> {
                    String script = "javascript:setVideoSource('" + videoUri.toString() + "')";
                    webView.loadUrl(script);
                });
            }
        }
    }
}
