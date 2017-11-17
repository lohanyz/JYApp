package cn.com.jy.model.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class MTConfigHelper {
	
	//	基本属性的内容;
	public static String   		CONFIG_SELF="self_information";
	public static String   		CONFIG_BUSINESS_ID="bid";
	public static String   		CONFIG_SELF_WID  ="wid",
								CONFIG_SELF_WNAME="wname",
								CONFIG_SELF_WCALL="wcall",
								CONFIG_SELF_WPWD ="wpwd",
								CONFIG_SELF_WNOTE="wnote";
	
	public static final String  SPACE="";
	public static final	int 	NTAG_SUCCESS=1,NTAG_FAIL=2;
	//	跳转轨迹;
	public static final int		NTRACK_TRACK_TO=20;
	public static final int		NTRACK_GGOODS_GID_TO=20;
	public static final int		NTRACK_GGOODS_OID_TO=21;
	public static final int		NTRACK_GGOODS_PHOTO_TO=22;
	
	public static final int		NTRACK_SIGN_GID_TO=24;
	public static final int		NTRACK_SIGN_PHOTO_TO=25;
	
	public static final int		NTRACK_GGOODS_CHECK_TO=23;
	public static final int		NTRACK_RESIGN_CHECK_TO=40;
	public static final int		NTRACK_OIL_TO=30;
	public static final int		NTRACK_FLUSH_TO_MENU=11;
	public static final int		NTRACK_CARMA_TO_OIL=31;
	//	数据库管理;
	public static final int 	NID_DB_VERSION	 =	3;
	public static final String 	SNAME_DB	  	 =	"myDB.db";
//	public static final String  TAG_IP_ADDRESS	 =	"39.106.70.111";
	public static final String  TAG_IP_ADDRESS	 =	"172.23.123.109";
	public static final int	    TAG_PORT		 =	8888;
	public static final String  TAG_PROGRAM		 =	"JYTest01";
	public static final int	    TAG_COUNT_TIMEOUT=	6000;
	private int   screenWidth;
	
	private int   screenHeigth;
	private String saveDir 		= 	Environment.getExternalStorageDirectory().getPath()+File.separator+"jyFile",
				   saveFolder	=	"photo",
				   fParentPath,
				   fState
				   ;
	//	配置信息的构造方法;
	public MTConfigHelper() {
		this.fParentPath=saveDir+File.separator+saveFolder+File.separator;
		this.fState=Environment.getExternalStorageState();
	}
	
	//	获得现有时间;
	public String getCurrentTime(String sFormat){
		SimpleDateFormat    formatter    =   new    SimpleDateFormat    (sFormat);       
		Date    	curDate  =   new    Date(System.currentTimeMillis());//获取当前时间       
		return formatter.format(curDate); 
	}

	@SuppressWarnings("deprecation")
	public void doSetScreenWidthAndHeigth(Context context){
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		screenWidth= wm.getDefaultDisplay().getWidth();
		screenHeigth = wm.getDefaultDisplay().getHeight();
	}
	public int getScreenWidth() {
		return screenWidth;
	}

	public int getScreenHeigth() {
		return screenHeigth;
	}
	public String getfParentPath() {
		return fParentPath;
	}

	public String getfState() {
		return fState;
	}

	public void setfState(String fState) {
		this.fState = fState;
	}	
	
	public String getCurrentDate(String format){
		SimpleDateFormat df = new SimpleDateFormat(format);//设置日期格式
		long l=System.currentTimeMillis();
		return df.format(l);
	}
	public String setDataFormat(EditText et){
		String data="0";
		try {			
			data=et.getText().toString().trim();
			if(data.equals("")){
				return "0";
			}
		} catch (Exception e) {
			return "0";
		}
		return data;
	}

	public String setStringFormat(EditText et) {
		String string="未填";
		try {
			string=et.getText().toString().trim();
			if(string.equals("")){
				return "未填";
			}
		} catch (Exception e) {
			return "未填";
		}
		if (string.equals(""))
			return "未填";
		return string;
	}
	
	public String setTimeFormat(Button btn){
		String time=null;
		try {
			time=btn.getText().toString().trim();
			if(!time.contains("年")){
				return "未填";	
			}
		} catch (Exception e) {
			return "未填";
		}
		return time;
	}
}
