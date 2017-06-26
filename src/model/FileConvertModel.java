package model;

import java.io.IOException;

import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;

public class FileConvertModel {
	private Process process = null;
	private OpenOfficeConnection connection = null;

	/**
	 * 启动openoffice并连接到openoffice
	 * 
	 * @return
	 */
	public OpenOfficeConnection execOpenOffice() {
		String ip = GetFileUrl.getOpenOffice(0);
		String port = GetFileUrl.getOpenOffice(1);
		String command = GetFileUrl.getOpenOfficeUrl() + "\\program\\soffice.exe -headless -accept=\"socket,host="
				+ ip + ",port=" + port + ";urp;\"";
		try {
			process = Runtime.getRuntime().exec(command);
			connection = new SocketOpenOfficeConnection(ip, Integer.parseInt(port));
			connection.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return connection;
	}

	/**
	 * 关闭连接
	 */
	public void close(OpenOfficeConnection connection) {
		connection.disconnect();
		process.destroy();
	}
}
