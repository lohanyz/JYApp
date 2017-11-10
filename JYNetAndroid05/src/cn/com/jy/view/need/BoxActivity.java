package cn.com.jy.view.need;

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
import android.util.Log;
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

import cn.com.jy.activity.R;
import cn.com.jy.model.entity.MEFile;
import cn.com.jy.model.helper.FileHelper;
import cn.com.jy.model.helper.MTConfigHelper;
import cn.com.jy.model.helper.MTFileHelper;
import cn.com.jy.model.helper.MTGetOrPostHelper;
import cn.com.jy.model.helper.MTGetTextUtil;
import cn.com.jy.model.helper.MTImgHelper;
import cn.com.jy.model.helper.MTSQLiteHelper;
import cn.com.jy.model.helper.MTSharedpreferenceHelper;

public class BoxActivity extends Activity implements OnClickListener {
	private ArrayList<String> list;
	private Context mContext;
	private ProgressDialog mDialog;
	private TextView tvTopic,state2;
	private EditText etSearch;
	private EditText etgetboxspace,ettranstid;
	private String getboxspace,transtid,wid;
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
	private ArrayList<MEFile> 	 listfile;
	private Builder   vBuilder;
	//	TODO 02.修改的相关内容;
	private LoadInfoThread mThread; // 线程内容;
	private UpLoadThread   mThread2;
	// 帮助类;
	private MTConfigHelper 	  mConfigHelper;
	private MTGetOrPostHelper mGetOrPostHelper;
	private MTImgHelper 	  mImgHelper;
	private MTFileHelper mtFileHelper;
	//
	private MTSharedpreferenceHelper mSpHelper; // 首选项存储;

