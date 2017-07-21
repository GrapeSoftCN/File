package interfaceApplication;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import JGrapeSystem.jGrapeFW_Message;
import json.JSONHelper;
import model.OpFile;
import nlogger.nlogger;
import time.TimeHelper;

@WebServlet("/VideoConvert")
public class VideoConvert extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private OpFile model = new OpFile();

	public VideoConvert() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String InputPath = "";
		String Date = TimeHelper.stampToDate(TimeHelper.nowMillis()).split(" ")[0];
		String OutputPath = "C://JavaCode/tomcat/webapps/File/upload/" + Date + "/Convert";

		String appid = request.getParameter("appid");
		String fileid = request.getParameter("fileid");
		int ckcode = model.get_file_type(appid, fileid);
		// 获取文件路径
		JSONObject object = model.find(appid, fileid);
		if (object != null) {
			InputPath = object.get("filePath").toString();
			String name = object.get("fileoldname").toString();
			OutputPath = OutputPath + name;
		}
		String message = "";
		switch (ckcode) {
		case 0:
			message = "非标准格式，不能进行格式转换";
			break;
		case 1:
			message = getImageUri(processAVI(InputPath, OutputPath));
			break;
		case 2:
			message = getImageUri(processOGG(InputPath, OutputPath));
			break;
		default:
			message = "视频格式转换失败，无法转换";
			break;
		}
		message = jGrapeFW_Message.netMSG(0, message);
		response.getWriter().write(message);
	}

	public String processOGG(String InputPath, String OutputPath) {
		String type = InputPath.substring(InputPath.indexOf(".") + 1, InputPath.length()).toLowerCase();
		if (type.equals("ogg")) {
			return InputPath;
		}
		// 获取视频文件总时间
		int size = getTotal(InputPath);
		// 格式转换
		List<String> commend = new ArrayList<String>();
		commend.add("e:\\ffmpegtest\\ffmpeg.exe");
		commend.add("-y");
		commend.add("-i");
		commend.add(InputPath);
		commend.add("-qscale");
		commend.add("0");
		commend.add("-acodec");
		commend.add("libvorbis");
		commend.add(OutputPath);
		execCommand(commend, size);
		return OutputPath;
	}

	/**
	 * 对于ffmpeg解析不了的格式，需通过工具mencoder转换成avi格式
	 * 
	 * @return
	 */
	private String processAVI(String path, String outpath) {
		List<String> command = new ArrayList<String>();
		command.add("e:\\ffmpegtest\\mencoder.exe");
		command.add(path);
		command.add("-oac");
		command.add("lavc");
		command.add("-lavcopts");
		command.add("acodec=mp3:abitrate=64");
		command.add("-ovc");
		command.add("xvid");
		command.add("-xvidencopts");
		command.add("bitrate=600");
		command.add("-of");
		command.add("avi");
		command.add("-o");
		command.add(outpath);
		return outpath;
	}

	/**
	 * 调用外部程序执行视频格式转换
	 * 
	 * @param command
	 * @param totalSize
	 */
	private void execCommand(List<String> command, int totalSize) {
		// try {
		// ProcessBuilder builder = new ProcessBuilder();
		// builder.command(command);
		// Process p = builder.start();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		try {
			Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while (br.readLine() == null) {
				br.close();
				process.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int getTotal(String path) {
		String duration = null;
		String cmd = "e:\\ffmpegtest\\ffprobe.exe" + " -v quiet -print_format json -show_format -i " + path;
		try {
			Runtime run = Runtime.getRuntime();
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			StringBuffer s = new StringBuffer();
			String lineStr;
			while ((lineStr = inBr.readLine()) != null) {
				s.append(lineStr);
			}
			JSONObject _obj = JSONHelper.string2json(s.toString());
			duration = JSONHelper.string2json(_obj.get("format").toString()).get("duration").toString();
		} catch (Exception e) {
		}
		return (int) Double.parseDouble(duration);
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

	// 获取文件url[内网url或者外网url]，0表示内网，1表示外网
	private String getFileHost(int signal) {
		String host = null;
		try {
			if (signal == 0 || signal == 1) {
				host = getAppIp("file").split("/")[signal];
			}
		} catch (Exception e) {
			nlogger.logout(e);
			host = null;
		}
		return host;
	}
	//截取文件路径[\File\。。。]
	private String getImageUri(String videoURL){
		String subString;
		String rString = null;
		int i = videoURL.indexOf("webapps");
		if( i >= 0){
			subString = videoURL.substring(i + 7);
			rString = subString.split("/")[0];
		}
		return rString;
	}
}
