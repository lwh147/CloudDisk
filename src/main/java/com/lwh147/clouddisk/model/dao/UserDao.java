package com.lwh147.clouddisk.model.dao;

import com.lwh147.clouddisk.entity.User;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository	//Dao注解
public class UserDao {
	@Resource	//注入会话工厂
	private SessionFactory sf;
	
	//查询用户列表
	public List<User> getUserList(){
		Session session = sf.openSession();
		String HQL = "from User";
		Query query = session.createQuery(HQL);
		Object obj = query.list();
		if (obj==null)
			return null;
		@SuppressWarnings("unchecked")
		List<User> list = (List<User>)obj;
		session.close();
		return list;
	}
	
	//根据id查询指定用户信息
	public User getUserById(int id){
		Session session = sf.openSession();
		Object obj = session.get(User.class, id);
		if (obj==null)
			return null;
		User user = (User)obj;
		session.close();
		return user;
	}
	
	//根据uname查询指定用户信息
	public User getUserByUname(String uname){
		Session session = sf.openSession();
		String HQL = "from User u where u.uname = ";
		HQL += "\'"+uname+"\'";
		Query query = session.createQuery(HQL);
		Object obj = query.uniqueResult();
		if (obj==null)
			return null;
		User user = (User)obj;
		session.close();
		return user;
	}
	
	//添加用户
	public void insertUser(User user){
		Session session = sf.openSession();
		session.save(user);
		session.close();
	}
}
