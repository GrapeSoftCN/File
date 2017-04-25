package model;

import org.bson.types.ObjectId;
import org.json.simple.JSONObject;

import esayhelper.DBHelper;

public class OpFile {
	private static DBHelper file;
	static{
		file = new DBHelper("mongodb", "file");
	}
	//文件信息入库
	public String insert(JSONObject object) {
		String info = file.data(object).insertOnce().toString();
		return find(info).toString();
	}
	//根据文件id查找文件
	public JSONObject find(String fid) {
		JSONObject object = file.eq("_id", new ObjectId(fid)).find();
		return object;
	}
}
