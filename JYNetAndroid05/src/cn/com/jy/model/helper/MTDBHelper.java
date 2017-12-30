package cn.com.jy.model.helper;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//	数据库的管理帮助类;
public class MTDBHelper extends SQLiteOpenHelper {
	//	业务表的结构;
	private String sql_businessinfo = 
			"create table businessinfo (" +
			"bid varchar(20) primary key not null," +
			"bname varchar(20)," +
			"bkind varchar(20)," +
			"bcoman varchar(20)," +
			"bgaddress varchar(500)," +
			"bgoid varchar(20)," +
			"bshipcom varchar(20)," +
			"bpretoportday varchar(50)," +
			"boxid varchar(50)," +
			"boxsize varchar(50)," +
			"boxkind varchar(50)," +
			"boxbelong varchar(100)," +
			"retransway varchar(20))";
	//	货物表的结构;
	private String sql_goodsinfo=
			"create table goodsinfo (" +
			"gid varchar(20) primary key not null," +
			"bid varchar(20) not null," +
			"gname varchar(100) not null," +
			"boxid varchar(20) not null," +
			"boxsize varchar(100) not null," +
			"boxkind varchar(100) not null," +
			"leadnumber varchar(20) not null," +
			"gcount integer," +
			"gunit varchar(20)," +
			"gtotalweight double," +
			"glength double," +
			"gwidth double," +
			"gheight double," +
			"gvolume double," +
			"gstate integer" +
			")";	

//	//	提货信息表;
//	private String sql_getgoodsinfo=
//			"create table getgoodsinfo (" +
//			"ggid integer primary key ," +
//			"gid varchar(20)," +
//			"bid varchar(20)," +
//			"gstate varchar(500)," +
//			"gsimg varchar(500)," +
//			"lkind varchar(20)," +
//			"tid varchar(500)," +
//			"tkind varchar(100)," +
//			"oid varchar(100)," +
//			"percount integer," +
//			"perweight double," +
//			"tformatweight double," +
//			"tcount integer," +
//			"gtime varchar(20)," +
//			"stime varchar(20)" +
//			")";
	
	private String sql_getgoodsinfo=
	"create table getgoodsinfo ( " +
	"_id integer primary key autoincrement," + 		//	id编号;
	"barcode 	 varchar(32) not null," +		//	二维码信息;
	"dttrailerno varchar(32) not null," +		//	拖车(取)拖车号(国内信息)
	
	"sealno 	 varchar(32) not null," +		//	铅封号(货物信息)
	
	"dtsingletrailernum	 varchar(32) not null," +		//	拖车(取)单车件数
	"dtsingletrailerton	 varchar(32) not null,"+	    //	拖车(取)单车吨数
	"svehiclescoll	 	 varchar(32) not null,"+	    //	车数(取)(仓储)
	
	"dtpickupdate		 varchar(32) not null,"+		//	拖车(取)提货时间(国内时间)
	"dtstartdate	 	 varchar(32) not null,"+		//	拖车(取)发车时间(国内时间)
	"dgtrainwagonno	 	 varchar(32) not null,"+		//	铁路车皮号(国内信息)
	
	"dgtraintype 	 	 varchar(32) not null,"+		//	铁路车型(国内信息)
	"dgtrainwaybillno 	 varchar(32) not null,"+		//	铁路运单号(国内信息)
	
	"dgtrainsinglenum 	 varchar(32) not null,"+		//	铁路单车件数(国内信息)
	"cargostatuscenter   varchar(500) not null,"+		//	货物状态
	"dgtrainsingleton	 varchar(32) not null,"+		//	铁路单车吨数
	"dgtrainwagonkg	 	 varchar(32) not null,"+		//	铁路车皮标重
	"dloadingtime	 	 varchar(32) not null,"+		//	装车时间(调度)
	
	"dgtrainstartdate 	 varchar(32) not null,"+		//	铁路发运日
	"dgtrailerno	 	 varchar(32) not null,"+		//	拖车送拖车号(国内信息)
	"dtrailermodelsdely	 varchar(32) not null,"+		//	拖车车型(送)(调度)
	"dgsingletrailernum	 varchar(32) not null,"+		//	拖车(送)单车件数(国内信息)
	"dgsingletrailerton	 varchar(32) not null,"+		//	拖车(送)单车吨数(国内信息)
	"svehiclesdely	 	 varchar(32) not null,"+		//	车数(送)(仓储)
	
