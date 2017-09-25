package interfaceApplication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import file.fileHelper;
import model.GetFileUrl;
import nlogger.nlogger;
import offices.excelHelper;
import time.TimeHelper;

/**
 * Servlet implementation class ExportReport
 */
@WebServlet("/ExportReport")
public class ExportReport extends HttpServlet {
	private static AtomicInteger fileNO = new AtomicInteger(0);
	private GetFileUrl fileUrl = new GetFileUrl();
	private static final long serialVersionUID = 1L;

	public ExportReport() {
		super();
	}
	private String getUnqueue() {
		return (new Integer(fileNO.incrementAndGet())).toString();
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
		try {
			String fileurl = fileUrl.GetTomcatUrl() + "/File/upload/" + Date + "\\Excel";
//			byte[] by = excelHelper.out(info);
			byte[] by = info.getBytes();
			if (by != null) {
				path = fileurl + "\\" + TimeHelper.nowMillis() + getUnqueue() + ".xls";
				if (fileHelper.createFileEx(path)) {
					FileOutputStream fos = new FileOutputStream(path);
					fos.write(by);
					fos.close();
				}
			}
		} catch (Exception e) {
			nlogger.logout(e);
		}
		return path;
	}
}
