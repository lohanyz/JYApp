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
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.app.AlertDialog.Builder;
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
import cn.com.jy.model.entity.Trainorder;
import cn.com.jy.model.entity.Truckorder;
import cn.com.jy.model.helper.FileHelper;
import cn.com.jy.model.helper.MTConfigHelper;
import cn.com.jy.model.helper.MTFileHelper;
import cn.com.jy.model.helper.MTGetOrPostHelper;
import cn.com.jy.model.helper.MTGetTextUtil;
import cn.com.jy.model.helper.MTImgHelper;
import cn.com.jy.model.helper.MTSQLiteHelper;
import cn.com.jy.model.helper.MTSharedpreferenceHelper;

/**
 * Created by loh on 2017/8/24.
 */

public class PortActivity extends Activity implements View.OnClickListener{
    private Context mContext;
    private TextView tvTopic,btnDetail,btnBack,state2;
    private ProgressDialog mDialog;

    private Button  mGsimg,
            btninporttime, btnctime, btnintime, btnpboxtime,btnCode,btnSearch,btnOk;
    private FileHelper mFileHelper;
    private EditText etSearch;
    private RadioGroup isLean;
    private Spinner mState;
    private Spinner splkind;
    private String gstate,lkind,bid,date,time,stime,gid,islean,sSize,wid;
    private ListView             mListView;
    private ArrayAdapter<String> mAdapter;
    private Intent mIntent;
    private AlertDialog.Builder mBuilder;
    private ArrayList<String>    list;
    private Trainorder trainorder;
    private Truckorder truckorder;
    private ArrayList<MEFile>    listfile;

    //  TODO 02.修改的相关内容;
    private LoadInfoThread mThread; // 线程内容;
    private UpLoadThread   mThread2;
    // 帮助类;
    private MTConfigHelper    mConfigHelper;
    private MTGetOrPostHelper mGetOrPostHelper;
    private MTImgHelper       mImgHelper;
    private MTFileHelper mtFileHelper;
    //
    private Builder vBuilder;
    private MTSharedpreferenceHelper mSpHelper; // 首选项存储;

