package cn.com.jy.view.need;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Map;

import cn.com.jy.activity.R;
import cn.com.jy.model.helper.FileHelper;
import cn.com.jy.model.helper.MTConfigHelper;
import cn.com.jy.model.helper.MTGetOrPostHelper;
import cn.com.jy.model.helper.MTGetTextUtil;
import cn.com.jy.model.helper.MTImgHelper;
import cn.com.jy.model.helper.MTSQLiteHelper;
import cn.com.jy.model.helper.MTSharedpreferenceHelper;

/**
 * Created by loh on 2017/8/24.
 */

public class HarborInformationActivity extends Activity implements View.OnClickListener,View.OnFocusChangeListener{

    private Context mContext;
    private ProgressDialog mDialog;
    private MTConfigHelper	mConfigHelper;
    private SQLiteDatabase mDB;
    private MTSQLiteHelper mSqLiteHelper;
    private TextView tvTopic;
    private ArrayAdapter<String> mAdapter;
    private ListView			 mListView;
    private MTGetOrPostHelper mGetOrPostHelper;
    private AlertDialog.Builder mBuilder;
    private MTSharedpreferenceHelper mSpHelper;
    private TextView  btnDetail,state2;
    private Button mGsimg;
    private Button btnftochnharbortime,btnpboxtime,
            btnsenttime,btntranstime,btnstime,
            btnCode,btnSearch,btnOk;
    private String bid, gstate,date,time,gid,stime,stimea;
    private String ftochnharbortime,pboxtime, senttime,transtime,transtid,transtcount,percount,perweight;
    private Intent mIntent;
    private MTImgHelper mImgHelper;
    private Spinner mState;
    private Thread mThread;
    private Thread mThread2;
    private EditText etSearch,ettranstid,ettranstcount,etpercount,etperweight;
    private String saveDir 	= 	Environment.getExternalStorageDirectory().getPath()+File.separator+"jyFile",
            saveFolder	=	"photo",
            folderPath,		//	文件夹路径;
            filePath,		//	文件路径;
            tmpPath,
            gsimg,		//	临时路径;
            sSize;
    private ArrayList<String> list;
    private FileHelper mFileHelper;
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mDialog.dismiss();
            switch (msg.what) {
                case MTConfigHelper.NTAG_SUCCESS:
                    Toast.makeText(mContext, R.string.tip_success, Toast.LENGTH_SHORT).show();
                    break;
                //	02.失败;
                case MTConfigHelper.NTAG_FAIL:
                    Toast.makeText(mContext, R.string.tip_fail, Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.harbor);
        initView();
        initEvent();
    }
    private void initView(){
        mContext	=	HarborInformationActivity.this;
        mListView	=   (ListView) findViewById(R.id.lvResult);
        etSearch	=	(EditText) findViewById(R.id.etSearch);
        etpercount  =   (EditText) findViewById(R.id.percount);
        ettranstcount=  (EditText) findViewById(R.id.transcount);
        etperweight =   (EditText) findViewById(R.id.perweight);
        ettranstid  =   (EditText) findViewById(R.id.transtid);
        tvTopic		=	(TextView) findViewById(R.id.tvTopic);
        mConfigHelper	=new MTConfigHelper();
        mFileHelper 	= new FileHelper();
        mSpHelper 		= new MTSharedpreferenceHelper(mContext, MTConfigHelper.CONFIG_SELF,mContext.MODE_APPEND);
        mSqLiteHelper 	= new MTSQLiteHelper(mContext);
        mDB 		= mSqLiteHelper.getmDB();
        mIntent		=	getIntent();
        btnDetail	=	(TextView) findViewById(R.id.btnFunction);
        state2 		= 	(TextView) findViewById(R.id.state2);
        btnCode		=   (Button) findViewById(R.id.btnCode);
        btnSearch	=   (Button) findViewById(R.id.btnSearch);
        mState	    =	(Spinner) findViewById(R.id.gstate);
        mGsimg		=	(Button) findViewById(R.id.btnPhoto);
        btnftochnharbortime =	(Button) findViewById(R.id.ftochnharbortime);
        btnpboxtime	=	(Button) findViewById(R.id.pboxtime);
        btnsenttime	=	(Button) findViewById(R.id.senttime);
        btntranstime=	(Button) findViewById(R.id.transtime);
        btnstime	=	(Button) findViewById(R.id.stime);
        btnOk       =   (Button) findViewById(R.id.btnOk);
        list		= 	new ArrayList<String>();
        mImgHelper = new MTImgHelper();
        folderPath	= 	saveDir+File.separator+saveFolder+File.separator+bid+File.separator+"harbor";

    }
    private void initEvent(){
        tvTopic.setText("口岸");
        btnDetail.setText("历史");
        btnDetail.setOnClickListener(this);
        mGsimg.setOnClickListener(this);
        btnftochnharbortime.setOnClickListener(this);
        btnpboxtime.setOnClickListener(this);
        btnsenttime.setOnClickListener(this);
        btntranstime.setOnClickListener(this);
        btnstime.setOnClickListener(this);
        btnCode.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnOk.setOnClickListener(this);
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
        etpercount.setOnFocusChangeListener(this);
        etperweight.setOnFocusChangeListener(this);
        ettranstcount.setOnFocusChangeListener(this);
        mGetOrPostHelper=new MTGetOrPostHelper();
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
        mState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long id) {
                switch (position) {
                    case 0:
                        gstate="正常";
                        break;
                    case 1:
                        mBuilder	=	new AlertDialog.Builder(mContext);
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == MTConfigHelper.NTRACK_GGOODS_GID_TO
				&& resultCode == MTConfigHelper.NTRACK_FLUSH_TO_MENU) {
			String gid = intent.getStringExtra("bid");
			etSearch.setText(gid);
		}
        if(requestCode == MTConfigHelper.NTRACK_GGOODS_PHOTO_TO
                && resultCode == -1 ){
            Toast.makeText(mContext, "拍照完成", Toast.LENGTH_SHORT).show();
            mImgHelper.compressPicture(tmpPath, filePath);
            mImgHelper.clearPicture(tmpPath, null);

            sSize = String.valueOf(mFileHelper.getFileCount(folderPath));
            state2.setText(sSize);
        }
    }
    protected void onDestroy() {
        super.onDestroy();
        if(mThread!=null){
            mThread.interrupt();
            mThread=null;
        }
        if(mThread2!=null){
            mThread2.interrupt();
            mThread2=null;
        }
    }
    public void onClickBack(View view){
        finish();
    }

