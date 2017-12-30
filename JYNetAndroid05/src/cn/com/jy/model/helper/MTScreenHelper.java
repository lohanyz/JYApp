package cn.com.jy.model.helper;

import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

public class MTScreenHelper {
	private Display mDisplay; 
	private int screenWidth;
	private int screenHeight;
	
	@SuppressWarnings("deprecation")
	public MTScreenHelper(Display mDisplay,Window mWindow) {
		this.mDisplay=mDisplay;
		this.screenWidth=this.mDisplay.getWidth();
		this.screenHeight=this.mDisplay.getHeight();
	}
	public int getScreenWidth() {
		return screenWidth;
	}
	
	public int getScreenHeight() {
		return screenHeight;
	}

	public LayoutParams setViewSize(View view,int width,int heigth){
		LayoutParams layoutParams=null;
		layoutParams=(LayoutParams) view.getLayoutParams();
		layoutParams.width=width;
		layoutParams.height=heigth;
		return layoutParams;
	}
}
