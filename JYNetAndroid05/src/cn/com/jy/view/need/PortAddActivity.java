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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
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

public class PortAddActivity extends Activity implements View.OnClickListener {
    private Context mContext;
    private Intent mIntent;
    /*控件内容*/
    private TextView vTopic, vBack, vFunction, btnBack;
    private LinearLayout layTrain, layTruck;
    private String barcode, cargostatusport, slkind, img;
    private Button vOk, btptoportdate, btpreinvoicedate_port, btpjinchangdate, btppackingtime, btbssj, btbssj2,
            btstartdate, btdgtrainstartdate;
    private ProgressDialog mDialog;
    private EditText etfcchgk, etdcjsgkdz, etdcdsgkdz, etdsgkdz, etblhtl, etdgtrainwagonno, etdgtraintype,
            etdgtrainwaybillno, etdgtrainsinglenum, etdgtrainsingleton, etdgtrainwagonkg;
    private String ptoportdate, preinvoicedate_port, pjinchangdate, ppackingtime, bssj, wid,
            startdate, dgtrainstartdate, sfpxpz, fcchgk, dcjsgkdz, dcdsgkdz, dsgkdz, blhtl, dgtrainwagonno, dgtraintype,
            dgtrainwaybillno, dgtrainsinglenum, dgtrainsingleton, dgtrainwagonkg, busiinvcode;