    private MTSQLiteHelper mSqLiteHelper;// 数据库的帮助类;
    private SQLiteDatabase mDB; // 数据库件;
    private String saveDir  =   Environment.getExternalStorageDirectory().getPath()+File.separator+"jyFile",
            saveFolder  =   "photo",
            folderPath,     //  文件夹路径;
            filePath,       //  文件路径;
            tmpPath,
            gsimg,      //  临时路径;
            inporttime, ctime, intime, pboxtime;
    Handler myHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mDialog.dismiss();
            switch (msg.what){
                case MTConfigHelper.NTAG_SUCCESS:
                    Toast.makeText(mContext, R.string.tip_success,Toast.LENGTH_SHORT).show();
                    mtFileHelper.fileDelAll();
                    break;
                //  02.失败;
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
        setContentView(R.layout.port);
        initView();
        initEvent();
    }
    private void initView(){
        mContext    =   PortActivity.this;
        mConfigHelper = new MTConfigHelper();
        mSqLiteHelper = new MTSQLiteHelper(mContext);
        mDB = mSqLiteHelper.getmDB();
        mSpHelper= new MTSharedpreferenceHelper(mContext, MTConfigHelper.CONFIG_SELF,
                mContext.MODE_APPEND);
        mGetOrPostHelper = new MTGetOrPostHelper();
        mImgHelper       = new MTImgHelper();
        //  文件的管理类对象;
        mtFileHelper     = new MTFileHelper();
        listfile         = mtFileHelper.getListfiles();
        tvTopic     =   (TextView) findViewById(R.id.tvTopic);
        mListView   =   (ListView) findViewById(R.id.lvResult);
        btnBack     =   (TextView) findViewById(R.id.btnBack);
        btnDetail   =   (TextView) findViewById(R.id.btnFunction);
        mState      =   (Spinner) findViewById(R.id.gstate);
        mGsimg      =   (Button) findViewById(R.id.btnPhoto);
        state2      = (TextView) findViewById(R.id.state2);
        btninporttime   =   (Button) findViewById(R.id.inporttime);
        btnctime        =   (Button) findViewById(R.id.ctime);
        btnintime       =   (Button) findViewById(R.id.intime);
        btnpboxtime =   (Button) findViewById(R.id.pboxtime);
        btnCode     =   (Button) findViewById(R.id.btnCode);
        btnSearch   =   (Button) findViewById(R.id.btnSearch);
        btnOk       =   (Button) findViewById(R.id.btnOk);
        splkind     =   (Spinner) findViewById(R.id.lkind);
        isLean      =   (RadioGroup) findViewById(R.id.isLean);
        etSearch    =   (EditText) findViewById(R.id.etSearch);
        list        =   new ArrayList<String>();
        truckorder  =   new Truckorder();
        trainorder  =   new Trainorder();
        ArrayAdapter adap = new ArrayAdapter<String>(this, R.layout.spinerlayout, new String[]{"运  输  方  式  ▼","汽   运", "铁   路"});
        splkind.setAdapter(adap);
        btnDetail.setText("历史");
        tvTopic.setText("港口");
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

    }
    private void initEvent(){
        mIntent     =   getIntent();
        mGetOrPostHelper=new MTGetOrPostHelper();
        folderPath  =   saveDir+File.separator+saveFolder+File.separator+bid+File.separator+"port";
        mImgHelper = new MTImgHelper();
        mFileHelper = new FileHelper();
        mGsimg.setOnClickListener(this);
        btnDetail.setOnClickListener(this);
        btninporttime.setOnClickListener(this);
        btnctime.setOnClickListener(this);
        btnintime.setOnClickListener(this);
        btnpboxtime.setOnClickListener(this);
        btnCode.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        splkind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long id) {
                Intent intent;
                switch (position) {
                    case 1:
                        intent=new Intent(PortActivity.this,PortActivity_Truck.class);
                        startActivityForResult(intent, 1);
                        break;
                    case 2:
                        intent=new Intent(PortActivity.this,PortActivity_Train.class);
                        startActivityForResult(intent, 2);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        mState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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
                        mBuilder    =   new AlertDialog.Builder(mContext);
                        mBuilder.setTitle("异常信息");
                        final EditText   edit   =   new EditText(mContext);
                        edit.setSingleLine(false);
                        edit.setLines(6);
                        mBuilder.setView(edit);
                        mBuilder.setPositiveButton(R.string.action_ok,new  DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                String tmp=edit.getText().toString().trim();
                                if(!tmp.equals("")){
                                    gstate=tmp;
                                    //state1.setBackgroundColor(Color.RED);
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
                //state1.setBackgroundColor(Color.GREEN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case MTConfigHelper.NTRACK_GGOODS_GID_TO:
                if(resultCode == MTConfigHelper.NTRACK_FLUSH_TO_MENU){
                    String gid = intent.getStringExtra("bid");
                    etSearch.setText(gid);
                }
            case MTConfigHelper.NTRACK_GGOODS_PHOTO_TO:
                if(resultCode==-1){
                    Toast.makeText(mContext, "拍照完成", Toast.LENGTH_SHORT).show();
                    mImgHelper.compressPicture(tmpPath, filePath);
                    mImgHelper.clearPicture(tmpPath, null);
                    sSize = String.valueOf(mFileHelper.getFileCount(folderPath));
                    state2.setText(sSize);
                }
                break;
            case 1:
                if(resultCode==RESULT_OK){
                    truckorder=(Truckorder) intent.getSerializableExtra("truckorder");
                }
                else {
                    splkind.setSelection(0);
                }
                break;
            case 2:
                if(resultCode==RESULT_OK){
                    trainorder= (Trainorder) intent.getSerializableExtra("trainorder");
                }
                else {
                    splkind.setSelection(0);
                }
            default:
                break;
        }
    }
    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btnPhoto:
                getPhoto_Ggoods();
                break;
            case R.id.inporttime:
            case R.id.ctime:
            case R.id.intime:
            case R.id.pboxtime:
                mBuilder = new AlertDialog.Builder(mContext);
                View view = getLayoutInflater().inflate(R.layout.activity_datatimepicker, null);
                mBuilder.setTitle("设置时间");
                mBuilder.setView(view);
                DatePicker datePicker = (DatePicker) view.findViewById(R.id.dpPicker);
                TimePicker timePicker = (TimePicker) view.findViewById(R.id.tpPicker);
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
                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay,
                                              int minute) {
                        String hour=hourOfDay<10?"0"+hourOfDay:""+hourOfDay;
                        String minutes=minute<10?"0"+minute:""+minute;
                        time = hour + "时" + minutes + "分";
                    }
                });
                mBuilder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        stime = date + time;
                        switch (v.getId()) {
                            case R.id.inporttime:
                            case R.id.ctime:
                            case R.id.intime:
                            case R.id.pboxtime:
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
            case R.id.btnCode:
                //  跳转至专门的intent控件;
                mIntent =   new Intent(mContext, FlushActivity.class);
                //  有返回值的跳转;
                startActivityForResult(mIntent,MTConfigHelper.NTRACK_GGOODS_GID_TO);
                break;
            case R.id.btnSearch:
                    // 进度条的内容;
                gid = etSearch.getText().toString().trim();
                InputMethodManager inputMethodManager =
                        (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(btnSearch.getWindowToken(), 0);
                final CharSequence strDialogTitle = getString(R.string.tip_dialog_wait);
                final CharSequence strDialogBody  = getString(R.string.tip_dialog_done);
                mDialog                           = ProgressDialog.show(mContext, strDialogTitle, strDialogBody,true);
                mThread=new LoadInfoThread();
                mThread.start();
                break;
            case R.id.btnFunction:
                mIntent=new Intent(mContext,PHistoryActivity.class);
                startActivity(mIntent);
                break;
            case R.id.btnOk:
                if(bid==null|gid==null){
                    Toast.makeText(this,"请进行搜索配对",Toast.LENGTH_LONG).show();
                    break;
                }
                if(splkind.getSelectedItemPosition()==0){
                    Toast.makeText(this,"请选择运输方式",Toast.LENGTH_LONG).show();
                    break;
                }
                try {
                    inporttime  = MTGetTextUtil.getText(btninporttime);
                    ctime       = MTGetTextUtil.getText(btnctime);
                    intime  = MTGetTextUtil.getText(btnintime);
                    pboxtime  = MTGetTextUtil.getText(btnpboxtime);
                    islean=isLean.getCheckedRadioButtonId()==R.id.lean?"1":"0";
                    gsimg = mFileHelper.getFileNamesByStrs(folderPath);
                    wid = mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
                    if (gsimg.equals("")) {
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
            default:
                break;
        }
    }
    public void getPhoto_Ggoods() {
        File file;
        if (mConfigHelper.getfState().equals(Environment.MEDIA_MOUNTED)) {
            if (bid != null && gid != null) {
                folderPath = mConfigHelper.getfParentPath() + bid
                        + File.separator + "port" + File.separator + gid;
                gsimg = bid + "port" + gid + "file"
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
    public void onClickBack(View view){

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
    public class LoadInfoThread extends Thread{
        private String url,
                param,response
                        ;
        @Override
        public void run() {
            url = "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":"+ MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM+ "/goods2";
            param     = "operType=2&gid=" + gid;
            response  = mGetOrPostHelper.sendGet(url, param);
            int nFlag = MTConfigHelper.NTAG_FAIL;
            JSONObject res;
            JSONObject body;
            if(!response.trim().equalsIgnoreCase("fail")) {
                nFlag = MTConfigHelper.NTAG_SUCCESS;
                try {
                    res = new JSONObject(response);
                    body = new JSONObject(res.getString("body"));
                } catch (JSONException e) {
                    res = null;
                    body = null;
                }
                if (body != null) {
                    try {
                        String busiinvcode = body.getString("busiinvcode");
                        String tradecode = body.getString("tradecode");
                        String wcode = body.getString("wcode");
                        String billoflading = body.getString("billoflading");
                        String shipcorm = body.getString("shipcorm");
                        String shipexparrivaldate = body.getString("shipexparrivaldate");
                        String cname = body.getString("cname");
                        String cid = body.getString("cid");
                        String goodsdesc = body.getString("goodsdesc");
                        String csize = body.getString("csize");
                        String ctype = body.getString("ctype");
                        String sealno = body.getString("sealno");
                        String pieces = body.getString("pieces");
                        String grossweight = body.getString("grossweight");
                        String grossweightjw = body.getString("grossweightjw");
                        String grossweighgn = body.getString("grossweighgn");
                        String volume = body.getString("volume");
                        String length = body.getString("length");
                        String width = body.getString("width");
                        String height = body.getString("height");
                        list.add("业务编号:" + busiinvcode);
                        list.add("业务类型编号:" + tradecode);
                        list.add("建单人:" + wcode);
                        list.add("提单号:" + billoflading);
                        list.add("船公司:" + shipcorm);
                        list.add("预计到港时间:" + shipexparrivaldate);
                        list.add("品名:" + cname);
                        list.add("箱号:" + cid);
                        list.add("包装类型:" + goodsdesc);
                        list.add("箱尺寸:" + csize);
                        list.add("箱型:" + ctype);
                        list.add("铅封号:" + sealno);
                        list.add("件数:" + pieces);
                        list.add("毛重量:" + grossweight);
                        list.add("毛重-境外(KGS):" + grossweightjw);
                        list.add("毛重-国内(KGS):" + grossweighgn);
                        list.add("体积（CBM）:" + volume);
                        list.add("长(CM):" + length);
                        list.add("宽(CM):" + width);
                        list.add("高(CM):" + height);

                    } catch (JSONException e) {
                        nFlag = MTConfigHelper.NTAG_FAIL;
                        Log.e("getdata", "run: ", e);
                    }
                }
            }
//            if (!response.equalsIgnoreCase("fail")) {
//                nFlag = MTConfigHelper.NTAG_SUCCESS;
//                try {
//                    JSONArray array = new JSONArray(response);
//                    int i = 0;
//                    JSONObject obj = null;
//                    do {
//                        try {
//                            // JsonObject的解析;
//                            obj            = array.getJSONObject(i);
//
//                            bid            = obj.getString("bid");
//                            String bname   = obj.getString("bname");
//                            String bkind   = obj.getString("bkind");
//                            String bcoman      = obj.getString("bcoman");
//                            String bgaddress = obj.getString("bgaddress");
//                            String bgoid   = obj.getString("bgoid");
//                            String bshipcom  = obj.getString("bshipcom");
//                            String bpretoportday = obj.getString("bpretoportday");
//                            String boxid   = obj.getString("boxid");
//                            String boxsize     = obj.getString("boxsize");
//                            String boxkind     = obj.getString("boxkind");
//                            String boxbelong = obj.getString("boxbelong");
//                            String retransway= obj.getString("retransway");
//
//                            gid            = obj.getString("gid");
//                            String gname   = obj.getString("gname");
//                            String leadnumber= obj.getString("leadnumber");
//                            String gcount      = obj.getString("gcount");
//                            String gunit   = obj.getString("gunit");
//                            String gtotalweight = obj.getString("gtotalweight");
//                            String glength     = obj.getString("glength");
//                            String gwidth      = obj.getString("gwidth");
//                            String gheight     = obj.getString("gheight");
//                            String gvolume     = obj.getString("gvolume");
//
//                            list.add("业务编号:" + bid);
//                            list.add("业务名称:" + bname);
//                            list.add("业务类型:" + bkind);
//                            list.add("建单人:" + bcoman);
//                            list.add("提货地址:" + bgaddress);
//                            list.add("提单号:" + bgoid);
//                            list.add("船舶公司:" + bshipcom);
//                            list.add("预计到港日:" + bpretoportday);
//                            list.add("箱号:" + boxid);
//                            list.add("箱尺寸:" + boxsize);
//                            list.add("箱型:" + boxkind);
//                            list.add("箱所属:" + boxbelong);
//                            list.add("回程运输方式:" + retransway);
//                            list.add("\r\n");
//                            list.add("货物编号:" + bid + "-" + gid);
//                            list.add("品名:" + gname);
//                            list.add("铅封号:" + leadnumber);
//                            list.add("件数:" + gcount);
//                            list.add("单位:" + gunit);
//                            list.add("总毛重:" + gtotalweight);
//                            list.add("长:" + glength);
//                            list.add("宽:" + gwidth);
//                            list.add("高:" + gheight);
//                            list.add("体积:" + gvolume);
//
//                            i++;
//                        } catch (Exception e) {
//                            obj = null;
//                        }
//                    } while (obj != null);
//                } catch (JSONException e) {
//                    nFlag = MTConfigHelper.NTAG_FAIL;
//                }
//            }
            myHandler.sendEmptyMessage(nFlag);

        }
    }

    public class UpLoadThread extends Thread{
        private String url,
                param,
                response,
                sql,
                tid,
                tkind,
                stime,
                pertcount,
                tcount,
                pertweight,
                classorderid,
                reporttime,
                oid,
                tformatweight,
                gtime,
                wid;
        public void run() {

            // 进行相应的登录操作的界面显示;
            //  01.Http 协议中的Get和Post方法;
            url = "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":"+ MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM+ "/port";
            //url        =  "http://172.23.24.155:"+"8080"+"/JYTest02/port";

                if(splkind.getSelectedItemPosition()==1){
                    lkind="汽运";
                    tid=truckorder.getTid();
                    tkind=truckorder.getTkind();
                    oid=truckorder.getLeadnumber();
                    stime=truckorder.getStime();
                    pertcount=Integer.toString(truckorder.getPertcount());
                    tcount=Integer.toString(truckorder.getTcount());
                    pertweight=Double.toString(truckorder.getPertweight());
                    classorderid="null";
                    reporttime="null";
                    tformatweight="0";
                    gtime=null;


                }
                else if(splkind.getSelectedItemPosition()==2){

                    lkind="铁路";
                    tid=trainorder.getTid();
                    tkind=trainorder.getTkind();
                    stime=trainorder.getStime();
                    pertcount=Integer.toString(trainorder.getPertcount());
                    tcount="0";
                    pertweight=Double.toString(trainorder.getPertweight());
                    classorderid=trainorder.getClassorderid();
                    reporttime=trainorder.getReportime();
                    oid=trainorder.getOid();
                    tformatweight=Double.toString(trainorder.getTformatweight());
                    gtime=null;
                }
                wid = mSpHelper.getValue(MTConfigHelper.CONFIG_SELF_WID);
            try {
                param    =  "operType=1&" +
                        "bid="+bid+"&" +
                        "gid="+gid+"&" +
                        "inporttime="+ URLEncoder.encode(inporttime,"utf-8")+"&"+
                        "ctime="+URLEncoder.encode(ctime,"utf-8")+"&"+
                        "intime="+URLEncoder.encode(intime,"utf-8")+"&"+
                        "pboxtime="+URLEncoder.encode(pboxtime,"utf-8")+"&"+
                        "islean="+islean+"&"+
                        "state="+URLEncoder.encode(gstate,"utf-8")+"&" +
                        "simg="+gsimg+"&" +
                        "lkind="+URLEncoder.encode(lkind,"utf-8")+"&" +
                        "reporttime="+URLEncoder.encode(reporttime,"utf-8")+"&"+
                        "classorderid="+classorderid+"&"+
                        "tid="+tid+"&"+
                        "tkind="+tkind+"&"+
                        "oid="+oid+"&"+
                        "percount="+pertcount+"&"+
                        "perweight="+pertweight+"&"+
                        "tformatweight="+tformatweight+"&"+
                        "tcount="+tcount+"&"+
                        "gtime="+gtime+"&"+
                        "stime="+URLEncoder.encode(stime,"utf-8")+"&"+
                        "wid="+wid
                ;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            response =  mGetOrPostHelper.sendGet(url,param);
            int nFlag=response.trim().equalsIgnoreCase("success")?MTConfigHelper.NTAG_SUCCESS:MTConfigHelper.NTAG_FAIL;
            if(nFlag== MTConfigHelper.NTAG_SUCCESS){
                sql="insert into portinfo(bid,gid,inporttime,ctime,intime, pboxtime,"
                        + "state,simg,lkind,reporttime,classorderid,tid,tkind,oid,gtime,stime,"
                        + " percount,tcount,islean,perweight,tformatweight) " +"values(" +
                        "'"+bid+"'," +
                        "'"+gid+"'," +
                        "'"+inporttime+"'," +
                        "'"+ctime+"'," +
                        "'"+intime+"'," +
                        "'"+pboxtime+"'," +
                        "'"+gstate+"'," +
                        "'"+gsimg+"'," +
                        "'"+lkind+"'," +
                        "'"+reporttime+"'," +
                        "'"+classorderid+"'," +
                        "'"+tid+"'," +
                        "'"+tkind+"'," +
                        "'"+oid+"'," +
                        "'"+gtime+"'," +
                        "'"+stime+"'," +
                        "'"+pertcount+"'," +
                        "'"+tcount+"'," +
                        "'"+islean+"'," +
                        "'"+pertweight+"'," +
                        "'"+tformatweight+"')";
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
        //  显示的列表和适配器绑定;
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
}
