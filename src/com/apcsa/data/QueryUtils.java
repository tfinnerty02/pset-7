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
    
    public static final String GET_TEACHER_DEPARTMENT_SQL =
    		"SELECT * FROM teachers INNER JOIN departments WHERE teachers.department_id = departments.department_id AND user_id = ?";
    
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
    		"SELECT * FROM teachers, department " +
    				"WHERE teachers.department_id = ? = department.department_id";
    
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
    
    public static final String DELETE_ASSIGNMENT_SQL =
    		"DELETE FROM assignements WHERE assignment_id = ? AND course_id = ?";
    
    public static final String UPDATE_GRADE_SQL =
    		"UPDATE assignment_grades SET points_earned = ? WHERE student_id = ? AND assignement_id = ?";
    
    public static final String GET_STUDENTS_BY_ASSIGNMENT =
    		"SELECT * FROM students INNER JOIN assignment_grades on students.student_id = assignment_grades.student_id WHERE assignment_id = ? AND course_id = ? ORDER BY student_id";
    
    public static final String GET_TEACHER_ID_FROM_USER_ID_SQL =
    		"SELECT * FROM teachers WHERE user_id = ?";
    
    public static final String GET_ASSIGNMENTS_SQL =
    		"SELECT * FROM assignments WHERE course_id = ?";
    
    public static final String GET_ASSIGNMENTS_BY_COURSE_MP =
    		"SELECT * FROM assignments WHERE course_id = ? AND marking_period = ? AND is_midterm = ? AND is_final = ?";

    public static final String CREATE_ASSIGNMENT_GRADE_SQL =
    		"INSERT INTO ASSIGNMENT_GRADES VALUES(?, ?, ?, ?, ?)";
    
    public static final String GET_POINTS_POSSIBLE_FROM_ASSIGNMENT_SQL =
    		"SELECT point_value FROM assignments WHERE course_id = ? AND assignment_id =?";
    
    public static final String GET_ASSIGNMENT_NAME =
    		"SELECT title FROM ASSIGNMENTS WHERE assignment_id = ? AND course_id = ?";
    
    public static final String GET_STUDENT_NAME =
    		"SELECT * FROM students WHERE student_id = ?";
    
    public static final String GET_ASSIGNMENT_GRADE =
    		"SELECT * FROM assignment_grades WHERE course_id = ? AND assignment_id = ? AND student_id = ?";
    
    public static final String GET_ALL_STUDENT_COURSE_INFO =
    		"SELECT * FROM students, courses INNER JOIN course_grades ON students.student_id=course_grades.student_id AND courses.course_id = course_grades.course_id WHERE students.student_id = ?";
    		
    public static final String UPDATE_COURSE_GRADE =
    		"UPDATE course_grades SET ? WHERE student_id = ? AND course_id = ?";
    
    public static final String GET_SPECIFIC_STUDENT_COURSE_INFO =
    		"SELECT * FROM students, courses INNER JOIN course_grades ON students.student_id=course_grades.student_id AND courses.course_id = course_grades.course_id WHERE students.student_id = ? AND courses.course_id = ?";

    public static final String GET_ASSIGNMENTS_BY_STUDENT_AND_COURSE =
    		"SELECT * FROM assignment_grades WHERE student_id = ? AND course_id = ?";
}