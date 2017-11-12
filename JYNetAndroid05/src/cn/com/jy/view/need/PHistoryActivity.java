package cn.com.jy.view.need;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.jy.activity.R;
import cn.com.jy.model.helper.FileHelper;
import cn.com.jy.model.helper.MTConfigHelper;
import cn.com.jy.model.helper.MTSQLiteHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PHistoryActivity extends Activity implements OnClickListener {
    //  信息内容的全局变量;
    private Context mContext;
    //  信息列表的显示控件;
    private ListView mListView;
    private SimpleAdapter mAdapter;
    //  数据库信息的加载;
    private MTSQLiteHelper mSqLiteHelper;  //01.数据库帮助类;
    private SQLiteDatabase mDB;            //02.数据库对象类;
    private MTConfigHelper mConfigHelper;    //03.参数工具类
    private FileHelper mFileHelper;    //04.文件辅助工具类;
    private Cursor mCursor;        //05.数据库遍历签;

    private List<Map<String, String>> mList;//06.数据信息的加载;
    private Set<String> mSetTmp;        //Set的临时表;
    private ArrayList<String> mListBid;        //列表;


    //  参数信息;
    private String sql;
    private TextView tvTopic, btnFunction, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  进行页面的加载;
        setContentView(R.layout.hisinfo);
        //  添加控件的视图;
        initView();
        //  添加空间的事件;
        initEvent();
    }

    //  控件的初始化声明;
    private void initView() {
        //  listView信息的加载;
        mListView = (ListView) findViewById(R.id.listView);
        btnBack = (TextView) findViewById(R.id.btnBack);
        btnFunction = (TextView) findViewById(R.id.btnFunction);
        tvTopic = (TextView) findViewById(R.id.tvTopic);
    }

    //  事件的初始化声明;
    private void initEvent() {
        mContext = PHistoryActivity.this;
        //  数据库信息的加载;
        mSqLiteHelper = new MTSQLiteHelper(mContext);
        mDB = mSqLiteHelper.getmDB();
        mFileHelper = new FileHelper();
        mConfigHelper = new MTConfigHelper();
        //  Set初始化;
        mSetTmp = new HashSet<String>();
        //  list初始化;
        mListBid = new ArrayList<String>();

        //  初始化信息内容;
        tvTopic.setText("港口历史信息");
        btnFunction.setText("清空");
        //  添加事件监听;
        btnBack.setOnClickListener(this);
        btnFunction.setOnClickListener(this);

        showData();
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position,
                                    long id) {
                Intent intent = new Intent(PHistoryActivity.this, PDetailActivity.class);
                Bundle bundle = new Bundle();
                String  _id		=mList.get(position).get("_id");
                String  img		=mList.get(position).get("img");
                bundle.putString("_id", _id);
                bundle.putString("imgs", img);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    //  显示信息的内容;
    private void showData() {
        //  表信息的加载;
        mList = loadData();
        //  适配器的添加;
        mAdapter = new SimpleAdapter(mContext, mList, R.layout.item02, new String[]{"content"}, new int[]{R.id.tvTopic});
        //  适配器列表的绑定;
        mListView.setAdapter(mAdapter);
    }

    //  加载数据信息法;
    private List<Map<String, String>> loadData() {
        mSetTmp.clear();
        mListBid.clear();
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        sql = "select * from portinfo order by _id";
        mCursor = mDB.rawQuery(sql, null);
        int nCount = 0;
        while (mCursor.moveToNext()) {
            nCount++;
            Map<String, String> map = new HashMap<String, String>();
            String _id		=	mCursor.getString(mCursor.getColumnIndex("_id")).toString();
            String barcode	=	mCursor.getString(mCursor.getColumnIndex("barcode")).toString();
            String img		=	mCursor.getString(mCursor.getColumnIndex("img")).toString();
            //String busiinvcode=	mCursor.getString(mCursor.getColumnIndex("busiinvcode")).toString();
            String busiinvcode=	"mCursor";
            if(mSetTmp.add(busiinvcode)){
                mListBid.add(busiinvcode);
            }

            map.put("content", nCount+" --> 业务"+busiinvcode+" 条码"+barcode+" 总序 "+_id+"  详情");
            map.put("img", img);

            map.put("_id",_id);
            list.add(map);
        }

        if (mCursor != null) {
            mCursor.close();
        }
        return list;
    }

    @Override
    public void onClick(View view) {
        int nVid = view.getId();
        switch (nVid) {
            case R.id.btnFunction:
                sql = "delete from portinfo";
                mDB.execSQL(sql);
                for (String bid : mListBid) {
                    String folder = mConfigHelper.getfParentPath() + bid + File.separator + "port";
                    mFileHelper.delAllFile(folder);
                }
            case R.id.btnBack:
                break;

            default:
                break;
        }
        finish();
    }
}
