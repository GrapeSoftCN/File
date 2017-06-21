package interfaceApplication;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.artofsolving.jodconverter.openoffice.converter.StreamOpenOfficeDocumentConverter;

import esayhelper.TimeHelper;
import esayhelper.jGrapeFW_Message;
import model.FileConvertModel;
import model.TranCharset;
import nlogger.nlogger;
import security.codec;

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
		sourceFile = "F:\\tomcat8.0\\webapps" + sourceFile;
		try {
			System.out.println(codeString(sourceFile));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// sourceFile = "C://JavaCode/tomcat/webapps" + sourceFile;
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
	 * @param sourceFile
	 *            源文件地址
	 * @param destFile
	 *            目标文件地址
	 * @throws IOException
	 */
	public String office2pdf(String sourceFile) throws IOException {
		File inputFile = new File(sourceFile);
		String Date = TimeHelper.stampToDate(TimeHelper.nowMillis()).split(" ")[0];
		String destFile = "C://JavaCode/tomcat/webapps/File/upload/" + Date;
		File outputFile = new File(destFile);
		if (!inputFile.exists()) {
			return getUTF8StringFromGBKString(jGrapeFW_Message.netMSG(99, "文件不存在"));
		}
		if (!outputFile.exists()) {
			outputFile.mkdir();
		}
		outputFile = new File(destFile + "\\" + (int) System.currentTimeMillis() / 1000 + ".pdf");
		OpenOfficeConnection connection = model.execOpenOffice();
		DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
		converter.convert(inputFile, outputFile);
		model.close(connection);
		return outputFile.toString();
	}

	/**
	 * office文件转换成html文件
	 * 
	 * @param sourceFile
	 *            源文件路径
	 * @param destFile
	 *            目标文件所在目录
	 */
	public String office2html(String sourceFile) {
		String Date = TimeHelper.stampToDate(TimeHelper.nowMillis()).split(" ")[0];
		// String outfile = "C://JavaCode/tomcat/webapps/File/upload/" + Date +
		// "/"
		// + (int) System.currentTimeMillis() / 1000 + ".html";
		String outfile = "F://tomcat8.0/webapps/File/upload/" + Date;
		File inputFile = new File(sourceFile);
		File outputFile = new File(outfile);
		if (!inputFile.exists()) {
			return jGrapeFW_Message.netMSG(99, "文件不存在");
		}
		if (!outputFile.exists()) {
			outputFile.mkdir();
		}
		outputFile = new File(outputFile + "/" + (int) System.currentTimeMillis() / 1000 + ".html");
		OpenOfficeConnection connection = model.execOpenOffice();
		DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
		
		converter.convert(new File(sourceFile), outputFile);
		// StreamOpenOfficeDocumentConverter converter2 = new
		// StreamOpenOfficeDocumentConverter(connection);
		// converter2.convert(new File(sourceFile), outputFile);
		model.close(connection);
		return outputFile.toString();
	}

	/**
	 * office转换成html格式，并获取html文件内容
	 * 
	 * @param sourceFile
	 *            源文件目录
	 * @param destFile
	 *            目标文件目录
	 * @return
	 */
	public String office2htmlString(String sourceFile) {
//		String string = "葡萄阿斯";
//		return string;
		String ffilepath = office2html(sourceFile);
		if (ffilepath.contains("errorcode")) {
			return ffilepath;
		}
		File htmlFile = new File(ffilepath);
		// 获取html文件流
		StringBuffer html = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(htmlFile),"gb2312"));
			while (br.ready()) {
				html.append(br.readLine());
			}
			br.close();
			// 删除临时文件
//			htmlFile.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String string = html.toString();
		System.out.println(TranCharset.getEncoding(string));
		string = TranCharset.TranEncode2utf8(string);
		string = string.replaceAll("gb2312", "utf-8");
		//String string = html.toString();
//		return string;
		return html.toString();
		// HTML文件字符串
		// String htmlStr = html.toString();
		// 返回经过清洁的html文本
		// return clearFormat(html.toString());
		
	}

	public String clearFormat(String htmlStr) {
		// 获取body内容的正则
		String bodyReg = "<BODY .*</BODY>";
		Pattern bodyPattern = Pattern.compile(bodyReg);
		Matcher bodyMatcher = bodyPattern.matcher(htmlStr);
		if (bodyMatcher.find()) {
			// 获取BODY内容，并转化BODY标签为DIV
			htmlStr = bodyMatcher.group().replaceFirst("<BODY", "<DIV").replaceAll("</BODY>", "</DIV>");
		}

		// 调整图片地址
		// htmlStr = htmlStr.replaceAll("<IMG SRC=\"", "<IMG SRC=\"" + filepath
		// + "/");
		// 把<P></P>转换成</div></div>保留样式
		// content = content.replaceAll("(<P)([^>]*>.*?)(<\\/P>)",
		// "<div$2</div>");
		// 把<P></P>转换成</div></div>并删除样式
		htmlStr = htmlStr.replaceAll("(<P)([^>]*)(>.*?)(<\\/P>)", "<p$3</p>");
		// 删除不需要的标签
		htmlStr = htmlStr.replaceAll(
				"<[/]?(font|FONT|span|SPAN|xml|XML|del|DEL|ins|INS|meta|META|[ovwxpOVWXP]:\\w+)[^>]*?>", "");
		// 删除不需要的属性
		htmlStr = htmlStr.replaceAll(
				"<([^>]*)(?:lang|LANG|class|CLASS|style|STYLE|size|SIZE|face|FACE|[ovwxpOVWXP]:\\w+)=(?:'[^']*'|\"\"[^\"\"]*\"\"|[^>]+)([^>]*)>",
				"<$1$2>");

		return StringEscapeUtils.unescapeJava(htmlStr);
	}

	public static String codeString(String fileName) throws Exception {
		BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));
		int p = (bin.read() << 8) + bin.read();
		String code = null;

		switch (p) {
		case 0xefbb:
			code = "UTF-8";
			break;
		case 0xfffe:
			code = "Unicode";
			break;
		case 0xfeff:
			code = "UTF-16BE";
			break;
		default:
			code = "GBK";
		}

		return code;
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

	public static void main(String[] args) {
		FileConvert convert = new FileConvert();
		// String file = "F:"+"\\"+"test.doc";
		// System.out.println(file);
		System.out.println(convert.office2htmlString("F:\\tomcat8.0\\webapps\\File\\upload\\2017-06-21\\微会议.xls"));
		// System.out.println(convert.office2pdf(file));
	}
}
