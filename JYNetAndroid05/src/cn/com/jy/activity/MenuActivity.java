package cn.com.jy.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.jy.helper.ConfigHelper;
import cn.com.jy.helper.SPHelper;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MenuActivity extends Activity implements OnClickListener{
	private TextView tvTopic;    // 标题窗口;
	private TextView btnBack,    // 返回按钮;
					 btnFunction;// 功能按钮;
	private SPHelper mSpHelper;  // 首选项存储;
	private Context  mContext;
	private Intent	 mIntent;
	//	列表按钮;
	private int[] nImages={R.drawable.tihuo2,R.drawable.gangkou2,R.drawable.xiangguan2,R.drawable.kouan2,R.drawable.qianshou2};
	private String[] sNames={"提  货","港  口","箱  管","口  岸","签  收"};
	private ListView mListView;
	private List<Map<String, Object>> list;
	private SimpleAdapter mSimpleAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		initView();
		initEvent();
	}
	//	控件声明;
	private void initView(){
		//	列表按钮;
		mListView=(ListView) findViewById(R.id.listView);
		//
		tvTopic  =(TextView) findViewById(R.id.tvTopic);
		btnBack  =(TextView) findViewById(R.id.btnBack);
		btnFunction= (TextView) findViewById(R.id.btnFunction);
	}
	//	事件声明;
	private void initEvent(){
		mContext   = MenuActivity.this;
		//	返回键按钮;
		btnBack.setOnClickListener(this);
		//	功能按钮舍弃;
		btnFunction.setVisibility(View.GONE);
		tvTopic.setText("");
		//	内容信息初始化操作;
		mSpHelper  = new SPHelper(this, ConfigHelper.CONFIG_SELF,MODE_APPEND);
		list=doLoad();
		mSimpleAdapter=new SimpleAdapter(mContext, list, R.layout.item_menu,new String[] {"name","img"},new int[]{R.id.name,R.id.iv} );
		mListView.setAdapter(mSimpleAdapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long id) {
					switch (position) {
					case 0:
						mIntent=new Intent(mContext, GgoodsActivity.class);
						break;

					case 1:
						mIntent=new Intent(mContext,PortActivity.class);
						break;
					case 2:
						mIntent=new Intent(mContext,BoxActivity.class);
						break;
						
					case 3:
						mIntent=new Intent(mContext,HarborActivity.class);
						break;
					case 4:
						mIntent=new Intent(mContext, SignActivity.class);
						break;
					default:
						break;
					}
					startActivity(mIntent);
				
			}
		});
		
	}
	private List<Map<String, Object>> doLoad(){
		List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
		for(int i=0;i<5;i++){
			Map<String, Object> map=new HashMap<String, Object>();
			map.put("name", sNames[i]);
			map.put("img", nImages[i]);
			list.add(map);
		}
		return list;
	}
	@Override
	public void onClick(View view) {
		int nVid=view.getId();
		switch (nVid) {
		//	退出信息操作;
		case R.id.btnBack:
			Builder builder=new Builder(this);
			builder.setTitle("提示");
			builder.setMessage("退出登录?");
			builder.create();
			builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					mSpHelper.putValue(ConfigHelper.CONFIG_SELF_WID,null);
					mSpHelper.putValue(ConfigHelper.CONFIG_SELF_WNAME,null);
					mSpHelper.putValue(ConfigHelper.CONFIG_SELF_WCALL,null);
					mSpHelper.putValue(ConfigHelper.CONFIG_SELF_WPWD,null);
					mSpHelper.putValue(ConfigHelper.CONFIG_SELF_WNOTE,null);
					Toast.makeText(mContext, R.string.result_exit, Toast.LENGTH_SHORT).show();				
					finish();
				}
			});
			
			builder.setNegativeButton(R.string.action_no, null);
			builder.show();
			break;
		default:
			break;
		}
	}
}
