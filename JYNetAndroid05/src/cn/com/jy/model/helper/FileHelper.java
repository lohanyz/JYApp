package cn.com.jy.model.helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.content.Context;

public class FileHelper {
	public FileHelper() {
	
	}
	
	//	01.读取文本文件的内容;
	public String getFileContent(Context mContext,String fileName,String charset){
		String 					sResult= null;
	    FileInputStream 		input  = null;  
	    ByteArrayOutputStream 	bout   = new ByteArrayOutputStream();  
	    byte[]					buf    = new byte[1024];  
	    int 					length = 0;  
	    try {  
	    	input		   = mContext.openFileInput(fileName); //获得输入流  
            while((length=input.read(buf))!=-1){  
               bout.write(buf,0,length);  
            }  
            byte[] content = bout.toByteArray(); 
            sResult		   = new String(content,charset);
	     } catch (Exception e) {  
	           sResult	   = null;
	     }try{  
	          if(input!=null){        	  
	        	  input.close();  
	          }
	          if(bout!=null){        	  
	        	  bout.close();  
	          }
	     }catch(Exception e){
	    	 sResult=null;
	     }
		return sResult;
	 }
	
     //	 02.写文本文件内容;
     public void setFileContent(Context mContext,String fileName,String content,String charset,int nMode){
	   FileOutputStream out = null;  
       try {  
           out = mContext.openFileOutput(fileName, nMode);  
           out.write(content.getBytes(charset));  
       } catch (Exception e) {  
           e.printStackTrace();  
       }finally{  
           try {  
        	   if(out!=null){        		   
        		   out.close();  
        	   }
           } catch (Exception e) {  
               e.printStackTrace();  
           }  
        }
     }
     //	03.以字符串形式存储文件名称;
     public String getFileNamesByStrs(String folderPath) {
  	   	 String str	= "";   
         File 	f 	= null;
         try {
      	   f=new File(folderPath);
      	   if (!f.exists()) {
      		   return "";
      	   }
      	   File fa[] 	= f.listFiles();
      	   int  nLength	= fa.length;
      	   for (int i = 0; i <nLength; i++) {
      		   File 	fs = 	fa[i];
      		   String 	tmp=	fs.getName();
      		   tmp		   =    tmp.substring(0, tmp.indexOf("."));
      		   if(i<nLength-1){
      				   str+=	tmp+"_";
      			   }else if(i==nLength-1){
      				   str+=	tmp;
      			   }
      		}
      	   }catch (Exception e) {
  			str="";
  		}

          return str;
      }
     //	04.以列表形式存储文件名称;
     public ArrayList<String> getFileNamesByList(String param){
 		ArrayList<String> list =	new ArrayList<String>();
    	 
    	 if(param.contains("_")){
    		 String[] names=param.split("_");
    		 for(String item:names){
    			 list.add(item);
    		 }
    	 }else list.add(param);
 		return list;
 	}

    //	05.删除文件夹下所有的文件利用递归算法;
    public void delAllFile(String path){
    	File   file	=	new File(path);
		File[] files= 	null;
		try {
			files	=	file.listFiles();
			for (File tmp : files) {
				String p=	path+File.separator+tmp.getName();
				delAllFile(p);
			}
		} catch (Exception e) {
			file.delete();
		}
    }
    //	06.删除单条文件;
    public void delItemFile(String path){
    	File 	file	=	new File(path);
    	if(file.exists()){
    		file.delete();
    	}
    }

    //	07.文件的个数;
    public int getFileCount(String folderPath){
    	int 	nSize	=	0;
    	File	file	=	null;
    	File[]	files	=	null;
    	try {			
    		file		=	new File(folderPath);
    		files		=	file.listFiles();
    		nSize		=	files.length;
    	} catch (Exception e) {
    		nSize		=	0;
    	}
    	return  nSize;
    }
}
