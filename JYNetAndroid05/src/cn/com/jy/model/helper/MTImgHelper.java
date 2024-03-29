package cn.com.jy.model.helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

public class MTImgHelper {
	public MTImgHelper() {
		// TODO Auto-generated constructor stub
	}

	// 清空原有图片;
	public void clearPicture(String filePath, ArrayList<Integer> list) {
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
			if(list!=null){				
				list.clear();
			}
		}
	}
	//	压缩图片;
	public void compressPicture(String srcPath, String desPath) {  
        FileOutputStream fos = null;  
        BitmapFactory.Options op = new BitmapFactory.Options();  
  
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了  
        op.inJustDecodeBounds = true;  
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, op);  
        op.inJustDecodeBounds = false;  
  
        // 缩放图片的尺寸  
        float w = op.outWidth;  
        float h = op.outHeight;  
        float hh = 1024f;//  
        float ww = 1024f;//  
        // 最长宽度或高度1024  
        float be = 1.0f;  
        if (w > h && w > ww) {  
            be = (float) (w / ww);  
        } else if (w < h && h > hh) {  
            be = (float) (h / hh);  
        }  
        if (be <= 0) {  
            be = 1.0f;  
        }  
        op.inSampleSize = (int) be;// 设置缩放比例,这个数字越大,图片大小越小.  
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了  
        bitmap = BitmapFactory.decodeFile(srcPath, op);  
        int desWidth = (int) (w / be);  
        int desHeight = (int) (h / be);  
        bitmap = Bitmap.createScaledBitmap(bitmap, desWidth, desHeight, true);  
        try {  
            fos = new FileOutputStream(desPath);  
            if (bitmap != null) {  
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);  
            }  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        }  
    } 
	
	//	获取图片的Bitmap容器;
	public List<BitmapDrawable> getBitmap01(String folderPath){
		List<BitmapDrawable> list=	new ArrayList<BitmapDrawable>();
		File 	 			 file=	null;
		file					 =	new File(folderPath);
	    if (!file.exists()) {
	    	return null;
	    }
	    File 			  files[]= 	file.listFiles();
	    for(File item:files){
	    	String 	tmp =	item.getName();
	    	String	path=	folderPath+File.separator+tmp;
	    	file		=	new File(path);
	    	Bitmap 	bm 	= 	BitmapFactory.decodeFile(path); 
	    	@SuppressWarnings("deprecation")
			BitmapDrawable bd=new BitmapDrawable(bm);
	    	list.add(bd);
	    }
		return list;
	}
	public List<BitmapDrawable> getBitmap01_2(String folderPath,String param){
		List<BitmapDrawable> list =	new ArrayList<BitmapDrawable>();
		String[]			 names= null;
		if(param.contains("_")){
			names=param.split("_");
		}else {
			names=new String[1];
			names[0]=param;
		}
	    for(String item:names){
	    	String	path=	folderPath+File.separator+item+".jpg";
	    	Bitmap 	bm 	= 	BitmapFactory.decodeFile(path); 
	    	@SuppressWarnings("deprecation")
			BitmapDrawable bd=new BitmapDrawable(bm);
	    	list.add(bd);
	    }
		return list;
	}
	
	
	
	//	获取图片的Bitmap容器;
	public List<BitmapDrawable> getBitmap02(String folderPath,String oimg){
		List<BitmapDrawable> list=	new ArrayList<BitmapDrawable>();	
    	String				 path=	folderPath+File.separator+oimg+".jpg";
    	Bitmap 				 bm  = 	BitmapFactory.decodeFile(path); 
    	@SuppressWarnings("deprecation")
		BitmapDrawable 		 bd  =new BitmapDrawable(bm);
    	list.add(bd);
		return list;
	}
	//	进行手写板图像的绘制;
	public Bitmap doWriteImg(Object object,String folderPath,String fileName){
		Bitmap 		zoombm = null;
		//	进行数据的长宽设置;
		Bitmap mSignBitmap = (Bitmap) object;
		String signPath    = doResignPhoto(mSignBitmap,folderPath,fileName);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 15;
		options.inTempStorage = new byte[5 * 1024];
		try {										
			zoombm	  = BitmapFactory.decodeFile(signPath, options);
		} catch (Exception e) {
			zoombm	  = null;
		}
		return zoombm;
	}
	public String doResignPhoto(Bitmap mSignBitmap,String folderPath,String fileName) {
		ByteArrayOutputStream 	baos 	  = null;
		File					file	  = null;
		String 					filePath  = null;
		try {
			file	  	= new File(folderPath);
			//	生成文件夹的方式;
			if(!file.exists()){
				file.mkdirs();
			}
			filePath					  = folderPath+File.separator+fileName+".jpg";
			baos 						  = new ByteArrayOutputStream();
			mSignBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			byte[] 			  photoBytes  = baos.toByteArray();
			if (photoBytes != null) {
				new FileOutputStream(new File(filePath)).write(photoBytes);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null)
					baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return filePath;
	}
}
