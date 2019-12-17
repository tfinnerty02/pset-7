package com.apcsa.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.apcsa.model.User;

public class Student extends User {
	
	public Student(User user, ResultSet rs) throws SQLException {
		// TODO Auto-generated constructor stub

	}
	private int studentId;
    private int classRank;
    private int gradeLevel;
    private int graduationYear;
    private double gpa;
    private String firstName;
    private String lastName;
	
}