    @Override
    public void onClick(final View v) {
        switch(v.getId()){
            case R.id.btnPhoto:
                getPhoto_Ggoods();
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
                    mThread=new MyThread();
                    mThread.start();
                }
                break;
            case R.id.btnOk:
                try {
                    if (bid != null && gid != null) {
                        gsimg = mFileHelper.getFileNamesByStrs(folderPath);
                        if (gsimg.equals("")) {
                            gsimg = "null";
                        }
                        ftochnharbortime = MTGetTextUtil.getText(btnftochnharbortime);
                        pboxtime = MTGetTextUtil.getText(btnpboxtime);
                        senttime = MTGetTextUtil.getText(btnsenttime);
                        transtime = MTGetTextUtil.getText(btntranstime);
                        stimea= MTGetTextUtil.getText(btnstime);
                        transtid = MTGetTextUtil.getText(ettranstid);
                        transtcount = MTGetTextUtil.getText(ettranstcount);
                        percount = MTGetTextUtil.getText(etpercount);
                        perweight = MTGetTextUtil.getText(etperweight);

                        if (mThread2 == null) {
                            mThread2 = new MyThread2();
                            mThread2.start();
                        }
                    }
                    else {
                        Toast.makeText(mContext, "请进行搜索配对", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(this,"请按要求填写内容",Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.btnFunction:
                mIntent = new Intent(mContext, HarborHistoryActivity.class);
                startActivity(mIntent);
                break;
            default:
                mBuilder = new AlertDialog.Builder(mContext);
                View view = getLayoutInflater().inflate(
                        R.layout.activity_datatimepicker, null);
                mBuilder.setTitle("设置时间");
                mBuilder.setView(view);
                DatePicker datePicker = (DatePicker) view
                        .findViewById(R.id.dpPicker);
                TimePicker timePicker = (TimePicker) view
                        .findViewById(R.id.tpPicker);
                Calendar calendar = Calendar.getInstance();

                int nYear 	=  calendar.get(Calendar.YEAR);
                int nMonth  =  calendar.get(Calendar.MONTH);
                int nDay    =  calendar.get(Calendar.DAY_OF_MONTH);
                int nHour   =  calendar.get(Calendar.HOUR_OF_DAY);
                int nMinute =  calendar.get(Calendar.MINUTE);
                String month=  nMonth+1<10?"0"+(nMonth+1):""+(nMonth+1);
                String day	=  nDay<10?"0"+nDay:""+nDay;
                String hour	=  nHour<10?"0"+nHour:""+nHour;
                String minute= nMinute<10?"0"+nMinute:""+nMinute;
                date 		 = nYear + "年" + month + "月" + day + "日";
                time 		 = hour + "时" + minute + "分";
                datePicker.init(nYear, nMonth, nDay, new DatePicker.OnDateChangedListener() {

                    @Override
                    public void onDateChanged(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                        // 日历控件;
                        String month=monthOfYear + 1<10?"0"+(monthOfYear+1):""+(monthOfYear+1);
                        String day=dayOfMonth<10?"0"+dayOfMonth:""+dayOfMonth;
                        date = year + "年" + month + "月" + day + "日";
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
                                    case R.id.ftochnharbortime:
                                    case R.id.pboxtime:
                                    case R.id.senttime:
                                    case R.id.transtime:
                                    case R.id.stime:
                                        findViewById(v.getId()).setBackgroundColor(Color.WHITE);
                                        ((Button)findViewById(v.getId())).setText(stime);
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
        }

    }
    public void getPhoto_Ggoods() {
        File file;
        if (mConfigHelper.getfState().equals(Environment.MEDIA_MOUNTED)) {
            if (bid != null && gid != null) {
                folderPath = mConfigHelper.getfParentPath() + bid
                        + File.separator + "harbor" + File.separator + gid;
                gsimg = bid + "harbor" + gid + "file"
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

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus){
            ((EditText) findViewById(v.getId())).setText("");
        }
    }


    public class MyThread extends Thread{
        private String url,
                param,
                response
                        ;
        @Override
        public void run() {
            url = "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":"+ MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM+ "/goods2";
            //url		 =	"http://172.23.24.155:8080/JYTest02/goods2";
            param	 =	"operType=2&gid="+gid;
            response= 	mGetOrPostHelper.sendGet(url,param);
            int nFlag= 	MTConfigHelper.NTAG_FAIL;
            if(!response.equalsIgnoreCase("fail")){
                nFlag= MTConfigHelper.NTAG_SUCCESS;
                try {
                    Log.e("resp:",response);
                    JSONArray array = new JSONArray(response);
                    int 	  i		= 0;
                    JSONObject obj 	= null;
                    do {
                        try {
                            //	JsonObject的解析;
                            obj			  =	array.getJSONObject(i);
                            Log.e("obj:",obj.toString());

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
                            list.add("————分割线————");
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
                    nFlag	=	MTConfigHelper.NTAG_FAIL;

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
            url = "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":"+ MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM+ "/harbor";
            //url		 =	"http://172.23.24.155:"+"8080"+"/JYTest02/harbor";
            wid = mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
            try {
                param	 =	"operType=1&" +
                        "bid="+bid+"&" +
                        "gid="+gid+"&" +
                        "state="+ URLEncoder.encode(gstate,"utf-8")+"&" +
                        "simg="+gsimg+"&" +
                        "ftochnharbortime="+URLEncoder.encode(ftochnharbortime,"utf-8")+"&" +
                        "pboxtime="+URLEncoder.encode(pboxtime,"utf-8")+"&" +
                        "senttime="+URLEncoder.encode(senttime,"utf-8")+"&" +
                        "transtime="+URLEncoder.encode(transtime,"utf-8")+"&" +
                        "transtid="+transtid+"&" +
                        "transtcount="+transtcount+"&" +
                        "percount="+percount+"&" +
                        "perweight="+perweight+"&" +
                        "stime="+URLEncoder.encode(stimea,"utf-8")+"&" +
                        "wid="+wid;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            response = 	mGetOrPostHelper.sendGet(url,param);
            String gtime = mConfigHelper.getCurrentTime("yyyy年MM月dd日HH时mm分");
            int nFlag=response.trim().equalsIgnoreCase("success")?MTConfigHelper.NTAG_SUCCESS:MTConfigHelper.NTAG_FAIL;
            sql = "insert into harborinfo (bid,gid,state,simg,ftochnharbortime,pboxtime,senttime,transtime,transtid,transtcount,pertcount,pertweight,gtime,stime) values ('"+
                    bid+ "',"+ "'"+ gid+ "',"+ "'"+ gstate+ "',"+ "'"+gsimg+ "',"+ "'"+ ftochnharbortime+ "',"+ "'"+ pboxtime+ "',"+ "'"+ senttime+ "',"+ "'"+ transtime+ "',"+"'"+transtid+ "',"+"'"+transtcount+ "',"+percount+ ","+ perweight+ ","+"'"+gtime+"',"+"'"+ stimea+ "')";
            bid=null;
            gid=null;
            mDB.execSQL(sql);
            myHandler.sendEmptyMessage(nFlag);
        }
    }
    private void showData(){
        folderPath = mConfigHelper.getfParentPath() + bid + File.separator
                + "harbor" + File.separator + gid;
        sSize = String.valueOf(mFileHelper.getFileCount(folderPath));
        state2.setText(sSize);
        mAdapter=new ArrayAdapter<String>(mContext, R.layout.item02, R.id.tvTopic, list);
        mListView.setAdapter(mAdapter);
    }
}