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
    private TextView tvTopic, tvImgCount;
    private EditText etSearch;
    private String wid;
    private TextView btnDetail;
    private Button mGsimg, btnAdd,
            btnCode, btnSearch;
    private String bid, gid, gstate, sSize;
    private Intent mIntent;
    private ListView mListView;
    private Spinner mState;
    private ArrayAdapter<String> mAdapter;
    private Builder mBuilder;
    private Bundle mBundle;
    private ArrayList<MEFile> listfile;
    //  TODO 02.修改的相关内容;
    private LoadInfoThread mThread; // 线程内容;
    // 帮助类;
    private MTConfigHelper mConfigHelper;
    private MTGetOrPostHelper mGetOrPostHelper;
    private MTImgHelper mImgHelper;
    private MTFileHelper mtFileHelper;
    //
    private MTSharedpreferenceHelper mSpHelper; // 首选项存储;

    private MTSQLiteHelper mSqLiteHelper;// 数据库的帮助类;
    private SQLiteDatabase mDB; // 数据库件;
    private String saveDir = Environment.getExternalStorageDirectory()
            .getPath() + File.separator + "jyFile", saveFolder = "photo", folderPath, // 文件夹路径;
            filePath, // 文件路径;
            tmpPath, gsimg; // 临时路径;
    @SuppressLint("HandlerLeak")
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
        setContentView(R.layout.box);
        init();
    }

    @SuppressWarnings("static-access")
    private void init() {
        list = new ArrayList<String>();
        mContext = BoxActivity.this;
        mIntent = getIntent();
        mImgHelper = new MTImgHelper();
        //  文件的管理类对象;
        mtFileHelper = new MTFileHelper();
        mConfigHelper = new MTConfigHelper();
        mGetOrPostHelper = new MTGetOrPostHelper();
        //mFileHelper = new FileHelper();
        mSqLiteHelper = new MTSQLiteHelper(mContext);
        mDB = mSqLiteHelper.getmDB();
        mImgHelper = new MTImgHelper();
        listfile = mtFileHelper.getListfiles();
        mListView = (ListView) findViewById(R.id.lvResult);
        tvImgCount = (TextView) findViewById(R.id.tvImgCount);
        tvTopic = (TextView) findViewById(R.id.tvTopic);
        etSearch = (EditText) findViewById(R.id.etSearch);
        mState = (Spinner) findViewById(R.id.gstate);
        btnCode = (Button) findViewById(R.id.btnCode);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        mGsimg = (Button) findViewById(R.id.btnPhoto);
        btnDetail = (TextView) findViewById(R.id.btnFunction);
        btnAdd = (Button) findViewById(R.id.btnAdd);

        btnDetail.setText("历史");
        mSpHelper = new MTSharedpreferenceHelper(mContext, MTConfigHelper.CONFIG_SELF,
                mContext.MODE_APPEND);
        tvTopic.setText("箱管");
        mGsimg.setOnClickListener(this);
        btnCode.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnDetail.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                doResetParam();
                // 重新加载数据;
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
            }
        });
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
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
                mIntent = new Intent(mContext, BMHistoryActivity.class);
                startActivity(mIntent);
                break;
            case R.id.btnAdd:
                if (list.size() > 0) {
                    mIntent = new Intent(mContext, BoxAddActivity.class);
                    mBundle = new Bundle();
                    mBundle.putString("barcode", gid);
                    mBundle.putString("cargostatusbox", gstate);
                    mBundle.putString("busiinvcode", bid);
                    gsimg = mtFileHelper.getFileNamesByStrs(mtFileHelper.getListfiles(), "_");
                    if (gsimg.equals("")) gsimg = "0张";
                    mBundle.putString("imgs", gsimg);
                    mIntent.putExtras(mBundle);
                    startActivityForResult(mIntent, 1);
                } else Toast.makeText(mContext, "请先扫描一维/二维码", Toast.LENGTH_SHORT).show();

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
                Toast.makeText(mContext, "请先扫描一维/二维码", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, "sdcard无效或没有插入!", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public class LoadInfoThread extends Thread {
        private String url,
                param,
                response;

        @Override
        public void run() {
            url = "http://" + MTConfigHelper.TAG_IP_ADDRESS + ":" + MTConfigHelper.TAG_PORT + "/" + MTConfigHelper.TAG_PROGRAM + "/goods";
            param = "operType=3&barcode=" + gid;
            response = mGetOrPostHelper.sendGet(url, param);
            int nFlag = MTConfigHelper.NTAG_FAIL;
            JSONArray res;
            JSONObject body;
            if (!response.trim().equalsIgnoreCase("fail")) {
                nFlag = MTConfigHelper.NTAG_SUCCESS;
                try {
                    res = new JSONArray(response);
                    body = res.getJSONObject(0);

                } catch (JSONException e) {
                    res = null;
                    body = null;
                }
                if (body != null) {
                    try {
                        bid = body.getString("busiinvcode");
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

                        list.add("业务编号:" + bid);
                        list.add("提单号:" + billoflading);
                        list.add("箱号:" + cid);
                        list.add("箱尺寸:" + csize);
                        list.add("箱型:" + ctype);
                        list.add("箱属:" + cowner);
                        list.add("包装类型:" + goodsdesc);
                        list.add("回程运输方式:" + etransportationmode);
                        list.add("品名:" + cname);
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
        mState.setSelection(0);
        bid = null;
        gid = null;
    }

    private void showImgCount() {
        sSize = String.valueOf(mtFileHelper.getListfiles().size());
        tvImgCount.setText(sSize);
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

    @Override
    protected void onDestroy() {

        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        super.onDestroy();
    }
}