    private RadioGroup rgsfpxpz;
    private String date, time;
    private MTSharedpreferenceHelper mSpHelper; // 首选项存储;
    private MTSQLiteHelper mSqLiteHelper;// 数据库的帮助类;
    private SQLiteDatabase mDB; // 数据库件;
    private MTGetOrPostHelper mGetOrPostHelper;
    private UpLoadThread mThread;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int nFlag = msg.what;
            mDialog.dismiss();
            switch (nFlag) {
                // 01.成功;
                case MTConfigHelper.NTAG_SUCCESS:
                    Toast.makeText(mContext, R.string.tip_success, Toast.LENGTH_SHORT).show();
                    break;
                // 02.失败;
                case MTConfigHelper.NTAG_FAIL:
                    Toast.makeText(mContext, R.string.tip_fail, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
            closeThread();
            if (nFlag == 1) {
                setResult(1, mIntent);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.port_add);
        initView();
        initEvent();
        checkView();
    }

    private void initView() {
        mContext = PortAddActivity.this;
        vBack = (TextView) findViewById(R.id.btnBack);
        vTopic = (TextView) findViewById(R.id.tvTopic);
        vFunction = (TextView) findViewById(R.id.btnFunction);
        layTrain = (LinearLayout) findViewById(R.id.layTrain);
        layTruck = (LinearLayout) findViewById(R.id.layTruck);

        vOk = (Button) findViewById(R.id.btnOk);
        btptoportdate = (Button) findViewById(R.id.btptoportdate);
        btpreinvoicedate_port = (Button) findViewById(R.id.btpreinvoicedate_port);
        btpjinchangdate = (Button) findViewById(R.id.btpjinchangdate);
        btppackingtime = (Button) findViewById(R.id.btppackingtime);
        btbssj = (Button) findViewById(R.id.btbssj);
        btbssj2 = (Button) findViewById(R.id.btbssj2);
        btstartdate = (Button) findViewById(R.id.btstartdate);
        btdgtrainstartdate = (Button) findViewById(R.id.btdgtrainstartdate);
        btnBack = (TextView) findViewById(R.id.btnBack);
        rgsfpxpz = (RadioGroup) findViewById(R.id.sfpxpz);
        etfcchgk = (EditText) findViewById(R.id.etfcchgk);
        etdcjsgkdz = (EditText) findViewById(R.id.etdcjsgkdz);
        etdcdsgkdz = (EditText) findViewById(R.id.etdcdsgkdz);
        etdsgkdz = (EditText) findViewById(R.id.etdsgkdz);
        etblhtl = (EditText) findViewById(R.id.etblhtl);
        etdgtrainwagonno = (EditText) findViewById(R.id.etdgtrainwagonno);
        etdgtraintype = (EditText) findViewById(R.id.etdgtraintype);
        etdgtrainwaybillno = (EditText) findViewById(R.id.etdgtrainwaybillno);
        etdgtrainsinglenum = (EditText) findViewById(R.id.etdgtrainsinglenum);
        etdgtrainsingleton = (EditText) findViewById(R.id.etdgtrainsingleton);
        etdgtrainwagonkg = (EditText) findViewById(R.id.etdgtrainwagonkg);

    }

    private void initEvent() {
        vFunction.setVisibility(View.GONE);
        getInfo();
        mSpHelper = new MTSharedpreferenceHelper(mContext, MTConfigHelper.CONFIG_SELF, Context.MODE_APPEND);
        mSqLiteHelper = new MTSQLiteHelper(mContext);
        mGetOrPostHelper = new MTGetOrPostHelper();
        mDB = mSqLiteHelper.getmDB();
        wid = mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
        vOk.setOnClickListener(this);
        btptoportdate.setOnClickListener(this);
        btpreinvoicedate_port.setOnClickListener(this);
        btpjinchangdate.setOnClickListener(this);
        btppackingtime.setOnClickListener(this);
        btbssj.setOnClickListener(this);
        btbssj2.setOnClickListener(this);
        btstartdate.setOnClickListener(this);
        btdgtrainstartdate.setOnClickListener(this);
        btnBack.setOnClickListener(this);

    }

    private void checkView() {
        if (slkind.equals("train")) {
            vTopic.setText("铁路信息");
            layTrain.setVisibility(View.VISIBLE);
            layTruck.setVisibility(View.GONE);

        } else {
            vTopic.setText("拖车信息");
            layTrain.setVisibility(View.GONE);
            layTruck.setVisibility(View.VISIBLE);
        }
    }

    private void getInfo() {
        mIntent = getIntent();
        Bundle mBundle = mIntent.getExtras();
        barcode = mBundle.getString("barcode");
        cargostatusport = mBundle.getString("cargostatusport");
        slkind = mBundle.getString("slkind");
        busiinvcode = mBundle.getString("busiinvcode");
        img = mBundle.getString("imgs");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                finish();
                break;
            case R.id.btptoportdate:
                setViewDate(mContext, btptoportdate);
                break;
            case R.id.btpreinvoicedate_port:
                setViewDate(mContext, btpreinvoicedate_port);
                break;
            case R.id.btpjinchangdate:
                setViewDate(mContext, btpjinchangdate);
                break;
            case R.id.btppackingtime:
                setViewDate(mContext, btppackingtime);
                break;
            case R.id.btbssj:
                setViewDate(mContext, btbssj);
                break;
            case R.id.btbssj2:
                setViewDate(mContext, btbssj2);
                break;
            case R.id.btstartdate:
                setViewDate(mContext, btstartdate);
                break;
            case R.id.btdgtrainstartdate:
                setViewDate(mContext, btdgtrainstartdate);
                break;
            case R.id.btnOk:
                getDataInfo();
                break;


        }
    }

