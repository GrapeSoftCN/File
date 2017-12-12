package interfaceApplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.sun.mail.handlers.message_rfc822;

import JGrapeSystem.jGrapeFW_Message;
import model.GetFileUrl;
import nlogger.nlogger;

public class ConvertVideo {
	private GetFileUrl fileUrl = new GetFileUrl();
	String dir = fileUrl.GetTomcatUrl();
	String ffmpegurl = fileUrl.getVideoUrl();

	public String ConvertMP4(String filepath) {
		// 补充路径
		String extName = filepath.substring(filepath.indexOf(".") + 1, filepath.length()).toLowerCase();
		int ckcode = Check(extName);
		switch (ckcode) {
		case 1:
			filepath = processAVI(filepath);
		case 2:
			filepath = processMP4(filepath);
			break;
		}
		return filepath;
	}

	/**
	 * 判断文件是否可以直接被转换
	 * 
	 * @project File
	 * @package model
	 * @file ConvertVideo.java
	 * 
	 * @param extName
	 * @return 1：不能直接转换，需转换为中间格式avi 2：可以直接转换
	 *
	 */
	private int Check(String extName) {
		int ckcode = 0;
		if (extName.equals("wmv9") || extName.equals("rm") || extName.equals("rmvb")) {
			ckcode = 1;
		} else {
			ckcode = 2;
		}
		return ckcode;
	}

	/**
	 * 对于ffmpeg解析不了的格式，需通过工具mencoder转换成avi格式
	 * 
	 * @return
	 * 
	 */
	private String processAVI(String path) {
		String name = path.substring(0, path.indexOf(".")).toLowerCase(); // 获取不带后缀名的文件名称
		String outpath = dir + name;
		List<String> command = new ArrayList<String>();
		command.add(ffmpegurl);
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
		return Convert(command);
	}

	/**
	 * 对于ffmpeg解析不了的格式，需通过工具mencoder转换成avi格式
	 * 
	 * @return
	 * 
	 */
	private String processMP4(String path) {
		String name = path.substring(0, path.indexOf(".")); // 获取不带后缀名的文件名称
		String outpath = dir + name + ".mp4";
		List<String> command = new ArrayList<String>();
		command.add(ffmpegurl);
		command.add("-y");
		command.add("-i");
		command.add(dir + path);
		command.add("-q:a");
		command.add("0");
		command.add("-acodec");
		command.add("libvorbis");
		command.add(outpath);
		String message = Convert(command);
		return (JSONObject.toJSON(message).getLong("errorcode") == 0) ? jGrapeFW_Message.netMSG(0, outpath) : message;
	}

	private String Convert(List<String> command) {
		String Message = jGrapeFW_Message.netMSG(0, "转换成功");
		BufferedReader br = null;
		Process process = null;
		try {
			process = new ProcessBuilder(command).redirectErrorStream(true).start();
			br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while (br.readLine()!=null) {
			}
			System.out.println("视频转换成功");
			Thread.sleep(1000);
		} catch (Exception e) {
			nlogger.logout(e);
		} finally {
			try {
				br.close();
				process.destroy();
			} catch (Exception e) {
				nlogger.logout(e);
			}
		}
		return Message;
	}
}
