package org.example.entity;

import javax.persistence.*;

@Entity	//实体注解
@Table(name="user",catalog="test")	//表映射
public class User {
	@Id	//主键
	@GeneratedValue(strategy = GenerationType.IDENTITY)	//自增策略
	private int id;
	private String uname;
	private String passwd;
	private int size;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUname() {
		return uname;
	}
	public void setUname(String uname) {
		this.uname = uname;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	
}
