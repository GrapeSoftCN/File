<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<form action="http://127.0.0.1:1006/11/GrapeFile/uploadFile/upload/123" method="post"
		enctype="multipart/form-data" onsubmit="return submitCheck()">
		<table id="stat_data_list" cellpadding=0 cellspacing=0 border=0>
			<tr>
				<td>导入的表名:</td>
				<td><input type="text" name="tableName" /></td>
			</tr>
			<tr>
				<td>导入的文件:</td>
				<td><input type="file" name="file" id="file" /></td>
			</tr>
			<tr>
				<td style="text-align: center;" colspan="2">
				<input type="submit" value="提交" class="button" />
				<input type="button" value="取消" class="button" onclick="window.history.go(-1)" /></td>
			</tr>
		</table>
	</form>
</body>
</html>