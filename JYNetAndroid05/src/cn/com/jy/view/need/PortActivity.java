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

public class PortActivity extends Activity implements View.OnClickListener {
    private Context mContext;
    private TextView tvTopic, btnDetail, btnBack, state2;
    private ProgressDialog mDialog;

    private Button mGsimg, btnCode, btnSearch;
    private EditText etSearch;
    private Spinner mState;
    private Spinner splkind;
    private String gstate, date, time, stime, sSize, wid, bid, gid;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private Intent mIntent;
    private AlertDialog.Builder mBuilder;
    private ArrayList<String> list;
    private ArrayList<MEFile> listfile;

    //  TODO 02.修改的相关内容;
    private Bundle mBundle;
    private LoadInfoThread mThread; // 线程内容;
    // 帮助类;
    private MTConfigHelper mConfigHelper;
    private MTGetOrPostHelper mGetOrPostHelper;
    private MTImgHelper mImgHelper;
    private MTFileHelper mtFileHelper;
    //
    private Builder vBuilder;
    private MTSharedpreferenceHelper mSpHelper; // 首选项存储;

    private MTSQLiteHelper mSqLiteHelper;// 数据库的帮助类;
    private SQLiteDatabase mDB; // 数据库件;
    private String saveDir = Environment.getExternalStorageDirectory().getPath() + File.separator + "jyFile",
            saveFolder = "photo",
            folderPath,     //  文件夹路径;
            filePath,       //  文件路径;
            tmpPath,
            gsimg;
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mDialog.dismiss();
            switch (msg.what) {
                case MTConfigHelper.NTAG_SUCCESS:
                    Toast.makeText(mContext, R.string.tip_success, Toast.LENGTH_SHORT).show();
                    mtFileHelper.fileDelAll();
                    break;
                //  02.失败;
                case MTConfigHelper.NTAG_FAIL:
                    Toast.makeText(mContext, R.string.tip_fail, Toast.LENGTH_LONG).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        splkind.setSelection(0);
    }

