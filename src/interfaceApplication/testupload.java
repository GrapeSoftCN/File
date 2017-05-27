package interfaceApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Servlet implementation class testupload
 */
@WebServlet("/testupload")
public class testupload extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public testupload() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("static-access")
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println(request.getParameter("Files").toString());
		String realPath = getServletContext().getRealPath("/upload");
		DiskFileItemFactory fac = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(fac);
		upload.setFileSizeMax(30 * 1024 * 1024);// 30M
		upload.setSizeMax(50 * 1024 * 1024); // 50M
		if (upload.isMultipartContent(request)) {
			try {
				List<FileItem> list = upload.parseRequest(request);
				for (FileItem item : list) {
					if (!item.isFormField()) {
//						String fieldName = item.getFieldName();// 获取元素名称
//						String value = item.getString("UTF-8"); // 获取元素值
//						System.out.println(fieldName + " : " + value);
//
//					} else {

						String name = item.getName(); // 上传的文件名称
						String id = UUID.randomUUID().toString();
						name = id + name;

						File file = new File(realPath, name);
						item.write(file);
						item.delete();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println(request.getParameter("name"));
			File file = new File(realPath, request.getParameter("name"));
			InputStream inputStream = request.getInputStream();
			FileOutputStream fos = new FileOutputStream(file);
			byte[] b = new byte[1024];
			while ((inputStream.read(b)) != -1) {
				fos.write(b);
			}
			System.out.println(file.toString());
			inputStream.close();
			fos.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	protected void dotest(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		
	}

}
