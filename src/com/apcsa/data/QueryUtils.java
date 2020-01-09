package com.apcsa.data;

public class QueryUtils {

    /////// QUERY CONSTANTS ///////////////////////////////////////////////////////////////
    
    /*
     * Determines if the default tables were correctly loaded.
     */
	
    public static final String SETUP_SQL =
        "SELECT COUNT(name) AS names FROM sqlite_master " +
            "WHERE type = 'table' " +
        "AND name NOT LIKE 'sqlite_%'";
    
    /*
     * Updates the last login timestamp each time a user logs into the system.
     */

    public static final String LOGIN_SQL =
        "SELECT * FROM users " +
            "WHERE username = ?" +
        "AND auth = ?";
    
    /*
     * Updates the last login timestamp each time a user logs into the system.
     */

    public static final String UPDATE_LAST_LOGIN_SQL =
        "UPDATE users " +
            "SET last_login = ? " +
        "WHERE username = ?";
    
    /*
     * Retrieves an administrator associated with a user account.
     */

    public static final String GET_ADMIN_SQL =
        "SELECT * FROM administrators " +
            "WHERE user_id = ?";
    
    /*
     * Retrieves a teacher associated with a user account.
     */

    public static final String GET_TEACHER_SQL =
        "SELECT * FROM teachers " +
            "WHERE user_id = ?";
    
    /*
     * Retrieves a student associated with a user account.
     */

    public static final String GET_STUDENT_SQL =
        "SELECT * FROM students " +
            "WHERE user_id = ?";
    /*
     * Resets another user's password and last login timestamp.
     */
        
    //
    // upset the users table
    // two columns need to be updated
    //          - auth
    //          - last_login
    //
    // auth will be set to the hash of the user's username
    // last_login will be reverted to 0000-00-00 00:00:00.000
    //
    // only modify rows where username matches parameter provided
    
    public static final String UPDATE_PASSWORD_SQL = 
    		"UPDATE users SET auth = ? WHERE username = ?"; 
    
    public static final String UPDATE_PASSWORD_AND_TIME_SQL = 
    		"UPDATE users SET auth = ?, last_login = ? WHERE username = ?"; 
    
    /*
     * Retrieves all teachers.
     */

    public static final String GET_ALL_TEACHERS_SQL =
        "SELECT * FROM " +
            "teachers, departments " +
        "WHERE " +
            "teachers.department_id = departments.department_id " +
        "ORDER BY " +
            "last_name, first_name";
    
    public static final String GET_TEACHERS_BY_DEPARTMENT_SQL =
    		"SELECT first_name, last_name, teacher_id, department_id FROM teachers " +
    				"WHERE department_id = ?";
    
    public static final String GET_DEPARTMENT_TITLE =
    		"SELECT title FROM departments WHERE department_id =?";
    
    public static final String GET_ALL_STUDENTS_SQL =
    		"SELECT * FROM students";
    
    public static final String GET_STUDENTS_BY_GRADE_SORT_RANK_SQL =
    		"SELECT * FROM students WHERE grade_level = ? ORDER BY last_name";
    
    public static final String GET_COURSE_ID_FROM_NO =
    		"SELECT course_id FROM courses WHERE course_no = ?";
    
    public static final String GET_STUDENTS_FROM_COURSE_ID =
    		"SELECT * FROM students INNER JOIN course_grades ON course_grades.student_id = students.student_id WHERE course_id = ? ORDER BY last_name";
    
    public static final String ADD_ASSIGNMENT_SQL =
    		"INSERT INTO ASSIGNMENTS VALUES(?, ?, ?, ?, ?, ?, ?)";
    
    public static final String GET_COURSES_BY_TEACHER_SQL =
    		"SELECT * FROM courses WHERE teacher_id = ?";
    
    public static final String DELETE_ASSIGNEMNT_SQL =
    		"DELETE FROM assignements WHERE assignment_id = ?";
    
    public static final String UPDATE_GRADE_SQL =
    		"UPDATE assignment_grades SET points_earned = ? WHERE student_id = ? AND assignement_id = ?";
    
    public static final String GET_STUDENTS_BY_ASSIGNMENT =
    		"SELECT * FROM students INNER JOIN assignment_grades on students.student_id = assignment_grades.student_id WHERE assignment_id = ? ORDER BY student_id";

}