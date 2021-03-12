package com.lwh147.clouddisk.model.action;

import com.alibaba.fastjson.JSON;
import com.lwh147.clouddisk.entity.User;
import com.lwh147.clouddisk.model.service.UserService;
import org.apache.struts2.ServletActionContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Controller("userAction")	//注解为控制器
@Scope("prototype")	//作用范围
public class UserAction {
	@Resource	//注入service
	private UserService userService;
	private HttpServletResponse response;
	private HttpServletRequest	request;
	private HttpSession session;
	private File uploadFile;		//保存用户上传的文件
	private int SIZE;				//用户默认的网盘大小(MB)
	private String saveRootPath;	//文件存放的根目录(不分用户)
	
	//构造函数
	public UserAction() throws UnsupportedEncodingException {
		response = ServletActionContext.getResponse();
		request = ServletActionContext.getRequest();
		session = request.getSession();
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/json");
		SIZE = 100;	//MB
		saveRootPath = "/res/disk";
	}

	//用户注册
	public void register() throws IOException{
		String uname = request.getParameter("uname");
		String passwd = request.getParameter("passwd");
		User user = new User();
		user.setUname(uname);
		user.setPasswd(passwd);
		user.setSize(SIZE);
		if (userService.register(user)){
			String jsonStr = JSON.toJSONString("1");
			PrintWriter pw = response.getWriter();
			pw.write(jsonStr);
			pw.close();
			//注册成功，给该用户分配存储空间
			String userRootSavePath = request.getServletContext().getRealPath(saveRootPath + "/" + uname);
			File newFolder = new File(userRootSavePath);
			if (!newFolder.exists())
				newFolder.mkdir();
			return;
		}
		String jsonStr = JSON.toJSONString("0");
		PrintWriter pw = response.getWriter();
		pw.write(jsonStr);
		pw.close();
		return;
	}
	
	//用户登录验证
	public void checkLogin() throws IOException{
		String uname = request.getParameter("uname");
		String passwd = request.getParameter("passwd");
		User user = new User();
		user.setUname(uname);
		user.setPasswd(passwd);
		String checkResult = userService.checkUserInfo(user);
		if (checkResult.equals("1"))
			session.setAttribute("loginUser", uname);
		checkResult = JSON.toJSONString(checkResult);
		PrintWriter pw = response.getWriter();
		pw.write(checkResult);
		pw.close();
		return;
	}
	
	//获取当前登录用户信息
	public void getLoginUserInfo() throws IOException{
		Object obj = session.getAttribute("loginUser");
		if (obj==null){
			String jsonStr = JSON.toJSONString("0");
			PrintWriter pw = response.getWriter();
			pw.write(jsonStr);
			pw.close();
			return;
		}
		String uname = obj.toString();
		User user = userService.getUserInfoByUname(uname);
		String jsonStr = JSON.toJSONString(user);
		PrintWriter pw = response.getWriter();
		pw.write(jsonStr);
		pw.close();
		return;
	}
	
	//退出登录
	public void logout() throws IOException{
		session.setAttribute("loginUser", null);
		session.invalidate(); 
		String jsonStr = JSON.toJSONString("1");
		PrintWriter pw = response.getWriter();
		pw.write(jsonStr);
		pw.close();
		return;
	}
	
	//用户上传文件，空间是否已满在前端上传文件前判断。
	public void userUploadFile() throws IOException{
		String jsonStr = JSON.toJSONString("1");
		PrintWriter pw = response.getWriter();
		//http请求默认为2M，上传文件大于该值便要对http请求头大小限制进行修改，同时还要对web项目中struts的文件上传缓冲池进行修改
		if (uploadFile != null) {
			String uname = session.getAttribute("loginUser").toString();
			String fileName = request.getParameter("fileName");
			String relativeSavePath = request.getParameter("relativeSavePath");
			// 该路径为已打包发布的web项目的路径
			// G:\Projects_MyEclipse\.metadata\.me_tcat85\webapps\CloudDisk\...
			// getServletContext方法是得到webroot的绝对路径
			// getRealPath方法会将虚拟路径转换为windows操作系统文件路径标识符\
			String absoluteSaveRootPath = request.getServletContext().getRealPath(saveRootPath + "/" + uname + relativeSavePath + "/" + fileName);
			File file = new File(absoluteSaveRootPath);
			System.out.println("====================>路径：" + absoluteSaveRootPath);
			if (!file.exists())
				file.createNewFile();
			FileInputStream input = new FileInputStream(uploadFile);
			FileOutputStream output = new FileOutputStream(file);
			byte[] buf = new byte[4096];
			int length = 0;
			while ((length = input.read(buf)) != -1) {
				output.write(buf, 0, length);
			}
			input.close();
			output.flush();
			output.close();
		}else
			jsonStr = JSON.toJSONString("0");
		pw.write(jsonStr);
		pw.close();
		return;
	}
	
