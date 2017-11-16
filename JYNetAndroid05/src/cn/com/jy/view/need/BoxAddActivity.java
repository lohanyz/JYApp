package cn.com.jy.view.need;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;

import cn.com.jy.activity.R;
import cn.com.jy.model.helper.MTConfigHelper;
import cn.com.jy.model.helper.MTGetOrPostHelper;
import cn.com.jy.model.helper.MTGetTextUtil;
import cn.com.jy.model.helper.MTSQLiteHelper;
import cn.com.jy.model.helper.MTSharedpreferenceHelper;

@SuppressLint("HandlerLeak")
public class BoxAddActivity extends Activity implements View.OnClickListener {
    private Context mContext;
    private Intent mIntent;
    private String date,time;
    /*自定义的类*/
    private ProgressDialog mDialog; // 对话方框;
    private MTSharedpreferenceHelper mSpHelper; // 首选项存储;
    private MTSQLiteHelper mSqLiteHelper;// 数据库的帮助类;
    private SQLiteDatabase mDB; // 数据库件;
    private MTGetOrPostHelper mGetOrPostHelper;
    private UpLoadThread	  mThread;
    private TextView vTopic,vBack,vFunction;
    private Button btnOk,btecarrydate,btechinaporttime,bteportstorageroomtime,btetimechangeofport,
            bterailwayofflinetime,btefeeofflinetime,bteactualreturntime;
    private EditText etecarryaddress,etechangenumber;
    private String ecarryaddress,ecarrydate,echinaporttime,eportstorageroomtime,etimechangeofport,
            echangenumber,efeeofflinetime,erailwayofflinetime,eactualreturntime,cargostatusbox,
            barcode,img,busiinvcode;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int nFlag = msg.what;
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
        setContentView(R.layout.box_add);
        initView();
        initEvent();
    }
    private void initView(){
        mContext =  BoxAddActivity.this;
        vBack	 =	(TextView) findViewById(R.id.btnBack);
        vTopic	 =	(TextView) findViewById(R.id.tvTopic);
        vFunction= 	(TextView) findViewById(R.id.btnFunction);
        btnOk    =  (Button) findViewById(R.id.btnOk);
        btecarrydate    =  (Button) findViewById(R.id.ecarrydate);
        btechinaporttime    =  (Button) findViewById(R.id.echinaporttime);
        bteportstorageroomtime    =  (Button) findViewById(R.id.eportstorageroomtime);
        btetimechangeofport    =  (Button) findViewById(R.id.etimechangeofport);
        bterailwayofflinetime    =  (Button) findViewById(R.id.erailwayofflinetime);
        btefeeofflinetime    =  (Button) findViewById(R.id.efeeofflinetime);
        bteactualreturntime    =  (Button) findViewById(R.id.eactualreturntime);
        etecarryaddress    =  (EditText) findViewById(R.id.ecarryaddress);
        etechangenumber    =  (EditText) findViewById(R.id.echangenumber);

    }
    private void initEvent(){
        vFunction.setVisibility(View.GONE);
        vBack.setOnClickListener(this);
        vTopic.setText("信息新增");
        getInfo();
        mSpHelper     = new MTSharedpreferenceHelper(mContext, MTConfigHelper.CONFIG_SELF,Context.MODE_APPEND);
        mSqLiteHelper = new MTSQLiteHelper(mContext);
        mGetOrPostHelper = new MTGetOrPostHelper();
        mDB 		  = mSqLiteHelper.getmDB();
//        wid 		  = mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
        btnOk.setOnClickListener(this);
        btecarrydate.setOnClickListener(this);
        btechinaporttime.setOnClickListener(this);
        bteportstorageroomtime.setOnClickListener(this);
        btetimechangeofport.setOnClickListener(this);
        bterailwayofflinetime.setOnClickListener(this);
        btefeeofflinetime.setOnClickListener(this);
        bteactualreturntime.setOnClickListener(this);
    }
    private void getInfo(){
        mIntent		  =getIntent();
        Bundle mBundle=mIntent.getExtras();
        barcode	  	  =mBundle.getString("barcode");
        cargostatusbox=mBundle.getString("cargostatusbox");
        img 	  	  =mBundle.getString("imgs");
        busiinvcode 	  	  =mBundle.getString("busiinvcode");
    }
    private void closeThread() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBack:
                finish();
                break;
            case R.id.ecarrydate:
                setViewDate(mContext, btecarrydate);
                break;
            case R.id.echinaporttime:
                setViewDate(mContext, btechinaporttime);
                break;
            case R.id.eportstorageroomtime:
                setViewDate(mContext, bteportstorageroomtime);
                break;
            case R.id.etimechangeofport:
                setViewDate(mContext, btetimechangeofport);
                break;
            case R.id.erailwayofflinetime:
                setViewDate(mContext, bterailwayofflinetime);
                break;
            case R.id.efeeofflinetime:
                setViewDate(mContext, btefeeofflinetime);
                break;
            case R.id.eactualreturntime:
                setViewDate(mContext, bteactualreturntime);
                break;
            case R.id.btnOk:
                getDataInfo();
                break;
            default:
                break;
        }
    }
    private void setViewDate(Context mContext,final Button btn){
        AlertDialog.Builder vBuilder   = new AlertDialog.Builder(mContext);
		/*布局控件*/
        View 	   view 	  = getLayoutInflater().inflate(R.layout.activity_datatimepicker, null);
        vBuilder.setTitle("设置时间");
        vBuilder.setView(view);
		/*时间日期有关控件*/
        DatePicker datePicker = (DatePicker) view.findViewById(R.id.dpPicker);
        TimePicker timePicker = (TimePicker) view.findViewById(R.id.tpPicker);
        Calendar calendar   = Calendar.getInstance();

        int 	   nYear 	  = calendar.get(Calendar.YEAR);
        int 	   nMonth 	  = calendar.get(Calendar.MONTH);
        int 	   nDay 	  = calendar.get(Calendar.DAY_OF_MONTH);
        int 	   nHour 	  = calendar.get(Calendar.HOUR_OF_DAY);
        int 	   nMinute 	  = calendar.get(Calendar.MINUTE);

        date = nYear + "年" + (nMonth + 1) + "月" + nDay + "日";
        time = nHour + "时" + nMinute + "分";
        datePicker.init(nYear, nMonth, nDay, new DatePicker.OnDateChangedListener() {

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
        ecarryaddress = MTGetTextUtil.getText(etecarryaddress);
        ecarrydate = MTGetTextUtil.getText(btecarrydate);
        echinaporttime = MTGetTextUtil.getText(btechinaporttime);
        eportstorageroomtime = MTGetTextUtil.getText(bteportstorageroomtime);
        etimechangeofport= MTGetTextUtil.getText(btetimechangeofport);
        echangenumber = MTGetTextUtil.getText(etechangenumber);
        efeeofflinetime = MTGetTextUtil.getText(btefeeofflinetime);
        erailwayofflinetime = MTGetTextUtil.getText(bterailwayofflinetime);
        eactualreturntime = MTGetTextUtil.getText(bteactualreturntime);
//        wid = mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
        AlertDialog.Builder vBuilder=new AlertDialog.Builder(mContext);
        vBuilder.setTitle("信息确认");
        String 	message = "二维码:"+barcode+"\r\n";
        message+="提箱地:"+ecarryaddress+"\r\n"+
                "提箱时间:"+ecarrydate+"\r\n"+
                "返回到中方口岸时间:"+echinaporttime+"\r\n"+
                "返回到口岸库房时间:"+eportstorageroomtime+"\r\n"+
                "口岸换装时间:"+etimechangeofport+"\r\n"+
                "换装车号:"+echangenumber+"\r\n"+
                "下线结费时间:"+efeeofflinetime+"\r\n"+
                "铁路下线时间:"+erailwayofflinetime+"\r\n"+
                "实际回空时间:"+eactualreturntime+"\r\n";

        message+="货物状态:"+cargostatusbox+"\r\n"+//	货物状态
                "图:"+img ;		//	图
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
    public class UpLoadThread extends Thread {
        private String url,
                param,
                response,
                sql,
                wid;

        public void run() {

            // 进行相应的登录操作的界面显示;
            //  01.Http 协议中的Get和Post方法;
            //
            url = "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":" + MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM + "/box";
            //url        =  "http://172.23.24.155:"+"8080"+"/JYTest02/harbor";
            wid = mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
            try {
                param = "operType=1&" +
                        "barcode=" + barcode + "&" +
                        "ecarryaddress=" + ecarryaddress + "&" +
                        "ecarrydate=" + URLEncoder.encode(ecarrydate, "utf-8") + "&" +
                        "img=" + img + "&" +
                        "echinaporttime=" + URLEncoder.encode(echinaporttime, "utf-8") + "&" +
                        "eportstorageroomtime=" + URLEncoder.encode(eportstorageroomtime, "utf-8") + "&" +
                        "etimechangeofport=" + URLEncoder.encode(etimechangeofport, "utf-8") + "&" +
                        "echangenumber=" + URLEncoder.encode(echangenumber, "utf-8") + "&" +
                        "efeeofflinetime=" + efeeofflinetime + "&" +
                        "erailwayofflinetime=" + erailwayofflinetime + "&" +
                        "eactualreturntime=" + eactualreturntime + "&" +
                        "cargostatusbox=" + cargostatusbox + "&" +
                        "wid=" + wid+"&"+
                        "busiinvcode="+busiinvcode;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            response = mGetOrPostHelper.sendGet(url, param);
            int nFlag = response.trim().equalsIgnoreCase("success") ? MTConfigHelper.NTAG_SUCCESS : MTConfigHelper.NTAG_FAIL;
            if (nFlag == MTConfigHelper.NTAG_SUCCESS) {
                sql=
                        "insert into boxmanageinfo (" +
                                "barcode," +
                                "ecarryaddress," +
                                "ecarrydate," +
                                "echinaporttime," +
                                "eportstorageroomtime," +
                                "etimechangeofport," +
                                "echangenumber," +
                                "efeeofflinetime," +
                                "erailwayofflinetime," +
                                "eactualreturntime,cargostatusbox,img,busiinvcode" +		//	图片
                                ") values (" +
                                "'"+barcode+"'," +
                                "'"+ecarryaddress+"'," +
                                "'"+ecarrydate+"'," +
                                "'"+echinaporttime+"'," +
                                "'"+eportstorageroomtime+"'," +
                                "'"+etimechangeofport+"'," +
                                "'"+echangenumber+"'," +
                                "'"+efeeofflinetime+"'," +
                                "'"+erailwayofflinetime+"'," +
                                "'"+eactualreturntime+"'," +
                                "'"+cargostatusbox+"'," +
                                "'"+img+"'," +
                                "'"+busiinvcode+"')";
                mDB.execSQL(sql);
            }
            mHandler.sendEmptyMessage(MTConfigHelper.NTAG_SUCCESS);
        }
    }
}
