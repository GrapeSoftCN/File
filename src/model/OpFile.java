package model;

import org.bson.types.ObjectId;
import org.json.simple.JSONObject;

import esayhelper.DBHelper;
import esayhelper.jGrapeFW_Message;
import nlogger.nlogger;

public class OpFile {
	private static DBHelper file;
	static {
		file = new DBHelper("mongodb", "file");
	}

	// 文件信息入库
	public String insert(String appid,JSONObject object) {
		if (object!=null&&!("").equals(object)) {
			String info = file.bind(appid).data(object).insertOnce().toString();
			return find(appid,info).toString();
		}
		return jGrapeFW_Message.netMSG(0, "");
	}

	// 根据文件id查找文件
	public JSONObject find(String appid,String fid) {
		JSONObject object = file.bind(appid).eq("_id", new ObjectId(fid)).find();
		return object;
	}

	// 根据文件名判断文件是否存在
	public boolean search(String appid,String MD5) {
		JSONObject object = new JSONObject();
		try {
			object = file.bind(appid).eq("MD5", MD5).find();
		} catch (Exception e) {
			return false;
		}
		
		return object != null ? true : false;
	}
	//只针对于文件转换
	public int get_file_type(String appid,String fid) {
		int ckcode = 0;
		JSONObject object = find(appid, fid);
		if (object!=null) {
			try {
				if (!("2").equals(object.get("filetype").toString())) {
					ckcode = 0;
				}else{
					String extName = object.get("fileextname").toString();
					if (extName.equals("wmv9") || extName.equals("rm") || extName.equals("rmvb")) {
						ckcode = 1;
					}else{
						ckcode = 2;
					}
				}
			} catch (Exception e) {
				nlogger.logout(e);
				ckcode = 0;
			}
		}
		return ckcode;
	}
}
