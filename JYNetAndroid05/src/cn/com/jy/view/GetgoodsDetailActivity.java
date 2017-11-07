package cn.com.jy.view;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.com.jy.activity.R;
import cn.com.jy.model.helper.MTConfigHelper;
import cn.com.jy.model.helper.MTFileHelper;
import cn.com.jy.model.helper.MTGetOrPostHelper;
import cn.com.jy.model.helper.MTImgHelper;
import cn.com.jy.model.helper.MTSQLiteHelper;
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
public class GetgoodsDetailActivity extends Activity implements OnClickListener{
	//	全程序内容;
	private Context	 		mContext;	//	上下文信息;
	//	主要的控件;
	private TextView 		tvTopic,	//	内容标题;
					 		tvShow;		//	内容信息;
	private TextView 	 	vBack,	//	返回按钮;
					 		btnFunction;//	功能按钮;
	private Gallery	 		mGallery;	//	画廊按钮;
	private	ProgressDialog	mDialog;	// 	对话框; 
	private String 	 		ggid,		//	id主键;
		  			 		sql,		//	SQl语句串;
		  			 		sResult,	//	结果字符串;
		  			 		bid,		//	业务编号;
		  			 		gid,		//	货物编号;
		  			 		folderPath,	//	文件夹路径;
		  			 		imgs
		  			 		;		
	//	数据库管理;
	private MTSQLiteHelper	mSqLiteHelper;//  数据库帮助类;
	private SQLiteDatabase 	mDB;		  //  数据库件;
	private Cursor 		   	mCursor;      //  数据库遍历签;
	private MTConfigHelper	mConfigHelper;//  配置项;
	private MTGetOrPostHelper mGetOrPostHelper;//	数据发送帮助类;
	private MTFileHelper    mtFileHelper;
	private MTImgHelper		mImgHelper;  // 图片辅助类;
	//	图片的集合列表;
	private List<BitmapDrawable> listBD = null;// 承装图片的列表;
	private ArrayList<String>    list;	 	   // 承装文件夹的列表;
	private MyThread		mThread;	 // 线程;
	
	
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int nFlag=msg.what;
			mDialog.dismiss();
			switch (nFlag) {
			case MTConfigHelper.NTAG_SUCCESS:				
				Toast.makeText(mContext, R.string.tip_success,Toast.LENGTH_SHORT).show();
				break;
			case MTConfigHelper.NTAG_FAIL:
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
		vBack			=	(TextView) findViewById(R.id.btnBack);
		btnFunction		=	(TextView) findViewById(R.id.btnFunction);
		mGallery		=	(Gallery) findViewById(R.id.gallery);
	}
	//	事件监听初始化;
	private void initEvent(){
		mContext		=	GetgoodsDetailActivity.this;
		mConfigHelper	=	new MTConfigHelper();
		mGetOrPostHelper=	new MTGetOrPostHelper();
		mtFileHelper	= 	new MTFileHelper();
		mImgHelper		=	new MTImgHelper();
		//	控件的初始化;
		btnFunction.setVisibility(View.GONE);
		tvTopic.setText("提货信息详情");
		//	获取id;
		Intent	mIntent =	getIntent();
		Bundle	mBundle =	mIntent.getExtras();
		ggid			=	mBundle.getString("ggid");
		imgs			=	mBundle.getString("imgs");
		//	数据库加载;
		mSqLiteHelper	=	new MTSQLiteHelper(mContext);
		mDB 			= 	mSqLiteHelper.getmDB();
		//	数据信息加载;
		doLoadData();
		tvShow.setText(sResult);
		//	提货信息路径;
		folderPath	=	mConfigHelper.getfParentPath()+bid+File.separator+"ggoods"+File.separator+gid;
		//	承装图片的容器;
		listBD		=	mImgHelper.getBitmap01_2(folderPath, imgs);
		//	设置图片适配器;
		mGallery.setAdapter(new ImageAdaper(mContext, listBD)); 
		list		=	mtFileHelper.getFileNamesByList(imgs,"_");
		//	添加事件监听;
		vBack.setOnClickListener(this);
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
							mThread=new MyThread(position);
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
		sql		=	"select * from getgoodsinfo where ggid="+ggid;
		mCursor	= 	mDB.rawQuery(sql, null);
		while (mCursor.moveToNext()) {	
			//	信息加载;
			bid		=	mCursor.getString(mCursor.getColumnIndex("bid")).toString();
			gid		=	mCursor.getString(mCursor.getColumnIndex("gid")).toString();
			String gstate	=	mCursor.getString(mCursor.getColumnIndex("gstate")).toString();
			String lkind	=	mCursor.getString(mCursor.getColumnIndex("lkind")).toString();
			String tid		=	mCursor.getString(mCursor.getColumnIndex("tid")).toString();
			String tkind	=	mCursor.getString(mCursor.getColumnIndex("tkind")).toString();
			String oid		=	mCursor.getString(mCursor.getColumnIndex("oid")).toString();
			int    percount	=	mCursor.getInt(mCursor.getColumnIndex("percount"));
			double perweight=	mCursor.getDouble(mCursor.getColumnIndex("perweight"));
			double tformatweight=mCursor.getDouble(mCursor.getColumnIndex("tformatweight"));
			int    tcount	=	mCursor.getInt(mCursor.getColumnIndex("tcount"));
			String gtime	=	mCursor.getString(mCursor.getColumnIndex("gtime")).toString();
			String stime	=	mCursor.getString(mCursor.getColumnIndex("stime")).toString();
			
			sResult="商品编号:"+bid+"-"+gid+"\r\n运输方式:"+lkind+"\r\n";
			
			if(lkind.equals("汽车运输")){
				sResult=sResult+
						"拖车编号:"+tid+"	车辆类型:"+tkind+"\r\n"+
						"铅封号:"+oid+"\r\n"+
						"单车件数:"+percount+"件	单车吨数:"+perweight+"吨	车数:"+tcount+"辆\r\n"+
						"提货时间:"+gtime+"\r\n" +
						"发车时间:"+stime+"\r\n";
			}else{
				sResult=sResult+
						"车皮编号:"+tid+"	车辆类型:"+tkind+"\r\n"+
						"运单号:"+oid+"\r\n"+
						"单车件数:"+percount+"件	单车吨数:"+perweight+"吨	标重(车):"+tformatweight+"吨\r\n"+
						"提货时间:"+gtime+"\r\n" +
						"发车时间:"+stime+"\r\n";
				
			}		
			sResult=sResult+"状态信息:\r\n"+
					gstate
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
			url				=	"http://"+MTConfigHelper.TAG_IP_ADDRESS+":"+MTConfigHelper.TAG_PORT+"/"+MTConfigHelper.TAG_PROGRAM+"/upPhoto";
			response		=	mGetOrPostHelper.uploadFile(url,path,list.get(this.position));
			int nFlag= MTConfigHelper.NTAG_FAIL;
			if(!response.endsWith("fail")){
				nFlag= MTConfigHelper.NTAG_SUCCESS;
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
