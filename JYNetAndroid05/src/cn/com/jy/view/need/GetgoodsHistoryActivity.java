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

public class GetgoodsHistoryActivity extends Activity implements OnClickListener{
	//	信息内容的全局变量;
	private Context			  mContext;
	//	信息列表的显示控件;
	private ListView 		  mListView;
	private SimpleAdapter	  mAdapter;
	//	数据库信息的加载;
	private MTSQLiteHelper	  mSqLiteHelper;//01.数据库帮助类;
	private SQLiteDatabase 	  mDB;		    //02.数据库对象类;
	private Cursor 		   	  mCursor;  	//03.数据库遍历签;
	private FileHelper		  mFileHelper;	//04.文件处理帮助类;
	private MTConfigHelper	  mConfigHelper;//05.参数处理帮助类;
	private List<Map<String, String>> mList;//数据信息的加载;
	private Set<String>       mSetTmp;		//文件夹下信息删除的方式;
	private ArrayList<String> mListBid;
	
	//	参数信息;
	private String 			  sql			//sql语句
							  ;
	//	按钮;
	private TextView		  btnBack,
							  btnFunction,
							  tvTopic;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//	进行页面的加载;
		setContentView(R.layout.hisinfo);
		//	添加控件的视图;
		initView();
		//	添加空间的事件;
		initEvent();
	}
	//	控件的初始化声明;
	private void initView(){
		//	listView信息的加载;
		mListView	=(ListView) findViewById(R.id.listView);
		btnBack		=(TextView) findViewById(R.id.btnBack);
		btnFunction	=(TextView) findViewById(R.id.btnFunction);
		tvTopic		=(TextView) findViewById(R.id.tvTopic);
		
	}
	//	事件的初始化声明;
	private void initEvent(){
		mContext=GetgoodsHistoryActivity.this;
		//	数据库信息的加载;
		mSqLiteHelper	=	new MTSQLiteHelper(mContext);
		mDB 			= 	mSqLiteHelper.getmDB();
		mFileHelper		=	new FileHelper();
		mConfigHelper	=	new MTConfigHelper();
		//	Set初始化;
		mSetTmp			=	new HashSet<String>();
		//	list初始化;
		mListBid		=	new ArrayList<String>();
		
		//	初始化信息内容;
		tvTopic.setText("提货历史信息");
		btnFunction.setText("清空");
		//	添加事件监听;
		btnBack.setOnClickListener(this);
		btnFunction.setOnClickListener(this);
		showData();
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long id) {
				Intent  intent	=new Intent(GetgoodsHistoryActivity.this, GetgoodsDetailActivity.class);
				Bundle	bundle	=new Bundle();
				String  ggid	=mList.get(position).get("id");
				String  simg	=mList.get(position).get("img");
				bundle.putString("ggid", ggid);
				bundle.putString("imgs", simg);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	//	显示信息的内容;
	private void showData(){
		//	表信息的加载;
		mList	=loadData();
		//	适配器的添加;
		mAdapter=new SimpleAdapter(mContext, mList, R.layout.item02, new  String[]{"content"}, new int[]{R.id.tvTopic});
		//	适配器列表的绑定;
		mListView.setAdapter(mAdapter);
	}
	//	加载数据信息法;
	private List<Map<String, String>> loadData(){
		//	set的清空;
		mSetTmp.clear();
		mListBid.clear();
		
		List<Map<String, String>> list=new ArrayList<Map<String,String>>();
		sql		=	"select * from getgoodsinfo order by ggid  ";
		mCursor	= 	mDB.rawQuery(sql, null);
		int nCount=0;
		while (mCursor.moveToNext()) {	
			nCount++;
			Map<String, String> map=new HashMap<String, String>();
			String ggid		=	mCursor.getString(mCursor.getColumnIndex("ggid")).toString();
			String gid		=	mCursor.getString(mCursor.getColumnIndex("gid")).toString();
			String bid		=	mCursor.getString(mCursor.getColumnIndex("bid")).toString();
			String gtime	=	mCursor.getString(mCursor.getColumnIndex("gtime")).toString();
			String stime	=	mCursor.getString(mCursor.getColumnIndex("stime")).toString();
			String gsimg	=	mCursor.getString(mCursor.getColumnIndex("gsimg")).toString();
			
			if(mSetTmp.add(bid)){
				mListBid.add(bid);
			}
			
			map.put("content", nCount+" --> "+bid+"-"+gid+" ["+gtime+"|"+stime+"] "+"  详情");
			map.put("img", gsimg);
			
			map.put("id",ggid);
			list.add(map);
		}
		
		if(mCursor!=null){
			mCursor.close();
		}	
		return list;
	}
	@Override
	public void onClick(View view) {
		int nVid=view.getId();
		switch (nVid) {
		//	删除按钮;
		case R.id.btnFunction:
			sql		=	"delete from getgoodsinfo";
			mDB.execSQL(sql);
			for(String bid:mListBid){
				String folder=mConfigHelper.getfParentPath()+bid+File.separator+"ggoods";
				mFileHelper.delAllFile(folder);
			}

		//	返回按钮;
		case R.id.btnBack:
			break;
		
		default:
			break;
		}	
		finish();	
	}
}
