package interfaceApplication;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.jodconverter.OfficeDocumentConverter;

import JGrapeSystem.jGrapeFW_Message;
import model.FileConvertModel;
import model.FileImg;
import model.GetFileUrl;
import nlogger.nlogger;
import security.codec;
import time.TimeHelper;

@WebServlet("/FileConvert")
public class FileConvert extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GetFileUrl fileUrl = new GetFileUrl();
	private FileConvertModel model = new FileConvertModel();
	private static AtomicInteger fileNO = new AtomicInteger(0);

	private String getUnqueue() {
		return (new Integer(fileNO.incrementAndGet())).toString();
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FileConvert() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		String sourceFile = request.getParameter("sourceFile");
		int type = Integer.parseInt(request.getParameter("type"));
		sourceFile = codec.DecodeHtmlTag(sourceFile);
		sourceFile = fileUrl.GetTomcatUrl() + sourceFile;
		switch (type) {
		case 0: // 转换成pdf
			response.getWriter().print(office2pdf(sourceFile));
			break;
		case 1: // office转换成html
			response.getWriter().print(office2html(sourceFile));
			break;
		case 2: // office转换成html格式，并获取html文件内容
			response.getWriter().print(office2htmlString(sourceFile));
			break;
		}
	}

	/**
	 * office文件转换成pdf
	 * 
	 * @project File
	 * @package interfaceApplication
	 * @file FileConvert.java
	 * 
	 * @param sourceFile
	 *            源文件地址
	 * @return String 文件不存在提示或者转换成功之后的文件的地址
	 * @throws IOException
	 *
	 */
	public String office2pdf(String sourceFile) throws IOException {
		File inputFile = new File(sourceFile);
		String Date = TimeHelper.stampToDate(TimeHelper.nowMillis()).split(" ")[0];
		String destFile = fileUrl.GetTomcatUrl() + "/File/upload/" + Date;
		File outputFile = new File(destFile);
		if (!inputFile.exists()) {
			return getUTF8StringFromGBKString(jGrapeFW_Message.netMSG(99, "文件不存在"));
		}
		if (!outputFile.exists()) {
			outputFile.mkdir();
		}
		outputFile = new File(destFile + "\\" + TimeHelper.nowMillis() + ".pdf");
		try {
			OfficeDocumentConverter odc = model.getConverter();
			odc.convert(inputFile, outputFile);
		} catch (Exception e) {
			nlogger.logout(e);
			return getUTF8StringFromGBKString(jGrapeFW_Message.netMSG(99, "文件转换失败"));
		}

		return outputFile.toString();
	}

	/**
	 * office文件转换成html文件
	 * 
	 * @project File
	 * @package interfaceApplication
	 * @file FileConvert.java
	 * 
	 * @param sourceFile
	 *            源文件路径
	 * @return String 文件不存在提示或者转换成功之后的文件的地址
	 *
	 */
	public String office2html(String sourceFile) {
		String result = "";
		String Date = TimeHelper.stampToDate(TimeHelper.nowMillis()).split(" ")[0];
		String outfile = fileUrl.GetTomcatUrl() + "/File/upload/" + Date;
		File inputFile = new File(sourceFile);
		File outputFile = new File(outfile);
		try {
			if (!inputFile.exists()) {
				return jGrapeFW_Message.netMSG(5, "文件不存在");
			}
			if (!outputFile.exists()) {
				outputFile.mkdir();
			}
			outputFile = new File(outputFile + "/" + TimeHelper.nowMillis() + getUnqueue() + ".html");
			// OpenOfficeConnection connection = model.execOpenOffice();
			// DocumentConverter converter = new
			// OpenOfficeDocumentConverter(connection);
			try {
				OfficeDocumentConverter odc = model.getConverter();
				odc.convert(inputFile, outputFile);
				result = outputFile.toString();
			} catch (Exception e) {
				nlogger.logout(e);
				result = jGrapeFW_Message.netMSG(7, "文件转换失败");
			}
		} catch (Exception e) {
			nlogger.logout(e);
			if (outputFile.exists()) {
				outputFile.delete();
			}
			result = jGrapeFW_Message.netMSG(6, "不支持当前格式");
		}
		return result;
	}

	public String pdf2Jpgs(String sourceFile) throws PDFException, PDFSecurityException, IOException {
		String Date = TimeHelper.stampToDate(TimeHelper.nowMillis()).split(" ")[0];
		String destFile = fileUrl.GetTomcatUrl() + "/File/upload/" + Date;
		List<String> list = new ArrayList<String>();
		sourceFile = office2pdf(sourceFile);
		if (sourceFile.contains("errorcode")) {
			return sourceFile;
		}
		String FileName = sourceFile.substring(sourceFile.lastIndexOf("\\") + 1, sourceFile.lastIndexOf("."));
		Document document = null;
		BufferedImage img = null;
		float rotation = 0f;
		float zoom = 1.5f;

		File file = new File(destFile);
		if (!file.exists()) {
			file.mkdirs();
		}
		document = new Document();
		document.setFile(sourceFile);
		for (int i = 0; i < document.getNumberOfPages(); i++) {
			img = (BufferedImage) document.getPageImage(i, GraphicsRenderingHints.SCREEN, Page.BOUNDARY_CROPBOX,
					rotation, zoom);
			Iterator iter = ImageIO.getImageWritersBySuffix("png");
			ImageWriter writer = (ImageWriter) iter.next();
			File outFile = new File(destFile + FileName + "_" + (i + 1) + ".png");
			FileOutputStream out = new FileOutputStream(outFile);
			ImageOutputStream outImage = ImageIO.createImageOutputStream(out);
			writer.setOutput(outImage);
			writer.write(new IIOImage(img, null, null));
			list.add(new FileImg().getImageUri(outFile.toString()));
		}
		img.flush();
		document.dispose();
		return string.StringHelper.join(list);
	}

	/**
	 * office转换成html格式，并获取html文件内容
	 * 
	 * @project File
	 * @package interfaceApplication
	 * @file FileConvert.java
	 * 
	 * @param sourceFile
	 *            源文件目录
	 * @return String 文件不存在提示或者转换成功之后的文件的地址
	 * 
	 *         备注：同时删除临时文件
	 */
	public String office2htmlString(String sourceFile) {
		System.out.println("转换文件地址: " + sourceFile);
		String result = "";
		String ffilepath = office2html(sourceFile);
		if (ffilepath.contains("errorcode")) {
			return ffilepath;
		}
		File htmlFile = new File(ffilepath);
		// 获取html文件流
		StringBuffer html = new StringBuffer();
		BufferedReader br = null;
		;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(htmlFile), "gb2312"));
			while (br.ready()) {
				html.append(br.readLine());
			}
			br.close();
		} catch (FileNotFoundException e) {
			result = "文件不存在";
			e.printStackTrace();
		} catch (IOException e) {
			result = "当前格式不支持";
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (htmlFile.exists()) {
				htmlFile.delete();
			}
		}
		String string = html.toString();
		result = clearFormat(string);
		return result;
	}

	private String clearFormat(String htmlStr) {
		String Date = TimeHelper.stampToDate(TimeHelper.nowMillis()).split(" ")[0];
		String filepath = "http://" + fileUrl.GetTomcatWebUrl() + "/File/upload/" + Date; // html中包含图片的地址
		nlogger.logout("图片地址： " + filepath);
		// 获取body内容的正则
		String bodyReg = "<BODY .*</BODY>";
		Pattern bodyPattern = Pattern.compile(bodyReg);
		Matcher bodyMatcher = bodyPattern.matcher(htmlStr);
		if (bodyMatcher.find()) {
			// 获取BODY内容，并转化BODY标签为DIV
			htmlStr = bodyMatcher.group().replaceFirst("<BODY", "<DIV").replaceAll("</BODY>", "</DIV>");
		}
		// 调整图片地址
		htmlStr = htmlStr.replaceAll("<img src=\"", "<img src=\"" + filepath + "/");
		htmlStr = htmlStr.replaceAll("<IMG SRC=\"", "<IMG SRC=\"" + filepath + "/");
		// 把<P></P>转换成</div></div>保留样式
		htmlStr = htmlStr.replaceAll("(<P)([^>]*>.*?)(<\\/P>)", "<div$2</div>");
		// 把<P></P>转换成</div></div>并删除样式
		// 删除不需要的标签
		htmlStr = htmlStr.replaceAll(
				"<[/]?(font|FONT|span|SPAN|xml|XML|del|DEL|ins|INS|meta|META|[ovwxpOVWXP]:\\w+)[^>]*?>", "");
		// 删除不需要的属性
		/*
		 * htmlStr = htmlStr.replaceAll(
		 * "<([^>]*)(?:lang|LANG|class|CLASS|style|STYLE|size|SIZE|face|FACE|[ovwxpOVWXP]:\\w+)=(?:'[^']*'|\"\"[^\"\"]*\"\"|[^>]+)([^>]*)>",
		 * "<$1$2>");
		 */
		return htmlStr;
	}

	private String getUTF8StringFromGBKString(String gbkStr) {
		try {
			return new String(getUTF8BytesFromGBKString(gbkStr), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new InternalError();
		}
	}

	private byte[] getUTF8BytesFromGBKString(String gbkStr) {
		int n = gbkStr.length();
		byte[] utfBytes = new byte[3 * n];
		int k = 0;
		for (int i = 0; i < n; i++) {
			int m = gbkStr.charAt(i);
			if (m < 128 && m >= 0) {
				utfBytes[k++] = (byte) m;
				continue;
			}
			utfBytes[k++] = (byte) (0xe0 | (m >> 12));
			utfBytes[k++] = (byte) (0x80 | ((m >> 6) & 0x3f));
			utfBytes[k++] = (byte) (0x80 | (m & 0x3f));
		}
		if (k < utfBytes.length) {
			byte[] tmp = new byte[k];
			System.arraycopy(utfBytes, 0, tmp, 0, k);
			return tmp;
		}
		return utfBytes;
	}
}