	"dgstartdate	 	 varchar(32) not null,"+		//	拖车(送)发车时间(国内信息)
	"img 	 			 varchar(1000) not null," +		//	图片
	"busiinvcode		 varchar(32) not null)";		
	
	
	//	港口信息的表;
	private String sql_portinfo=
			"create table portinfo (" +
					"_id integer primary key autoincrement," +
					"barcode 	 varchar(32) not null," +		//	二维码信息;
					"ptoportdate 	 varchar(32) ,"+
					"preinvoicedate_port 	 varchar(32) ,"+
					"pjinchangdate 	 varchar(32) ,"+
					"ppackingtime 	 varchar(32) ,"+
					"sfpxpz 	 varchar(32) ,"+
					"bssj 	 varchar(32) ,"+
					"fcchgk 	 varchar(32) ,"+
					"dcjsgkdz 	 varchar(32) ,"+
					"dcdsgkdz 	 varchar(32) ,"+
					"dsgkdz 	 varchar(32) ,"+
					"startdate 	 varchar(32) ,"+
					"blhtl 	 varchar(32) ,"+
					"dgtrainwagonno 	 varchar(32) ,"+
					"dgtraintype 	 varchar(32) ,"+
					"dgtrainwaybillno 	 varchar(32) ,"+
					"dgtrainsinglenum 	 varchar(32) ,"+
					"dgtrainsingleton 	 varchar(32) ,"+
					"cargostatusport 	 varchar(32) ,"+
					"dgtrainwagonkg 	 varchar(32) ,"+
					"dgtrainstartdate 	 varchar(32) ,"+
					"img 	 			 varchar(1000)," +		//	图片
					"busiinvcode		 varchar(32) not null)";	
	//	箱管信息的表;
	private String sql_boxmanageinfo=
			"create table boxmanageinfo (" +
				"_id integer primary key autoincrement," +
				"barcode 	 varchar(32) not null," +		//	二维码信息;
				"ecarryaddress 	 varchar(32) ,"+
				"ecarrydate 	 varchar(32) ,"+
				"echinaporttime 	 varchar(32) ,"+
				"eportstorageroomtime 	 varchar(32) ,"+
				"etimechangeofport 	 varchar(32) ,"+
				"echangenumber 	 varchar(32) ,"+
				"efeeofflinetime 	 varchar(32) ,"+
				"erailwayofflinetime 	 varchar(32) ,"+
				"eactualreturntime 	 varchar(32) ,"+
				"cargostatusbox 	 varchar(32) ,"+
				"img 	 			 varchar(1000)," +		//	图片
				"busiinvcode		 varchar(32) not null)";	
	//	口岸信息的表;
	private String sql_harborinfo=
			"create table harborinfo (" +
					"_id integer primary key autoincrement," +
					"barcode 	 varchar(32) not null," +		//	二维码信息;
					"pfactchportdate 	 varchar(32) ,"+
					"mpackingdate 	 varchar(32) ,"+
					"ppassdate 	 varchar(32) ,"+
					"preloadcarno 	 varchar(32) ,"+
					"preloadcarnum 	 varchar(32) ,"+
					"preloaddate 	 varchar(32) ,"+
					"msinglecarnum 	 varchar(32) ,"+
					"msinglecarton 	 varchar(32) ,"+
					"pstartdate 	 varchar(32) ,"+
					"cargostatusseaport 	 varchar(32) ,"+
					"img 	 			 varchar(1000)," +		//	图片
					"busiinvcode		 varchar(32) not null)";	
	//	签收信息的表;
	private String sql_signinfo=
			"create table signinfo ( " +
			"_id integer 	 primary key autoincrement," + 		//	id编号;
			"barcode 	 	 varchar(32) not null," +		//	二维码信息;
			"receiptdate	 varchar(32) not null," +		//	签收时间
			"cargostatussign varchar(500) not null," +		//	货物状态
			"img 	 		 varchar(1000) not null," +		//	图片
			"busiinvcode 	 varchar(32) not null" +		//	图片
			")";		
//	//	油料表的结构;
//	private String sql_oilinfo=
//			"create table oilinfo ("+
//			"id integer not null primary key,"+
//			"okind varchar(20) not null,"+
//			"oid varchar(20) not null,"+
//			"oliter double not null,"+
//			"omoney double not null,"+
//			"opayway varchar(20) not null,"+
//			"ocardid varchar(20),"+
//			"olmoney double,"+
//			"omile double not null,"+
//			"olat double not null,"+
//			"olng double not null,"+
//			"ocity varchar(20),"+
//			"oimg varchar(20),"+
//			"otime varchar(20) not null,"+
//			"tid varchar(20) not null,"+
//			"wid varchar(20) not null,"+
//			"wname varchar(20) not null"+
//			")";
	
//	//	主车表的结构;
//	private String sql_truckinfo=
//			"create table truckinfo (" +
//			"tid varchar(20) primary key not null," +
//			"toil01 double," +
//			"wid varchar(20) not null," +
//			"bid varchar(20) not null" +
//			")";
//	//	挂车表的结构;
//	private String sql_struckinfo=
//			"create table struckinfo (" +
//			"sid varchar(20) primary key not null," +
//			"snote varchar(500)," +
//			"tid varchar(20) not null," +
//			"bid varchar(20)" +
//			")";

	//	数据库管理类的构造函数;
	public MTDBHelper(Context context, String name, int version) {
		super(context, name, null, version);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		//	编辑列表;
		db.execSQL(sql_businessinfo);
		//	01.创建货物表;
		db.execSQL(sql_goodsinfo);
		//	02.建立提货信息表;
		db.execSQL(sql_getgoodsinfo);
		//	03.建立签收信息表;
		db.execSQL(sql_signinfo);
		//	05.建立口岸信息表;
		db.execSQL(sql_harborinfo);
		//	06.建立箱管信息表;
		db.execSQL(sql_boxmanageinfo);
		//	07.建立港口信息表;
		db.execSQL(sql_portinfo);
//		//	04.建立车辆信息表;
//		db.execSQL(sql_truckinfo);
//		//	05.建立挂车信息表;
//		db.execSQL(sql_struckinfo);

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {


	}
}
