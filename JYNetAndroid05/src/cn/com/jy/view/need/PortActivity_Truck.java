package cn.com.jy.view.need;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import cn.com.jy.activity.R;
import cn.com.jy.model.entity.Truckorder;
import cn.com.jy.model.helper.MTGetTextUtil;

public class PortActivity_Truck extends Activity implements OnClickListener ,TextView.OnEditorActionListener,View.OnTouchListener{
	private Button btnsubmit,btncancle,stime;
	private AlertDialog.Builder mBuilder;
	private EditText tid,
					tkind,
					leadnumber,
					pertcount,
					pertweight,
					tcount;
	private  String time,date,stimea;
	private Truckorder truckorder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.port_truck);
		init();
	}
	private void init(){
		tid=(EditText)findViewById(R.id.tid);
		tkind=(EditText)findViewById(R.id.tkind);
		leadnumber=(EditText)findViewById(R.id.leadnumber);
		pertcount=(EditText)findViewById(R.id.pertcount);
		pertweight=(EditText)findViewById(R.id.pertweight);
		tcount=(EditText)findViewById(R.id.tcount);
		stime=(Button)findViewById(R.id.stime);
		tid.setOnEditorActionListener(this);
		tkind.setOnEditorActionListener(this);
		leadnumber.setOnEditorActionListener(this);
		pertcount.setOnEditorActionListener(this);
		pertweight.setOnEditorActionListener(this);
		tcount.setOnEditorActionListener(this);
		stime.setOnClickListener(this);
		btnsubmit=(Button) findViewById(R.id.submit);
		btncancle=(Button) findViewById(R.id.cancle);
		truckorder=new Truckorder();
		btnsubmit.setOnClickListener(this);
		btncancle.setOnClickListener(this);

		tid.setOnTouchListener(this);
		tkind.setOnTouchListener(this);
		leadnumber.setOnTouchListener(this);
		pertcount.setOnTouchListener(this);
		pertweight.setOnTouchListener(this);
		tcount.setOnTouchListener(this);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.stime:
			mBuilder = new AlertDialog.Builder(PortActivity_Truck.this);
			View view2 = getLayoutInflater().inflate(
					R.layout.activity_datatimepicker, null);
			mBuilder.setTitle("设置时间");
			mBuilder.setView(view2);
			DatePicker datePicker = (DatePicker) view2
					.findViewById(R.id.dpPicker);
			TimePicker timePicker = (TimePicker) view2
					.findViewById(R.id.tpPicker);
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
							stimea = date + time;
							stime.setTextColor(Color.parseColor("#000000"));
							stime.setGravity(Gravity.CENTER);
							stime.setText(stimea);

						}
					});
			mBuilder.setNegativeButton(R.string.action_no, null);
			mBuilder.create();
			mBuilder.show();
			break;
		case R.id.submit:
			try {
			truckorder.setLeadnumber(MTGetTextUtil.getText(leadnumber));
			truckorder.setPertcount(Integer.parseInt(MTGetTextUtil.getText(pertcount)));
			truckorder.setPertweight(Double.parseDouble(MTGetTextUtil.getText(pertweight)));
			truckorder.setStime(MTGetTextUtil.getText(stime));
			truckorder.setTcount(Integer.parseInt(MTGetTextUtil.getText(tcount)));
			truckorder.setTid(MTGetTextUtil.getText(tid));
			truckorder.setTkind(MTGetTextUtil.getText(tkind));
			Intent intent=new Intent();
			Bundle bundle=new Bundle();
			bundle.putSerializable("truckorder", truckorder);
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
			}
			catch (Exception e){
			Toast.makeText(this,"请将内容填写完整",Toast.LENGTH_LONG).show();
			}
			break;


		case R.id.cancle:
			setResult(RESULT_CANCELED);
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		((EditText)findViewById(v.getId())).setTextColor(Color.BLACK);
		((EditText)findViewById(v.getId())).setGravity(Gravity.CENTER);
		return false;
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		((EditText)findViewById(v.getId())).setTextColor(Color.BLACK);
		((EditText)findViewById(v.getId())).setGravity(Gravity.CENTER);
		return false;
	}
}
