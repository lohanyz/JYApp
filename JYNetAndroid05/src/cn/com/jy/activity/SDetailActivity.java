package cn.com.jy.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.com.jy.helper.ConfigHelper;
import cn.com.jy.helper.FileHelper;
import cn.com.jy.helper.GetOrPostHelper;
import cn.com.jy.helper.ImgHelper;
import cn.com.jy.helper.SQLiteHelper;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

@SuppressWarnings("deprecation")
public class SDetailActivity extends Activity implements OnClickListener{
	//	全程序内容;
	private Context	 		mContext;	
	//	主要的控件;
	private TextView 		tvTopic,	//	内容标题;
					 		tvShow,	//	内容信息;
					 		btnBack,	//	返回按钮;
					 		btnFunction;//	功能按钮;
	private Gallery	 		mGallery;	//	画廊按钮;
	private	ProgressDialog	mDialog;	 // 对话框; 
	private String 	 		rid,		//	id主键;
		  			 		sql,
		  			 		sResult,
		  			 		bid,
		  			 		gid,
		  			 		folderPath
		  			 		;		
	//	数据库管理;
	private SQLiteHelper	mSqLiteHelper;
	private SQLiteDatabase 	mDB;		 //  数据库件;
	private Cursor 		   	mCursor;     //  数据库遍历签;
	private ConfigHelper	mConfigHelper;// 配置项;
	private GetOrPostHelper mGetOrPostHelper;
	private FileHelper		mFileHelper; // 文件配置项;
	private ImgHelper		mImgHelper;  // 图片辅助类;
	//	图片的集合列表;
	private List<BitmapDrawable> listBD = null;
	private ArrayList<String>    list;
	private MyThread		mThread;	 // 线程;
	
	
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int nFlag=msg.what;
			mDialog.dismiss();
			switch (nFlag) {
			case ConfigHelper.NTAG_SUCCESS:				
				Toast.makeText(mContext, R.string.tip_success,Toast.LENGTH_SHORT).show();
				break;
			case ConfigHelper.NTAG_FAIL:
				Toast.makeText(mContext, R.string.tip_fail,Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
			closeThread();
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail);
		//	添加控件;
		initView();
		//	添加事件监听;
		initEvent();
	}
	//	控件初始化;
	private void initView(){
		tvTopic			=	(TextView) findViewById(R.id.tvTopic);
		tvShow			=	(TextView) findViewById(R.id.tvShow);
		btnBack			=	(TextView) findViewById(R.id.btnBack);
		btnFunction		=	(TextView) findViewById(R.id.btnFunction);
		mGallery		=	(Gallery) findViewById(R.id.gallery);
	}
	//	事件监听初始化;
	private void initEvent(){
		mContext		=	SDetailActivity.this;
		mConfigHelper	=	new ConfigHelper();
		mGetOrPostHelper=	new GetOrPostHelper();
		mFileHelper		=	new FileHelper();
		mImgHelper		=	new ImgHelper();
		//	控件的初始化;
		btnFunction.setVisibility(View.GONE);
		tvTopic.setText("签收信息详情");
		
		//	获取id;
		Intent	mIntent =	getIntent();
		Bundle	mBundle =	mIntent.getExtras();
		rid				=	mBundle.getString("rid");
		//	数据库加载;
		mSqLiteHelper	=	new SQLiteHelper(mContext);
		mDB 			= 	mSqLiteHelper.getmDB();
		//	数据信息加载;
		doLoadData();
		tvShow.setText(sResult);
		//	提货信息路径;
		folderPath	=	mConfigHelper.getfParentPath()+bid+File.separator+"sign"+File.separator+gid;
		//	承装图片的容器;
		listBD		=	mImgHelper.getBitmap01(folderPath);
		//	设置图片适配器;
		mGallery.setAdapter(new ImageAdaper(mContext, listBD)); 
		list		=	mFileHelper.getFileNamesByList(folderPath);
		//	添加事件监听;
		btnBack.setOnClickListener(this);
		//	图片长按的上传;
		mGallery.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view,
					final int position, long id) {
				Builder builder=new Builder(mContext);
				builder.setTitle("提示信息:");
				builder.setPositiveButton("上传", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if(mThread==null){
							// 进度条的内容;
							final CharSequence strDialogTitle = getString(R.string.tip_dialog_wait);
							final CharSequence strDialogBody  = getString(R.string.tip_dialog_done);
							mDialog = ProgressDialog.show(mContext, strDialogTitle, strDialogBody,true);
							mThread	= new MyThread(position);
							mThread.start();
						}
					}
				});
				builder.setNegativeButton(R.string.action_no, null);
				builder.create();
				builder.show();
				return false;
			}
		});				
	}
	//	信息加载;
	private void doLoadData(){
		sql		=	"select * from resigninfo where rid="+rid;
		mCursor	= 	mDB.rawQuery(sql, null);
		while (mCursor.moveToNext()) {	
			//	信息加载;
			bid		=	mCursor.getString(mCursor.getColumnIndex("bid")).toString();
			gid		=	mCursor.getString(mCursor.getColumnIndex("gid")).toString();
			String state	=	mCursor.getString(mCursor.getColumnIndex("state")).toString();
			
			sResult="商品编号:"+bid+"-"+gid+"\r\n状态信息:\r\n"+
					state
					;
		}
		if(mCursor!=null){
			mCursor.close();
		}
		
		tvShow.setText(sResult);		
	}
	//	适配器的类;
	public class ImageAdaper extends BaseAdapter{  
        private Context mContext;  
        private int 	mGalBackgroundItem;
        private int 	nSize;
        private List<BitmapDrawable> listBD;
        
        public ImageAdaper(Context mContext,List<BitmapDrawable> list){  
            this.mContext = mContext;  
            this.listBD	  = list;
            this.nSize	  = list.size();
            TypedArray typedArray = obtainStyledAttributes(R.styleable.Gallery);  
            mGalBackgroundItem 	  = typedArray.getResourceId( R.styleable.Gallery_android_galleryItemBackground, 0);  
            typedArray.recycle();
        }  

        public int getCount() {  
            return this.nSize;  
        }  
  
        public Object getItem(int position) {  
            return this.listBD.get(position);  
        }  
  
        public long getItemId(int position) {  
            return position;  
        }  

		public View getView(int position, View convertView, ViewGroup parent) {  
            
            ImageView imageview = new ImageView(this.mContext);  	           
            imageview.setImageDrawable(this.listBD.get(position));	            	
            
            imageview.setScaleType(ImageView.ScaleType.FIT_XY);  
            imageview.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT,400));  
            imageview.setBackgroundResource(mGalBackgroundItem);  
            notifyDataSetChanged();
            return imageview;   
        }            
    }
	//	线程的自定义形式;
	class MyThread extends Thread{
		private String url,
					   response;
		private int position;
		public MyThread(int position) {
			this.position=position;
		}
		@Override
		public void run() {
			String path		=	folderPath+File.separator+list.get(this.position)+".jpg";
			url				=	"http://"+ConfigHelper.TAG_IP_ADDRESS+":"+ConfigHelper.TAG_PORT+"/"+ConfigHelper.TAG_PROGRAM+"/upPhoto";
			response		=	mGetOrPostHelper.uploadFile(url,path,list.get(position));
			int nFlag= ConfigHelper.NTAG_FAIL;
			if(!response.endsWith("fail")){
				nFlag= ConfigHelper.NTAG_SUCCESS;
			}
			mHandler.sendEmptyMessage(nFlag);
		}
	}
	private void closeThread(){
		if(mThread!=null){
			mThread.interrupt();
			mThread=null;
		}
	 }
	 
	 @Override
	protected void onDestroy() {
		super.onDestroy();
		closeThread();
		mSqLiteHelper.doCloseDataBase();
	}
	
	
	@Override
	public void onClick(View view) {
		int nVid=view.getId();
		switch (nVid) {
		case R.id.btnBack:
			finish();
			break;

		default:
			break;
		}
	}
}