    private void initView() {
        mContext = PortActivity.this;
        mConfigHelper = new MTConfigHelper();
        mSqLiteHelper = new MTSQLiteHelper(mContext);
        mDB = mSqLiteHelper.getmDB();
        mSpHelper = new MTSharedpreferenceHelper(mContext, MTConfigHelper.CONFIG_SELF,
                mContext.MODE_APPEND);
        mGetOrPostHelper = new MTGetOrPostHelper();
        mImgHelper = new MTImgHelper();
        //  文件的管理类对象;
        mtFileHelper = new MTFileHelper();
        listfile = mtFileHelper.getListfiles();
        tvTopic = (TextView) findViewById(R.id.tvTopic);
        mListView = (ListView) findViewById(R.id.lvResult);
        btnBack = (TextView) findViewById(R.id.btnBack);
        btnDetail = (TextView) findViewById(R.id.btnFunction);
        mState = (Spinner) findViewById(R.id.gstate);
        mGsimg = (Button) findViewById(R.id.btnPhoto);
        state2 = (TextView) findViewById(R.id.state2);
        btnCode = (Button) findViewById(R.id.btnCode);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        splkind = (Spinner) findViewById(R.id.lkind);
        etSearch = (EditText) findViewById(R.id.etSearch);
        list = new ArrayList<String>();
        ArrayAdapter adap = new ArrayAdapter<String>(this, R.layout.spinerlayout, new String[]{"运  输  方  式  ▼", "汽   运", "铁   路"});
        splkind.setAdapter(adap);
        btnDetail.setText("历史");
        tvTopic.setText("港口");
        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                doResetParam();
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

    private void initEvent() {
        mIntent = getIntent();
        mGetOrPostHelper = new MTGetOrPostHelper();
        mImgHelper = new MTImgHelper();
        mtFileHelper = new MTFileHelper();
        mGsimg.setOnClickListener(this);
        btnDetail.setOnClickListener(this);
        btnCode.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        splkind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long id) {
                if (list.size() > 0) {
                    mIntent = new Intent(PortActivity.this, PortAddActivity.class);
                    mBundle = new Bundle();
                    mBundle.putString("barcode", gid);
                    mBundle.putString("cargostatusport", gstate);
                    mBundle.putString("busiinvcode", bid);
                    gsimg = mtFileHelper.getFileNamesByStrs(mtFileHelper.getListfiles(), "_");
                    if (gsimg.equals("")) gsimg = "0张";
                    mBundle.putString("imgs", gsimg);

                    switch (position) {
                        case 1:
                            mBundle.putString("slkind", "truck");
                            mIntent.putExtras(mBundle);
                            startActivityForResult(mIntent, 1);
                            break;
                        case 2:
                            mBundle.putString("slkind", "train");
                            mIntent.putExtras(mBundle);
                            startActivityForResult(mIntent, 1);
                            break;
                        default:
                            break;
                    }


                } else if (position != 0) {
                    Toast.makeText(mContext, "请先扫描一维/二维码", Toast.LENGTH_SHORT).show();
                    splkind.setSelection(0);
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
                                        String tmp = edit.getText().toString().trim();
                                        if (!tmp.equals("")) {
                                            gstate = tmp;
                                        }
                                    }
                                });
                        mBuilder.setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                gstate = "正常";
                                mState.setSelection(0);
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
                //state1.setBackgroundColor(Color.GREEN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == MTConfigHelper.NTRACK_GGOODS_GID_TO
                && resultCode == MTConfigHelper.NTRACK_FLUSH_TO_MENU) {
            String gid = intent.getStringExtra("bid");
            etSearch.setText(gid);
        } else if (requestCode == MTConfigHelper.NTRACK_GGOODS_PHOTO_TO
                && resultCode == -1) {
            Toast.makeText(mContext, "拍照完成", Toast.LENGTH_SHORT).show();
            mImgHelper.compressPicture(tmpPath, filePath);
            mImgHelper.clearPicture(tmpPath, null);
            //  进行文件内容的叠加;
            MEFile meFile = new MEFile(gsimg, filePath);
            //  将拍照操作放入列表;
            // TODO 修改的内容;
            mtFileHelper.fileAdd(meFile);
            showImgCount();

        } else if (requestCode == 1) {
            if (resultCode == 1) {
                doResetParam();
            }
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btnPhoto:
                getPhoto_Ggoods();
                break;
            case R.id.btnCode:
                //  跳转至专门的intent控件;
                mIntent = new Intent(mContext, FlushActivity.class);
                //  有返回值的跳转;
                startActivityForResult(mIntent, MTConfigHelper.NTRACK_GGOODS_GID_TO);
                break;
            case R.id.btnSearch:
                // 进度条的内容;
                splkind.setSelection(0);
                if (mThread == null) {
                    int nSize = list.size();
                    if (nSize != 0) {
                        list.clear();
                    }
                    InputMethodManager inputMethodManager =
                            (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(btnSearch.getWindowToken(), 0);
                    // 进度条的内容;
                    final CharSequence strDialogTitle = getString(R.string.tip_dialog_wait);
                    final CharSequence strDialogBody = getString(R.string.tip_dialog_done);
                    mDialog = ProgressDialog.show(mContext, strDialogTitle, strDialogBody, true);
                    gid = etSearch.getText().toString().trim();
                    mThread = new LoadInfoThread();
                    mThread.start();
                }
                break;
            case R.id.btnFunction:
                mIntent = new Intent(mContext, PHistoryActivity.class);
                startActivity(mIntent);
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
                Log.e("port", tmpPath + ":" + filePath);
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
                Toast.makeText(mContext, "请先扫描一维/二维码", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, "sdcard无效或没有插入!", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void onClickBack(View view) {

        int n = listfile.size();
        if (n != 0) {
            for (MEFile item : listfile) {
                String path = item.getPath();
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        finish();
    }

    public class LoadInfoThread extends Thread {
        private String url,
                param, response;

        @Override
        public void run() {
            url = "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":" + MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM + "/goods";
            param = "operType=2&barcode=" + gid;
            response = mGetOrPostHelper.sendGet(url, param);
            int nFlag = MTConfigHelper.NTAG_FAIL;
            JSONArray res;
            JSONObject body;
            if (!response.trim().equalsIgnoreCase("fail")) {
                nFlag = MTConfigHelper.NTAG_SUCCESS;
                try {
                    Log.e("response", response);
                    res = new JSONArray(response);
                    body = res.getJSONObject(0);
                } catch (JSONException e) {
                    res = null;
                    body = null;
                }
                if (body != null) {
                    try {
                        bid = body.getString("busiinvcode");
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

                        list.add("业务编号:" + bid);
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

            myHandler.sendEmptyMessage(nFlag);

        }
    }


    private void doResetParam() {
        // 数据列表;
        list.clear();
        // 重新加载数据;
        showImgCount();
        showData();
        // 异常按钮重置;
        gstate = "正常";
        splkind.setSelection(0);
        mState.setSelection(0);
        bid = null;
        gid = null;
    }

    private void showImgCount() {
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
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }
}
