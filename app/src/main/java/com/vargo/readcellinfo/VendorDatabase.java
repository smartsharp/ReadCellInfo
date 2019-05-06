/**
* Filename : VendorDatabase.java
* Author : {author}
* Creation time : 上午10:17:01 - 2019年1月27日
* Description :
*/
package com.vargo.readcellinfo;

import android.content.Context;
import android.database.Cursor;

/**
 * @author zhanghongbiao@vargo.com.cn
 */
public class VendorDatabase {
	private static final String TAG = "VendorDatabase";
	private VendorDatabaseUtil mUtil;	
	private static VendorDatabase instance;
	private static final String DBNAME = "vendor";
	private static final int DBVERSION = 1;

	
	public static void init(Context context){
		if(instance == null){
			instance = new VendorDatabase(context);
		}
	}
	
	public VendorDatabase(Context context) {
		mUtil = new VendorDatabaseUtil(context, DBNAME, null, DBVERSION);
	}
	public static Cursor getCellsCursor() {
		return instance.mUtil.getCellsCursor();
	}
	public static boolean insertCell(CellRecord info){
		return instance.mUtil.insertCell(info);
	}
	public static boolean isCellExist(String location) { return instance.mUtil.isCellExist(location); }
	public static long getCellsCount(){
		return instance.mUtil.getCellsCount();
	}
	public static CellRecord getCellByIndex(int index) {return instance.mUtil.getCellByIndex(index); }
}
