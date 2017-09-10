package model;

public class FileImg {
	public String getImageUri(String imageURL) {
		int i = 0;
		if (imageURL.contains("File//upload")) {
			i = imageURL.toLowerCase().indexOf("file//upload");
			imageURL = "\\" + imageURL.substring(i);
		}
		if (imageURL.contains("File\\upload")) {
			i = imageURL.toLowerCase().indexOf("file\\upload");
			imageURL = "\\" + imageURL.substring(i);
		}
		if (imageURL.contains("File/upload")) {
			i = imageURL.toLowerCase().indexOf("file/upload");
			imageURL = "\\" + imageURL.substring(i);
		}
		return imageURL;
	}
	
	public static String getIcon(String extName) {
		String out = "icon\\";
		extName = extName.toLowerCase();
		switch (extName) {
		case "doc":
		case "docx":
			out = out+"word.ico";
			break;
		case "xls":
		case "xlsx":
			out = out+"excel.ico";
			break;
		case "ppt":
			out = out+"PowerPoint.ico";
			break;
		case "txt":
			out = out+"txt.ico";
			break;
		case "htm":
		case "html":
			out = out+"html.ico";
			break;
		case "pdf":
			out = out+"pdf.ico";
			break;
		case "mp3":
			out = out+"mp3.ico";
			break;
		case "wav":
			out = out+"wav.ico";
			break;
		default:
			out = out+"other.ico";
			break;
		}
		return out;
	}
}
