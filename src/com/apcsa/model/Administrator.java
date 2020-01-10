package com.apcsa.model;

import java.sql.ResultSet;
import com.apcsa.model.User;
import java.sql.SQLException;

public class Administrator extends User {

	/**
	 * Creates an instance of the Administrator class.
	 * 
	 * @param user
	 * @param rs
	 * @throws SQLException
	 */
	    
	public Administrator(User user, ResultSet rs) throws SQLException {
	    super(user);
	    
	    this.administratorId = rs.getInt("administrator_id");
	    this.firstName = rs.getString("first_name");
	    this.lastName = rs.getString("last_name");
	    this.jobTitle = rs.getString("job_title");
	}
	
	public String getFirstName() {
		return firstName;
	}
    
	private int administratorId;
    private String firstName;
    private String lastName;
    private String jobTitle;

}