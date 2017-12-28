package cn.com.jy.view.need;

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
import android.util.Log;
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

public class HarborAddActivity extends Activity implements View.OnClickListener {
    private Context mContext;
    private Intent mIntent;
    /*控件内容*/
    private TextView vTopic,vBack,vFunction;
    private EditText etpreloadcarno,etpreloadcarnum,etmsinglecarnum,etmsinglecarton,
    etSvehiclescoll;
    private Button btpfactchportdate,btmpackingdate,btppassdate,btpreloaddate,btpstartdate,btnOk;
    private ProgressDialog mDialog; // 对话方框;
    private String date,time;
    /*自定义的类*/
    private MTSharedpreferenceHelper mSpHelper; // 首选项存储;
    private MTSQLiteHelper mSqLiteHelper;// 数据库的帮助类;
    private SQLiteDatabase mDB; // 数据库件;
    private MTGetOrPostHelper mGetOrPostHelper;
    private UpLoadThread      mThread;

    private String pfactchportdate,mpackingdate,ppassdate,preloadcarno,
            preloadcarnum,preloaddate,msinglecarnum,msinglecarton,
            pstartdate,cargostatusseaport,wid,barcode,img,busiinvcode;

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
        setContentView(R.layout.harbor_add);
        initView();
        initEvent();
    }
    private void initView(){
        mContext =  HarborAddActivity.this;
        vBack    =  (TextView) findViewById(R.id.btnBack);
        vTopic   =  (TextView) findViewById(R.id.tvTopic);
        vFunction=  (TextView) findViewById(R.id.btnFunction);

        etpreloadcarno= (EditText) findViewById(R.id.preloadcarno);
        etmsinglecarnum= (EditText) findViewById(R.id.Dtsingletrailernum);
        etmsinglecarton= (EditText) findViewById(R.id.Dtsingletrailerton);
        etpreloadcarnum= (EditText) findViewById(R.id.preloadcarnum);

        btpfactchportdate= (Button) findViewById(R.id.pfactchportdate);
        btppassdate= (Button) findViewById(R.id.ppassdate);
        btpreloaddate= (Button) findViewById(R.id.preloaddate);
        btpstartdate= (Button) findViewById(R.id.pstartdate);
        btmpackingdate= (Button) findViewById(R.id.mpackingdate);
        btnOk= (Button) findViewById(R.id.btnOk);


    }
    private void initEvent(){
        vFunction.setVisibility(View.GONE);
        vBack.setOnClickListener(this);
        vTopic.setText("信息新增");
        getInfo();
        mSpHelper     = new MTSharedpreferenceHelper(mContext, MTConfigHelper.CONFIG_SELF,Context.MODE_APPEND);
        mSqLiteHelper = new MTSQLiteHelper(mContext);
        mGetOrPostHelper = new MTGetOrPostHelper();
        mDB           = mSqLiteHelper.getmDB();
        wid           = mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
        btnOk.setOnClickListener(this);
        btpfactchportdate.setOnClickListener(this);
        btmpackingdate.setOnClickListener(this);
        btppassdate.setOnClickListener(this);
        btpreloaddate.setOnClickListener(this);
        btpstartdate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBack:
                finish();
                break;
            case R.id.pfactchportdate:
                setViewDate(mContext, btpfactchportdate);
                break;
            case R.id.mpackingdate:
                setViewDate(mContext, btmpackingdate);
                break;
            case R.id.ppassdate:
                setViewDate(mContext, btppassdate);
                break;
            case R.id.preloaddate:
                setViewDate(mContext, btpreloaddate);
                break;
            case R.id.pstartdate:
                setViewDate(mContext, btpstartdate);
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
        View       view       = getLayoutInflater().inflate(R.layout.activity_datatimepicker, null);
        vBuilder.setTitle("设置时间");
        vBuilder.setView(view);
        /*时间日期有关控件*/
        DatePicker datePicker = (DatePicker) view.findViewById(R.id.dpPicker);
        TimePicker timePicker = (TimePicker) view.findViewById(R.id.tpPicker);
        Calendar calendar   = Calendar.getInstance();

        int        nYear      = calendar.get(Calendar.YEAR);
        int        nMonth     = calendar.get(Calendar.MONTH);
        int        nDay       = calendar.get(Calendar.DAY_OF_MONTH);
        int        nHour      = calendar.get(Calendar.HOUR_OF_DAY);
        int        nMinute    = calendar.get(Calendar.MINUTE);

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
    private void getInfo(){
        mIntent       =getIntent();
        Bundle mBundle=mIntent.getExtras();
        barcode       =mBundle.getString("barcode");
        cargostatusseaport=mBundle.getString("cargostatusseaport");
        img           =mBundle.getString("imgs");
        busiinvcode=mBundle.getString("busiinvcode");
    }
    private void getDataInfo(){
        pfactchportdate = MTGetTextUtil.getText(btpfactchportdate);
        mpackingdate = MTGetTextUtil.getText(btmpackingdate);
        ppassdate = MTGetTextUtil.getText(btppassdate);
        preloadcarno = MTGetTextUtil.getText(etpreloadcarno);
        preloadcarnum= MTGetTextUtil.getText(etpreloadcarnum);
        preloaddate = MTGetTextUtil.getText(btpreloaddate);
        msinglecarnum = MTGetTextUtil.getText(etmsinglecarnum);
        msinglecarton = MTGetTextUtil.getText(etmsinglecarton);
        pstartdate = MTGetTextUtil.getText(btpstartdate);
        wid = mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
        AlertDialog.Builder vBuilder=new AlertDialog.Builder(mContext);
        vBuilder.setTitle("信息确认");
        String  message = "二维码:"+barcode+"\r\n";
        message+="实际到中方口岸日:"+pfactchportdate+"\r\n"+
                "口岸装箱日:"+mpackingdate+"\r\n"+
                "放行时间:"+ppassdate+"\r\n"+
                "换装车号:"+preloadcarno+"\r\n"+
                "换装车数:"+preloadcarnum+"\r\n"+
                "换装时间:"+preloaddate+"\r\n"+
                "单车件数:"+msinglecarnum+"\r\n"+
                "单车吨数:"+msinglecarton+"\r\n"+
                "发车时间/出境时间:"+pstartdate+"\r\n";

        message+="货物状态:"+cargostatusseaport+"\r\n"+//   货物状态
                "图:"+img ;      //  图
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
            url = "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":" + MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM + "/harbor";
            //url        =  "http://172.23.24.155:"+"8080"+"/JYTest02/harbor";
            wid = mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
            try {
                param = "operType=1&" +
                        "barcode=" + URLEncoder.encode(barcode, "utf-8") + "&" +
                        "pfactchportdate=" + URLEncoder.encode(pfactchportdate, "utf-8") + "&" +
                        "mpackingdate=" + URLEncoder.encode(mpackingdate, "utf-8") + "&" +
                        "img=" + URLEncoder.encode(img, "utf-8") + "&" +
                        "preloadcarno=" + URLEncoder.encode(preloadcarno, "utf-8") + "&" +
                        "ppassdate=" + URLEncoder.encode(ppassdate, "utf-8") + "&" +
                        "preloadcarnum=" + URLEncoder.encode(preloadcarnum, "utf-8") + "&" +
                        "preloaddate=" + URLEncoder.encode(preloaddate, "utf-8") + "&" +
                        "msinglecarnum=" + URLEncoder.encode(msinglecarnum, "utf-8") + "&" +
                        "msinglecarton=" + URLEncoder.encode(msinglecarton, "utf-8") + "&" +
                        "pstartdate=" + URLEncoder.encode(pstartdate, "utf-8") + "&" +
                        "cargostatusseaport=" + URLEncoder.encode(cargostatusseaport, "utf-8") + "&" +
                        "wid=" + URLEncoder.encode(wid, "utf-8")+ "&" +
                        "busiinvcode="+URLEncoder.encode(busiinvcode, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            response = mGetOrPostHelper.sendGet(url, param);
            Log.e("aha", url +"::"+param);
            int nFlag = response.trim().equalsIgnoreCase("success") ? MTConfigHelper.NTAG_SUCCESS : MTConfigHelper.NTAG_FAIL;
            if (nFlag == MTConfigHelper.NTAG_SUCCESS) {
                sql=
                        "insert into harborinfo (" +
                                "barcode," +
                                "pfactchportdate," +
                                "mpackingdate," +
                                "ppassdate," +
                                "preloadcarno," +
                                "preloadcarnum," +
                                "preloaddate," +
                                "msinglecarnum," +
                                "msinglecarton,pstartdate,cargostatusseaport,img,busiinvcode" +     //  图片
                                ") values (" +
                                "'"+barcode+"'," +
                                "'"+pfactchportdate+"'," +
                                "'"+mpackingdate+"'," +
                                "'"+ppassdate+"'," +
                                "'"+preloadcarno+"'," +
                                "'"+preloadcarnum+"'," +
                                "'"+preloaddate+"'," +
                                "'"+msinglecarnum+"'," +
                                "'"+msinglecarton+"'," +
                                "'"+pstartdate+"'," +
                                "'"+cargostatusseaport+"'," +
                                "'"+img+"'," +
                                "'"+busiinvcode+"')";
                mDB.execSQL(sql);

            }
            mHandler.sendEmptyMessage(MTConfigHelper.NTAG_SUCCESS);
        }
    }
    private void closeThread() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }
}