	private MTSQLiteHelper mSqLiteHelper;// 数据库的帮助类;
	private SQLiteDatabase mDB; // 数据库件;
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
			showImgCount();
			showData();
			closeThread();
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
		mImgHelper 		 = new MTImgHelper();
		//	文件的管理类对象;
		mtFileHelper	 = new MTFileHelper();
		mConfigHelper	=new MTConfigHelper();
		mGetOrPostHelper=new MTGetOrPostHelper();
		mFileHelper = new FileHelper();
		mSqLiteHelper = new MTSQLiteHelper(mContext);
		mDB = mSqLiteHelper.getmDB();
		mImgHelper = new MTImgHelper();
		listfile		 = mtFileHelper.getListfiles();
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
		mSpHelper = new MTSharedpreferenceHelper(mContext, MTConfigHelper.CONFIG_SELF,
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
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == MTConfigHelper.NTRACK_GGOODS_GID_TO
				&& resultCode == MTConfigHelper.NTRACK_FLUSH_TO_MENU) {
			String gid = intent.getStringExtra("bid");
			etSearch.setText(gid);
		}
		if (requestCode == MTConfigHelper.NTRACK_GGOODS_PHOTO_TO
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
				startActivityForResult(mIntent,MTConfigHelper.NTRACK_GGOODS_GID_TO);
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
					mThread=new LoadInfoThread();
					mThread.start();
				}
				break;
			case R.id.btnOk:
				if(bid==null|gid==null){
					Toast.makeText(this,"请进行搜索配对",Toast.LENGTH_LONG).show();
					break;
				}
				try {
					backchnportime=MTGetTextUtil.getText(btnbackchnportime);
					backportstorehoustime=MTGetTextUtil.getText(btnbackportstorehoustime);
					portranstime=MTGetTextUtil.getText(btnportranstime);
					downlineovertime=MTGetTextUtil.getText(btndownlineovertime);
					railwaydownlinetime=MTGetTextUtil.getText(btnrailwaydownlinetime);
					fbacknulltime=MTGetTextUtil.getText(btnfbacknulltime);
					getboxtime=MTGetTextUtil.getText(btngetboxtime);
					getboxspace=MTGetTextUtil.getText(etgetboxspace);
					transtid=MTGetTextUtil.getText(ettranstid);
					gsimg = mFileHelper.getFileNamesByStrs(folderPath);
					wid = mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
					if (gsimg.isEmpty()) {
						gsimg = "null";
					}
					vBuilder=new Builder(mContext);
					vBuilder.setTitle("信息确认");

					showImgCount();
					String sContent=
							"状态:"+gstate+"\r\n" +
									"商品编号:"+bid+"-"+gid+"\r\n"+
									"图片张数:"+sSize+"\r\n" +
									"运输方式:";
					vBuilder.setMessage(sContent);
					vBuilder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if (mThread2 == null) {
								mThread2 = new UpLoadThread();
								mThread2.start();
							}
						}
					});
					vBuilder.setNegativeButton(R.string.action_no, null);

					vBuilder.create();
					vBuilder.show();
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
						MTConfigHelper.NTRACK_GGOODS_PHOTO_TO);
			} else {
				Toast.makeText(mContext, "没有基础信息", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(mContext, "sdcard无效或没有插入!", Toast.LENGTH_SHORT)
					.show();
		}
	}

	public class LoadInfoThread extends Thread{
		private String url,
				param,
				response;
		@Override
		public void run() {
			url = "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":"+ MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM+ "/goods2";
			param	 =	"operType=2&gid="+gid;
			response= 	mGetOrPostHelper.sendGet(url,param);
			int nFlag= 	MTConfigHelper.NTAG_FAIL;
			JSONObject res;
			JSONObject body;
			if(!response.trim().equalsIgnoreCase("fail")){
				nFlag= MTConfigHelper.NTAG_SUCCESS;
				try {
					res = new JSONObject(response);
					body = new JSONObject(res.getString("body"));
				} catch (JSONException e) {
					res = null;
					body = null;
				}
				if(body!=null){
					try {
						String busiinvcode = body.getString("busiinvcode");
						String billoflading = body.getString("billoflading");
						String cid = body.getString("cid");
						String csize = body.getString("csize");
						String ctype = body.getString("ctype");
						String cowner = body.getString("cowner");
						String goodsdesc = body.getString("goodsdesc");
						String etransportationmode = body.getString("etransportationmode");
						String cname = body.getString("cname");
						String sealno = body.getString("sealno");
						String pieces = body.getString("pieces");
						String grossweight = body.getString("grossweight");
						String grossweightjw = body.getString("grossweightjw");
						String grossweighgn = body.getString("grossweighgn");
						String volume = body.getString("volume");
						String length = body.getString("length");
						String width = body.getString("width");
						String height = body.getString("height");
						list.add("业务编号:"+busiinvcode);
						list.add("提单号:"+billoflading);
						list.add("箱号:"+cid);
						list.add("箱尺寸:"+csize);
						list.add("箱型:"+ctype);
						list.add("箱属:"+cowner);
						list.add("包装类型:"+goodsdesc);
						list.add("回程运输方式:"+etransportationmode);
						list.add("品名:"+cname);
						list.add("铅封号:"+sealno);
						list.add("件数:"+pieces);
						list.add("毛重量:"+grossweight);
						list.add("毛重-境外(KGS):"+grossweightjw);
						list.add("毛重-国内(KGS):"+grossweighgn);
						list.add("体积（CBM）:"+volume);
						list.add("长(CM):"+length);
						list.add("宽(CM):"+width);
						list.add("高(CM):"+height);

					}catch (JSONException e){
						nFlag=MTConfigHelper.NTAG_FAIL;
						Log.e("getdata", "run: ", e);
					}
//					"busiinvcode": "业务编号",
//							"billoflading": "提单号",
//							"cid": "箱号（货物信息）",
//							"csize": "箱尺寸（货物信息）",
//							"ctype": "箱型（货物信息）",
//							"cowner": "箱属（货物信息）",
//						“goodsdesc”;”包装类型”,
//					"etransportationmode": "回程运输方式（箱管）",
//							"cname": "品名（货物信息）",
//							"sealno": "铅封号（货物信息）",
//							"pieces": "件数（货物信息）",
//							"grossweight": "毛重量（货物信息）",
//							"grossweightjw": "毛重-境外(KGS)（货物信息-大宗）",
//							"grossweighgn": "毛重-国内(KGS)（货物信息-大宗）",
//							"volume": "体积（CBM）（货物信息）",
//							"length": "长(CM)（货物信息）",
//							"width": "宽(CM)（货物信息）",
//							"height": "高(CM)（货物信息）"

				}
			}
//			if(!response.equalsIgnoreCase("fail")){
//				nFlag= MTConfigHelper.NTAG_SUCCESS;
//				try {
//					JSONArray array = new JSONArray(response);
//					int 	  i		= 0;
//					JSONObject obj 	= null;
//					do {
//						try {
//							//	JsonObject的解析;
//							obj			  =	array.getJSONObject(i);
//							String bgoid	  = obj.getString("bgoid");
//							String boxid	  = obj.getString("boxid");
//							String boxsize	  = obj.getString("boxsize");
//							String boxkind	  = obj.getString("boxkind");
//							String boxbelong  = obj.getString("boxbelong");
//							String retransway  = obj.getString("retransway");
//
//							bid               = obj.getString("bid");
//							gid		  		  = obj.getString("gid");
//							String gname	  = obj.getString("gname");
//							String leadnumber = obj.getString("leadnumber");
//							String gcount	  = obj.getString("gcount");
//							String gunit	  = obj.getString("gunit");
//							String gtotalweight= obj.getString("gtotalweight");
//							String glength 	  = obj.getString("glength");
//							String gwidth	  = obj.getString("gwidth");
//							String gheight	  = obj.getString("gheight");
//							String gvolume	  = obj.getString("gvolume");
//
//							list.add("业务编号:"+bid);
//							list.add("提单号:"+bgoid);
//							list.add("箱号:"+boxid);
//							list.add("箱尺寸:"+boxsize);
//							list.add("箱型:"+boxkind);
//							list.add("箱属"+boxbelong);
//							list.add("回城运输方式"+retransway);
//							list.add("\r\n");
//							list.add("品名:"+gname);
//							list.add("铅封号:"+leadnumber);
//							list.add("件数:"+gcount);
//							list.add("单位:"+gunit);
//							list.add("总毛重:"+gtotalweight);
//							list.add("长:"+glength);
//							list.add("宽:"+gwidth);
//							list.add("高:"+gheight);
//							list.add("体积:"+gvolume);
//							i++;
//						} catch (Exception e) {
//							obj=null;
//						}
//					} while (obj!=null);
//				} catch (JSONException e) {
//					nFlag	=	MTConfigHelper.NTAG_FAIL;
//
//				}
//			}
			myHandler.sendEmptyMessage(nFlag);
		}
	}
	public class UpLoadThread extends Thread{
		private String url,
				param,
				response,
				sql,
				wid;
		public void run() {

			// 进行相应的登录操作的界面显示;
			//	01.Http 协议中的Get和Post方法;
			//
			url = "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":"+ MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM+ "/boxmanage";
			//url		 =	"http://172.23.24.155:"+"8080"+"/JYTest02/boxmanage";
			wid = mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
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
			int nFlag=response.trim().equalsIgnoreCase("success")?MTConfigHelper.NTAG_SUCCESS:MTConfigHelper.NTAG_FAIL;
            if(nFlag==MTConfigHelper.NTAG_SUCCESS){
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
	private void showImgCount(){
		sSize = String.valueOf(mtFileHelper.getListfiles().size());
		state2.setText(sSize);
	}
	private void showData() {
		mAdapter = new ArrayAdapter<String>(mContext, R.layout.item02, R.id.tvTopic, list);
		//	显示的列表和适配器绑定;
		mListView.setAdapter(mAdapter);
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