	//获取当前用户剩余空间大小（MB）
	public void getRemainder() throws IOException{
		String uname = session.getAttribute("loginUser").toString();
		String root = request.getServletContext().getRealPath(saveRootPath + "/" + uname);
		File rootFolder = new File(root);
		//调用递归计算文件夹大小
		long size = getFolderSize(rootFolder)/1048576;
		String jsonStr = JSON.toJSONString(String.valueOf(SIZE-size));
		PrintWriter pw = response.getWriter();
		pw.write(jsonStr);
		pw.close();
		return;
	}
	
	//用户创建文件夹
	public void createFolder() throws IOException{
		String uname = session.getAttribute("loginUser").toString();
		String folderName = request.getParameter("folderName");
		String relativeSavePath = request.getParameter("relativeSavePath");
		String root = request.getServletContext().getRealPath(saveRootPath + "/" + uname + relativeSavePath + "/" + folderName);
		File newFolder = new File(root);
		String jsonStr = JSON.toJSONString("1");
		PrintWriter pw = response.getWriter();
		if (!newFolder.exists())
			newFolder.mkdir();
		else
			jsonStr = JSON.toJSONString("0");
		pw.write(jsonStr);
		pw.close();
		return;
	}
	
	//删除文件及文件夹
	public void deleteFile() throws IOException{
		String uname = session.getAttribute("loginUser").toString();
		String fileName = request.getParameter("fileName");
		String relativeSavePath = request.getParameter("relativeSavePath");
		String root = request.getServletContext().getRealPath(saveRootPath + "/" + uname + relativeSavePath + "/" + fileName);
		File newFile = new File(root);
		String jsonStr = JSON.toJSONString("1");
		PrintWriter pw = response.getWriter();
		if (!delfile(newFile))
			jsonStr = JSON.toJSONString("0");
		pw.write(jsonStr);
		pw.close();
		return;
	}
	
	//显示用户文件列表
	public void showUserFile() throws IOException{
		Map<String, String> fileInfoList = new HashMap<String, String>();
		String uname = session.getAttribute("loginUser").toString();
		String relativeSavePath = request.getParameter("relativeSavePath");
		String root = request.getServletContext().getRealPath(saveRootPath + "/" + uname + relativeSavePath);
		File folder = new File(root);
		PrintWriter pw = response.getWriter();
		String jsonStr = "-1";
		if (folder.exists()){
			File[] fileList = folder.listFiles();
			for (File f:fileList){
				String fileName = f.getName();
				String fileInfo = String.valueOf(f.lastModified()) + "-" + String.valueOf(f.length()/1048576);
				fileInfoList.put(fileName, fileInfo);
			}
			jsonStr = JSON.toJSONString(fileInfoList);
		}
		pw.write(jsonStr);
		pw.close();
		return;
	}
	
	//获取文件绝对路径
	public void getFileDownLoadTruePath() throws IOException{
		String uname = session.getAttribute("loginUser").toString();
		String fileName = request.getParameter("fileName");
		String relativeSavePath = request.getParameter("relativeSavePath");
		String root = request.getServletContext().getRealPath(saveRootPath + "/" + uname + relativeSavePath + "/" + fileName);
		String jsonStr = JSON.toJSONString(root);
		PrintWriter pw = response.getWriter();
		pw.write(jsonStr);
		pw.close();
		return;
	}
	
	//递归计算文件夹大小(MB)
	private long getFolderSize(File file){
		long size = 0;
		File[]	fileList = file.listFiles();
		if(fileList == null){
			return 0;
		}
		for (int i=0; i<fileList.length; i++){
			if (fileList[i].isDirectory())
				size += getFolderSize(fileList[i]);
			else
				size += fileList[i].length();
		}
		return size;
	}
	
	//递归删除文件
	private boolean delfile(File file){
		if (!file.exists())
			return false;
		if (file.isFile()){
			return file.delete();
		} else{
			File[] fileList = file.listFiles();
			for (File f:fileList){
				delfile(f);
			}
			return file.delete();
		}
	}
	
	//get\set
	public File getUploadFile() {
		return uploadFile;
	}
	public void setUploadFile(File uploadFile) {
		this.uploadFile = uploadFile;
	}

}
