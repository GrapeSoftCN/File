package interfaceApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

import JGrapeSystem.jGrapeFW_Message;
import model.FileConvertModel;
import model.GetFileUrl;
import security.codec;
import time.TimeHelper;

@WebServlet("/FileConvert")
public class FileConvert extends HttpServlet {
	private FileConvertModel model = new FileConvertModel();
	private static final long serialVersionUID = 1L;

	public FileConvert() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		String sourceFile = request.getParameter("sourceFile");
		sourceFile = codec.DecodeHtmlTag(sourceFile);
		sourceFile = GetFileUrl.GetTomcatUrl() + sourceFile;
		int type = Integer.parseInt(request.getParameter("type"));
		switch (type) {
		case 0: // 转换成pdf
			response.getWriter().write(office2pdf(sourceFile));
			break;
		case 1: // office转换成html
			response.getWriter().write(office2html(sourceFile));
			break;
		case 2: // office转换成html格式，并获取html文件内容
			response.getWriter().write(office2htmlString(sourceFile));
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
		String destFile = GetFileUrl.GetTomcatUrl() + "/File/upload/" + Date;
		File outputFile = new File(destFile);
		if (!inputFile.exists()) {
			return getUTF8StringFromGBKString(jGrapeFW_Message.netMSG(99, "文件不存在"));
		}
		if (!outputFile.exists()) {
			outputFile.mkdir();
		}
		outputFile = new File(destFile + "\\" + TimeHelper.nowMillis() + ".pdf");
		OpenOfficeConnection connection = model.execOpenOffice();
		DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
		converter.convert(inputFile, outputFile);
		model.close(connection);
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
		String Date = TimeHelper.stampToDate(TimeHelper.nowMillis()).split(" ")[0];
		String outfile = GetFileUrl.GetTomcatUrl() + "/File/upload/" + Date;
		File inputFile = new File(sourceFile);
		File outputFile = new File(outfile);
		if (!inputFile.exists()) {
			return jGrapeFW_Message.netMSG(99, "文件不存在");
		}
		if (!outputFile.exists()) {
			outputFile.mkdir();
		}
		outputFile = new File(outputFile + "/" + TimeHelper.nowSecond() + ".html");
		OpenOfficeConnection connection = model.execOpenOffice();
		DocumentConverter converter = new OpenOfficeDocumentConverter(connection);

		converter.convert(inputFile, outputFile);
		model.close(connection);
		return outputFile.toString();
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
		String ffilepath = office2html(sourceFile);
		if (ffilepath.contains("errorcode")) {
			return ffilepath;
		}
		File htmlFile = new File(ffilepath);
		// 获取html文件流
		StringBuffer html = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(htmlFile), "gb2312"));
			while (br.ready()) {
				html.append(br.readLine());
			}
			br.close();
			// 删除临时文件
			htmlFile.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String string = html.toString();
		return clearFormat(string);
//		 return string;
		// HTML文件字符串
		// String htmlStr = html.toString();
		// 返回经过清洁的html文本
		// return clearFormat(html.toString());

	}

	private String clearFormat(String htmlStr) {
		String Date = TimeHelper.stampToDate(TimeHelper.nowMillis()).split(" ")[0];
		String filepath = "http://" + GetFileUrl.GetTomcatWebUrl() + "/File/upload/" + Date; // html中包含图片的地址
		// 获取body内容的正则
		String bodyReg = "<BODY .*</BODY>";
		Pattern bodyPattern = Pattern.compile(bodyReg);
		Matcher bodyMatcher = bodyPattern.matcher(htmlStr);
		if (bodyMatcher.find()) {
			// 获取BODY内容，并转化BODY标签为DIV
			htmlStr = bodyMatcher.group().replaceFirst("<BODY", "<DIV").replaceAll("</BODY>", "</DIV>");
		}

		// 调整图片地址
		htmlStr = htmlStr.replaceAll("<IMG SRC=\"", "<IMG SRC=\"" + filepath + "/");
		// 把<P></P>转换成</div></div>保留样式
		htmlStr = htmlStr.replaceAll("(<P)([^>]*>.*?)(<\\/P>)", "<div$2</div>");
		// 把<P></P>转换成</div></div>并删除样式
		// htmlStr = htmlStr.replaceAll("(<P)([^>]*)(>.*?)(<\\/P>)",
		// "<p$3</p>");
		// 删除不需要的标签
		htmlStr = htmlStr.replaceAll(
				"<[/]?(font|FONT|span|SPAN|xml|XML|del|DEL|ins|INS|meta|META|[ovwxpOVWXP]:\\w+)[^>]*?>", "");
		// 删除不需要的属性
		htmlStr = htmlStr.replaceAll(
				"<([^>]*)(?:lang|LANG|class|CLASS|style|STYLE|size|SIZE|face|FACE|[ovwxpOVWXP]:\\w+)=(?:'[^']*'|\"\"[^\"\"]*\"\"|[^>]+)([^>]*)>",
				"<$1$2>");

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
