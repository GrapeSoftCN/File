package interfaceApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import esayhelper.JSONHelper;
import esayhelper.TimeHelper;
import esayhelper.jGrapeFW_Message;
import offices.excelHelper;

/**
 * Servlet implementation class ExportReport
 */
@WebServlet(name = "ExportReport", urlPatterns = { "/ExportReport" })
public class ExportReport extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ExportReport() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String info = request.getParameter("info");
	}

	// 导出举报件
	private String print(String info) {
		String Date = TimeHelper.stampToDate(TimeHelper.nowMillis()).split(" ")[0];
		File file = excelHelper.out("GrapeReport/Report/excel/" + info);
		if (file == null) {
			return resultMessage(0, "没有符合条件的数据");
		}
		String uuid = UUID.randomUUID().toString();
		File tarFile = new File("\file\\tomcat\\webapps\\File\\upload\\" + Date + "\\Grape" + uuid + ".xls");
		file.renameTo(tarFile);
		String target = tarFile.toString();
		String hoString = "http://";
		target = hoString + getAppIp("file").split("/")[1] + target.split("webapps")[1];
		return resultMessage(0, target);
	}

	@SuppressWarnings("unchecked")
//	private void excel(String info) {
//		JSONObject objs = JSONHelper.string2json(info);
//		JSONObject object = null;
//		if (objs != null) {
//			try {
//				object = new JSONObject();
//				JSONArray array = findexcel(objs);
//				for (int i = 0; i < array.size(); i++) {
//					object = (JSONObject) array.get(i);
//					for (Object obj : object.keySet()) {
//						String value = object.get(obj.toString()).toString();
//						if (value.contains("$numberLong")) {
//							JSONObject object2 = JSONHelper.string2json(value);
//							object.put(obj.toString(), Long.parseLong(object2.get("$numberLong").toString()));
//						}
//					}
//				}
//			} catch (Exception e) {
//				object = null;
//			}
//		}
//	}
//
//	private JSONArray findexcel(JSONObject object) {
//		if (object == null) {
//			getdb().eq("state", 1L);
//		} else {
//			getdb().and();
//			for (Object obj : object.keySet()) {
//				if (obj.equals("_id")) {
//					getdb().eq("_id", new ObjectId(object.get("_id").toString()));
//				}
//				if (obj.equals("state")) {
//					getdb().eq(obj.toString(), Long.parseLong(object.get(obj.toString()).toString()));
//				} else {
//					getdb().like(obj.toString(), object.get(obj.toString()).toString());
//				}
//			}
//		}
//		JSONArray array = getdb().limit(50).select();
//		return array;
//	}
	private String getAppIp(String key) {
		String value = "";
		try {
			Properties pro = new Properties();
			pro.load(new FileInputStream("URLConfig.properties"));
			value = pro.getProperty(key);
		} catch (Exception e) {
			value = "";
		}
		return value;
	}

	private String resultMessage(int num, String message) {
		String msg = "";
		switch (num) {
		case 0:
			msg = message;
			break;
		default:
			msg = "其它异常";
			break;
		}
		return jGrapeFW_Message.netMSG(0, msg);
	}
}
