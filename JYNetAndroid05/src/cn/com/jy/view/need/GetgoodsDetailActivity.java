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
public class GetgoodsDetailActivity extends Activity implements OnClickListener{
	//	全程序内容;
	private Context	 		mContext;	//	上下文信息;
	//	主要的控件;
	private TextView 		tvTopic,	//	内容标题;
							vBack,		//	返回按钮;
					 		btnFunction;//	功能按钮;
	private Gallery	 		mGallery;	//	画廊按钮;
	private	ProgressDialog	mDialog;	// 	对话框; 
	private WebView			vWvShow;	//	webview;
	/*参数*/
	private String 	 		_id,		//	id主键;
		  			 		sql,		//	SQl语句串;
		  			 		sResult,	//	结果字符串;
		  			 		bid,		//	业务编号;
		  			 		gid,		//	货物编号;
		  			 		folderPath,	//	文件夹路径;
		  			 		imgs
		  			 		;		
	//	数据库管理;
	private MTSQLiteHelper	  mSqLiteHelper;//  数据库帮助类;
	private SQLiteDatabase 	  mDB;		  //  数据库件;
	private Cursor 		   	  mCursor;      //  数据库遍历签;
	private MTConfigHelper	  mConfigHelper;//  配置项;
	private MTGetOrPostHelper mGetOrPostHelper;//	数据发送帮助类;
	private MTFileHelper      mtFileHelper;
	private MTImgHelper		  mImgHelper;   // 图片辅助类;
	//	图片的集合列表;
	private List<BitmapDrawable> listBD = null;// 承装图片的列表;
	private ArrayList<String>    listMapName;  // 承装文件夹的列表;
	private MyThread			 mThread;	   // 线程;
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
		//	设置内容;
		mDisplay 		= 	getWindowManager().getDefaultDisplay();
		mWindow			= 	getWindow(); 
		mtScreenHelper  =	new MTScreenHelper(mDisplay, mWindow);
		int screenWidth =   mtScreenHelper.getScreenWidth();
		int screenHeight=   mtScreenHelper.getScreenHeight();
		int tablewidth	=(int) (screenWidth*15f);
		int wordsize	=(int) (screenHeight*0.07f);
		int nImgHeight	=	screenHeight-4*wordsize;
		
		//	控件的初始化;
		btnFunction.setVisibility(View.GONE);
		tvTopic.setText("提货信息详情");
		//	获取id;
		Intent	mIntent =	getIntent();
		Bundle	mBundle =	mIntent.getExtras();
		_id				=	mBundle.getString("_id");
		imgs			=	mBundle.getString("imgs");
		//	数据库加载;
		mSqLiteHelper	=	new MTSQLiteHelper(mContext);
		mDB 			= 	mSqLiteHelper.getmDB();
		//	添加事件监听;
		vBack.setOnClickListener(this);
		//	设置图片适配器;
		listMapName		=	mtFileHelper.getFileNamesByList(imgs,"_");
		int size		=	listMapName.size();
		//	数据信息加载;
		doLoadData(size,tablewidth,wordsize);
		//	提货信息路径;
		folderPath		=	mConfigHelper.getfParentPath()+bid+File.separator+"ggoods"+File.separator+gid;
		//	承装图片的容器;
		if(size>0){
			listBD		=	mImgHelper.getBitmap01_2(folderPath, imgs);
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
	}

