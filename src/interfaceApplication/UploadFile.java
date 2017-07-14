package interfaceApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONObject;

import esayhelper.TimeHelper;
import model.FileImg;
import model.GetFileUrl;
import model.OpFile;
import net.coobird.thumbnailator.Thumbnails;
import nlogger.nlogger;

@WebServlet(name = "UploadFile", urlPatterns = { "/UploadFile" })
public class UploadFile extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private OpFile files = new OpFile();
	private String path = "";
	private String newName = "";
	private String fileName = ""; // 文件名称
	private String fatherid = ""; // 所属文件夹id
	private String MD5 = ""; // MD5码
	private String ExtName = ""; // 扩展名
	private String ThumbailsPath = "";

	public UploadFile() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		String appid = request.getParameter("appid"); // 分表字段
		fatherid = request.getParameter("folderid");
		// JSONObject obj = new JSONObject();
		boolean uploadDone = true;
		try {
			String Date = TimeHelper.stampToDate(TimeHelper.nowMillis()).split(" ")[0];
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart) {
				FileItemFactory factory = new DiskFileItemFactory();
				path = this.getServletContext().getRealPath("/upload/" + Date);
				if (!new File(path).exists()) {
					new File(path).mkdir();
				}
				ThumbailsPath = path + "\\" + "thumbnail\\";
				if (!new File(ThumbailsPath).exists()) {
					new File(ThumbailsPath).mkdir();
				}
				ServletFileUpload upload = new ServletFileUpload(factory);
				// 得到所有的表单域，它们目前都被当作FileItem
				List<FileItem> fileItems = upload.parseRequest(request);
				String id = "";
				// 如果大于1说明是分片处理
				int chunks = 1;
				int chunk = 0;
				long filesize = 0;
				FileItem tempFileItem = null;
				if (fileItems != null && fileItems.size() > 0) {
					nlogger.logout("fileItems.fileItems: " + fileItems.toString());
					for (FileItem fileItem : fileItems) {
						if (fileItem.getFieldName().equals("id")) {
							id = fileItem.getString();
						} else if (fileItem.getFieldName().equals("name")) {
							fileName = new String(fileItem.getString().getBytes("ISO-8859-1"), "UTF-8");
						} else if (fileItem.getFieldName().equals("chunks")) {
							chunks = NumberUtils.toInt(fileItem.getString());
						} else if (fileItem.getFieldName().equals("chunk")) {
							chunk = NumberUtils.toInt(fileItem.getString());
						} else if (fileItem.getFieldName().equals("file")) {
							tempFileItem = fileItem;
						}
						filesize += fileItem.getSize();
						nlogger.logout("fileItem.name: " + fileItem.getName());
					}
				}
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
					nlogger.logout("uploadDone: " + uploadDone);
					newName = rename(appid, 0);
					File destTempFile = new File(path, newName);
					for (int i = 0; i < chunks; i++) {
						File partFile = new File(parentFileDir, fileName + "_" + i + ".part");
						FileOutputStream destTempfos = new FileOutputStream(destTempFile, true);
						FileUtils.copyFile(partFile, destTempfos);
						destTempfos.close();
					}
					FileUtils.deleteDirectory(parentFileDir);
					String mString = getJson(appid, fileName, String.valueOf(filesize), fatherid);
					nlogger.logout("上传成功返回值： " + mString);
					if (mString != null && !("").equals(mString)) {
						response.getWriter().print(mString);
					}

				} else {
					if (chunk == chunks - 1) {
						FileUtils.deleteDirectory(parentFileDir);
					}
					response.getWriter().print("文件上传失败");
				}
			}
		} catch (Exception e) {
			response.getWriter().print("文件上传失败");
		}
	}

	private String getTempFilePath(String tempath) {
		File file = new File(tempath);
		if (!file.exists()) {
			file.mkdirs();
		}
		return tempath;
	}

	// 获取扩展名
	private String ext(String name) {
		if (name.contains(".")) {
			ExtName = name.substring(name.lastIndexOf(".") + 1);
		} else {
			ExtName = "";
		}
		return ExtName;
	}

	@SuppressWarnings("unchecked")
	private String getJson(String appid, String filename, String filesize, String fatherid) {
		MD5 = DigestUtils.md5Hex(path + "\\" + fileName);
		ExtName = ext(fileName);
		int filetype = GetFileType(ExtName);
		String filepath = path.split("webapps")[1] + "\\" + newName;
		JSONObject object = new JSONObject();
		object.put("fileoldname", fileName);
		object.put("filenewname", newName);
		object.put("filetype", filetype);
		object.put("fileextname", ExtName);
		object.put("size", String.valueOf(filesize));
		object.put("fatherid", fatherid);
		object.put("filepath", filepath);
		object.put("MD5", MD5);
		object.put("isdelete", 0);
		object.put("ThumbnailImage", Thumbailnail(filetype, filepath, ExtName)); // 缩略图
		nlogger.logout("getJson: " + object.toString());
		String string = files.insert(appid, object);
		if (string != null && !("").equals(string)) {
			return string;
		}
		return "";
	}

	// 判断是否同时上传多个文件，重新命名
	private String rename(String appid, int Suffix) {
		if (newName.equals("")) {
			newName = String.valueOf(TimeHelper.nowSecond());
		} else {
			newName = newName + Suffix;
		}
		JSONObject object = files.search(appid, newName);
		if (object != null) {
			Suffix++;
			newName = rename(appid, Suffix);
		}
		return newName + "." + ext(fileName);
	}

	// 判断文件类型
	private int GetFileType(String extname) {
		int type;
		switch (extname.toLowerCase()) {
		// 图片
		case "png":
		case "jpg":
		case "gif":
		case "jpeg":
		case "tiff":
		case "raw":
		case "bmp":
			type = 1;
			break;
		// 视频
		case "avi":
		case "rmvb":
		case "rm":
		case "mkv":
		case "mp4":
		case "wmv":
		case "mov":
			type = 2;
			break;
		// 文档
		case "doc":
		case "docx":
		case "wps":
		case "xls":
		case "xlxs":
		case "ppt":
		case "txt":
		case "htm":
		case "html":
		case "pdf":
		case "dwg":
			type = 3;
			break;
		// 音频
		case "mp3":
		case "wav":
		case "wma":
		case "ogg":
			type = 4;
			break;
		// 其他
		default:
			type = 5;
			break;
		}
		return type;
	}

	/**
	 * 获取图片，视频缩略图
	 * 
	 * @project File
	 * @package interfaceApplication
	 * @file UploadFile.java
	 * 
	 * @param type
	 *            文件类型
	 * @param filepath
	 *            待操作文件地址
	 * @return
	 *
	 */
	private String Thumbailnail(int type, String filepath, String extName) {
		String outpath = ThumbailsPath + newName.substring(0, newName.lastIndexOf(".")) + ".jpg";
		String otherPath = GetFileUrl.GetTomcatUrl() + "\\File\\upload\\";
		String result = "";
		switch (type) {
		case 1: // 图片
			result = ImageThumbnail(filepath, outpath);
			break;
		case 2: // 视频
			result = VideoThumbnail(outpath);
			break;
		default:
			result = otherPath + FileImg.getIcon(extName);
			break;
		/*
		 * case 3: // 文档 result = otherPath + ""; break; case 4: // 音频 result =
		 * otherPath + ""; break; case 5: // 其他 result = otherPath + ""; break;
		 */
		}
		return getImgUrl(result);
	}

	/**
	 * 截取指定时间的视频图片
	 * 
	 * @project File
	 * @package interfaceApplication
	 * @file UploadFile.java
	 * 
	 * @param outpath
	 *            视频缩略图输出地址
	 * @return
	 *
	 */
	private String VideoThumbnail(String outpath) {
		List<String> commend = new ArrayList<String>();
		System.out.println(GetFileUrl.getVideoUrl());
		commend.add(GetFileUrl.getVideoUrl());
		commend.add("-i");
		commend.add(path + "\\" + newName);
		commend.add("-y");
		commend.add("-ss");
		commend.add(GetFileUrl.getTime()); // 截取多少秒之后的图片
		commend.add("-s");
		commend.add(GetFileUrl.getSize());
		commend.add(outpath);
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(commend);
			builder.start();
		} catch (Exception e) {
			nlogger.logout(e);
			outpath = "";
		}
		return outpath;
	}

	/**
	 * 获取上传图片缩略图
	 * 
	 * @project File
	 * @package interfaceApplication
	 * @file UploadFile.java
	 * 
	 * @param inputpath
	 *            待操作图片
	 * @param outpath
	 *            图片缩略图输出地址
	 * @return
	 *
	 */
	private String ImageThumbnail(String inputpath, String outpath) {
		try {
			Thumbnails.of(GetFileUrl.GetTomcatUrl() + inputpath)
					.forceSize(Integer.parseInt(GetFileUrl.getImgSize(0)), Integer.parseInt(GetFileUrl.getImgSize(1)))
					.outputFormat(GetFileUrl.getImgType()).outputQuality(Float.parseFloat(GetFileUrl.getImgQuality()))
					.toFile(outpath);
		} catch (Exception e) {
			nlogger.logout(e);
			outpath = "";
		}
		return outpath;
	}

	private String getImgUrl(String imgurl) {
		if (imgurl.contains("webapps")) {
			imgurl = imgurl.split("webapps")[1];
		}
		return imgurl;
	}
}