    private void setViewDate(Context mContext, final Button btn) {
        AlertDialog.Builder vBuilder = new AlertDialog.Builder(mContext);
        /*布局控件*/
        View view = getLayoutInflater().inflate(R.layout.activity_datatimepicker, null);
        vBuilder.setTitle("设置时间");
        vBuilder.setView(view);
        /*时间日期有关控件*/
        DatePicker datePicker = (DatePicker) view.findViewById(R.id.dpPicker);
        TimePicker timePicker = (TimePicker) view.findViewById(R.id.tpPicker);
        Calendar calendar = Calendar.getInstance();

        int nYear = calendar.get(Calendar.YEAR);
        int nMonth = calendar.get(Calendar.MONTH);
        int nDay = calendar.get(Calendar.DAY_OF_MONTH);
        int nHour = calendar.get(Calendar.HOUR_OF_DAY);
        int nMinute = calendar.get(Calendar.MINUTE);

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

    private void getDataInfo() {

        ptoportdate = MTGetTextUtil.getText(btptoportdate);
        preinvoicedate_port = MTGetTextUtil.getText(btpreinvoicedate_port);
        pjinchangdate = MTGetTextUtil.getText(btpjinchangdate);
        ppackingtime = MTGetTextUtil.getText(btppackingtime);
        bssj = slkind.equals("train") ? MTGetTextUtil.getText(btbssj2) : MTGetTextUtil.getText(btbssj);
        fcchgk = MTGetTextUtil.getText(etfcchgk);
        dcjsgkdz = MTGetTextUtil.getText(etdcjsgkdz);
        dcdsgkdz = MTGetTextUtil.getText(etdcdsgkdz);
        dsgkdz = MTGetTextUtil.getText(etdsgkdz);
        startdate = MTGetTextUtil.getText(btstartdate);
        blhtl = MTGetTextUtil.getText(etblhtl);
        dgtrainwagonno = MTGetTextUtil.getText(etdgtrainwagonno);
        dgtraintype = MTGetTextUtil.getText(etdgtraintype);
        dgtrainwaybillno = MTGetTextUtil.getText(etdgtrainwaybillno);
        dgtrainsinglenum = MTGetTextUtil.getText(etdgtrainsinglenum);
        dgtrainsingleton = MTGetTextUtil.getText(etdgtrainsingleton);
        dgtrainwagonkg = MTGetTextUtil.getText(etdgtrainwagonkg);
        dgtrainstartdate = MTGetTextUtil.getText(btdgtrainstartdate);
        sfpxpz = rgsfpxpz.getCheckedRadioButtonId() == R.id.lean ? "是" : "否";
        AlertDialog.Builder vBuilder = new AlertDialog.Builder(mContext);

        String message = "二维码:" + barcode + "\r\n";
        message += "到港时间:" + ptoportdate + "\r\n";
        message += "换单时间:" + preinvoicedate_port + "\r\n";
        message += "进场时间:" + pjinchangdate + "\r\n";
        message += "装箱时间:" + ppackingtime + "\r\n";
        message += "报数时间:" + bssj + "\r\n";
        if (slkind.equals("train")) {

            message +=
                    "铁路信息----->\r\n" +
                            "铁路车皮号:" + dgtrainwagonno + "\r\n" +//  铁路车皮号(国内信息)
                            "铁路车型:" + dgtraintype + "\r\n" +//  铁路车型(国内信息)
                            "铁路运单号:" + dgtrainwaybillno + "\r\n" +//    铁路运单号(国内信息)
                            "铁路单车件数:" + dgtrainsinglenum + "\r\n" +    //   铁路单车件数(国内信息)
                            "铁路单车吨数:" + dgtrainsingleton + "\r\n" +//   铁路单车吨数
                            "铁路车皮标重:" + dgtrainwagonkg + "\r\n" +// 铁路车皮标重
                            "发车时间:" + dgtrainstartdate + "\r\n";//  铁路发运日

        } else {
            message +=
                    "拖车信息----->\r\n" +
                            "报数时间:" + bssj + "\r\n" +        // 拖车(取)拖车号(国内信息)
                            "发车车号:" + fcchgk + "\r\n" +                    //   铅封号(货物信息)
                            "单车件数:" + dcjsgkdz + "\r\n" +// 拖车(取)单车件数
                            "单车吨数:" + dcdsgkdz + "\r\n" +// 拖车(取)单车吨数
                            "车数:" + dsgkdz + "\r\n" +    // 车数(取)(仓储)

                            "发车时间:" + startdate + "\r\n";
        }
        message += "货物状态:" + cargostatusport + "\r\n" +//   货物状态
                "图:" + img;        //   图
        vBuilder.setMessage(message);
        vBuilder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (mThread == null) {
                    // 进度条的内容;
                    final CharSequence strDialogTitle = getString(R.string.tip_dialog_wait);
                    final CharSequence strDialogBody = getString(R.string.tip_dialog_done);
                    mDialog = ProgressDialog.show(mContext, strDialogTitle, strDialogBody, true);
                    mThread = new UpLoadThread();
                    mThread.start();
                }

            }
        });
        vBuilder.setNegativeButton(R.string.action_no, null);
        vBuilder.create();
        vBuilder.show();

    }

    public class UpLoadThread extends Thread {
        @Override
        public void run() {

            // 进行相应的登录操作的界面显示;
            // 01.Http 协议中的Get和Post方法;
            String url = "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":" + MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM + "/port";
            String param = null;
            try {
                param =
                        "operType=1" +
                                "&barcode=" + barcode +
                                "&ptoportdate=" + URLEncoder.encode(ptoportdate, "utf-8") +
                                "&preinvoicedate_port=" + URLEncoder.encode(preinvoicedate_port, "utf-8") +
                                "&pjinchangdate=" + pjinchangdate +
                                "&ppackingtime=" + ppackingtime +
                                "&sfpxpz=" + sfpxpz +
                                "&bssj=" + URLEncoder.encode(bssj, "utf-8") +
                                "&fcchgk=" + URLEncoder.encode(fcchgk, "utf-8") +
                                "&dcjsgkdz=" + URLEncoder.encode(dcjsgkdz, "utf-8") +
                                "&dcdsgkdz=" + URLEncoder.encode(dcdsgkdz, "utf-8") +
                                "&dsgkdz=" + URLEncoder.encode(dsgkdz, "utf-8") +
                                "&startdate=" + startdate +
                                "&blhtl=" + URLEncoder.encode(blhtl, "utf-8") +
                                "&dgtrainwagonno=" + dgtrainwagonno +
                                "&dgtraintype=" + dgtraintype +
                                "&dgtrainwaybillno=" + URLEncoder.encode(dgtrainwaybillno, "utf-8") +
                                "&dgtrainsinglenum=" + URLEncoder.encode(dgtrainsinglenum, "utf-8") +
                                "&dgtrainsingleton=" + URLEncoder.encode(dgtrainsingleton, "utf-8") +
                                "&cargostatusport=" + URLEncoder.encode(cargostatusport, "utf-8") +
                                "&dgtrainwagonkg=" + dgtrainwagonkg +
                                "&dgtrainstartdate=" + dgtrainstartdate +
                                "&img=" + URLEncoder.encode(img, "utf-8") +
                                "&busiinvcode=" + URLEncoder.encode(busiinvcode, "utf-8") +
                                "&wid=" + wid;


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String response = mGetOrPostHelper.sendGet(url, param);
            int nFlag = MTConfigHelper.NTAG_FAIL;

            if (!response.trim().equalsIgnoreCase("fail")) {
                nFlag = MTConfigHelper.NTAG_SUCCESS;

                String sql =
                        "insert into portinfo (" +
                                "barcode," +
                                "ptoportdate," +
                                "preinvoicedate_port," +
                                "pjinchangdate," +
                                "ppackingtime," +
                                "sfpxpz," +
                                "bssj," +
                                "fcchgk," +
                                "dcjsgkdz,dcdsgkdz,dsgkdz,startdate,blhtl,dgtrainwagonno,dgtraintype," +
                                "dgtrainwaybillno,dgtrainsinglenum,dgtrainsingleton,cargostatusport,dgtrainwagonkg,dgtrainstartdate,img,busiinvcode" +        //    图片
                                ") values (" +
                                "'" + barcode + "'," +
                                "'" + ptoportdate + "'," +
                                "'" + preinvoicedate_port + "'," +
                                "'" + pjinchangdate + "'," +
                                "'" + ppackingtime + "'," +
                                "'" + sfpxpz + "'," +
                                "'" + bssj + "'," +
                                "'" + fcchgk + "'," +
                                "'" + dcjsgkdz + "'," +
                                "'" + dcdsgkdz + "'," +
                                "'" + dsgkdz + "'," +
                                "'" + startdate + "'," +
                                "'" + blhtl + "'," +
                                "'" + dgtrainwagonno + "'," +
                                "'" + dgtraintype + "'," +
                                "'" + dgtrainwaybillno + "'," +
                                "'" + dgtrainsinglenum + "'," +
                                "'" + dgtrainsingleton + "'," +
                                "'" + cargostatusport + "'," +
                                "'" + dgtrainwagonkg + "'," +
                                "'" + dgtrainstartdate + "'," +
                                "'" + img + "'," +
                                "'" + busiinvcode + "')";
                mDB.execSQL(sql);
            }
            mHandler.sendEmptyMessage(nFlag);
        }
    }

    private void closeThread() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }
}
