package interfaceApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esayhelper.TimeHelper;
import nlogger.nlogger;
import offices.excelHelper;

/**
 * Servlet implementation class ExportReport
 */
@WebServlet("/ExportReport")
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
		response.getWriter().write(print(info));
	}

	// 导出举报件
	private String print(String info) {
		String path = "";
		String Date = TimeHelper.stampToDate(TimeHelper.nowMillis()).split(" ")[0];
//		String uuid = UUID.randomUUID().toString().replace("-", "");
		try {
			String fileurl = "C:\\JavaCode\\tomcat\\webapps\\File\\upload\\" + Date + "\\Excel";
			// byte[] by = excelHelper.out(info);
			// if (by != null) {
			// path = fileurl + "\\" + uuid + ".xls";
			// if (fileHelper.createFileEx(path)) {
			// FileOutputStream fos = new FileOutputStream(path);
			// fos.write(by);
			// fos.close();
			// }
			// }
			File file = excelHelper.out(info);
			path = fileurl + file.toString();
		} catch (Exception e) {
			nlogger.logout(e);
		}
		return path;
	}
}
