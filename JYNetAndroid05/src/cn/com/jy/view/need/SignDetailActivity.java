package cn.com.jy.view.need;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.com.jy.activity.R;
import cn.com.jy.model.helper.MTConfigHelper;
import cn.com.jy.model.helper.MTFileHelper;
import cn.com.jy.model.helper.MTGetOrPostHelper;
import cn.com.jy.model.helper.MTImgHelper;
import cn.com.jy.model.helper.MTSQLiteHelper;
import cn.com.jy.model.helper.MTScreenHelper;
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
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

@SuppressWarnings("deprecation")
public class SignDetailActivity extends Activity implements OnClickListener{
	//	全程序内容;
	private Context	 		mContext;	//	上下文信息;
	//	主要的控件;
	private TextView 		tvTopic,	//	内容标题;
					 		btnBack,	//	返回按钮;
					 		btnFunction;//	功能按钮;
	private WebView			vWvShow;
	private Gallery	 		mGallery;	//	画廊按钮;
	private	ProgressDialog	mDialog;	 // 对话框; 
	private String 	 		_id,		//	id主键;
		  			 		sql,
		  			 		sResult,
		  			 		bid,
		  			 		gid,
		  			 		folderPath,
		  			 		imgs
		  			 		;		
	//	数据库管理;
	private MTSQLiteHelper	mSqLiteHelper;
	private SQLiteDatabase 	mDB;		 //  数据库件;
	private Cursor 		   	mCursor;     //  数据库遍历签;
	private MTConfigHelper	mConfigHelper;// 配置项;
	private MTGetOrPostHelper mGetOrPostHelper;

