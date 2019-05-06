/**
* Filename : VendorDatabaseUtil.java
* Author : {author}
* Creation time : 上午10:17:13 - 2019年1月27日
* Description :
*/
package com.vargo.readcellinfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

/**
 * @author zhanghongbiao@vargo.com.cn
 */
public class VendorDatabaseUtil extends SQLiteOpenHelper {
	
	public static final String TABLE_CELL = "cells";
	public static final String TABLE_CELL_ITEM_ID = "_id";
	public static final String TABLE_CELL_ITEM_LOCATION = "location";
	public static final String TABLE_CELL_ITEM_CELLS = "cells";
	public static final String TABLE_CELL_ITEM_NEIGHBORS = "neighbors";
	public static final String TABLE_CELL_ITEM_TYPE = "type";


	
	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public VendorDatabaseUtil(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("create table if not exists " + TABLE_CELL + "("
				+ TABLE_CELL_ITEM_ID + " integer primary key autoincrement,"
				+ TABLE_CELL_ITEM_LOCATION + " text,"
				+ TABLE_CELL_ITEM_CELLS + " text,"
				+ TABLE_CELL_ITEM_NEIGHBORS + " text,"
				+ TABLE_CELL_ITEM_TYPE + " integer);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean insertCell(CellRecord info){
		if(info == null) return false;
		if(TextUtils.isEmpty(info.location)) return false;

		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(TABLE_CELL_ITEM_LOCATION, info.location);
		values.put(TABLE_CELL_ITEM_CELLS, info.cells);
		values.put(TABLE_CELL_ITEM_NEIGHBORS, info.neighbors);
		values.put(TABLE_CELL_ITEM_TYPE, info.type);
		long rowId = db.insert(TABLE_CELL, null, values);
		return rowId != -1;
	}
	
	public boolean isCellExist(String location){
		if(TextUtils.isEmpty(location)) return false;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(TABLE_CELL, null,
				TABLE_CELL_ITEM_LOCATION+"=\""+location+"\"",
				null, null, null, null);
		boolean b = cursor.moveToNext();
		cursor.close();
		return b;
	}
	
	public void deleteCellById(int id) {
		if(id >= 0) {
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_CELL, TABLE_CELL_ITEM_ID + "=" + id, null);
		}
	}
	/**
	 * @return
	 */
	public Cursor getCellsCursor() {
		// TODO Auto-generated method stub
		SQLiteDatabase db = getReadableDatabase();
		String sql = "select "+
				 TABLE_CELL_ITEM_ID+","+
				TABLE_CELL_ITEM_LOCATION+","+
				TABLE_CELL_ITEM_CELLS+","+
				TABLE_CELL_ITEM_NEIGHBORS+","+
				TABLE_CELL_ITEM_TYPE +
                " from "+TABLE_CELL;
		return db.rawQuery(sql, null);
	}

	/**
	 * @param i
	 * @return
	 */
	public long getCellsCount() {
		// TODO Auto-generated method stub
		SQLiteDatabase db = getReadableDatabase();
		String sql = "select count(*) from "+TABLE_CELL;
		Cursor cursor = db.rawQuery(sql, null);
		cursor.moveToFirst();
		long count = cursor.getLong(0);
		cursor.close();
		return count;
	}

	/**
	 * @param index
	 * @param i
	 * @return
	 */
	public CellRecord getCellByIndex(int index) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = getReadableDatabase();
		String sql = "select "+
						 TABLE_CELL_ITEM_ID+","+
				TABLE_CELL_ITEM_LOCATION+","+
				TABLE_CELL_ITEM_CELLS+","+
				TABLE_CELL_ITEM_NEIGHBORS+","+
				TABLE_CELL_ITEM_TYPE+
		                 " from "+TABLE_CELL+" limit 1 offset "+index;
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			CellRecord info = new CellRecord();
			info.id = cursor.getInt(0);
			info.location = cursor.getString(1);
			info.cells = cursor.getString(2);
			info.neighbors = cursor.getString(3);
			info.type = cursor.getInt(4);
			cursor.close();
			return info;
		}else {
			cursor.close();
			return null;
		}
	}

}
