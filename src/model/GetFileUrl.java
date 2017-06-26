package model;

import java.io.FileInputStream;
import java.util.Properties;

import nlogger.nlogger;

public class GetFileUrl {
	/**
	 * 读取配置文件
	 * 
	 * @project File
	 * @package model
	 * @file GetFileUrl.java
	 * 
	 * @param key
	 *            配置文件中 key值
	 * @return
	 *
	 */
	private static String getConfig(String key) {
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

	/**
	 * 获取openoffice安装地址
	 * 
	 * @project File
	 * @package model
	 * @file GetFileUrl.java
	 * 
	 * @return
	 *
	 */
	public static String getOpenOfficeUrl() {
		return getConfig("openofficeurl");
	}

	/**
	 * 获取openoffice ip和端口
	 * 
	 * @project File
	 * @package model
	 * @file GetFileUrl.java
	 * 
	 * @param sign
	 *            0：获取openoffice ip 1：获取openoffice 端口
	 * @return
	 *
	 */
	public static String getOpenOffice(int sign) {
		String host = null;
		try {
			if (sign == 0 || sign == 1) {
				host = getConfig("openoffice").split(":")[sign];
			}
		} catch (Exception e) {
			nlogger.logout(e);
			host = null;
		}
		return host;
	}

	/**
	 * 获取tomcat安装路径地址
	 * 
	 * @project File
	 * @package model
	 * @file GetFileUrl.java
	 * 
	 * @return
	 *
	 */

	public static String GetTomcatUrl() {
		return getConfig("tomcatUrl");
	}

	/**
	 * 获取tomcat访问路径
	 * 
	 * @project File
	 * @package model
	 * @file GetFileUrl.java
	 * 
	 * @return
	 *
	 */

	public static String GetTomcatWebUrl() {
		return getConfig("tomcatWebUrl");
	}

	/**
	 * 视频截图工具安装地址
	 * 
	 * @project File
	 * @package model
	 * @file GetFileUrl.java
	 * 
	 * @return
	 *
	 */
	public static String getVideoUrl() {
		return getConfig("ffmpegUrl");
	}

	/**
	 * 视频截图时间设置，即截取多少秒之后的图片
	 * 
	 * @project File
	 * @package model
	 * @file GetFileUrl.java
	 * 
	 * @return
	 *
	 */
	public static String getTime() {
		return getConfig("Video");
	}

	/**
	 * 视频缩略图大小
	 * 
	 * @project File
	 * @package model
	 * @file GetFileUrl.java
	 * 
	 * @return
	 *
	 */
	public static String getSize() {
		return getConfig("VideoSize");
	}

	/**
	 * 图片缩略图大小
	 * 
	 * @project File
	 * @package model
	 * @file GetFileUrl.java
	 * 
	 * @return
	 *
	 */
	public static String getImgSize(int Sign) {
		String value = null;
		try {
			if (Sign == 0 || Sign == 1) {
				value = getConfig("ImgSize").split(":")[Sign];
			}
		} catch (Exception e) {
			nlogger.logout(e);
			value = null;
		}
		return value;
	}

	/**
	 * 图片缩略图格式，如jpg。。。
	 * 
	 * @project File
	 * @package model
	 * @file GetFileUrl.java
	 * 
	 * @return
	 *
	 */
	public static String getImgType() {
		return getConfig("ImgType");
	}

	/**
	 * 图片缩略图质量
	 * 
	 * @project File
	 * @package model
	 * @file GetFileUrl.java
	 * 
	 * @return
	 *
	 */
	public static String getImgQuality() {
		return getConfig("ImgQuality");
	}
}
