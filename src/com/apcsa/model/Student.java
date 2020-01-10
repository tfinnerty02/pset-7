package com.apcsa.model;

import java.sql.ResultSet;

import java.sql.SQLException;

import com.apcsa.model.User;

public class Student extends User {
	
	/**
	 * Creates an instance of the Student class.
	 * 
	 * @param user
	 * @param rs
	 * @throws SQLException
	 */

	public Student(User user, ResultSet rs) throws SQLException {
	    super(user);
	    
	    this.studentId = rs.getInt("student_id");
	    this.classRank = rs.getInt("class_rank");
	    this.gradeLevel = rs.getInt("grade_level");
	    this.graduationYear = rs.getInt("graduation");
	    this.gpa = rs.getDouble("gpa");
	    this.firstName = rs.getString("first_name");
	    this.lastName = rs.getString("last_name");
	}
	
	public Student(ResultSet rs) throws SQLException {
	    super(-1, "student", null, null, null);
	    
	    this.studentId = rs.getInt("student_id");
	    this.classRank = rs.getInt("class_rank");
	    this.gradeLevel = rs.getInt("grade_level");
	    this.graduationYear = rs.getInt("graduation");
	    this.gpa = rs.getDouble("gpa");
	    this.firstName = rs.getString("first_name");
	    this.lastName = rs.getString("last_name");
	}
	
	private int studentId;
    private int classRank;
    private int gradeLevel;
    private int graduationYear;
    private double gpa;
    private String firstName;
    private String lastName;
    
    public double getGpa() {
    	return gpa;
    }
    
    public void setClassRank(int rank) {
    	classRank = rank;
    }
    
    public String getName() {
    	return lastName + ", " + firstName;
    }
    
    public int getGradYear() {
    	return graduationYear;
    }
    
    public int getRank() {
    	return classRank;
    }
    
    public int getStudentId() {
    	return studentId;
    }
    
    public String getFirstName() {
    	return firstName;
    }
	
}