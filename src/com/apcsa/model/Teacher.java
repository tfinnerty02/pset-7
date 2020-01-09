package com.apcsa.model;

import java.sql.ResultSet;

import com.apcsa.model.User;
import java.sql.SQLException;

public class Teacher extends User {
	
	/**
	 * Creates an instance of the Teacher class.
	 * 
	 * @param user
	 * @param rs
	 * @throws SQLException
	 */

	public Teacher(User user, ResultSet rs) throws SQLException {
	    super(user);
	    
	    this.teacherId = rs.getInt("teacher_id");
	    this.departmentId = rs.getInt("department_id");
	    this.firstName = rs.getString("first_name");
	    this.lastName = rs.getString("last_name");
	    this.departmentName = rs.getString("title");
	}
	
	/**
	 * Creates an instance of the Teacher class.
	 * 
	 * @param rs
	 * @throws SQLException
	 */

	public Teacher(ResultSet rs) throws SQLException {
	    super(-1, "teacher", null, null, null);
	    
	    this.teacherId = rs.getInt("teacher_id");
	    this.departmentId = rs.getInt("department_id");
	    this.firstName = rs.getString("first_name");
	    this.lastName = rs.getString("last_name");
	    this.departmentName = rs.getString("title");
	}
	
	/**
	 * @return departmentName
	 */

	public String getDepartmentName() {
	    return departmentName;
	}

	/**
	 * Retrieves the student's name formatted as LAST, FIRST.
	 * 
	 * @return the formatted name
	 */

	public String getName() {
	    return lastName + ", " + firstName;
	}
	
	public int getTeacher_id() {
		return teacherId;
	}
	
	
	private int teacherId;
    private int departmentId;
    private String firstName;
    private String lastName;
    private String departmentName;

}