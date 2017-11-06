package cn.com.jy.helper;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class SQLiteHelper {
	private SQLiteDatabase 		 	mDB;		//  数据库件;
    private DBHelper 	   		 	mDBhelper;	//  辅助控件;
	
	public SQLiteDatabase getmDB() {
		return mDB;
	}

	public void setmDB(SQLiteDatabase mDB) {
		this.mDB = mDB;
	}

	public SQLiteHelper(Context mContext) {
		File 	file 	=	mContext.getFilesDir();
		String 	path 	= 	file.getAbsolutePath() + "/"+ConfigHelper.SNAME_DB;
		mDBhelper  		= 	new DBHelper(mContext, path, ConfigHelper.NID_DB_VERSION);
		mDB 			= 	mDBhelper.getReadableDatabase();
	}
	
	public void doCloseDataBase(){
		if(mDB!=null){
			mDB.close();
			mDB=null;
		}
		if(mDBhelper!=null){
			mDBhelper.close();
			mDBhelper=null;
		}
	}
}