	//	信息加载;
	private void doLoadData(int size,int tablewidth,int wordsize){
		sql		=	"select * from getgoodsinfo where _id="+_id;
		tablewidth=wordsize*24*7;
		mCursor	= 	mDB.rawQuery(sql, null);
		while (mCursor.moveToNext()) {	
			gid					=	mCursor.getString(mCursor.getColumnIndex("barcode")).toString(); // 二维码信息;
			String dttrailerno	=	mCursor.getString(mCursor.getColumnIndex("dttrailerno")).toString(); // 拖车(取)拖车号(国内信息)

			String sealno		=	mCursor.getString(mCursor.getColumnIndex("sealno")).toString(); // 铅封号(货物信息)

			String dtsingletrailernum	=	mCursor.getString(mCursor.getColumnIndex("dtsingletrailernum")).toString(); // 拖车(取)单车件数
			String dtsingletrailerton	=	mCursor.getString(mCursor.getColumnIndex("dtsingletrailerton")).toString(); // 拖车(取)单车吨数
			String svehiclescoll		=	mCursor.getString(mCursor.getColumnIndex("svehiclescoll")).toString(); // 车数(取)(仓储)

			String dtpickupdate =	mCursor.getString(mCursor.getColumnIndex("dtpickupdate")).toString(); // 拖车(取)提货时间(国内时间)
			String dtstartdate	=	mCursor.getString(mCursor.getColumnIndex("dtstartdate")).toString(); // 拖车(取)发车时间(国内时间)
			String dgtrainwagonno=	mCursor.getString(mCursor.getColumnIndex("dgtrainwagonno")).toString(); // 铁路车皮号(国内信息)
			String dgtraintype	=	mCursor.getString(mCursor.getColumnIndex("dgtraintype")).toString(); // 铁路车型(国内信息)
			String dgtrainwaybillno=mCursor.getString(mCursor.getColumnIndex("dgtrainwaybillno")).toString(); // 铁路运单号(国内信息)
			String dgtrainsinglenum=mCursor.getString(mCursor.getColumnIndex("dgtrainsinglenum")).toString(); // 铁路单车件数(国内信息)
			String cargostatuscenter=mCursor.getString(mCursor.getColumnIndex("cargostatuscenter")).toString(); // 货物状态
			
			String dgtrainsingleton	=mCursor.getString(mCursor.getColumnIndex("dgtrainsingleton")).toString(); // 铁路单车吨数
			String dgtrainwagonkg	=mCursor.getString(mCursor.getColumnIndex("dgtrainwagonkg")).toString(); // 铁路车皮标重
			String dloadingtime		=mCursor.getString(mCursor.getColumnIndex("dloadingtime")).toString(); // 装车时间(调度)
			String dgtrainstartdate =mCursor.getString(mCursor.getColumnIndex("dgtrainstartdate")).toString(); // 铁路发运日
			String dgtrailerno		=mCursor.getString(mCursor.getColumnIndex("dgtrailerno")).toString(); // 拖车送拖车号(国内信息)
			String dtrailermodelsdely=mCursor.getString(mCursor.getColumnIndex("dtrailermodelsdely")).toString(); // 拖车车型(送)(调度)
			String dgsingletrailernum=mCursor.getString(mCursor.getColumnIndex("dgsingletrailernum")).toString(); // 拖车(送)单车件数(国内信息)
			String dgsingletrailerton=mCursor.getString(mCursor.getColumnIndex("dgsingletrailerton")).toString(); // 拖车(送)单车吨数(国内信息)
			String svehiclesdely	 =mCursor.getString(mCursor.getColumnIndex("svehiclesdely")).toString(); // 车数(送)(仓储)

			String dgstartdate		 =mCursor.getString(mCursor.getColumnIndex("dgstartdate")).toString(); // 拖车(送)发车时间(国内信息)
			// mCursor.getString(mCursor.getColumnIndex("img 	 			 varchar(1000) not null,"
			// + // 图片
			bid=mCursor.getString(mCursor.getColumnIndex("busiinvcode")).toString();		
			
			sResult="<html>" +
						"<body>" +
							"<table border=\"2\" style=\"width:"+tablewidth+"px;font-family:'宋体';font-weight:bold;font-size:"+wordsize+"px\">" +
						"<tr bgcolor=\"#00FF00\" align=\"center\">" +
							"<td >业务编号</td>" +
							"<td >条码信息</td>" +
							"<td >拖车号(取)</td>" +
							"<td >铅封号</td>" +
							"<td >拖车单车件数(取)</td>" +
							"<td >拖车单车吨数(取)</td>" +
							"<td >车数(取)</td>" +
							"<td >提货时间(取)</td>" +
							"<td >发车时间(取)</td>" +
							"<td >铁路车皮号(取)</td>" +
							"<td >铁路车型(取)</td>" +
							"<td >铁路运单(取)</td>" +
							"<td >铁路单车件数(取)</td>" +
							"<td >货物状态</td>" +
							"<td >铁路单车吨数</td>" +
							"<td >铁路单车标重</td>" +
							"<td >铁路装车时间</td>" +
							"<td >铁路发送日</td>" +
							"<td >拖车号(送)</td>" +
							"<td >拖车型(送)</td>" +
							"<td >拖车单车件数(送)</td>" +
							"<td >拖车单车吨数(送)</td>" +
							"<td >车数(送)</td>" +
							"<td >发车时间(送)</td>" +
						"</tr>";
				sResult+=
								"<tr align=\"center\">" +
										"<td >"+bid+"</td>" +
										"<td >"+gid+"</td>" +
										"<td >"+dttrailerno+"</td>" +
										"<td >"+sealno+"</td>" +
										"<td >"+dtsingletrailernum+"</td>" +
										"<td >"+dtsingletrailerton+"</td>" +
										"<td >"+svehiclescoll+"</td>" +
										"<td >"+dtpickupdate+"</td>" +
										"<td >"+dtstartdate+"</td>" +
										"<td >"+dgtrainwagonno+"</td>" +
										"<td >"+dgtraintype+"</td>" +
										"<td >"+dgtrainwaybillno+"</td>" +
										"<td >"+dgtrainsinglenum+"</td>" +
										"<td >"+cargostatuscenter+"</td>" +
										"<td >"+dgtrainsingleton+"</td>" +
										"<td >"+dgtrainwagonkg+"</td>" +
										"<td >"+dloadingtime+"</td>" +
										"<td >"+dgtrainstartdate+"</td>" +
										"<td >"+dgtrailerno+"</td>" +
										"<td >"+dtrailermodelsdely+"</td>" +
										"<td >"+dgsingletrailernum+"</td>" +
										"<td >"+dgsingletrailerton+"</td>" +
										"<td >"+svehiclesdely+"</td>" +
										"<td >"+dgstartdate+"</td>" +
								 "</tr>" +
							"</table><br/>" +
							"<table border=\"2\" style=\"font-family:'宋体';font-weight:bold;font-size:"+wordsize+"px\">" +
							 "<tr>" +
							 "<td align=\"center\" bgcolor=\"#00FF00\">图片</td><td align=\"center\">"+size+"张</td>" +
							 "</tr>"+
							"</table><br/><br/>" +
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
            return nSize;  
        }  
  
        public Object getItem(int position) {  
            return listBD.get(position);  
        }  
  
        public long getItemId(int position) {  
            return position;  
        }  

		public View getView(int position, View convertView, ViewGroup parent) {  
            
            ImageView imageview = new ImageView(mContext); 
            imageview.setImageDrawable(listBD.get(position));	            	
            
            imageview.setScaleType(ImageView.ScaleType.FIT_XY);  
            imageview.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT,nImgHeight));  
            imageview.setBackgroundResource(mGalBackgroundItem);  
            notifyDataSetChanged();
            return imageview;   
        }            
    }
	//	线程的自定义形式;
	class MyThread extends Thread{
		private int    position;
		public MyThread(int position) {
			this.position=position;
		}
		@Override
		public void run() {
			String path		=	folderPath+File.separator+listMapName.get(position)+".jpg";
			String url		=	"http://"+MTConfigHelper.TAG_IP_ADDRESS+":"+MTConfigHelper.TAG_PORT+"/"+MTConfigHelper.TAG_PROGRAM+"/upPhoto";
			String response	=	mGetOrPostHelper.uploadFile(url,path,listMapName.get(this.position));
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
