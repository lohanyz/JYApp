package cn.com.jy.view;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.jy.activity.R;
import cn.com.jy.model.entity.MEFile;
import cn.com.jy.model.helper.MTConfigHelper;
import cn.com.jy.model.helper.MTFileHelper;
import cn.com.jy.model.helper.MTGetOrPostHelper;
import cn.com.jy.model.helper.MTImgHelper;
import cn.com.jy.model.helper.MTSQLiteHelper;
import cn.com.jy.model.helper.MTSharedpreferenceHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class GetgoodsInformationActivity extends Activity implements OnClickListener {
	/*程序内部的上下文*/
	private Context   mContext;   // 文件内容;
	private Button	  vCode, 	  // 扫码按钮;
					  vSearch, 	  // 搜索按钮;
					  vPhoto, 	  // 拍照按钮;
					  vOid, 	  // 运单扫码;
					  vSetTime,   // 设置时间;
					  vOk // 确认按钮;
					  ;
	private TextView vTopic, // 标题显示;
					 vState2, // 状态2;
					 vBack, // 返回按钮;
					 vFunction;// 功能按钮;
					 ;

	private Builder mBuilder; // 对话框;
	private Spinner vState;
	private EditText ettid, // 拖车号;
			ettkind, // 车型;
			etoid, // 运输号;
			etPercount, // 件数(单);
			etPerweight,// 吨数(单);
			etTformatweight,// 标重(车)
			etTcount, // 车数;
			etSearch // 搜索的框;
			;
	private ListView mListView;
	/*参数定义*/
	private String slkind, // 标题栏;
	taskid,
	gid, // 货品号;
	bid, gstate = "正常", gsimg = "null", lkind 	   = "汽运",
	tid 		= "null",
	tkind 		= "null", oid   = "null", percount = "1",
	perweight 	= "1",
	tformatweight = "1", tcount = "0", gtime, stime, folderPath, // 文件夹路径;
	filePath, 	// 文件路径;
	tmpPath, 	// 临时路径;
	sSize,
	wid;
	private ArrayList<String> list;
	private ArrayAdapter<String> mAdapter;
	private ArrayList<MEFile> 	 listfile;
	
	/*进行信息传递的intent*/
	private Intent mIntent;
	private ProgressDialog mDialog; // 对话方框;
	private MyThread mThread; // 线程内容;
	private MyThread2 mThread2;
	// 帮助类;
	private MTConfigHelper 	  mConfigHelper;
	private MTGetOrPostHelper mGetOrPostHelper;
	private MTImgHelper 	  mImgHelper;
	private MTFileHelper 	  mtFileHelper;
	//
	private MTSharedpreferenceHelper mSpHelper; // 首选项存储;

	private MTSQLiteHelper mSqLiteHelper;// 数据库的帮助类;
	private SQLiteDatabase mDB; // 数据库件;

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int nFlag = msg.what;
			mDialog.dismiss();
			switch (nFlag) {
			// 01.成功;
			case MTConfigHelper.NTAG_SUCCESS:
				Toast.makeText(mContext, R.string.tip_success,
						Toast.LENGTH_SHORT).show();
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

	@SuppressWarnings("static-access")
	private void chooseLoadKind() {
		slkind 		  = "汽运";
		mContext 	  = GetgoodsInformationActivity.this;
		mSpHelper     = new MTSharedpreferenceHelper(mContext, MTConfigHelper.CONFIG_SELF,mContext.MODE_APPEND);
		mSqLiteHelper = new MTSQLiteHelper(mContext);
		mDB = mSqLiteHelper.getmDB();
		mBuilder = new Builder(mContext);
		mBuilder.setTitle("选择运输方式(默认汽运)");
		final String[] kinds = { "汽运", "铁路" };

		mBuilder.setItems(kinds, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int position) {
				slkind = kinds[position];
				vTopic.setText("提货	( " + slkind + " )");
				checkEvent();
			}
		});

		mBuilder.setNegativeButton(R.string.action_no, null);
		mBuilder.create();
		mBuilder.show();
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
		vOid 	  =	(Button) findViewById(R.id.btnOid);
		vSetTime  = (Button) findViewById(R.id.btnSetTime);
		vOk 	  = (Button) findViewById(R.id.btnOk);

		vState2   = (TextView) findViewById(R.id.state2);
		mListView = (ListView) findViewById(R.id.lvResult);
		//
		ettid = (EditText) findViewById(R.id.ettid);
		ettkind = (EditText) findViewById(R.id.ettkind);
		etoid = (EditText) findViewById(R.id.etoid);
		etPercount = (EditText) findViewById(R.id.etPercount);
		etPerweight = (EditText) findViewById(R.id.etPerweight);
		etTformatweight = (EditText) findViewById(R.id.etTformatweight);
		etTcount = (EditText) findViewById(R.id.etTcount);
		etSearch = (EditText) findViewById(R.id.etSearch);
		//
		vState = (Spinner) findViewById(R.id.gstate);
	}

	// 事件初始化;
	private void initEvent() {
		// 系统的配置工具类的添加;
		mGetOrPostHelper = new MTGetOrPostHelper();
		mConfigHelper 	 = new MTConfigHelper();
		mImgHelper 		 = new MTImgHelper();
		//	文件的管理类对象;
		mtFileHelper	 = new MTFileHelper();
		listfile		 = mtFileHelper.getListfiles();
		// 顶部按钮的事件监听的添加;
		vBack.setOnClickListener(this);
		vFunction.setOnClickListener(this);
		vFunction.setText("历史");

		// 中间层次按钮的事件监听的添加;
		vCode.setOnClickListener(this);
		vSearch.setOnClickListener(this);

		vPhoto.setOnClickListener(this);
		vOid.setOnClickListener(this);
		vSetTime.setOnClickListener(this);
		vOk.setOnClickListener(this);
		
		// 信息列表的加载;
		list = new ArrayList<String>();

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
					mBuilder = new Builder(mContext);
					mBuilder.setTitle("异常信息");
					final EditText edit = new EditText(mContext);
					edit.setSingleLine(false);
					edit.setLines(6);
					mBuilder.setView(edit);
					mBuilder.setPositiveButton(R.string.action_ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									String tmp = edit.getText().toString()
											.trim();
									if (!tmp.equals("")) {
										gstate = tmp;
									}
								}
							});
					mBuilder.setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							gstate = "正常";
							vState.setSelection(0);
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
			public void onNothingSelected(AdapterView<?> arg0) {
				gstate = "正常";
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
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	// 事件监测;
	private void checkEvent() {
		if (slkind.equals("铁路")) {
			ettid.setHint("车皮号");
			etoid.setHint("运单号");
			etTformatweight.setVisibility(View.VISIBLE);
			etTcount.setVisibility(View.GONE);
			vSetTime.setText("发送时间(铁)");
		} else {
			ettid.setHint("托车号");
			etoid.setHint("铅封号");
			etTformatweight.setVisibility(View.GONE);
			etTcount.setVisibility(View.VISIBLE);
			vSetTime.setText("发送时间(汽)");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		showData();
	}

	String date, time;

	@Override
	public void onClick(View view) {
		int nVid = view.getId();
		switch (nVid) {
		// 返回按钮;
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
				mThread = new MyThread();
				mThread.start();
			}
			break;
		case R.id.btnPhoto:
			getPhoto_Ggoods();
			break;

		case R.id.btnOid:
			// 跳转至专门的intent控件;
			mIntent = new Intent(mContext, FlushActivity.class);
			// 有返回值的跳转;
			startActivityForResult(mIntent, MTConfigHelper.NTRACK_GGOODS_OID_TO);
			break;
		// 设置时间;
		case R.id.btnSetTime:
			mBuilder = new Builder(mContext);
			View view2 = getLayoutInflater().inflate(
					R.layout.activity_datatimepicker, null);
			mBuilder.setTitle("设置时间");
			mBuilder.setView(view2);
			DatePicker datePicker = (DatePicker) view2
					.findViewById(R.id.dpPicker);
			TimePicker timePicker = (TimePicker) view2
					.findViewById(R.id.tpPicker);
			Calendar calendar = Calendar.getInstance();

			int nYear = calendar.get(Calendar.YEAR);
			int nMonth = calendar.get(Calendar.MONTH);
			int nDay = calendar.get(Calendar.DAY_OF_MONTH);
			int nHour = calendar.get(Calendar.HOUR_OF_DAY);
			int nMinute = calendar.get(Calendar.MINUTE);

			date = nYear + "年" + (nMonth + 1) + "月" + nDay + "日";
			time = nHour + "时" + nMinute + "分";
			datePicker.init(nYear, nMonth, nDay, new OnDateChangedListener() {

				@Override
				public void onDateChanged(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					// 日历控件;
					date = year + "年" + (monthOfYear + 1) + "月" + dayOfMonth
							+ "日";
				}
			});

			timePicker.setIs24HourView(true);
			timePicker
					.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
						@Override
						public void onTimeChanged(TimePicker view,
								int hourOfDay, int minute) {
							time = hourOfDay + "时" + minute + "分";
						}
					});
			mBuilder.setPositiveButton(R.string.action_ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							stime = date + time;
							vSetTime.setText(stime);

						}
					});
			mBuilder.setNegativeButton(R.string.action_no, null);
			mBuilder.create();
			mBuilder.show();
			break;
		// 确认按钮;
		case R.id.btnOk:
			if (bid != null && gid != null) {
				// 车皮标重;
				try {
					tformatweight = etPerweight.getText().toString().trim();
				} catch (Exception e) {
					tformatweight = "0";
				}
				if (tformatweight.equals("")) {
					tformatweight = "0";
				}

				// 车数;
				try {
					tcount = etTcount.getText().toString().trim();
				} catch (Exception e) {
					tcount = "1";
				}

				if (slkind.equals("铁路")) {
					tcount = "0";
				} else {
					tformatweight = "0";
				}
				// 图片;
				gsimg = mtFileHelper.getFileNamesByStrs(mtFileHelper.getListfiles(),"_");
				if (gsimg.equals("")) {
					gsimg = "null";
				}
				// 运输方式;
				lkind = slkind;
				// 车辆编号;
				tid = ettid.getText().toString().trim();
				if (tid.equals("")) {
					tid = "null";
				}
				// 车辆类型;
				tkind = ettkind.getText().toString().trim();
				if (tkind.equals("")) {
					tkind = "null";
				}
				// 车辆运输号;
				oid = etoid.getText().toString().trim();
				if (oid.equals("")) {
					oid = "null";
				}
				try {
					// 单车件数;
					percount = etPercount.getText().toString().trim();
				} catch (Exception e) {
					percount = "0";
				}
				if (percount.equals("")) {
					percount = "0";
				}

				try {
					// 单车吨数;
					perweight = etPerweight.getText().toString().trim();
				} catch (Exception e) {
					perweight = "0";
				}
				if (perweight.equals("")) {
					perweight = "0";
				}
				if (tcount.equals("")) {
					tcount = "1";
				}
				// 得时;
				gtime = mConfigHelper.getCurrentTime("yyyy年MM月dd日HH时mm分");
				// 首选项中的用户名称+密码;
				wid = mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
				if (stime == null) {
					stime = "无";
				}
			
				mBuilder=new Builder(mContext);
				mBuilder.setTitle("信息确认");
		
				sSize = String.valueOf(mtFileHelper.getListfiles().size());
				String sContent=
						"状态:"+gstate+"\r\n" +
						"商品编号:"+bid+"-"+gid+"\r\n"+
						"图片张数:"+sSize+"\r\n" +
						"运输方式:";
				//	区别信息的内容;
				if (slkind.equals("铁路")) {
					sContent+=
							"铁路\r\n" +
							"车皮号:"+tid+"\r\n" +
							"车型:"+tkind+"\r\n" +
							"运单号:"+oid+"\r\n" +
							"单车件数:"+percount+"\r\n" +
							"单车吨数"+perweight+"\r\n" +
							"单车标重:"+tformatweight+"\r\n"; 
				} else {
					sContent+=
							"汽运\r\n" +
							"拖车号:"+tid+"\r\n" +
							"车型:"+tkind+"\r\n" +
							"铅封号:"+oid+"\r\n" +
							"单车件数:"+percount+"\r\n" +
							"单车吨数:"+perweight+"\r\n" +
							"车数:"+tcount+"\r\n";
				}
				sContent+=
						"提货时间:"+gtime+"\r\n" +
						"发货时间:"+stime;
				mBuilder.setMessage(sContent);
				mBuilder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (mThread2 == null) {
							mThread2 = new MyThread2();
							mThread2.start();
						}
					}
				});
				mBuilder.setNegativeButton(R.string.action_no, null);
				
				mBuilder.create();
				mBuilder.show();
				
				
			} else {
				Toast.makeText(mContext, "请进行搜索配对", Toast.LENGTH_SHORT).show();
			}

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
			mImgHelper.compressPicture(tmpPath, filePath);
			mImgHelper.clearPicture(tmpPath, null);
			//	进行文件内容的叠加;
			MEFile meFile=new MEFile(gsimg, filePath);
			mtFileHelper.fileAdd(meFile);
			//	
			sSize = String.valueOf(mtFileHelper.getListfiles().size());
