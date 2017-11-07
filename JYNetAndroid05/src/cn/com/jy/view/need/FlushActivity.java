package cn.com.jy.view.need;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;


import cn.com.jy.activity.R;
import cn.com.jy.camera.CameraManager;
import cn.com.jy.decoding.CaptureActivityHandler;
import cn.com.jy.decoding.InactivityTimer;
import cn.com.jy.decoding.RGBLuminanceSource;
import cn.com.jy.model.helper.MTConfigHelper;
import cn.com.jy.view.extra.ViewfinderView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;



public class FlushActivity extends Activity implements Callback {

	private ViewfinderView 		 viewfinderView;//自定义的View绘图控件;
	private boolean 			   hasSurface;	//判断surface的标志位;	
	private CaptureActivityHandler handler;		//绘制抓图的handler控件;
	private Vector<BarcodeFormat> decodeFormats;//编码统一字符集;
	private String 				  characterSet;	//编码集合;
	private InactivityTimer 	inactivityTimer;//activity计时跳转结束;
	private MediaPlayer 		  mediaPlayer;	//多媒体控件;
	private Intent				  	myIntent;	//跳转的intent;
	private boolean 				playBeep;	//扫描时发出的嘟嘟声;
	@SuppressWarnings("unused")
	private static final long VIBRATE_DURATION = 200L;
	private static final float BEEP_VOLUME = 0.10f;
	private Bitmap 					scanBitmap;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flush);
		initView();
	}
	
	public void initView(){
		//	镜头捕捉控件;
		CameraManager.init(getApplication());
		viewfinderView  = (ViewfinderView) findViewById(R.id.viewfinder_view);
		hasSurface 	    = false;
		inactivityTimer = new InactivityTimer(this);
	}
	//	退出
	public void onClick_Back(View view){
		FlushActivity.this.finish();
	}

	private void onResultHandler(String resultString, Bitmap bitmap){
		if(TextUtils.isEmpty(resultString)){
			Toast.makeText(FlushActivity.this, R.string.tip_fail, Toast.LENGTH_SHORT).show();
			return;
		}

		myIntent=new Intent();
		myIntent.putExtra("bid", resultString);
		FlushActivity.this.setResult(MTConfigHelper.NTRACK_FLUSH_TO_MENU,myIntent);
		FlushActivity.this.finish();
	}

	public Result scanningImage(String path) {
		if(TextUtils.isEmpty(path)){
			return null;
		}
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); 

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; 
		scanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false;
		int sampleSize = (int) (options.outHeight / (float) 200);
		if (sampleSize <= 0)
			sampleSize = 1;
		options.inSampleSize = sampleSize;
		scanBitmap = BitmapFactory.decodeFile(path, options);
		RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader  reader = new QRCodeReader();
		try {
			return reader.decode(bitmap1, hints);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}
	
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		String resultString = result.getText();
		if (resultString.equals("")) {
			Toast.makeText(FlushActivity.this, R.string.tip_null, Toast.LENGTH_SHORT).show();
		}else {
			onResultHandler(resultString, barcode);
		}
		FlushActivity.this.finish();
	}
	
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,characterSet);
		}
	}



	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
	
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
	
	}
}