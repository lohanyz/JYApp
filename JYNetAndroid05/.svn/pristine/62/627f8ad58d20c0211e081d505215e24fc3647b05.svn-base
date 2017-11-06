package cn.com.jy.activity;


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

import cn.com.jy.entity.Trainorder;
import cn.com.jy.helper.GetTextUtil;

public class PortActivity_Train extends Activity implements OnClickListener,TextView.OnEditorActionListener,View.OnTouchListener{
	private Button btnsubmit,btncancle;
	private EditText classorderid,	//班列号
					reportime,		//报数时间
					tid,			//车号
					tkind,			//车型
					oid,			//运输号
					pertweight,		//单车吨数
					tformatweight,	//车皮标重
					pertcount;		//单车件数
	private Button stime;			//发时间
	private String date,time,stimea;
	private Trainorder trainOrder;
	private AlertDialog.Builder mBuilder;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.port_train);
		init();
	}
	protected void init() {
		trainOrder=new Trainorder();
		btnsubmit=(Button) findViewById(R.id.submit);
		btncancle=(Button) findViewById(R.id.cancle);
		classorderid=(EditText) findViewById(R.id.classorderid);
		reportime=(EditText) findViewById(R.id.reportime);
		tid=(EditText) findViewById(R.id.tid);
		tkind=(EditText) findViewById(R.id.tkind);
		oid=(EditText) findViewById(R.id.oid);
		pertweight=(EditText) findViewById(R.id.pertweight);
		tformatweight=(EditText) findViewById(R.id.tformatweight);
		stime=(Button) findViewById(R.id.stime);
		pertcount=(EditText) findViewById(R.id.pertcount);
		classorderid.setOnEditorActionListener(this);
		reportime.setOnEditorActionListener(this);
		tid.setOnEditorActionListener(this);
		tkind.setOnEditorActionListener(this);
		oid.setOnEditorActionListener(this);
		pertweight.setOnEditorActionListener(this);
		tformatweight.setOnEditorActionListener(this);
		pertcount.setOnEditorActionListener(this);
		stime.setOnClickListener(this);
		btnsubmit.setOnClickListener(this);
		btncancle.setOnClickListener(this);

		classorderid.setOnTouchListener(this);
		reportime.setOnTouchListener(this);
		tid.setOnTouchListener(this);
		tkind.setOnTouchListener(this);
		oid.setOnTouchListener(this);
		pertweight.setOnTouchListener(this);
		tformatweight.setOnTouchListener(this);
		pertcount.setOnTouchListener(this);

	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.stime:
				mBuilder = new AlertDialog.Builder(PortActivity_Train.this);
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
			//需加入输入判定，否则不能跳转
            try {
                trainOrder.setClassorderid(GetTextUtil.getText(classorderid));
                trainOrder.setOid(GetTextUtil.getText(oid));
                trainOrder.setPertcount(Integer.parseInt(GetTextUtil.getText(pertcount)));
                trainOrder.setPertweight(Double.valueOf(GetTextUtil.getText(pertweight)));
                trainOrder.setReportime(GetTextUtil.getText(reportime));
                trainOrder.setStime(GetTextUtil.getText(stime));
                trainOrder.setTformatweight(Double.valueOf(GetTextUtil.getText(tformatweight)));
                trainOrder.setTid(GetTextUtil.getText(tid));
                trainOrder.setTkind(GetTextUtil.getText(tkind));
                Intent intent=new Intent();
                Bundle bundle=new Bundle();
                bundle.putSerializable("trainorder", trainOrder);
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
