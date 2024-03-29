package cn.com.jy.view.need;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import cn.com.jy.activity.R;
import cn.com.jy.listener.DialogListener;
import cn.com.jy.model.entity.MEFile;
import cn.com.jy.model.helper.MTConfigHelper;
import cn.com.jy.model.helper.MTFileHelper;
import cn.com.jy.model.helper.MTGetOrPostHelper;
import cn.com.jy.model.helper.MTImgHelper;
import cn.com.jy.model.helper.MTSQLiteHelper;
import cn.com.jy.model.helper.MTSharedpreferenceHelper;
import cn.com.jy.view.extra.WritePadDialog;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SignInformationActivity extends Activity implements OnClickListener{
	//	上下文的内容信息;
	private Context	 mContext;
	private Intent	 mIntent;
	//	顶层的信息按钮;
	private TextView btnBack,
					 btnFunction,
					 vD1;
	private Button	 btnSearch,
					 btnCode,
					 btnOk,
					 btnSign,
					 btnPhoto
					 ;

	private TextView tvTopic,
					 mState02;
	
	private Spinner	 mState
					 ;
	
	private Builder				 mBuilder;	//	对话框;
	private EditText 			 etSearch;
	private ListView 			 mListView;
	private ArrayList<String>    list;
	private ArrayList<MEFile> 	 listfile;
	private ArrayAdapter<String> mAdapter;

	private ProgressDialog 		 mDialog; 	//	对话方框;
	private MyThread	   		 mThread; 	//	线程内容;
	private MyThread2	   		 mThread2; 	//	线程内容;
	
	private String 				 taskid,	//	标签Id编号;
								 bid,		//	业务编号;
								 gid,		//	货品编号;
								 state,		//	状态内容;
								 simg,
								 folderPath,		//	文件夹路径;
								    filePath,		//	文件路径;
								    tmpPath,		//	临时路径;
								 sSize,
								 wid,
								 operkind
								 ;
	//	帮助类;
	private MTConfigHelper	mConfigHelper;
	private MTGetOrPostHelper mGetOrPostHelper;
	private MTImgHelper		mImgHelper;
	private MTFileHelper	mtFileHelper;
	private MTSharedpreferenceHelper 		mSpHelper;	  // 首选项存储;
	
	private MTSQLiteHelper    mSqLiteHelper;// 数据库的帮助类;	
	private SQLiteDatabase  mDB;	  	  // 数据库件;
	private final String TAG_AUTO	=	"自动查询";
	private final String TAG_MANUAL	=	"手动输入";
	
	@SuppressLint("HandlerLeak")
	Handler mHandler    = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//	控制符的标签;
			Bundle bundle= msg.getData();
			int    nFlag = bundle.getInt("flag");
			mDialog.dismiss();
			
			switch (nFlag) {
			//	01.成功;
			case MTConfigHelper.NTAG_SUCCESS:				
				Toast.makeText(mContext, R.string.tip_success,Toast.LENGTH_SHORT).show();
				mtFileHelper.fileDelAll();
				break;
			//	02.失败;
			case MTConfigHelper.NTAG_FAIL:
				Toast.makeText(mContext, R.string.tip_fail,Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
			Log.i("MyLog", "bid="+bid+"|gid="+gid);
			showData();
			closeThread();
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign);
		//	控件的初始化;
		initView();
		//	事件的初始化;
		initEvent();
	}
	
	//	控件的初始化;
	private void initView(){
		//	顶层控件初始化;
		btnBack		=(TextView) findViewById(R.id.btnBack);
		btnFunction	=(TextView) findViewById(R.id.btnFunction);
		tvTopic		=(TextView) findViewById(R.id.tvTopic);
		
		btnSearch	=(Button) findViewById(R.id.btnSearch);
		btnCode		=(Button) findViewById(R.id.btnCode);
		btnOk		=(Button) findViewById(R.id.btnOk);
		btnSign		=(Button) findViewById(R.id.sign);
		btnPhoto	=(Button) findViewById(R.id.photo);
		mState02	=(TextView) findViewById(R.id.state02);
		mState		=(Spinner) findViewById(R.id.state);
		
		etSearch	=(EditText) findViewById(R.id.etSearch);
		mListView	=(ListView) findViewById(R.id.listView);
		
		vD1=(TextView) findViewById(R.id.d1);
	}
	//	事件监听初始;
	@SuppressWarnings("static-access")
	private void initEvent(){
		mContext		=	SignInformationActivity.this;
		operkind		=	TAG_AUTO;
		tvTopic.setText("签收——操作模式	( " + operkind + " )");
		chooseOperKind(mContext, tvTopic);
		processOperKind();
		//	系统的配置工具类的添加;
		mGetOrPostHelper=	new MTGetOrPostHelper();
		mConfigHelper	=	new MTConfigHelper();
		mImgHelper		=	new MTImgHelper();
		mtFileHelper	=	new MTFileHelper();
		mSpHelper  	  	= 	new MTSharedpreferenceHelper(mContext, MTConfigHelper.CONFIG_SELF,mContext.MODE_APPEND);
		
		//	数据库的操作;
		mSqLiteHelper 	=	new MTSQLiteHelper(mContext);
		mDB			  	=	mSqLiteHelper.getmDB();
		
		//	信息列表的加载;
		list			=	new ArrayList<String>();
		listfile		=	mtFileHelper.getListfiles();

		//	控件信息事件初始化;
		btnFunction.setText("历史");

		//	添加事件的监听;
		btnBack.setOnClickListener(this);
		btnFunction.setOnClickListener(this);	
		btnSearch.setOnClickListener(this);
		btnCode.setOnClickListener(this);
		btnOk.setOnClickListener(this);
		btnSign.setOnClickListener(this);
		btnPhoto.setOnClickListener(this);
		//	状态选择;
		mState.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapter, View view,
					int position, long id) {
				switch (position) {
				case 0:
					state="正常";
					break;
				case 1:
					mBuilder	=	new Builder(mContext);
					mBuilder.setTitle("异常信息");
					final EditText   edit	=	new EditText(mContext);
					edit.setSingleLine(false);
					edit.setLines(6);
					mBuilder.setView(edit);
					mBuilder.setPositiveButton(R.string.action_ok,new  DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							String tmp=edit.getText().toString().trim();
							if(!tmp.equals("")){								
								state=tmp;	
							}else if(tmp.equals("")){
								state="异常";
							}
						}
					});
					mBuilder.setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							state	="正常";
							mState.setSelection(0);
						}
					});
					mBuilder.create();
					mBuilder.show();
					break;
				default:
					break;
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> adapter) {
				state="正常";	
			}
		});

		etSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				doResetParam2();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
	}
	@Override
	protected void onResume() {
		super.onResume();
		showData();
	}
	@Override
	public void onClick(View view) {
		int nVid=view.getId();
		switch (nVid) {
		//	照相按钮;
		case R.id.photo:
			getPhoto_Ggoods();
			break;
		//	签字板;
		case R.id.sign:
			if(operkind.equals(TAG_AUTO)){				
				if(bid!=null&&gid!=null){						
					folderPath	= mConfigHelper.getfParentPath()+bid+File.separator+"sign"+File.separator+gid;
					mConfigHelper.doSetScreenWidthAndHeigth(mContext);
					
					WritePadDialog writeTabletDialog = new WritePadDialog(
							mContext, new DialogListener() {
								@Override
								public void refreshActivity(Object object) {			
									//	进行数据的长宽设置;
									simg			   = bid+"sign"+gid+"file"+java.lang.System.currentTimeMillis();
									Bitmap 	zoombm	   = mImgHelper.doWriteImg(object, folderPath, simg);
									if(zoombm!=null){
										MEFile meFile=new MEFile(simg, filePath);
										mtFileHelper.fileAdd(meFile);
										sSize		   = String.valueOf(mtFileHelper.getListfiles().size());
										mState02.setText(sSize);
									}
								}
							},mConfigHelper.getScreenWidth(),mConfigHelper.getScreenHeigth());
					writeTabletDialog.show();
				}else Toast.makeText(mContext, "请先扫描一维/二维码", Toast.LENGTH_SHORT).show();
			}else if(operkind.equals(TAG_MANUAL)){
				String tmp=etSearch.getText().toString().trim();
				if(!tmp.equals("")){
					bid=gid=tmp;
					folderPath	= mConfigHelper.getfParentPath()+bid+File.separator+"sign"+File.separator+gid;
					mConfigHelper.doSetScreenWidthAndHeigth(mContext);
					
					WritePadDialog writeTabletDialog = new WritePadDialog(
							mContext, new DialogListener() {
								@Override
								public void refreshActivity(Object object) {			
									//	进行数据的长宽设置;
									simg			   = bid+"sign"+gid+"file"+java.lang.System.currentTimeMillis();
									Bitmap 	zoombm	   = mImgHelper.doWriteImg(object, folderPath, simg);
									if(zoombm!=null){
										MEFile meFile=new MEFile(simg, filePath);
										mtFileHelper.fileAdd(meFile);
										sSize		   = String.valueOf(mtFileHelper.getListfiles().size());
										mState02.setText(sSize);
									}
								}
							},mConfigHelper.getScreenWidth(),mConfigHelper.getScreenHeigth());
					writeTabletDialog.show();
				}else Toast.makeText(mContext, "请先扫描一维/二维码", Toast.LENGTH_SHORT).show();
			}
				
			break;
		//	返回键;
		case R.id.btnBack:
			int n=listfile.size();
			if(n!=0){
				for(MEFile item:listfile){
					String path=item.getPath();
					File file=new File(path);
					if(file.exists()){
						file.delete();
					}
				}
			}
			finish();
			break;
			
		//	历史信息;
		case R.id.btnFunction:
			mIntent=new Intent(mContext, SignHistoryActivity.class);
			startActivity(mIntent);
			break;
			
		//	重置按钮;
		case R.id.btnReset:
			etSearch.setText(MTConfigHelper.SPACE);
			break;
			
		//	搜索按钮;
		case R.id.btnSearch:
			if(mThread==null){
				int nSize=list.size();
				if(nSize!=0){
					list.clear();
				}
				// 进度条的内容;
				final CharSequence strDialogTitle = getString(R.string.tip_dialog_wait);
				final CharSequence strDialogBody  = getString(R.string.tip_dialog_done);
				mDialog 						  = ProgressDialog.show(mContext, strDialogTitle, strDialogBody,true);	
				taskid							  = etSearch.getText().toString().trim();
				mThread=new MyThread(mGetOrPostHelper);
				mThread.start();
			}
			break;
		
		//	二维码按钮;
		case R.id.btnCode:
			//	跳转至专门的intent控件;
			mIntent	=	new Intent(mContext, FlushActivity.class);
			//	有返回值的跳转;
			startActivityForResult(mIntent,MTConfigHelper.NTRACK_SIGN_GID_TO);
			break;
			
		//	上传按钮;
		case R.id.btnOk:
			if(operkind.equals(TAG_AUTO)){
				if(bid!=null&&gid!=null){
					//	图片;
					simg = mtFileHelper.getFileNamesByStrs(mtFileHelper.getListfiles(),"_");
					if (simg.equals("")) simg = "未拍照";
					wid		= 	mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
					mBuilder=	new Builder(mContext);
					mBuilder.setTitle("信息确认");
					sSize		=	String.valueOf(mtFileHelper.getListfiles().size());
					String sContent="二维码号:"+bid+"\r\n状态:"+state+"\r\n图片张数:"+sSize;
					mBuilder.setMessage(sContent);
					mBuilder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							//	线程启动;
							if(mThread2==null){
								// 进度条的内容;
								final CharSequence strDialogTitle = getString(R.string.tip_dialog_wait);
								final CharSequence strDialogBody = getString(R.string.tip_dialog_done);
								mDialog = ProgressDialog.show(mContext, strDialogTitle,strDialogBody, true);
								mThread2=new MyThread2(mGetOrPostHelper);
								mThread2.start();
							}						
						}
					});
					
					mBuilder.setNegativeButton(R.string.action_no, null);
					mBuilder.create();
					mBuilder.show();
				}else Toast.makeText(mContext, "请先扫描一维/二维码", Toast.LENGTH_SHORT).show();
			}else if(operkind.equals(TAG_MANUAL)){
				String tmp=etSearch.getText().toString().trim();
				if(!tmp.equals("")){
					bid=gid=tmp;
//					图片;
					simg = mtFileHelper.getFileNamesByStrs(mtFileHelper.getListfiles(),"_");
					if (simg.equals("")) simg = "未拍照";
					wid		= 	mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
					mBuilder=	new Builder(mContext);
					mBuilder.setTitle("信息确认");
					sSize		=	String.valueOf(mtFileHelper.getListfiles().size());
					String sContent="二维码号:"+bid+"\r\n状态:"+state+"\r\n图片张数:"+sSize;
					mBuilder.setMessage(sContent);
					mBuilder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							//	线程启动;
							if(mThread2==null){
								// 进度条的内容;
								final CharSequence strDialogTitle = getString(R.string.tip_dialog_wait);
								final CharSequence strDialogBody = getString(R.string.tip_dialog_done);
								mDialog = ProgressDialog.show(mContext, strDialogTitle,strDialogBody, true);
								mThread2=new MyThread2(mGetOrPostHelper);
								mThread2.start();
							}						
						}
					});
					
					mBuilder.setNegativeButton(R.string.action_no, null);
					mBuilder.create();
					mBuilder.show();
				}else Toast.makeText(mContext, "请先扫描一维/二维码", Toast.LENGTH_SHORT).show();
			}
	
			break;
			
		default:
			break;
		}
	}
	//	检测网络信息的线程;
	//	定义的线程——自定义的线程内容;
	public class MyThread extends Thread{
		private MTGetOrPostHelper	mtGetOrPostHelper;
		
		public MyThread(MTGetOrPostHelper mtGetOrPostHelper) {
			this.mtGetOrPostHelper=mtGetOrPostHelper;
		}
		
		@Override
		public void run() {
			// 进行相应的登录操作的界面显示;
			//	01.Http 协议中的Get和Post方法;
			String url		 =	"http://"+MTConfigHelper.TAG_IP_ADDRESS+":"+MTConfigHelper.TAG_PORT+"/"+MTConfigHelper.TAG_PROGRAM+"/goods";
			String param	 =	"operType=5&barcode="+taskid;
			String response  = 	mtGetOrPostHelper.sendGet(url,param);
			Message	message	 =	new Message();
			Bundle	bundle	 =  new Bundle();
			int nFlag= 	MTConfigHelper.NTAG_FAIL;
			
			if(!response.trim().equalsIgnoreCase("fail")){
				nFlag= MTConfigHelper.NTAG_SUCCESS;
				try {
					JSONArray array = new JSONArray(response);
					int 	  i		= 0;
					JSONObject obj 	= null;
					do {
						try {		
							//	JsonObject的解析;
							obj			  =	array.getJSONObject(i);	
							
							
							bid			=obj.getString("busiinvcode");
							gid			=etSearch.getText().toString();
							String wcode=obj.getString("wcode");
							String cname=obj.getString("cname");
							String cid	=obj.getString("cid");
							String csize=obj.getString("csize");
							String ctype=obj.getString("ctype");
							String sealno=obj.getString("sealno");
							String pieces=obj.getString("pieces");
							String goodsdesc	=obj.getString("goodsdesc");
							String grossweight	=obj.getString("grossweight");
							String grossweightjw=obj.getString("grossweightjw");
							String grossweighgn	=obj.getString("grossweighgn");
							String volume=obj.getString("volume");
							String length=obj.getString("length");
							String width =obj.getString("width");
							String height=obj.getString("height");

							list.add("业务编号:"+bid);
							list.add("建单人:"+wcode);
							list.add("品名:"+cname);
							list.add("箱号:"+cid);
							list.add("箱尺寸:"+csize);
							list.add("箱型:"+ctype);
							list.add("铅封号:"+sealno);
							list.add("件数:"+pieces);
							list.add("包装类型:"+goodsdesc);
							list.add("毛重量:"+grossweight);
							list.add("毛重量-境外(KGS):"+grossweightjw);
							list.add("毛重量-国内(KGS):"+grossweighgn);
							list.add("体积(CBM):"+volume+" 长(CM):"+length+" 宽(CM):"+width+" 高(CM):"+height);
	
							i++;
						} catch (Exception e) {
							obj=null;
						}
					} while (obj!=null);
				} catch (JSONException e) {
					nFlag	=	MTConfigHelper.NTAG_FAIL;
				}
			}
			bundle.putInt("flag", nFlag);
			message.setData(bundle);
			mHandler.sendMessage(message);
		}
	}
	//	信息的另一个添加;
	public class MyThread2 extends Thread{

		private MTGetOrPostHelper	mtGetOrPostHelper;
		public MyThread2(MTGetOrPostHelper	mtGetOrPostHelper) {
			this.mtGetOrPostHelper=mtGetOrPostHelper;
		}
		
		@Override
		public void run() {
			// 进行相应的登录操作的界面显示;
			//	01.Http 协议中的Get和Post方法;
			String url	 =	"http://"+MTConfigHelper.TAG_IP_ADDRESS+":"+MTConfigHelper.TAG_PORT+"/"+MTConfigHelper.TAG_PROGRAM+"/resign";
			String date  =  mConfigHelper.getCurrentDate("yyyy年MM月dd日HH时mm分");
			String param =  null;
			String response=null;
			String sql	 =  null;
			int    nFlag = 	MTConfigHelper.NTAG_FAIL;
			Message	message=new Message();
			Bundle bundle=	new Bundle();
			try {
				param =	"operType=1" +
						"&barcode="+gid+
						"&cargostatussign="+URLEncoder.encode(state,"utf-8")+
						"&receiptdate="+URLEncoder.encode(date,"utf-8")+
						"&img="+URLEncoder.encode(simg,"utf-8")+
						"&wid="+wid+
						"&busiinvcode="+bid
						;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			response = 	mtGetOrPostHelper.sendGet(url,param);
			
			if(!response.trim().equalsIgnoreCase("fail")){
				nFlag= MTConfigHelper.NTAG_SUCCESS;
				sql=
				"insert into signinfo (" +
				"barcode,receiptdate,cargostatussign,img,busiinvcode) values (" +
				"'"+gid+"'," +
				"'"+date+"'," +
				"'"+state+"',"+ 
				"'"+simg+"'," +
				"'"+bid+"')"; 
				mDB.execSQL(sql);
			}
			bundle.putInt("flag", nFlag);
			message.setData(bundle);
			mHandler.sendMessage(message);
		}
	}
	//	重新置空所有选项卡;
	private void doResetParam2(){
		//	bid置空&gid置空;
		bid=null;
		gid=null;
		//	数据列表;
		list.clear();
		//	重新加载数据;
		showData();
		//	异常按钮重置;
		state	="正常";
		mState.setSelection(0);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(requestCode==MTConfigHelper.NTRACK_SIGN_GID_TO&&resultCode==MTConfigHelper.NTRACK_FLUSH_TO_MENU){
			String 	gid	=	intent.getStringExtra("bid");
			etSearch.setText(gid);
		}else if(requestCode==MTConfigHelper.NTRACK_SIGN_PHOTO_TO&&resultCode==-1 ){			
			Toast.makeText(mContext, "拍照完成", Toast.LENGTH_SHORT).show();
			mImgHelper.compressPicture(tmpPath, filePath);
			mImgHelper.clearPicture(tmpPath,null);
			MEFile meFile=new MEFile(simg, filePath);
			mtFileHelper.fileAdd(meFile);
			sSize		=	String.valueOf(mtFileHelper.getListfiles().size());
			mState02.setText(sSize);
		}
	}
	private void showData(){
		//	图片的大小张数;
		folderPath	=	mConfigHelper.getfParentPath()+bid+File.separator+"sign"+File.separator+gid;
		sSize		=	String.valueOf(mtFileHelper.getListfiles().size());
		mState02.setText(sSize);
		mAdapter=new ArrayAdapter<String>(mContext, R.layout.item02,R.id.tvTopic, list);
		mListView.setAdapter(mAdapter);
	}
	//	拍照功能;
	public void getPhoto_Ggoods(){
		File 	file;
		if (mConfigHelper.getfState().equals(Environment.MEDIA_MOUNTED)) {
			if(operkind.endsWith(TAG_AUTO)){
				if(bid!=null&&gid!=null){
					folderPath	= mConfigHelper.getfParentPath()+bid+File.separator+"sign"+File.separator+gid;
					simg  		= bid+"sign"+gid+"file"+java.lang.System.currentTimeMillis();
					file	  	= new File(folderPath);
					//	生成文件夹的方式;
					if(!file.exists()){
						file.mkdirs();
					}
					//	生成2中文件路径:01.临时的 02.永久的
					tmpPath		= folderPath+File.separator+simg+"_tmp.jpg";
					filePath  	= folderPath+File.separator+simg+".jpg";
					file 	  	= new File(tmpPath);
					if(file.exists()){				
						file.delete();
					}
					if (!file.exists()) {
						try {
							file.createNewFile();
						} catch (IOException e) {
							Toast.makeText(mContext, "照片创建失败!",Toast.LENGTH_LONG).show();
							return;
						}
					}
					mIntent = new Intent("android.media.action.IMAGE_CAPTURE");
					mIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
					startActivityForResult(mIntent, MTConfigHelper.NTRACK_SIGN_PHOTO_TO);
				}else Toast.makeText(mContext, "请先扫描一维/二维码", Toast.LENGTH_SHORT).show();		
			}else if(operkind.equals(TAG_MANUAL)){
				String tmp=etSearch.getText().toString().trim();
				
				if(!tmp.equals("")){
					bid=gid=tmp;
					folderPath	= mConfigHelper.getfParentPath()+bid+File.separator+"sign"+File.separator+gid;
					simg  		= bid+"sign"+gid+"file"+java.lang.System.currentTimeMillis();
					file	  	= new File(folderPath);
					//	生成文件夹的方式;
					if(!file.exists()){
						file.mkdirs();
					}
					//	生成2中文件路径:01.临时的 02.永久的
					tmpPath		= folderPath+File.separator+simg+"_tmp.jpg";
					filePath  	= folderPath+File.separator+simg+".jpg";
					file 	  	= new File(tmpPath);
					if(file.exists()){				
						file.delete();
					}
					if (!file.exists()) {
						try {
							file.createNewFile();
						} catch (IOException e) {
							Toast.makeText(mContext, "照片创建失败!",Toast.LENGTH_LONG).show();
							return;
						}
					}
					mIntent = new Intent("android.media.action.IMAGE_CAPTURE");
					mIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
					startActivityForResult(mIntent, MTConfigHelper.NTRACK_SIGN_PHOTO_TO);
				}else Toast.makeText(mContext, "请先扫描一维/二维码", Toast.LENGTH_SHORT).show();
			}
		
		} else Toast.makeText(mContext, "sdcard无效或没有插入!",Toast.LENGTH_SHORT).show();
	}
	// 进行选择的按钮内容;
	private void chooseOperKind(Context context,final TextView vTopic){
		operkind=TAG_AUTO;
		Builder vBuilder=new Builder(context);
		vBuilder.setTitle("选择操作方式");
		final String[] kinds = { TAG_AUTO, TAG_MANUAL };

		vBuilder.setItems(kinds, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int position) {
				operkind = kinds[position];
				vTopic.setText("签收——操作模式	( " + operkind + " )");
				processOperKind();
			}
		});

		vBuilder.setNegativeButton(R.string.action_no, null);
		vBuilder.create();
		vBuilder.show();
	}
	
	//	进行相应的内容;
	private void processOperKind(){
		Log.i("MyLog", "操作状态="+operkind);
		if(operkind.equals(TAG_AUTO)){
			vD1.setVisibility(View.VISIBLE);
			btnSearch.setVisibility(View.VISIBLE);
		}else if(operkind.equals(TAG_MANUAL)){
			vD1.setVisibility(View.GONE);
			btnSearch.setVisibility(View.GONE);
		}
	}
	
	//	关闭线程;
	private void closeThread(){
		if(mThread!=null){
			mThread.interrupt();
			mThread=null;
		}
		
		if(mThread2!=null){
			mThread2.interrupt();
			mThread2=null;
		}
	}
}
