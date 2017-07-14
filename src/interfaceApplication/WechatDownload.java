package interfaceApplication;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esayhelper.TimeHelper;
import esayhelper.fileHelper;
import thirdsdk.wechatHelper;

/**
 * Servlet implementation class WechatDownload
 */
@WebServlet(name = "/WechatDownload", urlPatterns = { "/WechatDownload" })
public class WechatDownload extends HttpServlet {
	private String APPID = "wxd4ed724da52799cb";
	private String APPSECRET = "6b45df4cd58422eff5a3a707500cb8ca";
	private wechatHelper helper = new wechatHelper(APPID, APPSECRET);
	private static final long serialVersionUID = 1L;

	public WechatDownload() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String mediaid = request.getParameter("mediaid");
		String fileHost = mediaDownload(mediaid);
		response.getWriter().write(fileHost);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String mediaid = request.getParameter("mediaid");
		String fileHost = mediaDownload(mediaid);
		response.getWriter().write(fileHost);
	}

	// 下载微信素材
	private String mediaDownload(String mediaid) {
		String fileHost = "";
		byte[] by = helper.materialTempData(mediaid);
		if (by != null) {
			// if (JSONHelper.string2json(data) == null) {
			String Date = TimeHelper.stampToDate(TimeHelper.nowMillis()).split(" ")[0];
			String fileurl = "C://JavaCode/tomcat/webapps/File/upload/" + Date + "/wechat";
			String path = fileurl + "/" + mediaid + ".mp3";
			if (fileHelper.createFileEx(path)) {
				try {
					FileOutputStream fos = new FileOutputStream(path);
					fos.write(by);
					fos.close();

					fileHost = "http://" + getAppIp("file").split("/")[1];
					if (path.contains("webapps/")) {
						path = path.split("webapps/")[1];
					}
					fileHost = fileHost + "/" + path;
					System.out.println(fileHost);
				} catch (Exception e) {
					fileHost = "";
					e.printStackTrace();
				}
			}
			// }
		}
		return fileHost;
	}

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
}