//					String.valueOf(mFileHelper.getFileCount(folderPath));
			vState2.setText(sSize);
		} else if (requestCode == MTConfigHelper.NTRACK_GGOODS_OID_TO
				&& resultCode == MTConfigHelper.NTRACK_FLUSH_TO_MENU) {
			String oid = intent.getStringExtra("bid");
			etoid.setText(oid);
		}
	}

	public class MyThread2 extends Thread {
		private String url, param, response, sql;

		@Override
		public void run() {

			// 进行相应的登录操作的界面显示;
			// 01.Http 协议中的Get和Post方法;
			url = "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":"+ MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM+ "/goods2";
			try {
				param = "operType=1&" + "bid=" + bid + "&" + "gid=" + gid + "&"+ "gstate=" + URLEncoder.encode(gstate, "utf-8") + "&"+ "gsimg=" + gsimg + "&" + "lkind="+ URLEncoder.encode(lkind, "utf-8") + "&" + "tid="+ tid + "&" + "tkind="+ URLEncoder.encode(tkind, "utf-8") + "&" + "oid="+ oid + "&" + "percount=" + percount + "&"+ "perweight=" + perweight + "&" + "tformatweight="+ tformatweight + "&" + "tcount=" + tcount + "&" + "gtime="+ URLEncoder.encode(gtime, "utf-8") + "&" + "stime="+ URLEncoder.encode(stime, "utf-8") + "&" + "wid="+ wid;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			response = mGetOrPostHelper.sendGet(url, param);
			int nFlag = MTConfigHelper.NTAG_FAIL;

			if (!response.equalsIgnoreCase("fail")) {
				nFlag = MTConfigHelper.NTAG_SUCCESS;
				sql = "insert into getgoodsinfo (bid,gid,gstate,gsimg,lkind,tid,tkind,oid,percount,perweight,tformatweight,tcount,gtime,stime) values ('"+ bid+ "',"+ "'"+ gid+ "',"+ "'"+ gstate+ "',"+ "'"+ gsimg+ "',"+ "'"+ lkind+ "',"+ "'"+ tid+ "',"+ "'"+ tkind+ "',"+ "'"+ oid+ "',"+ percount+ ","+ perweight+ ","+ tformatweight+ ","+ tcount+ ","+ "'"+ gtime+ "',"+ "'"+ stime+ "')";
				mDB.execSQL(sql);
			}
			mHandler.sendEmptyMessage(nFlag);
		}
	}	
	private void doResetParam2() {
		// 数据列表;
		list.clear();
		// 重新加载数据;
		showData();
		// 异常按钮重置;
		gstate = "正常";
		vState.setSelection(0);
		// 拖车号;
		ettid.setText(MTConfigHelper.SPACE);
		// 车型
		ettkind.setText(MTConfigHelper.SPACE);
		// 铅封号;
		etoid.setText(MTConfigHelper.SPACE);
		// 件数;
		etPercount.setText(MTConfigHelper.SPACE);
		// 吨数;
		etPerweight.setText(MTConfigHelper.SPACE);
		// 车数;
		etTcount.setText(MTConfigHelper.SPACE);
		// 发车时间;
		stime = null;
		// bid置空&gid置空;
		bid = null;
		gid = null;
		taskid=null;
	}

	// 定义的线程——自定义的线程内容;
	public class MyThread extends Thread {
		private String url, param, response;

		@Override
		public void run() {
			// 进行相应的登录操作的界面显示;
			// 01.Http 协议中的Get和Post方法;
			url 	  = "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":"+ MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM+ "/goods2";
			param 	  = "operType=2&gid=" + taskid;
			response  = mGetOrPostHelper.sendGet(url, param);
			int nFlag = MTConfigHelper.NTAG_FAIL;

			if (!response.equalsIgnoreCase("fail")) {
				nFlag = MTConfigHelper.NTAG_SUCCESS;
				try {
					JSONArray array = new JSONArray(response);
					int i = 0;
					JSONObject obj = null;
					do {
						try {
							// JsonObject的解析;
							obj 			 = array.getJSONObject(i);

							bid 			 = obj.getString("bid");
							String bname 	 = obj.getString("bname");
							String bkind 	 = obj.getString("bkind");
							String bcoman 	 = obj.getString("bcoman");
							String bgaddress = obj.getString("bgaddress");
							String bgoid 	 = obj.getString("bgoid");
							String bshipcom  = obj.getString("bshipcom");
							String bpretoportday = obj.getString("bpretoportday");
							String boxid 	 = obj.getString("boxid");
							String boxsize 	 = obj.getString("boxsize");
							String boxkind 	 = obj.getString("boxkind");
							String boxbelong = obj.getString("boxbelong");
							String retransway= obj.getString("retransway");

							gid 			 = obj.getString("gid");
							String gname 	 = obj.getString("gname");
							String boxid2 	 = obj.getString("boxid");
							String boxsize2  = obj.getString("boxsize");
							String boxkind2  = obj.getString("boxkind");
							String leadnumber= obj.getString("leadnumber");
							String gcount 	 = obj.getString("gcount");
							String gunit 	 = obj.getString("gunit");
							String gtotalweight = obj.getString("gtotalweight");
							String glength 	 = obj.getString("glength");
							String gwidth 	 = obj.getString("gwidth");
							String gheight 	 = obj.getString("gheight");
							String gvolume 	 = obj.getString("gvolume");

							list.add("业务编号:" + bid);
							list.add("业务名称:" + bname);
							list.add("业务类型:" + bkind);
							list.add("建单人:" + bcoman);
							list.add("提货地址:" + bgaddress);
							list.add("提单号:" + bgoid);
							list.add("船舶公司:" + bshipcom);
							list.add("预计到港日:" + bpretoportday);
							list.add("箱号:" + boxid);
							list.add("箱尺寸:" + boxsize);
							list.add("箱型:" + boxkind);
							list.add("箱所属:" + boxbelong);
							list.add("回程运输方式:" + retransway);
							list.add("————分割线————");
							list.add("货物编号:" + bid + "-" + gid);
							list.add("品名:" + gname);
							list.add("箱号:" + boxid2);
							list.add("箱尺寸:" + boxsize2);
							list.add("箱型:" + boxkind2);
							list.add("铅封号:" + leadnumber);
							list.add("件数:" + gcount);
							list.add("单位:" + gunit);
							list.add("总毛重:" + gtotalweight);
							list.add("长:" + glength);
							list.add("宽:" + gwidth);
							list.add("高:" + gheight);
							list.add("体积:" + gvolume);

							i++;
						} catch (Exception e) {
							obj = null;
						}
					} while (obj != null);
				} catch (JSONException e) {
					nFlag = MTConfigHelper.NTAG_FAIL;
				}
			}
			mHandler.sendEmptyMessage(nFlag);
		}
	}

	private void showData() {
		sSize = String.valueOf(mtFileHelper.getListfiles().size());
		vState2.setText(sSize);
		mAdapter = new ArrayAdapter<String>(mContext, R.layout.item02, R.id.tvTopic, list);
		mListView.setAdapter(mAdapter);
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
						Toast.makeText(mContext, "照片创建失败!", Toast.LENGTH_LONG)
								.show();
						return;
					}
				}
				mIntent = new Intent("android.media.action.IMAGE_CAPTURE");
				mIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
				startActivityForResult(mIntent,MTConfigHelper.NTRACK_GGOODS_PHOTO_TO);
				
			} else {
				Toast.makeText(mContext, "没有基础信息", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(mContext, "sdcard无效或没有插入!", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void closeThread() {
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
		if (mThread2 != null) {
			mThread2.interrupt();
			mThread2 = null;
		}
	}
}
