package cn.com.jy.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;


//	首选项的相应内容操作
@SuppressLint("CommitPrefEdits")
public class SPHelper {
	private SharedPreferences sPreferences;
	private SharedPreferences.Editor editor;
	private Context mContext;
	public SPHelper(Context context,String fName,int nMode){
		mContext     = context;
		sPreferences = mContext.getSharedPreferences(fName, nMode);
		editor = sPreferences.edit();
	}
	
	public void putValue(String key, String value){
		editor.putString(key, value);
		editor.commit();
	}

	public String getValue(String key){
		return sPreferences.getString(key, null);
	}
}
