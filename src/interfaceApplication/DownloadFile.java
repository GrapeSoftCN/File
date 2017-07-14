package interfaceApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import esayhelper.jGrapeFW_Message;
import model.OpFile;

@WebServlet(name = "Download", urlPatterns = { "/Download" })
public class DownloadFile extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private OpFile files = new OpFile();
    public DownloadFile() {
        super();
    }
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String msg = null;
		try {
			String fileid = request.getParameter("_id"); // 得到要下载的文件名
			String appid = request.getParameter("appid"); // 得到要下载的文件名
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			JSONObject object = files.find(appid,fileid);
			if (object==null) {
				msg = jGrapeFW_Message.netMSG(1, "文件不存在");
				return;
			}
			String downpath = object.get("filepath").toString();
			String filname = object.get("fileoldname").toString();
			File file = new File(downpath + "\\" + filname);
//			File file = new File(downpath);
			if (!file.exists()) {
				msg = jGrapeFW_Message.netMSG(1, "文件不存在");
				return;
			} else {
				String realname = filname.substring(filname.indexOf("_") + 1);
				response.setHeader("content-disposition",
						"attachment;filename=" + URLEncoder.encode(realname, "UTF-8"));
				FileInputStream in = new FileInputStream(downpath + "\\" + filname);
//				FileOutputStream os = new FileOutputStream("C://downfile//" + downfile);
				OutputStream out = response.getOutputStream();
				byte[] buffer = new byte[4 * 1024];
				int read;
				while ((read = in.read(buffer)) > 0) {
//					os.write(buffer, 0, read);
					out.write(buffer, 0, read);
				}
//				os.close();
				out.close();
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.getWriter().print(msg);
	}

}
