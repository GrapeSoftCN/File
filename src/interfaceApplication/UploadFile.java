package interfaceApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONObject;

import esayhelper.JSONHelper;
import esayhelper.TimeHelper;
import esayhelper.jGrapeFW_Message;
import model.OpFile;

@WebServlet(name = "Upload", urlPatterns = { "/Upload" })
public class UploadFile extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private OpFile files = new OpFile();
	private JSONObject _obj = new JSONObject();
	
	private String ExtName = ""; // 扩展名
	public UploadFile() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		String fatherid = request.getParameter("folderid");
		String msg ="";
		boolean uploadDone=true;
		try {
			String Date = TimeHelper.stampToDate(TimeHelper.nowMillis()).split(" ")[0];
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart) {
				FileItemFactory factory = new DiskFileItemFactory();
				String path = this.getServletContext().getRealPath("/upload/"+Date);
				if (!new File(path).exists()) {
					new File(path).mkdir();
				}
				ServletFileUpload upload = new ServletFileUpload(factory);
				// 得到所有的表单域，它们目前都被当作FileItem
				List<FileItem> fileItems = upload.parseRequest(request);
				String id = "";
				String fileName = "";
				// 如果大于1说明是分片处理
				int chunks = 1;
				int chunk = 0;
				long filesize=0;
				FileItem tempFileItem = null;
				for (FileItem fileItem : fileItems) {
					if (fileItem.getFieldName().equals("id")) {
						id = fileItem.getString();
					} else if (fileItem.getFieldName().equals("name")) {
						fileName = new String(fileItem.getString().getBytes("ISO-8859-1"), "UTF-8");
						if (files.search(fileName)) {
							response.getWriter().print(jGrapeFW_Message.netMSG(0, "文件已存在"));
							return;
						}
					} else if (fileItem.getFieldName().equals("chunks")) {
						chunks = NumberUtils.toInt(fileItem.getString());
					} else if (fileItem.getFieldName().equals("chunk")) {
						chunk = NumberUtils.toInt(fileItem.getString());
					} else if (fileItem.getFieldName().equals("file")) {
						tempFileItem = fileItem;
					}
					filesize +=fileItem.getSize();
				}
				JSONObject object = new JSONObject();
				object.put("fileoldname", fileName);
				object.put("filenewname", mknew(fileName));
				object.put("filetype", GetFileType(ExtName));
				object.put("fileextname", ext(fileName));
				object.put("size", String.valueOf(filesize));
				object.put("fatherid", fatherid);
				object.put("filepath", path);
				object.put("isdelete", 0);
				_obj.put("records", JSONHelper.string2json(files.insert(object)));
				msg = jGrapeFW_Message.netMSG(0, _obj.toString());
				String tempFileDir = getTempFilePath(path) + File.separator + id;
				File parentFileDir = new File(tempFileDir);
				if (!parentFileDir.exists()) {
					parentFileDir.mkdirs();
				}
				// 分片处理时，前台会多次调用上传接口，每次都会上传文件的一部分到后台(默认每片为5M)
				File tempPartFile = new File(parentFileDir, fileName + "_" + chunk + ".part");
				FileUtils.copyInputStreamToFile(tempFileItem.getInputStream(), tempPartFile);
				// 是否全部上传完成
				// 所有分片都存在才说明整个文件上传完成
				for (int i = 0; i < chunks; i++) {
					File partFile = new File(parentFileDir, fileName + "_" + i + ".part");
					if (!partFile.exists()) {
						uploadDone = false;
					}
				}
				if (uploadDone) {
					File destTempFile = new File(path, fileName);
					for (int i = 0; i < chunks; i++) {
						File partFile = new File(parentFileDir, fileName + "_" + i + ".part");
						FileOutputStream destTempfos = new FileOutputStream(destTempFile, true);
						FileUtils.copyFile(partFile, destTempfos);
						destTempfos.close();
					}
					FileUtils.deleteDirectory(parentFileDir);
				} else {
					// 临时文件创建失败
					if (chunk == chunks - 1) {
						FileUtils.deleteDirectory(parentFileDir);
					}
				}
			}
		} catch (Exception e) {
		}
		response.getWriter().print(msg);
	}

	private String getTempFilePath(String tempath) {
		File file = new File(tempath);
		if (!file.exists()) {
			file.mkdirs();
		}
		return tempath;
	}
	//新文件名称
	public String mknew(String name) {
		String str = UUID.randomUUID().toString();
		String names = ext(name);
		return !names.equals(".") ? (str.replace("-", "") + "." + names)
				: (str.replace("-", ""));
	}
	//获取扩展名
	public String ext(String name) {
		if (name.contains(".")) {
			ExtName = name.substring(name.lastIndexOf(".") + 1);
		} else {
			ExtName = "";
		}
		return ExtName;
	}
	
	//判断文件类型
	public int GetFileType(String extname){
		int type;
		switch (extname.toLowerCase()) {
		//图片
		case "png":
		case "jpg":
		case "gif":
		case "jpeg":
		case "tiff":
		case "raw":
		case "bmp":
			type = 1;
			break;
		//视频
		case "avi":
		case "rmvb":
		case "rm":
		case "mkv":
		case "mp4":
		case "wmv":
		case "ogg":
			type = 2;
			break;
		//文档
		case "doc":
		case "docx":
		case "wps":
		case "xls":
		case "ppt":
		case "txt":
		case "htm":
		case "html":
		case "pdf":
		case "dwg":
		case "exe":
			type = 3;
			break;
		//音频
		case "mp3":
		case "wav":
		case "wma":
			type = 4;
			break;
		//其他
		default:
			type=5;
			break;
		}
		return type;
	}
}
