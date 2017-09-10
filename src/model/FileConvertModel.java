package model;


import org.jodconverter.OfficeDocumentConverter;
import org.jodconverter.office.DefaultOfficeManagerBuilder;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;

import com.alibaba.druid.sql.dialect.mysql.ast.MysqlForeignKey.On;

import nlogger.nlogger;

public class FileConvertModel {
	private static GetFileUrl fileUrl = new GetFileUrl();
	private static OfficeManager om;

	static{
//		String ip = fileUrl.getOpenOffice(0);
		initOffice();
	}
	public static void initOffice(){
		String port = fileUrl.getOpenOffice(1);
		DefaultOfficeManagerBuilder omConfig = new DefaultOfficeManagerBuilder();
		omConfig.setOfficeHome(fileUrl.getOpenOfficeUrl());
		omConfig.setPortNumber( (new Integer(port)).intValue() );
		omConfig.setTaskExecutionTimeout(1000 * 60 * 10L);//
		// 设置任务执行超时为5分钟
		omConfig.setTaskQueueTimeout(1000 * 60 * 60 * 24L);//
		// 设置任务队列超时为24小时
		om = omConfig.build();
	}
	public void startServ(){
		if( !om.isRunning() ){
			try {
				om.start();
				Thread.sleep(1000);
			} catch (OfficeException | InterruptedException e) {
				nlogger.logout(e);
				e.printStackTrace();
				try {
					om.stop();
				} catch (OfficeException e1) {
					;
				}
			}
		}
	}
	public OfficeDocumentConverter getConverter(){
		startServ();
		return new OfficeDocumentConverter(om);
	}
}
