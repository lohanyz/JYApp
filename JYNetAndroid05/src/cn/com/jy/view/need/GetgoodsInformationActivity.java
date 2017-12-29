package cn.com.jy.view.need;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.jy.activity.R;
import cn.com.jy.model.entity.MEFile;
import cn.com.jy.model.helper.MTConfigHelper;
import cn.com.jy.model.helper.MTFileHelper;
import cn.com.jy.model.helper.MTGetOrPostHelper;
import cn.com.jy.model.helper.MTImgHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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

/**
 * @author Li Yuxuan;
 * */
public class GetgoodsInformationActivity extends Activity implements OnClickListener {
	/*程序内部的上下文*/
	private Context   mContext;   // 文件内容;
	/*进行信息传递的intent*/
	private Intent 	  mIntent;
	private Bundle	  mBundle;
	/*控件的声明*/
	private Button	  vCode, 	  // 扫码按钮;
					  vSearch, 	  // 搜索按钮;
					  vPhoto, 	  // 拍照按钮;
					  vOk 		  // 确认按钮;
					  ;
	private TextView  vTopic, 	  // 标题显示;
					  vMapCount,    // 状态2;
					  vBack, 	  // 返回按钮;
					  vFunction;  // 功能按钮;
					 ;

	private Builder   vBuilder;    // 对话框;
	private Spinner   vState;
	private EditText  etoid, 	   // 运输号;
					  etSearch 	   // 搜索的框;
					  ;
	private ListView  vListView;
	private ProgressDialog mDialog;// 对话方框;
	private ArrayAdapter<String> mAdapter;
	/*参数定义*/
	private String   slkind, 	  // 标题栏;
					 taskid,
					 gid, 		  // 货品号;
					 bid, gstate   = "正常", 
					 gsimg 		   = "null", 
					 folderPath, // 文件夹路径;
					 filePath, 	 // 文件路径;
					 tmpPath, 	 // 临时路径;
					 sSize;
	private ArrayList<String> 	 list;
	//	TODO 01.新增的相关内容;
	private ArrayList<MEFile> 	 listfile;
	
