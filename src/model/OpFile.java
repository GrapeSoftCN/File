package model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.json.simple.JSONObject;

import JGrapeSystem.jGrapeFW_Message;
import database.DBHelper;
import nlogger.nlogger;
import string.StringHelper;

public class OpFile {
	private DBHelper file;

	public OpFile() {
		file = new DBHelper("mongodb", "file");
	}

	// 文件信息入库
	public String insert(String appid, JSONObject object) {
		if (object != null && !("").equals(object)) {
			String info = file.bind(appid).data(object).insertOnce().toString();
			return find(appid, info).toString();
		}
		return jGrapeFW_Message.netMSG(0, "");
	}

	// 文件覆盖
	public String CoverFile(String appid, JSONObject object) {
		JSONObject obj = (JSONObject) object.get("_id");
		int code = file.bind(appid).eq("_id", new ObjectId(obj.get("$oid").toString())).delete() != null ? 0 : 99;
		if (code == 0) {
			object.remove("_id");
			return insert(appid, object);
		}
		return object.toString();
	}

	// 根据文件id查找文件
	@SuppressWarnings("unchecked")
	public JSONObject find(String appid, String fid) {
		String image = "";
		String[] value = null;
		List<String> list = new ArrayList<String>();
		JSONObject object = file.bind(appid).eq("_id", new ObjectId(fid)).find();
		if (object != null && object.size() > 0) {
			if (object.containsKey("pptImage")) {
				image = object.getString("pptImage");
				if (!image.equals("")) {
					value = image.split(",");
					for (String string : value) {
						string = "http://" + new GetFileUrl().GetTomcatWebUrl() + string;
						list.add(string);
					}
				}
				object.put("pptImage", StringHelper.join(list));
			}
		}
		return object;
	}

	// 根据文件名判断文件是否存在
	public JSONObject search(String appid, String newName) {
		JSONObject object = null;
		try {
			object = new JSONObject();
			object = file.bind(appid).eq("filenewname", newName).find();
		} catch (Exception e) {
			object = null;
		}
		return object;
	}

	/**
	 * 更新文件信息
	 * 
	 * @project File
	 * @package model
	 * @file OpFile.java
	 * 
	 * @param appid
	 * @param fid
	 * @param newInfo
	 * @return
	 *
	 */
	public String update(String appid, String fid, JSONObject newInfo) {
		String message = jGrapeFW_Message.netMSG(99, "文件信息更新失败");
		JSONObject FileInfo = new JSONObject();
		if (newInfo != null && newInfo.size() != 0) {
			Object obj = file.bind(appid).eq("_id", fid).data(newInfo).update();
			if (obj != null) {
				FileInfo = find(appid, fid);
				message = jGrapeFW_Message.netMSG(0, FileInfo);
			}
		}
		return message;
	}

	// 只针对于文件转换
	public int get_file_type(String appid, String fid) {
		int ckcode = 0;
		JSONObject object = find(appid, fid);
		if (object != null) {
			try {
				if (!("2").equals(object.get("filetype").toString())) {
					ckcode = 0;
				} else {
					String extName = object.get("fileextname").toString();
					if (extName.equals("wmv9") || extName.equals("rm") || extName.equals("rmvb")) {
						ckcode = 1;
					} else {
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
