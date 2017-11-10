package cn.com.jy.model.helper;
import java.util.ArrayList;

import cn.com.jy.model.entity.MEFile;

public class MTFileHelper {
	private ArrayList<MEFile> listfiles;
	
	//	对文件进行管理的类;
	public MTFileHelper() {
		this.listfiles=new ArrayList<MEFile>();
	}

	//	获得图片列表;
	public ArrayList<MEFile> getListfiles() {
		return listfiles;
	}

	//	图片内容添加;
	public void fileAdd(MEFile mefile){
		if(listfiles!=null){
			listfiles.add(mefile);
		}
	}
	//	清空图片;
	public void fileDelAll(){
		listfiles.clear();
	}
	//	删除条目;
	public void fileDelItem(String name){
		int index=0;
		for(MEFile itemfile:listfiles){
			String fname=itemfile.getName();
			if(fname.equals(name)){
				listfiles.remove(index);
				break;
			}
			index++;
		}
	}	
	
	  //	03.以字符串形式存储文件名称;
    public String getFileNamesByStrs(ArrayList<MEFile> list,String code) {
 	   String str	= "";   
 	   int  nLength	= list.size();
 	   for (int i = 0; i <nLength; i++) {
 		   String 	tmp=	list.get(i).getName();
 		   if(i<nLength-1){
 				   str+=	tmp+code;
 			   }else if(i==nLength-1){
 				   str+=	tmp;
 			   }
 		}
         return str;
     }
    
    public ArrayList<String> getFileNamesByList(String param,String code){
 		ArrayList<String> list =	new ArrayList<String>();
    	 
    	 if(param.contains(code)){
    		 String[] names=param.split(code);
    		 for(String item:names){
    			 list.add(item);
    		 }
    	 }else if(!param.equals("未拍照"))
    		 list.add(param);
    	 
 		return list;
 	}
}
