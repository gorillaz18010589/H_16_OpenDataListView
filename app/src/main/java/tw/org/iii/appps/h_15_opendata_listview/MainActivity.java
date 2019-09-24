package tw.org.iii.appps.h_15_opendata_listview;
//目的:抓取openData,灌到list View
//選用要抓的資料為:http://data.coa.gov.tw/Service/OpenData/RuralTravelData.aspx
//權限帶到  <uses-permission android:name="android.permission.INTERNET"/>
//新增laout 把資料灌進去:=> layout=> Layout res file :叫item
//按下list跳到第二頁
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private SimpleAdapter adapter; //條便器
    private String[] from ={"title","type"}; //從open data資料裡面的兩個欄位
    private int[] to ={R.id.item_title,R.id.item_type};//灌到我寫好的兩個item_view
    private LinkedList<HashMap<String,String>> data = new LinkedList<>();//data資料
    private UIhandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new UIhandler(); //UIhandle物件實體
        listView = findViewById(R.id.listView);
        initListView();
        fetchData();


    }

    //1.初始化條便器方法,配合listView
    private  void initListView(){
        //SimpleAdapter(1.Context context, 2.List<? extends Map<String, ?>> data,3. int resource, 4.String[] from, 5.int[] to)
        adapter = new SimpleAdapter(this //1.this
                ,data //2.LinkedList<HashMap<String,String>> data = new LinkedList<>()
                ,R.layout.item //3.我在layout寫的資源區檔案
                ,from //4.從那些資料區而來 from ={"title","type"};
                ,to); //5.灌到對應的兩個資料 to ={R.id.item_title,R.id.item_type};

        fetchData();
        listView.setAdapter(adapter);//設定條便器

        //5.按下onlistitem到第二頁
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { //回傳的INT I代表你所按的那個Item
                // Intent(Context packageContext, Class<?> cls)://Intent道別頁(1."從這個頁面",2.第二頁面.class)
                Intent intent = new Intent(MainActivity.this,ContentActivity.class);//Intent道別頁(1."從這個頁面",2.第二頁面.class)
                intent.putExtra("pic",data.get(i).get("pic"));//設定參數(1.name ,2.value()) //抓到這個data資料取得點到(i)item在抓裡面的pic照片直
                intent.putExtra("content",data.get(i).get("content"));//把content參數掛上去,帶到第二頁去玩/
                startActivity(intent);//啟動到第二頁y
            }
        });
    }
    //2.抓取openData資料
    private  void fetchData(){
        new Thread(){
            @Override
            public void run() {
                try {
                    URL url = new URL("http://data.coa.gov.tw/Service/OpenData/RuralTravelData.aspx");//http要去開明馬權限
                    HttpURLConnection conn = ( HttpURLConnection) url.openConnection();
                    conn.connect();

                    //用BufferedReader讀資料比較快,且可以用readLine
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(conn.getInputStream()));
                    //讀入的資料,一行一行加上去,所以要用StringBuffer的read.line
                    String line = null;   StringBuffer sb =  new StringBuffer();
                    while((line = reader.readLine()) != null){//當讀取的資料不等於空,代表還有一行一行讀讀好存到line
                        sb.append(line);//每讀到一行,就產生一段資料
                    }
                    reader.close();
//                    Log.v("brad",sb.toString());檢測url有抓到
                    //抓完資料成功後呼叫解析方法
                    parseJSONData(sb.toString());//呼叫解析json方法
                }catch (Exception e){
                    Log.v("brad","抓取opendata出現錯誤" +e.toString());
                }
            }
        }.start();
    }

    //3.解析json Data資料
    //JSONArray(String json): //解析json解析物件(要解新的json)
    private  void parseJSONData (String json){
        //1.資料是物件在陣列包起,所以先解
        try {
//            data.clear();//一開始先清掉,在冠上資料,這樣重複點才不會資料越來越多

            JSONArray root = new JSONArray(json);//解析json解析物件(要解新的json)
            for(int i=0; i<root.length(); i++){//尋訪裡面是物件
                JSONObject row = root.getJSONObject(i);//從這個root陣列裡面去抓到一個一個物件

          //2.把資料掛在hashMap上在add到data上
            HashMap<String,String> d = new HashMap<>();
            d.put(from[0],row.getString("Title"));
            d.put(from[1],row.getString("TravelType")
                    .replace('\n',' ')
                    .replace('\r', ' ')
                    .replace("  ",""));
//                    .replace('\r',' ') //拿掉\r換成空白
//                    .replace('\n', ' ')//拿掉\n換成空白
//                    .replace(" ","");//拿掉空白換成,空自原

            d.put("pic",row.getString("PhotoUrl"));
            d.put("content",row.getString("Contents").replace('\r',' '));//拿掉\r被視為字元

            data.add(d);//掛上從opendata的新資料
            }
            //3.把訊息交給handler去處理布袋資料的
            handler.sendEmptyMessage(0);//不掛資料代表0,直接把這個參數帶到handleMessage
            Log.v("brad","有交續息給handele");
        }catch (Exception e){
            Log.v("brad","解析json失敗" +e.toString());
        }

    }




    //4.接收你抓完資料的參數因為網路傳遞是在執行緒哩,所以需要handle來傳遞
    private  class  UIhandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            adapter.notifyDataSetChanged();//條便器更新讓資料灌進來
            Log.v("brad","handlet有處理:" );
        }
    }
}
