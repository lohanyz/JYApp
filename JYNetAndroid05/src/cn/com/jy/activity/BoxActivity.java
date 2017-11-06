package cn.com.jy.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

import cn.com.jy.helper.ConfigHelper;
import cn.com.jy.helper.FileHelper;
import cn.com.jy.helper.GetOrPostHelper;
import cn.com.jy.helper.GetTextUtil;
import cn.com.jy.helper.ImgHelper;
import cn.com.jy.helper.SPHelper;
import cn.com.jy.helper.SQLiteHelper;

public class BoxActivity extends Activity implements OnClickListener {
	private ArrayList<String> list;
	private Context mContext;
	private ProgressDialog mDialog;
	private GetOrPostHelper mGetOrPostHelper;
	private ImgHelper mImgHelper;
	private TextView tvTopic,state2;
	private SPHelper mSpHelper;
	private ConfigHelper	mConfigHelper;
	private SQLiteDatabase mDB; // 数据库件;
	private SQLiteHelper mSqLiteHelper;// 数据库的帮助类;
	private Thread mThread;
	private Thread mThread2;
	private EditText etSearch;
	private EditText etgetboxspace,ettranstid;
	private String getboxspace,transtid;
	private TextView  btnDetail ;
	private Button  mGsimg,
			btnbackchnportime, btnbackportstorehoustime, btnportranstime,btngetboxtime,
			btndownlineovertime, btnrailwaydownlinetime, btnfbacknulltime,btnCode,btnSearch,btnOk;
	private String bid, gstate, date, time, stime,gid,sSize;
	private Intent mIntent;
	private ListView			 mListView;
	private Spinner mState;
	private ArrayAdapter<String> mAdapter;
	private Builder mBuilder;
	private FileHelper mFileHelper;
	private String backchnportime, backportstorehoustime, portranstime,
			downlineovertime, railwaydownlinetime, fbacknulltime,getboxtime;
	private String saveDir = Environment.getExternalStorageDirectory()
			.getPath() + File.separator + "jyFile",saveFolder = "photo", folderPath, // 文件夹路径;
			filePath, // 文件路径;
			tmpPath, gsimg; // 临时路径;
	@SuppressLint("HandlerLeak")
	Handler myHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			mDialog.dismiss();
			switch (msg.what){
				case ConfigHelper.NTAG_SUCCESS:
					Toast.makeText(mContext, R.string.tip_success,Toast.LENGTH_SHORT).show();
					break;
				//	02.失败;
				case ConfigHelper.NTAG_FAIL:
					Toast.makeText(mContext, R.string.tip_fail,Toast.LENGTH_LONG).show();
					break;
				default:
					break;
			}
			showData();
			onDestroy();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.box);
		init();
	}

	@SuppressWarnings("static-access")
	private void init() {
		list = new ArrayList<String>();
		mContext = BoxActivity.this;
		mIntent = getIntent();
		mConfigHelper	=new ConfigHelper();
		mGetOrPostHelper=new GetOrPostHelper();
		mFileHelper = new FileHelper();
		mSqLiteHelper = new SQLiteHelper(mContext);
		mDB = mSqLiteHelper.getmDB();
		mImgHelper = new ImgHelper();
		mListView	=   (ListView) findViewById(R.id.lvResult);
		state2 = (TextView) findViewById(R.id.state2);
		tvTopic 	= 	(TextView) findViewById(R.id.tvTopic);
		etgetboxspace =	(EditText) findViewById(R.id.getboxspace);
		ettranstid 	=	(EditText) findViewById(R.id.transtid);
		etSearch	=	(EditText) findViewById(R.id.etSearch);
		mState 		= 	(Spinner) findViewById(R.id.gstate);
		btnCode		=   (Button) findViewById(R.id.btnCode);
		btnSearch	=   (Button) findViewById(R.id.btnSearch);
		mGsimg 		= 	(Button) findViewById(R.id.btnPhoto);
		btnDetail 	= 	(TextView) findViewById(R.id.btnFunction);
		btnOk		=	(Button) findViewById(R.id.btnOk);
		btnbackchnportime 			= (Button) findViewById(R.id.backchnportime);
		btnbackportstorehoustime 	= (Button) findViewById(R.id.backportstorehoustime);
		btnportranstime 			= (Button) findViewById(R.id.portranstime);
		btndownlineovertime 		= (Button) findViewById(R.id.downlineovertime);
		btnrailwaydownlinetime 		= (Button) findViewById(R.id.railwaydownlinetime);
		btnfbacknulltime 			= (Button) findViewById(R.id.fbacknulltime);
		btngetboxtime				= (Button) findViewById(R.id.getboxtime);
		folderPath = saveDir + File.separator + saveFolder + File.separator
				+ bid + File.separator + "boxmanage";
		btnDetail.setText("历史");
		mSpHelper = new SPHelper(mContext, ConfigHelper.CONFIG_SELF,
				mContext.MODE_APPEND);
		tvTopic.setText("箱管");
		btnbackchnportime.setOnClickListener(this);
		btnbackportstorehoustime.setOnClickListener(this);
		btnportranstime.setOnClickListener(this);
		btndownlineovertime.setOnClickListener(this);
		mGsimg.setOnClickListener(this);
		btnrailwaydownlinetime.setOnClickListener(this);
		btnfbacknulltime.setOnClickListener(this);
		btnCode.setOnClickListener(this);
		btnSearch.setOnClickListener(this);
		btngetboxtime.setOnClickListener(this);
		btnDetail.setOnClickListener(this);
		etgetboxspace.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				etgetboxspace.setBackgroundColor(Color.WHITE);
			}
		});
		ettranstid.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				ettranstid.setBackgroundColor(Color.WHITE);
			}
		});
		btnOk.setOnClickListener(this);
		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				bid = null;
				gid = null;
				// 数据列表;
				list.clear();
				// 重新加载数据;
				showData();
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
		mState.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
									   int position, long id) {
				switch (position) {
					case 0:
						gstate="正常";
					case 1:
						gstate="正常";
						//state1.setBackgroundColor(Color.GREEN);
						break;
					case 2:
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
									gstate=tmp;
								}
							}
						});
						mBuilder.setNegativeButton(R.string.action_no, null);
						mBuilder.create();
						mBuilder.show();
						break;
					default:
						break;
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				gstate="正常";
			}
		});
	}

	public void onClickBack(View view) {
		finish();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == ConfigHelper.NTRACK_GGOODS_GID_TO
				&& resultCode == ConfigHelper.NTRACK_FLUSH_TO_MENU) {
			String gid = intent.getStringExtra("bid");
			etSearch.setText(gid);
		}
		if (requestCode == ConfigHelper.NTRACK_GGOODS_PHOTO_TO
				&& resultCode == -1) {
			Toast.makeText(mContext, "拍照完成", Toast.LENGTH_SHORT).show();
			mImgHelper.compressPicture(tmpPath, filePath);
			mImgHelper.clearPicture(tmpPath, null);

			sSize = String.valueOf(mFileHelper.getFileCount(folderPath));
			state2.setText(sSize);
		}
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.btnPhoto:
			getPhoto_Ggoods();
			break;
		case R.id.backchnportime:
		case R.id.backportstorehoustime:
		case R.id.portranstime:
		case R.id.downlineovertime:
		case R.id.railwaydownlinetime:
		case R.id.fbacknulltime:
		case R.id.getboxtime:


			mBuilder = new Builder(mContext);
			View view = getLayoutInflater().inflate(
					R.layout.activity_datatimepicker, null);
			mBuilder.setTitle("设置时间");
			mBuilder.setView(view);
			DatePicker datePicker = (DatePicker) view
					.findViewById(R.id.dpPicker);
			TimePicker timePicker = (TimePicker) view
					.findViewById(R.id.tpPicker);
			Calendar calendar = Calendar.getInstance();

			int nYear = calendar.get(Calendar.YEAR);
			int nMonth = calendar.get(Calendar.MONTH);
			int nDay = calendar.get(Calendar.DAY_OF_MONTH);
			int nHour = calendar.get(Calendar.HOUR_OF_DAY);
			int nMinute = calendar.get(Calendar.MINUTE);

			String month=nMonth+1<10?"0"+(nMonth+1):""+(nMonth+1);
			String day=nDay<10?"0"+nDay:""+nDay;
			String hour=nHour<10?"0"+nHour:""+nHour;
			String minute=nMinute<10?"0"+nMinute:""+nMinute;
			date = nYear + "年" + month + "月" + day + "日";
			time = hour + "时" + minute + "分";
			datePicker.init(nYear, nMonth, nDay, new OnDateChangedListener() {

				@Override
				public void onDateChanged(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					// 日历控件;
					String month=monthOfYear + 1<10?"0"+(monthOfYear+1):""+(monthOfYear+1);
					String day=dayOfMonth<10?"0"+dayOfMonth:""+dayOfMonth;
					date = year + "年" + month + "月" + day
							+ "日";
				}
			});

			timePicker.setIs24HourView(true);
			timePicker
					.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
						@Override
						public void onTimeChanged(TimePicker view,
								int hourOfDay, int minute) {
							String hour=hourOfDay<10?"0"+hourOfDay:""+hourOfDay;
							String minutes=minute<10?"0"+minute:""+minute;
							time = hour + "时" + minutes + "分";
						}
					});
			mBuilder.setPositiveButton(R.string.action_ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							stime = date + time;
							switch (v.getId()) {
							case R.id.backchnportime:
							case R.id.backportstorehoustime:
							case R.id.portranstime:
							case R.id.downlineovertime:
							case R.id.railwaydownlinetime:
							case R.id.fbacknulltime:
								findViewById(v.getId()).setBackgroundColor(Color.WHITE);
								((Button)findViewById(v.getId())).setText(stime);
								break;
							case R.id.getboxtime:
								btngetboxtime.setTextColor(Color.parseColor("#000000"));
								btngetboxtime.setGravity(Gravity.CENTER);
								btngetboxtime.setText(stime);
								break;
							default:
								break;
							}

						}
					});
			mBuilder.setNegativeButton(R.string.action_no, null);
			mBuilder.create();
			mBuilder.show();

			break;
			case R.id.btnCode:
				//	跳转至专门的intent控件;
				mIntent	=	new Intent(mContext, FlushActivity.class);
				//	有返回值的跳转;
				startActivityForResult(mIntent,ConfigHelper.NTRACK_GGOODS_GID_TO);
				break;

			case R.id.btnSearch:
				if(mThread==null){
					int nSize=list.size();
					if(nSize!=0){
						list.clear();
					}
					InputMethodManager inputMethodManager =
							(InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(btnSearch.getWindowToken(), 0);
					// 进度条的内容;
					final CharSequence strDialogTitle = getString(R.string.tip_dialog_wait);
					final CharSequence strDialogBody  = getString(R.string.tip_dialog_done);
					mDialog 						  = ProgressDialog.show(mContext, strDialogTitle, strDialogBody,true);
					gid								  = etSearch.getText().toString().trim();
					mThread=new MyThread();
					mThread.start();
				}
				break;
			case R.id.btnOk:
				if(bid==null|gid==null){
					Toast.makeText(this,"请进行搜索配对",Toast.LENGTH_LONG).show();
					break;
				}
				try {
					backchnportime=GetTextUtil.getText(btnbackchnportime);
					backportstorehoustime=GetTextUtil.getText(btnbackportstorehoustime);
					portranstime=GetTextUtil.getText(btnportranstime);
					downlineovertime=GetTextUtil.getText(btndownlineovertime);
					railwaydownlinetime=GetTextUtil.getText(btnrailwaydownlinetime);
					fbacknulltime=GetTextUtil.getText(btnfbacknulltime);
					getboxtime=GetTextUtil.getText(btngetboxtime);
					getboxspace=GetTextUtil.getText(etgetboxspace);
					transtid=GetTextUtil.getText(ettranstid);
					gsimg = mFileHelper.getFileNamesByStrs(folderPath);
					if (gsimg.isEmpty()) {
						gsimg = "null";
					}
					if(mThread2==null){
						mThread2=new MyThread2();
						mThread2.start();
					}
				}catch (Exception e){
					Toast.makeText(this,"请按要求填写内容",Toast.LENGTH_LONG).show();
				}

				break;
			case R.id.btnFunction:
				mIntent=new Intent(mContext,BMHistoryActivity.class);
				startActivity(mIntent);
			default:
			break;
		}
	}

	public void getPhoto_Ggoods() {
		File file;
		if (mConfigHelper.getfState().equals(Environment.MEDIA_MOUNTED)) {
			if (bid != null && gid != null) {
				folderPath = mConfigHelper.getfParentPath() + bid
						+ File.separator + "boxmanage" + File.separator + gid;
				gsimg = bid + "boxmanage" + gid + "file"
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
				startActivityForResult(mIntent,
						ConfigHelper.NTRACK_GGOODS_PHOTO_TO);
			} else {
				Toast.makeText(mContext, "没有基础信息", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(mContext, "sdcard无效或没有插入!", Toast.LENGTH_SHORT)
					.show();
		}
	}

	public class MyThread extends Thread{
		private String url,
				param,
				response;
		@Override
		public void run() {
			url = "http://" + ConfigHelper.TAG_IP_ADDRESS + ":"+ ConfigHelper.TAG_PORT + "/" + ConfigHelper.TAG_PROGRAM+ "/goods2";
			//url		 =	"http://172.23.24.155:8080/JYTest02/goods2";
			param	 =	"operType=2&gid="+gid;
			response= 	mGetOrPostHelper.sendGet(url,param);
			int nFlag= 	ConfigHelper.NTAG_FAIL;
			if(!response.equalsIgnoreCase("fail")){
				nFlag= ConfigHelper.NTAG_SUCCESS;
				try {
					JSONArray array = new JSONArray(response);
					int 	  i		= 0;
					JSONObject obj 	= null;
					do {
						try {
							//	JsonObject的解析;
							obj			  =	array.getJSONObject(i);
							String bgoid	  = obj.getString("bgoid");
							String boxid	  = obj.getString("boxid");
							String boxsize	  = obj.getString("boxsize");
							String boxkind	  = obj.getString("boxkind");
							String boxbelong  = obj.getString("boxbelong");
							String retransway  = obj.getString("retransway");

							bid               = obj.getString("bid");
							gid		  		  = obj.getString("gid");
							String gname	  = obj.getString("gname");
							String leadnumber = obj.getString("leadnumber");
							String gcount	  = obj.getString("gcount");
							String gunit	  = obj.getString("gunit");
							String gtotalweight= obj.getString("gtotalweight");
							String glength 	  = obj.getString("glength");
							String gwidth	  = obj.getString("gwidth");
							String gheight	  = obj.getString("gheight");
							String gvolume	  = obj.getString("gvolume");

							list.add("业务编号:"+bid);
							list.add("提单号:"+bgoid);
							list.add("箱号:"+boxid);
							list.add("箱尺寸:"+boxsize);
							list.add("箱型:"+boxkind);
							list.add("箱属"+boxbelong);
							list.add("回城运输方式"+retransway);
							list.add("\r\n");
							list.add("品名:"+gname);
							list.add("铅封号:"+leadnumber);
							list.add("件数:"+gcount);
							list.add("单位:"+gunit);
							list.add("总毛重:"+gtotalweight);
							list.add("长:"+glength);
							list.add("宽:"+gwidth);
							list.add("高:"+gheight);
							list.add("体积:"+gvolume);
							i++;
						} catch (Exception e) {
							obj=null;
						}
					} while (obj!=null);
				} catch (JSONException e) {
					nFlag	=	ConfigHelper.NTAG_FAIL;

				}
			}
			myHandler.sendEmptyMessage(nFlag);
		}
	}
	public class MyThread2 extends Thread{
		private String url,
				param,
				response,
				sql,
				wid;
		public void run() {

			// 进行相应的登录操作的界面显示;
			//	01.Http 协议中的Get和Post方法;
			//
			url = "http://" + ConfigHelper.TAG_IP_ADDRESS + ":"+ ConfigHelper.TAG_PORT + "/" + ConfigHelper.TAG_PROGRAM+ "/boxmanage";
			//url		 =	"http://172.23.24.155:"+"8080"+"/JYTest02/boxmanage";
			wid = mSpHelper.getValue(ConfigHelper.CONFIG_SELF_WID);
			try {
				param	 =	"operType=1&" +
						"bid="+bid+"&" +
						"gid="+gid+"&" +
						"state="+ URLEncoder.encode(gstate,"utf-8")+"&" +
						"simg="+gsimg+"&" +
						"img=null"+"&" +
						"transtid="+transtid+"&" +
						"getboxspace="+URLEncoder.encode(getboxspace,"utf-8")+"&" +
						"getboxtime="+URLEncoder.encode(getboxtime,"utf-8")+"&" +
						"backportstorehoustime="+URLEncoder.encode(backportstorehoustime,"utf-8")+"&" +
						"portranstime="+URLEncoder.encode(portranstime,"utf-8")+"&" +
						"backchnportime="+URLEncoder.encode(backchnportime,"utf-8")+"&" +
						"transtid="+transtid+"&" +
						"downlineovertime="+URLEncoder.encode(downlineovertime,"utf-8")+"&" +
						"railwaydownlinetime="+URLEncoder.encode(railwaydownlinetime,"utf-8")+"&" +
						"fbacknulltime="+URLEncoder.encode(fbacknulltime,"utf-8")+"&" +

						"stime="+URLEncoder.encode("0","utf-8")+"&" +
						"wid="+wid;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			response = 	mGetOrPostHelper.sendGet(url,param);
			int nFlag=response.trim().equalsIgnoreCase("success")?ConfigHelper.NTAG_SUCCESS:ConfigHelper.NTAG_FAIL;
            if(nFlag==ConfigHelper.NTAG_SUCCESS){
				sql="insert into boxmanageinfo (" +
                        "bid,gid,state,simg,getboxspace,getboxtime,backchnportime,backportstorehoustime,portranstime," +
						"transtid,downlineovertime,railwaydownlinetime,fbacknulltime,stime) values (" +
                        "'"+bid+"'," +
                        "'"+gid+"'," +
                        "'"+gstate+"',"+
                        "'"+gsimg+"'," +
                        "'"+getboxspace+"'," +
                        "'"+getboxtime+"'," +
                        "'"+backchnportime+"'," +
                        "'"+backportstorehoustime+"'," +
						"'"+portranstime+"'," +
						"'"+transtid+"',"+
						"'"+downlineovertime+"',"+
						"'"+railwaydownlinetime+"',"+
						"'"+fbacknulltime+"',"+
                        "'"+0+"')";
				bid=null;
				gid=null;
                mDB.execSQL(sql);
            }
			myHandler.sendEmptyMessage(nFlag);
		}
	}
	private void showData(){
		mAdapter=new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, list);
		mListView.setAdapter(mAdapter);
	}
	@Override
	protected void onDestroy() {

		if(mThread!=null){
			mThread.interrupt();
			mThread=null;
		}
		if(mThread2!=null){
			mThread2.interrupt();
			mThread2=null;
		}
		super.onDestroy();
	}
}