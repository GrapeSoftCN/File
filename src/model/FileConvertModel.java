package model;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;

import nlogger.nlogger;

public class FileConvertModel {
	private Process process = null;
	private OpenOfficeConnection connection = null;

	/**
	 * 启动openoffice并连接到openoffice
	 * 
	 * @return
	 */
	public OpenOfficeConnection execOpenOffice() {
		// System.out.println(getOpenOffice(0));
		// System.out.println(getOpenOffice(1));
		// String command = "E:\\OpenOffice\\OpenOffice4\\program\\soffice.exe
		// -headless -accept=\"socket,host=192.168.3.16,port=8100;urp;\"";
		String command = getIp("url")+"\\OpenOffice4\\program\\soffice.exe -headless -accept=\"socket,host="
				+ getOpenOffice(0) + ",port=" + getOpenOffice(1) + ";urp;\"";
		try {
			process = Runtime.getRuntime().exec(command);
			connection = new SocketOpenOfficeConnection(getOpenOffice(0), Integer.parseInt(getOpenOffice(1)));
			connection.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return connection;
	}

	private String getIp(String key) {
		String value = "";
		try {
			Properties pro = new Properties();
			pro.load(new FileInputStream("OfficeUrl.properties"));
			value = pro.getProperty(key);
		} catch (Exception e) {
			value = "";
		}
		return value;
	}

	private String getOpenOffice(int sign) {
		String host = null;
		try {
			if (sign == 0 || sign == 1) {
				host = getIp("openoffice").split(":")[sign];
			}
		} catch (Exception e) {
			nlogger.logout(e);
			host = null;
		}
		return host;
	}

	/**
	 * 关闭连接
	 */
	public void close(OpenOfficeConnection connection) {
		connection.disconnect();
		process.destroy();
	}
}