	//	TODO 02.修改的相关内容;
	private LoadInfoThread mThread; // 线程内容;
	// 帮助类;
	private MTConfigHelper 	  mConfigHelper;
	private MTGetOrPostHelper mGetOrPostHelper;
	private MTImgHelper 	  mImgHelper;
	private MTFileHelper 	  mtFileHelper;

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle= msg.getData();
			int    nFlag = bundle.getInt("flag");
			mDialog.dismiss();
			switch (nFlag) {
			// 01.成功;
			case MTConfigHelper.NTAG_SUCCESS:
				Toast.makeText(mContext, R.string.tip_success,Toast.LENGTH_SHORT).show();
				mtFileHelper.fileDelAll();
				break;
			// 02.失败;
			case MTConfigHelper.NTAG_FAIL:
				Toast.makeText(mContext, R.string.tip_fail, Toast.LENGTH_LONG)
						.show();
				break;
			default:
				break;
			}
			showImgCount();
			showData();
			closeThread();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 加载正式页面信息;
		setContentView(R.layout.ggoods);
		initView();
		initEvent();
		// 首先选择运输类型;
		chooseLoadKind();
	}

	private void chooseLoadKind() {
		slkind 		  = "拖车";
		mContext 	  = GetgoodsInformationActivity.this;
		vBuilder 	  = new Builder(mContext);
		
		vBuilder.setTitle("选择运输方式(默认拖车)");
		final String[] kinds = { "拖车", "铁路" };

		vBuilder.setItems(kinds, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int position) {
				slkind = kinds[position];
				vTopic.setText("提货	( " + slkind + " )");
			}
		});

		vBuilder.setNegativeButton(R.string.action_no, null);
		vBuilder.create();
		vBuilder.show();
		vTopic.setText("提货	( " + slkind + " )");
	}

	// 控件初始化;
	private void initView() {
		vBack 	  = (TextView) findViewById(R.id.btnBack);
		vFunction = (TextView) findViewById(R.id.btnFunction);
		vTopic 	  = (TextView) findViewById(R.id.tvTopic);
		//	扫描二维码的按钮;
		vCode 	  = (Button) findViewById(R.id.btnCode);
		vSearch   = (Button) findViewById(R.id.btnSearch);
		//	拍照按钮;
		vPhoto 	  = (Button) findViewById(R.id.btnPhoto);
		vOk 	  = (Button) findViewById(R.id.btnAdd);

		vMapCount = (TextView) findViewById(R.id.tvMapCount);
		vListView = (ListView) findViewById(R.id.lvResult);
		etSearch  = (EditText) findViewById(R.id.etSearch);
		vState 	  = (Spinner) findViewById(R.id.gstate);
	}

	// 事件初始化;
	private void initEvent() {
		// 系统的配置工具类的添加;
		mGetOrPostHelper = new MTGetOrPostHelper();
		mConfigHelper 	 = new MTConfigHelper();
		mImgHelper 		 = new MTImgHelper();
		//	文件的管理类对象;
		mtFileHelper	 = new MTFileHelper();
		// 信息列表的加载;
		list = new ArrayList<String>();

		listfile		 = mtFileHelper.getListfiles();
		// 顶部按钮的事件监听的添加;
		vBack.setOnClickListener(this);
		vFunction.setOnClickListener(this);
		vFunction.setText("历史");

		// 中间层次按钮的事件监听的添加;
		vCode.setOnClickListener(this);
		vSearch.setOnClickListener(this);

		vPhoto.setOnClickListener(this);
		vOk.setOnClickListener(this);		
		
		// 进行事件监听的添加;
		vState.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				switch (position) {
				case 0:
					gstate = "正常";
					break;
				case 1:
					vBuilder = new Builder(mContext);
					vBuilder.setTitle("异常信息");
					final EditText edit = new EditText(mContext);
					edit.setSingleLine(false);
					edit.setLines(6);
					vBuilder.setView(edit);
					vBuilder.setPositiveButton(R.string.action_ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									String tmp = edit.getText().toString().trim();
									if (!tmp.equals("")) {
										gstate = tmp;
									}
								}
							});
					vBuilder.setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							gstate = "正常";
							vState.setSelection(0);
						}
					});
					vBuilder.create();
					vBuilder.show();
					break;
				default:
					break;
				}

			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				gstate = "正常";
			}
		});
		
		etSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				doResetParam();
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
		showImgCount();
		showData();
	}

	String date, time;

	@Override
	public void onClick(View view) {
		int nVid = view.getId();
		switch (nVid) {
		// 返回按钮;
		case R.id.btnBack:
			// TODO 修改的内容;
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
		case R.id.btnFunction:
			mIntent = new Intent(mContext, GetgoodsHistoryActivity.class);
			startActivity(mIntent);
			break;
		// 扫描二维码按钮;
		case R.id.btnCode:
			// 跳转至专门的intent控件;
			mIntent = new Intent(mContext, FlushActivity.class);
			// 有返回值的跳转;
			startActivityForResult(mIntent, MTConfigHelper.NTRACK_GGOODS_GID_TO);
			break;
		// 搜索按钮;
		case R.id.btnSearch:
			if (mThread == null) {
				int nSize = list.size();
				if (nSize != 0) {
					list.clear();
				}
				// 进度条的内容;
				final CharSequence strDialogTitle = getString(R.string.tip_dialog_wait);
				final CharSequence strDialogBody = getString(R.string.tip_dialog_done);
				mDialog = ProgressDialog.show(mContext, strDialogTitle,
						strDialogBody, true);
				taskid = etSearch.getText().toString().trim();
				mThread = new LoadInfoThread();
				mThread.start();
			}
			break;
		case R.id.btnPhoto:
			getPhoto_Ggoods();
			break;
			
		// 确认按钮;
		case R.id.btnAdd:
			if (bid != null && gid != null) {				
				mIntent=new Intent(mContext, GetgoodsAddActivity.class);
				mBundle=new Bundle();
				mBundle.putString("busiinvcode", bid);
				mBundle.putString("barcode", gid);
				mBundle.putString("slkind", slkind);
				mBundle.putString("cargostatuscenter", gstate);
				gsimg=mtFileHelper.getFileNamesByStrs(mtFileHelper.getListfiles(),"_");
				if (gsimg.equals("")) gsimg = "未拍照";
				mBundle.putString("imgs", gsimg);
				mIntent.putExtras(mBundle);
				startActivityForResult(mIntent, 1);
			}else Toast.makeText(mContext, "请先扫描一维/二维码", Toast.LENGTH_SHORT).show();

			break;
		default:
			break;
		}
	}

	// 返回键
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == MTConfigHelper.NTRACK_GGOODS_GID_TO
				&& resultCode == MTConfigHelper.NTRACK_FLUSH_TO_MENU) {
			String gid = intent.getStringExtra("bid");
			etSearch.setText(gid);
		} else if (requestCode == MTConfigHelper.NTRACK_GGOODS_PHOTO_TO
				&& resultCode == -1) {
			Toast.makeText(mContext, "拍照完成", Toast.LENGTH_SHORT).show();
			//	清空照片列表;
			mImgHelper.compressPicture(tmpPath, filePath);
			mImgHelper.clearPicture(tmpPath, null);
			//	进行文件内容的叠加;
			MEFile meFile=new MEFile(gsimg, filePath);
			//	将拍照操作放入列表;
			// TODO 修改的内容;
			mtFileHelper.fileAdd(meFile);
			showImgCount();
		} else if (requestCode == MTConfigHelper.NTRACK_GGOODS_OID_TO
				&& resultCode == MTConfigHelper.NTRACK_FLUSH_TO_MENU) {
			String oid = intent.getStringExtra("bid");
			etoid.setText(oid);
		}else if(requestCode ==1){
			if(resultCode==1){
				doResetParam();
			}
		}
	}
	private void doResetParam() {
		// 数据列表;
		list.clear();
		// 图片列表清空;
		mtFileHelper.fileDelAll();
		// 重新加载数据;
		showImgCount();
		showData();
		// 异常按钮重置;
		gstate = "正常";
		vState.setSelection(0);
		// bid置空&gid置空;
		bid = null;
		gid = null;
		taskid=null;
	}

	// 定义的线程——自定义的线程内容;
	public class LoadInfoThread extends Thread {
		@Override
		public void run() {
			// 进行相应的登录操作的界面显示;
			// 01.Http 协议中的Get和Post方法;
			String url 	  	 = "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":"+ MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM+ "/goods";
			String param 	 = "operType=1&barcode=" + taskid;
			String response  = mGetOrPostHelper.sendGet(url, param);
			int    nFlag 	 = MTConfigHelper.NTAG_FAIL;
			//	发送包;
			Message	msg		 = new Message();
			Bundle	bundle	 = new Bundle();
			
			if (!response.trim().equalsIgnoreCase("fail")) {
				nFlag = MTConfigHelper.NTAG_SUCCESS;
				try {
					JSONArray array = new JSONArray(response);
					int i = 0;
					JSONObject obj = null;
					do {
						try {
							// JsonObject的解析;
							obj 			 = array.getJSONObject(i);
							bid 			 = obj.getString("busiinvcode");
							String tradecode = obj.getString("tradecode");
							String wcode 	 = obj.getString("wcode");
							String deliveryaddress 	 = obj.getString("deliveryaddress");
							
							String cname 	 = obj.getString("cname");
							String cid 	 	 = obj.getString("cid");
							String csize  	 = obj.getString("csize");
							String ctype 	 = obj.getString("ctype");
							String sealno 	 = obj.getString("sealno");
							String goodsdesc 	= obj.getString("goodsdesc");
							
							String pieces 	    = obj.getString("pieces");
							String grossweight  = obj.getString("grossweight");
							String grossweightjw= obj.getString("grossweightjw");
							String grossweighgn = obj.getString("grossweighgn");
							
							String volume	 = obj.getString("volume");
							String length	 = obj.getString("length");
							String width	 = obj.getString("width");
							String height	 = obj.getString("height");
							gid				 = etSearch.getText().toString();
							list.add("业务编号:" + bid);
							list.add("业务类型编号:" + tradecode);
							list.add("建单人:" + wcode);
							list.add("提货地址:" + deliveryaddress);
							list.add("品名(货物信息):" + cname);
							list.add("箱号(货物信息):" + cid);
							list.add("箱尺寸(货物信息):" + csize);
							list.add("箱型(货物信息):" + ctype);
							list.add("铅封号(货物信息):" + sealno);
							list.add("包装类型:" + goodsdesc);
							list.add("件数:" + pieces);
							list.add("毛重量(货物信息):" + grossweight);
							list.add("毛重量——境外(KGS):" + grossweightjw);
							list.add("毛重量——境内(KGS):" + grossweighgn);
							list.add("体积(CBM)(货物信息):" + volume);
							list.add("长(CM):" + length+" 宽(CM):"+width+" 高(CM):"+height);
							i++;
						} catch (Exception e) {
							obj = null;
						}
					} while (obj != null);
				} catch (JSONException e) {
					nFlag = MTConfigHelper.NTAG_FAIL;
				}
			}
			bundle.putInt("flag", nFlag);
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}
	}
	// TODO 修改的内容;
	private void showImgCount(){
		sSize = String.valueOf(mtFileHelper.getListfiles().size());
		vMapCount.setText(sSize);		
	}
	private void showData() {
		mAdapter = new ArrayAdapter<String>(mContext, R.layout.item02, R.id.tvTopic, list);
		//	显示的列表和适配器绑定;
		vListView.setAdapter(mAdapter);
	}

	// 拍照功能;
	public void getPhoto_Ggoods() {
		File file;
		if (mConfigHelper.getfState().equals(Environment.MEDIA_MOUNTED)) {
			if (bid != null && gid != null) {
				folderPath = mConfigHelper.getfParentPath() + bid
						+ File.separator + "ggoods" + File.separator + gid;
				gsimg = bid + "getgoods" + gid + "file"
						+ java.lang.System.currentTimeMillis();
				file = new File(folderPath);
				// 生成文件夹的方式;
				if (!file.exists()) {
					file.mkdirs();
				}
				// 生成2中文件路径:01.临时的 02.永久的
				tmpPath = folderPath + File.separator + gsimg + "_tmp.jpg";
				filePath = folderPath + File.separator + gsimg + ".jpg";
				file = new File(tmpPath);
				if (file.exists()) {
					file.delete();
				}
				if (!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException e) {
						Toast.makeText(mContext, "照片创建失败!", Toast.LENGTH_LONG).show();
						return;
					}
				}
				mIntent = new Intent("android.media.action.IMAGE_CAPTURE");
				mIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
				startActivityForResult(mIntent,MTConfigHelper.NTRACK_GGOODS_PHOTO_TO);
			} else Toast.makeText(mContext, "请先扫描一维/二维码", Toast.LENGTH_SHORT).show();
		} else Toast.makeText(mContext, "sdcard无效或没有插入!", Toast.LENGTH_SHORT).show();
	}
	private void closeThread() {
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
	}
}