	private MTFileHelper	mtFileHelper;
	private MTImgHelper		mImgHelper;  // 图片辅助类;
	//	图片的集合列表;
	private List<BitmapDrawable> listBD = null;
	private ArrayList<String>    listMapName;
	private MyThread		mThread;	 // 线程;
	/*设置内容*/
	private Display 		  mDisplay; // 为获取屏幕宽、高
	private Window 			  mWindow;	
	private MTScreenHelper 	  mtScreenHelper;
	
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
		setContentView(R.layout.detail2);
		//	添加控件;
		initView();
		//	添加事件监听;
		initEvent();
	}
	//	控件初始化;
	private void initView(){
		tvTopic			=	(TextView) findViewById(R.id.tvTopic);
		vWvShow			= 	(WebView) findViewById(R.id.wvShow);
		btnBack			=	(TextView) findViewById(R.id.btnBack);
		btnFunction		=	(TextView) findViewById(R.id.btnFunction);
		mGallery		=	(Gallery) findViewById(R.id.gallery);
		
//		WebSettings settings = vWvShow.getSettings();
//		settings.setAppCacheEnabled(true);
//		settings.setDatabaseEnabled(true);
//		settings.setDomStorageEnabled(true);//开启DOM缓存，关闭的话H5自身的一些操作是无效的
////	settings.setCacheMode(WebSettings.LOAD_DEFAULT);
//		settings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);//不使用网络，只加载缓存

	}
	//	事件监听初始化;
	private void initEvent(){
		mContext		=	SignDetailActivity.this;
		mConfigHelper	=	new MTConfigHelper();
		mGetOrPostHelper=	new MTGetOrPostHelper();
		mtFileHelper	=	new MTFileHelper();
		mImgHelper		=	new MTImgHelper();
		
		//	设置内容;
		mDisplay 		= 	getWindowManager().getDefaultDisplay();
		mWindow			= 	getWindow(); 
		mtScreenHelper  =	new MTScreenHelper(mDisplay, mWindow);
		int screenWidth =   mtScreenHelper.getScreenWidth();
		int screenHeight=   mtScreenHelper.getScreenHeight();
		int tablewidth	=(int) (screenWidth*15f);
		int wordsize	=(int) (screenHeight*0.07f);
		int nImgHeight	=	screenHeight-5*wordsize;
		
		
		//	控件的初始化;
		btnFunction.setVisibility(View.GONE);
		tvTopic.setText("签收信息详情");
		
		//	获取id;
		Intent	mIntent =	getIntent();
		Bundle	mBundle =	mIntent.getExtras();
		_id				=	mBundle.getString("_id");
		imgs				=	mBundle.getString("imgs");
		Log.i("MyLog", "info="+imgs);
		//	数据库加载;
		mSqLiteHelper	=	new MTSQLiteHelper(mContext);
		mDB 			= 	mSqLiteHelper.getmDB();
		//	添加事件监听;
		btnBack.setOnClickListener(this);
		listMapName		=	mtFileHelper.getFileNamesByList(imgs,"_");
		int 	size	=	listMapName.size();
		
		//	数据信息加载;
		doLoadData(size,tablewidth,wordsize);

		//	提货信息路径;
		folderPath	=	mConfigHelper.getfParentPath()+bid+File.separator+"sign"+File.separator+gid;
		if(size>0){
			
		//	承装图片的容器;
		listBD		=	mImgHelper.getBitmap01_2(folderPath, imgs);
		//	设置图片适配器;
		mGallery.setAdapter(new ImageAdaper(mContext, listBD,nImgHeight)); 
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
	}
	//	信息加载;
	private void doLoadData(int size,int tablewidth,int wordsize){
		sql		=	"select * from signinfo where _id="+_id;
		tablewidth=wordsize*3*7;
		wordsize=(int) (wordsize/1.5);
		mCursor	= 	mDB.rawQuery(sql, null);
		while (mCursor.moveToNext()) {	
			//	信息加载;
			gid		=	mCursor.getString(mCursor.getColumnIndex("barcode")).toString();
			String receiptdate		=	mCursor.getString(mCursor.getColumnIndex("receiptdate")).toString();
			String cargostatussign	=	mCursor.getString(mCursor.getColumnIndex("cargostatussign")).toString();
			bid 	=	mCursor.getString(mCursor.getColumnIndex("busiinvcode")).toString();
			sResult="<html>" +
						"<body>" +
							"<table border=\"1\" style=\"width:"+tablewidth+"px;font-family:'宋体';font-size:"+wordsize+"px\">" +
								"<tr bgcolor=\"#00FF00\" align=\"center\">" +
									"<td>业务编号</td>"+
									"<td>二维码</td>"+
									"<td>签收时间</td>"+
								"</tr>" +
								"<tr align=\"center\">" +
									"<td>"+bid+"</td>" +
									"<td>"+gid+"</td>" +
									"<td>"+receiptdate+"</td>" +
								"</tr>"+
								"<tr align=\"center\">" +
									"<td colspan=\"3\">状态</td>" +
								"</tr>"+
								"<tr align=\"center\">" +
									"<td colspan=\"3\">"+cargostatussign+"</td>" +
								"</tr>"+
								"<tr align=\"center\">" +
									"<td >图片</td>" +
									"<td colspan=\"2\">"+size+"张</td>" +
								"</tr>"+
							"</table>"+
						"</body>" +
					"</html>"
					;
		}
		if(mCursor!=null){
			mCursor.close();
		}
		vWvShow.getSettings().setDefaultTextEncodingName("utf-8") ;
		vWvShow.loadDataWithBaseURL(null, sResult, "text/html", "utf-8", null);	
	}
	//	适配器的类;
	public class ImageAdaper extends BaseAdapter{  
        private Context mContext;  
        private int 	mGalBackgroundItem;
        private int 	nSize;
        private List<BitmapDrawable> listBD;
        private int 	nImgHeight;
        
        public ImageAdaper(Context mContext,List<BitmapDrawable> list,int nImgHeight){  
            this.mContext = mContext;  
            this.listBD	  = list;
            this.nSize	  = list.size();
            this.nImgHeight=nImgHeight;
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
            imageview.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT,nImgHeight));  
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
			String path		=	folderPath+File.separator+listMapName.get(this.position)+".jpg";
			url				=	"http://"+MTConfigHelper.TAG_IP_ADDRESS+":"+MTConfigHelper.TAG_PORT+"/"+MTConfigHelper.TAG_PROGRAM+"/upPhoto";
			response		=	mGetOrPostHelper.uploadFile(url,path,listMapName.get(position));
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
