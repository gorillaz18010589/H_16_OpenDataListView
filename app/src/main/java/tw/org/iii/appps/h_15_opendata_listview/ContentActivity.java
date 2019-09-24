package tw.org.iii.appps.h_15_opendata_listview;
//接收MainActivity intent過來的content,img參數,把它顯示出來
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;

public class ContentActivity extends AppCompatActivity {
    private  String pic,content;
    private ImageView img;
    private TextView tvContent;
    private Bitmap bmp;
    private UIhandler handler;//呼叫自己寫的hadnle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);



        //抓取第一頁的參數pic,content
        pic = getIntent().getStringExtra("pic");//抓取intente物件.//抓取String型態參數(String name/參數),這個pic是url圖片檔
        content = getIntent().getStringExtra("content");

        tvContent = findViewById(R.id.content);//顯示內容物件實體
        img = findViewById(R.id.img);//顯示圖片物件實體
        tvContent.setText(content);//把抓到的文章參數資訊灌進去呈現出來

        handler = new UIhandler();//物件初始化才能玩
        fetchImage();
    }
    //1.因為pic參數是url的圖片檔,所以要用url去抓
    private  void fetchImage (){
        new Thread(){
            @Override
            public void run() {
                try{
                    URL url = new URL(pic);//連接url(在第一頁抓到的圖片url)
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();

                    bmp = BitmapFactory.decodeStream(conn.getInputStream());
                    handler.sendEmptyMessage(0);//把這個圖片串流參數傳給UIhandle去做
                }catch (Exception e){
                    Log.v("brad","抓取圖片錯誤" + e.toString());
                }
            }
        }.start();
    }
    //2.接收sendEmptryMessage照片pic參數
    private class UIhandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            img.setImageBitmap(bmp);//imageView設置圖片

        }
    }
}

