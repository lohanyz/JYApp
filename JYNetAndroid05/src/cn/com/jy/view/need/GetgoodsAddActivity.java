package cn.com.jy.view.need;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;

import cn.com.jy.activity.R;
import cn.com.jy.model.helper.MTConfigHelper;
import cn.com.jy.model.helper.MTGetOrPostHelper;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class GetgoodsAddActivity extends Activity implements OnClickListener{
	
	/*上下文内容*/
	private Context		 mContext;
	private Intent		 mIntent;
	/*控件内容*/
	private TextView	 vTopic,vBack,vFunction;
	private LinearLayout layTrain,layTruck;
	private EditText	 
						 vdttrailerno ,		//	拖车(取)拖车号(国内信息)
						 vsealno 	 ,		//	铅封号(货物信息)
						 vdtsingletrailernum,//	拖车(取)单车件数
						 vdtsingletrailerton,//	拖车(取)单车吨数
						 vsvehiclescoll	 ,	//	车数(取)(仓储)
						 vdgtrainwagonno ,	//	铁路车皮号(国内信息)
						 vdgtraintype 	 ,	//	铁路车型(国内信息)
						 vdgtrainwaybillno,	//	铁路运单号(国内信息)							
						 vdgtrainsinglenum,	//	铁路单车件数(国内信息)
						 vdgtrainsingleton,	//	铁路单车吨数
						 vdgtrainwagonkg ,	//	铁路车皮标重

						 vdgtrailerno	  ,	 //	拖车送拖车号(国内信息)
						 vdtrailermodelsdely,//	拖车车型(送)(调度)
						 vdgsingletrailernum,//	拖车(送)单车件数(国内信息)
						 vdgsingletrailerton,//	拖车(送)单车吨数(国内信息)
						 vsvehiclesdely	  ,	 //	车数(送)(仓储)						 
						 vdtpickupdate	 ,	//	拖车(取)提货时间(国内时间)
						 vdtstartdate	 ,	//	拖车(取)发车时间(国内时间)
						 vdloadingtime	 ,	//	装车时间(调度)
						 vdgtrainstartdate ,	//	铁路发运日
						 vdgstartdate	 	//	拖车(送)发车时间(国内信息)
						 ;
	
	private Button		vOk
						 ;
	private ProgressDialog mDialog; // 对话方框;
	/*自定义参量*/
	private String		 pslkind,
						 /*与提货信息有关的参数*/
						 busiinvcode,
						 barcode ,
						 dttrailerno ,		//	拖车(取)拖车号(国内信息)
							
						 sealno 	 ,		//	铅封号(货物信息)
							
						 dtsingletrailernum,//	拖车(取)单车件数
						 dtsingletrailerton,//	拖车(取)单车吨数
						 svehiclescoll	 ,	//	车数(取)(仓储)
							
						 dtpickupdate	 ,	//	拖车(取)提货时间(国内时间)
						 dtstartdate	 ,	//	拖车(取)发车时间(国内时间)
						 dgtrainwagonno	 ,	//	铁路车皮号(国内信息)
						 
						 dgtraintype 	 ,	//	铁路车型(国内信息)
						 dgtrainwaybillno,	//	铁路运单号(国内信息)
							
						 dgtrainsinglenum,	//	铁路单车件数(国内信息)
						 cargostatuscenter,	//	货物状态
						 dgtrainsingleton,	//	铁路单车吨数
						 dgtrainwagonkg	 ,	//	铁路车皮标重
						 dloadingtime	 ,	//	装车时间(调度)
							
						 dgtrainstartdate ,	//	铁路发运日
						 dgtrailerno	  ,	//	拖车送拖车号(国内信息)
						 dtrailermodelsdely,//	拖车车型(送)(调度)
						 dgsingletrailernum,//	拖车(送)单车件数(国内信息)
						 dgsingletrailerton,//	拖车(送)单车吨数(国内信息)
						 svehiclesdely	  ,	//	车数(送)(仓储)
							
						 dgstartdate	  ,	//	拖车(送)发车时间(国内信息)
						 img, 	 			//	图片
						 wid
						;
	private String date,time;
	/*自定义的类*/
	private MTSharedpreferenceHelper mSpHelper; // 首选项存储;
	private MTSQLiteHelper 	  mSqLiteHelper;// 数据库的帮助类;
	private SQLiteDatabase 	  mDB; // 数据库件;
	private MTGetOrPostHelper mGetOrPostHelper;
	private UpLoadThread	  mThread;
	private MTConfigHelper	  mtConfigHelper;
	/*设置屏幕类型*/
	
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//	控制符的标签;
			Bundle bundle= msg.getData();
			int    nFlag = bundle.getInt("flag");
			mDialog.dismiss();
			
			switch (nFlag) {
			// 01.成功;
			case MTConfigHelper.NTAG_SUCCESS:
				Toast.makeText(mContext, R.string.tip_success,Toast.LENGTH_SHORT).show();
				break;
			// 02.失败;
			case MTConfigHelper.NTAG_FAIL:
				Toast.makeText(mContext, R.string.tip_fail, Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
			closeThread();
			if(nFlag==1){
				setResult(1, mIntent);
				finish();                                              
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.getgoods_add);
		initView();
		initEvent();
		checkView();
	}
	
	private void initView(){
		vBack	 =	(TextView) findViewById(R.id.btnBack);
		vTopic	 =	(TextView) findViewById(R.id.tvTopic);
		vFunction= 	(TextView) findViewById(R.id.btnFunction);
		layTrain =	(LinearLayout) findViewById(R.id.layTrain);
		layTruck =	(LinearLayout) findViewById(R.id.layTruck);
		/*控件声明*/		 
		vdttrailerno		=(EditText) findViewById(R.id.etDttrailerno) ;		//	拖车(取)拖车号(国内信息)
		vsealno	 		=(EditText) findViewById(R.id.etSeaino);
		vdtsingletrailernum=(EditText) findViewById(R.id.etDtsingletrailernum);
		vdtsingletrailerton=(EditText) findViewById(R.id.etDtsingletrailerton);
		vsvehiclescoll		=(EditText) findViewById(R.id.etSvehiclescoll);
		vdgtrainwagonno	=(EditText) findViewById(R.id.etDgtrainwagonno);
		vdgtraintype		=(EditText) findViewById(R.id.etDgtraintype);
		vdgtrainwaybillno	=(EditText) findViewById(R.id.etDgtrainwaybillno);					
		vdgtrainsinglenum	=(EditText) findViewById(R.id.etDgtrainsinglenum);
		vdgtrainsingleton	=(EditText) findViewById(R.id.etDgtrainsingleton);
		vdgtrainwagonkg	=(EditText) findViewById(R.id.etDgtrainwagonkg);

		vdgtrailerno		=(EditText) findViewById(R.id.etDgtrailerno);
		vdtrailermodelsdely=(EditText) findViewById(R.id.etDtrailermodelsdely);
		vdgsingletrailernum=(EditText) findViewById(R.id.etDgsingletrailernum);
		vdgsingletrailerton=(EditText) findViewById(R.id.etDgsingletrailerton);
		vsvehiclesdely		=(EditText) findViewById(R.id.etSvehiclesdely);
		/*按钮触发*/
		vdloadingtime		=(EditText) findViewById(R.id.btnDloadingtime);
		vdgtrainstartdate	=(EditText) findViewById(R.id.btnDgtrainstartdate);
		vdtpickupdate		=(EditText) findViewById(R.id.btnDtpickupdate);
		vdtstartdate		=(EditText) findViewById(R.id.btnDtstartdate);
		vdgstartdate		=(EditText) findViewById(R.id.btnDgstartdate);
		vOk					=(Button) findViewById(R.id.btnOk);
		 
	}
	
	private void initEvent(){
		mContext		=	GetgoodsAddActivity.this;
		mtConfigHelper	=	new MTConfigHelper();
		
		vFunction.setVisibility(View.GONE);
		vBack.setOnClickListener(this);
		getInfo();
		vTopic.setText("["+pslkind+"]信息新增");
		/*添加事件的内容*/
		vdloadingtime.setOnClickListener(this);
		vdgtrainstartdate.setOnClickListener(this);
		vdtpickupdate.setOnClickListener(this);
		vdtstartdate.setOnClickListener(this);
	    vdgstartdate.setOnClickListener(this);
	    /*自定义的对象类*/
	    mSpHelper     = new MTSharedpreferenceHelper(mContext, MTConfigHelper.CONFIG_SELF,Context.MODE_APPEND);
		mSqLiteHelper = new MTSQLiteHelper(mContext);
		mGetOrPostHelper = new MTGetOrPostHelper();
		mDB 		  = mSqLiteHelper.getmDB();
		wid 		  = mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
		vOk.setOnClickListener(this);
		//	检测数值型的内容信息;
		mtConfigHelper.checkDataFormat(mContext,vdtsingletrailernum);//	拖车(取)单车件数
		mtConfigHelper.checkDataFormat(mContext,vdtsingletrailerton);//	拖车(取)单车吨数
		mtConfigHelper.checkDataFormat(mContext,vsvehiclescoll);	 //	车数(取)(仓储)
		mtConfigHelper.checkDataFormat(mContext,vdgsingletrailernum);//	拖车(送)单车件数(国内信息)
		mtConfigHelper.checkDataFormat(mContext,vdgsingletrailerton);//	拖车(送)单车吨数(国内信息)
		mtConfigHelper.checkDataFormat(mContext,vsvehiclesdely);	 //	车数(送)(仓储)				
		//
	}
	//	获取数据;
	private void getInfo(){
		mIntent		  =getIntent();
		Bundle mBundle=mIntent.getExtras();
		busiinvcode   =mBundle.getString("busiinvcode");
		barcode	  	  =mBundle.getString("barcode");
		cargostatuscenter=mBundle.getString("cargostatuscenter");
		pslkind 	  =mBundle.getString("slkind");
		img 	  	  =mBundle.getString("imgs");
	}
	//	获取控件;
	private void checkView(){
		if(pslkind.equals("铁路")){
			layTrain.setVisibility(View.VISIBLE);
			layTruck.setVisibility(View.GONE);
			
		}else {
			layTrain.setVisibility(View.GONE);
			layTruck.setVisibility(View.VISIBLE);			
		}
	}
	
	
	@Override
	public void onClick(View view) {
		int vId=view.getId();
		switch (vId) {
		case R.id.btnBack:
			finish();
			break;
		//	铁路——装车时间;
		case R.id.btnDloadingtime:
			setViewDate(mContext, vdloadingtime);
			break;
		case R.id.btnDgtrainstartdate:
			setViewDate(mContext, vdgtrainstartdate);
			break;
		
		case R.id.btnDtpickupdate:
			setViewDate(mContext, vdtpickupdate);
			break;
			
		case R.id.btnDtstartdate:
			setViewDate(mContext, vdtstartdate);
			break;
		case R.id.btnDgstartdate:
			setViewDate(mContext, vdgstartdate);
			break;
		case R.id.btnOk:
			getDataInfo();
			break;
		default:
			break;
		}
	}

	private void setViewDate(Context mContext,final EditText btn){
		Builder    vBuilder   = new Builder(mContext);
		/*布局控件*/
		View 	   view 	  = getLayoutInflater().inflate(R.layout.activity_datatimepicker, null);
		vBuilder.setTitle("设置时间");
		vBuilder.setView(view);
		/*时间日期有关控件*/
		DatePicker datePicker = (DatePicker) view.findViewById(R.id.dpPicker);
		TimePicker timePicker = (TimePicker) view.findViewById(R.id.tpPicker);
		Calendar   calendar   = Calendar.getInstance();

		int 	   nYear 	  = calendar.get(Calendar.YEAR);
		int 	   nMonth 	  = calendar.get(Calendar.MONTH);
		int 	   nDay 	  = calendar.get(Calendar.DAY_OF_MONTH);
		int 	   nHour 	  = calendar.get(Calendar.HOUR_OF_DAY);
		int 	   nMinute 	  = calendar.get(Calendar.MINUTE);

		date = nYear + "年" + (nMonth + 1) + "月" + nDay + "日";
		time = nHour + "时" + nMinute + "分";
		datePicker.init(nYear, nMonth, nDay, new OnDateChangedListener() {

			@Override
			public void onDateChanged(DatePicker view, int year,
					int monthOfYear, int dayOfMonth) {
				// 日历控件;
				date = year + "年" + (monthOfYear + 1) + "月" + dayOfMonth + "日";
			}
		});

		timePicker.setIs24HourView(true);
		timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view,
					int hourOfDay, int minute) {
				time = hourOfDay + "时" + minute + "分";
			}
		});
		vBuilder.setPositiveButton(R.string.action_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						String stime = date + time;
						btn.setText(stime);
					}
				});
		vBuilder.setNegativeButton(R.string.action_no, null);
		vBuilder.create();
		vBuilder.show();
	}
	private void getDataInfo(){
	
			dttrailerno			=mtConfigHelper.setStringFormat(vdttrailerno);	//	拖车(取)拖车号(国内信息)		
			
			sealno				=mtConfigHelper.setStringFormat(vsealno);		//	铅封号(货物信息)
			//
			dtsingletrailernum	=mtConfigHelper.setDataFormat(vdtsingletrailernum);//	拖车(取)单车件数			
			//
			dtsingletrailerton	=mtConfigHelper.setDataFormat(vdtsingletrailerton);//	拖车(取)单车吨数
			//
			svehiclescoll		=mtConfigHelper.setDataFormat(vsvehiclescoll);	//	车数(取)(仓储)
			//
			dgtrainwagonno		=mtConfigHelper.setStringFormat(vdgtrainwagonno);	//	铁路车皮号(国内信息)			
			dgtraintype			=mtConfigHelper.setStringFormat(vdgtraintype);	//	铁路车型(国内信息)
			dgtrainwaybillno	=mtConfigHelper.setStringFormat(vdgtrainwaybillno);	//	铁路运单号(国内信息)
			//
			dgtrainsinglenum	=mtConfigHelper.setDataFormat(vdgtrainsinglenum);	//	铁路单车件数(国内信息)
			svehiclesdely		=mtConfigHelper.setDataFormat(vsvehiclesdely);
			//
			dgtrainsingleton	=mtConfigHelper.setDataFormat(vdgtrainsingleton);	//	铁路单车吨数
			//
			dgtrainwagonkg		=mtConfigHelper.setDataFormat(vdgtrainwagonkg);	//	铁路车皮标重
			dgtrailerno			=mtConfigHelper.setStringFormat(vdgtrailerno);	 //	拖车送拖车号(国内信息)
			dtrailermodelsdely	=mtConfigHelper.setStringFormat(vdtrailermodelsdely);//	拖车车型(送)(调度)
			//
			dgsingletrailernum	=mtConfigHelper.setDataFormat(vdgsingletrailernum);//	拖车(送)单车件数(国内信息)
			//
			dgsingletrailerton	=mtConfigHelper.setDataFormat(vdgsingletrailerton);//	拖车(送)单车吨数(国内信息)
			dloadingtime		=mtConfigHelper.setTimeFormat(vdloadingtime);	//	装车时间(调度)
			dgtrainstartdate	=mtConfigHelper.setTimeFormat(vdgtrainstartdate);	//	铁路发运日
			dtpickupdate		=mtConfigHelper.setTimeFormat(vdtpickupdate);	//	拖车(取)提货时间(国内时间)
			dgstartdate			=mtConfigHelper.setTimeFormat(vdgstartdate);	
			dtstartdate			=mtConfigHelper.setTimeFormat(vdtstartdate);	//	拖车(取)发车时间(国内时间)
			if(dtsingletrailernum.equals("-1")||dtsingletrailerton.equals("-1")||svehiclescoll.equals("-1")
			   ||dgtrainsinglenum.equals("-1")||dgtrainsingleton.equals("-1")||dgtrainwagonkg.equals("-1")
			   ||dgsingletrailernum.equals("-1")||dgsingletrailerton.equals("-1")
			   ){
				Toast.makeText(mContext, "输入格式不正确,请重新输入", Toast.LENGTH_SHORT).show();
			}else{
				if(dttrailerno.equals("未填")&&sealno.equals("未填")&&
						dtsingletrailernum.equals("未填")&&dtsingletrailerton.equals("未填")&&
						svehiclescoll.equals("未填")&&dgtrainwagonno.equals("未填")&&dgtraintype.equals("未填")&&
						dgtrainwaybillno.equals("未填")&&dgtrainsinglenum.equals("未填")&&dgsingletrailerton.equals("未填")&&
						dloadingtime.equals("未填")&&dgtrainstartdate.equals("未填")&&dtpickupdate.equals("未填")&&
						dgstartdate.equals("未填")&&dtstartdate.equals("未填")){	
					Toast.makeText(mContext, "空值请填写", Toast.LENGTH_SHORT).show();
				}else{
					Builder 	vBuilder=new Builder(mContext);
					String 		message = "二维码:"+barcode+"\r\n";
					if(pslkind.equals("铁路")){
					
						message+=
							"铁路信息----->\r\n"+
							"铁路车皮号:"+dgtrainwagonno+"\r\n"+//	铁路车皮号(国内信息)
							"铁路车型:"+dgtraintype+"\r\n"+//	铁路车型(国内信息)
							"铁路运单号:"+dgtrainwaybillno+"\r\n"+//	铁路运单号(国内信息)
							"铁路单车件数:"+dgtrainsinglenum +"\r\n"+	//	铁路单车件数(国内信息)
							"铁路单车吨数:"+dgtrainsingleton+"\r\n"+//	铁路单车吨数
							"铁路车皮标重:"+dgtrainwagonkg+"\r\n"+//	铁路车皮标重
							"装车时间:"+dloadingtime+"\r\n"+//	装车时间(调度)
							"铁路发运日:\r\n"+dgtrainstartdate+"\r\n";//	铁路发运日
						
					}else {
						message+=
							"拖车信息----->\r\n"+	
							"拖车(取)拖车号:"+dttrailerno+"\r\n"+		//	拖车(取)拖车号(国内信息)	
							"铅封号:"+sealno+"\r\n"+					//	铅封号(货物信息)
							"拖车(取)单车件数:"+dtsingletrailernum+"\r\n"+//	拖车(取)单车件数
							"拖车(取)单车吨数:"+dtsingletrailerton+"\r\n"+//	拖车(取)单车吨数
							"车数(取):"+svehiclescoll+"\r\n"+	//	车数(取)(仓储)
							
							"拖车(取)提货时间:\r\n"+dtpickupdate+"\r\n"+	//	拖车(取)提货时间(国内时间)
							"拖车(取)发车时间:\r\n"+dtstartdate+"\r\n"+	//	拖车(取)发车时间(国内时间)
							"--------------------------\r\n"+
							"拖车(送)拖车号:"+dgtrailerno+"\r\n"+		//	拖车送拖车号(国内信息)
							"拖车车型(送):"+dtrailermodelsdely+"\r\n"+		//	拖车车型(送)(调度)
							"拖车(送)单车件数:"+dgsingletrailernum+"\r\n"+		//	拖车(送)单车件数(国内信息)
							"拖车(送)单车吨数:"+dgsingletrailerton+"\r\n"+		//	拖车(送)单车吨数(国内信息)
							"车数(送):"+svehiclesdely+"\r\n"+	//	车数(送)(仓储)
							
							"拖车(送)发车时间:\r\n"+dgstartdate+"\r\n";	//	拖车(送)发车时间(国内信息)
								
					}
					message+="货物状态:"+cargostatuscenter+"\r\n"+//	货物状态
							"图:"+getImgCount(img)+"张" ;		//	图
					vBuilder.setMessage(message);
					vBuilder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if(mThread==null){
								// 进度条的内容;
								final CharSequence strDialogTitle = getString(R.string.tip_dialog_wait);
								final CharSequence strDialogBody = getString(R.string.tip_dialog_done);
								mDialog = ProgressDialog.show(mContext, strDialogTitle,strDialogBody, true);
								mThread=new UpLoadThread();
								mThread.start();
							}
							
						}
					});
					vBuilder.setNegativeButton(R.string.action_no, null);
					vBuilder.create();
					vBuilder.show();
				} 
			}
	}
	
	public class UpLoadThread extends Thread {
		@Override
		public void run() {

			// 进行相应的登录操作的界面显示;
			// 01.Http 协议中的Get和Post方法;
			String  url  	= "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":"+ MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM+ "/goods2";
			String  param	= null;
			String  response= null;
			int     nFlag 	= MTConfigHelper.NTAG_FAIL;
			Message msg		= new Message();
			Bundle	bundle	= new Bundle();
			try {
				param=
				"operType=1" +
				"&barcode="+barcode+
				"&dttrailerno="+URLEncoder.encode(dttrailerno,"utf-8") +
				"&sealno="+URLEncoder.encode(sealno,"utf-8") +
				"&dtsingletrailernum="+dtsingletrailernum +
				"&dtsingletrailerton=" +dtsingletrailerton+
				"&svehiclescoll=" +svehiclescoll+
				"&dtpickupdate="+URLEncoder.encode(dtpickupdate,"utf-8") +
				"&dtstartdate="+URLEncoder.encode(dtstartdate,"utf-8") +
				"&dgtrainwagonno="+URLEncoder.encode(dgtrainwagonno,"utf-8")+
				"&dgtraintype=" +URLEncoder.encode(dgtraintype,"utf-8")+
				"&dgtrainwaybillno=" +URLEncoder.encode(dgtrainwaybillno,"utf-8")+
				"&dgtrainsinglenum=" +dgtrainsinglenum+
				"&cargostatuscenter=" +URLEncoder.encode(cargostatuscenter,"utf-8")+
				"&dgtrainsingleton=" +dgtrainsingleton+
				"&dgtrainwagonkg=" +dgtrainwagonkg+
				"&dloadingtime=" +URLEncoder.encode(dloadingtime,"utf-8")+
				"&dgtrainstartdate=" +URLEncoder.encode(dgtrainstartdate,"utf-8")+
				"&dgtrailerno=" +URLEncoder.encode(dgtrailerno,"utf-8")+
				"&dtrailermodelsdely=" +URLEncoder.encode(dtrailermodelsdely,"utf-8")+
				"&dgsingletrailernum=" +dgsingletrailernum+
				"&dgsingletrailerton=" +dgsingletrailerton+
				"&svehiclesdely=" +svehiclesdely+
				"&dgstartdate=" +URLEncoder.encode(dgstartdate,"utf-8")+
				"&img="+URLEncoder.encode(img,"utf-8")+
				"&wid="+wid+
				"&busiinvcode="+busiinvcode; 
			
			
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			response 		= mGetOrPostHelper.sendGet(url, param);

			if (!response.trim().equalsIgnoreCase("fail")) {
				nFlag 	    = MTConfigHelper.NTAG_SUCCESS;

				String sql=
				"insert into getgoodsinfo (" +
				"barcode," +
				"dttrailerno," +
				"sealno," +
				"dtsingletrailernum," +
				"dtsingletrailerton," +
				"svehiclescoll," +
				"dtpickupdate," +
				"dtstartdate," +
				"dgtrainwagonno,dgtraintype,dgtrainwaybillno,dgtrainsinglenum,cargostatuscenter,dgtrainsingleton,dgtrainwagonkg," +
				"dloadingtime,dgtrainstartdate,dgtrailerno,dtrailermodelsdely,dgsingletrailernum,dgsingletrailerton,svehiclesdely,dgstartdate,img,busiinvcode" +		//	图片
				") values (" +
				"'"+barcode+"'," +
				"'"+dttrailerno+"'," +
				"'"+sealno+"'," +
				"'"+dtsingletrailernum+"'," +
				"'"+dtsingletrailerton+"'," +
				"'"+svehiclescoll+"'," +
				"'"+dtpickupdate+"'," +
				"'"+dtstartdate+"'," +
				"'"+dgtrainwagonno+"'," +
				"'"+dgtraintype+"'," +
				"'"+dgtrainwaybillno+"'," +
				"'"+dgtrainsinglenum+"'," +
				"'"+cargostatuscenter+"'," +
				"'"+dgtrainsingleton+"'," +
				"'"+dgtrainwagonkg+"'," +
				"'"+dloadingtime+"'," +
				"'"+dgtrainstartdate+"'," +
				"'"+dgtrailerno+"'," +
				"'"+dtrailermodelsdely+"'," +
				"'"+dgsingletrailernum+"'," +
				"'"+dgsingletrailerton+"'," +
				"'"+svehiclesdely+"'," +
				"'"+dgstartdate+"'," +
				"'"+img+"'," +
				"'"+busiinvcode+"')";		
				mDB.execSQL(sql);
			}
			bundle.putInt("flag", nFlag);
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}
	}	
	private void closeThread() {
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
	}
	private int getImgCount(String str){
		int count=0;
		if(str.contains("_")){
			String[] strs=str.split("_");
			count=strs.length;
		}else if(!str.equals("未拍照")){
			count=1;
		}else count=0;
		
		return count;
	}
	
}
