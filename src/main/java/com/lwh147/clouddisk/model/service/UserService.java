package com.lwh147.clouddisk.model.service;

import com.lwh147.clouddisk.entity.User;
import com.lwh147.clouddisk.model.dao.UserDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service	//注解为service
public class UserService {
	@Resource	//注入dao
	private UserDao userDao;
	
	//用户注册
	public Boolean register(User user){
		User user1 = userDao.getUserByUname(user.getUname());
		if (user1==null){	//说明该用户名未被注册，可以注册
			userDao.insertUser(user);
			return true;
		}else				//说明用户名已经被注册
			return false;
	}
	
	//验证用户名密码
	public String checkUserInfo(User user){
		User user1 = userDao.getUserByUname(user.getUname());
		if (user1==null)
			return "-1";
		else if (user1.getPasswd().equals(user.getPasswd()))
			return "1";
		else
			return "0";
	}
	
	//获取指定用户名的用户信息
	public User getUserInfoByUname(String uname){
		return userDao.getUserByUname(uname);
	}
}
