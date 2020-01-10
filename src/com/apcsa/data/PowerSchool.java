package com.apcsa.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;
import com.apcsa.controller.Utils;
import com.apcsa.model.Administrator;
import com.apcsa.model.Student;
import com.apcsa.model.Teacher;
import com.apcsa.model.User;

public class PowerSchool {

    private final static String PROTOCOL = "jdbc:sqlite:";
    private final static String DATABASE_URL = "data/powerschool.db";

    /**
     * Initializes the database if needed (or if requested).
     *
     * @param force whether or not to force-reset the database
     * @throws Exception
     */

    public static void initialize(boolean force) {
        if (force) {
            reset();    // force reset
        } else {
            boolean required = false;

            // check if all tables have been created and loaded in database

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(QueryUtils.SETUP_SQL)) {

                while (rs.next()) {
                    if (rs.getInt("names") != 9) {
                        required = true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // build database if needed

            if (required) {
                reset();
            }
        }
    }

    /**
     * Retrieves the User object associated with the requested login.
     *
     * @param username the username of the requested User
     * @param password the password of the requested User
     * @return the User object for valid logins; null for invalid logins
     */

    public static User login(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(QueryUtils.LOGIN_SQL)) {

            stmt.setString(1, username);
            stmt.setString(2, Utils.getHash(password));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = new Timestamp(new Date().getTime());
                    int affected = PowerSchool.updateLastLogin(conn, username, ts);

                    if (affected != 1) {
                        System.err.println("Unable to update last login (affected rows: " + affected + ").");
                    }

                    return new User(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns the administrator account associated with the user.
     *
     * @param user the user
     * @return the administrator account if it exists
     */

    public static User getAdministrator(User user) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ADMIN_SQL)) {

            stmt.setInt(1, user.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Administrator(user, rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Returns the teacher account associated with the user.
     *
     * @param user the user
     * @return the teacher account if it exists
     */

    public static User getTeacher(User user) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_TEACHER_DEPARTMENT_SQL)) {

            stmt.setInt(1, user.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Teacher(user, rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Returns the student account associated with the user.
     *
     * @param user the user
     * @return the student account if it exists
     */

    public static User getStudent(User user) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_SQL)) {

            stmt.setInt(1, user.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(user, rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    /*
     * Establishes a connection to the database.
     *
     * @return a database Connection object
     * @throws SQLException
     */

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(PROTOCOL + DATABASE_URL);
    }

    /*
     * Updates the last login time for the user.
     *
     * @param conn the current database connection
     * @param username the user's username
     * @param ts the current timestamp
     * @return the number of affected rows
     */

    private static int updateLastLogin(Connection conn, String username, Timestamp ts) {
        try (PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_LAST_LOGIN_SQL)) {

            conn.setAutoCommit(false);
            stmt.setString(1, ts.toString());
            stmt.setString(2, username);

            if (stmt.executeUpdate() == 1) {
                conn.commit();

                return 1;
            } else {
                conn.rollback();

                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();

            return -1;
        }
    }
    
    /*
     * Updates the last login time for the user.
     *
     * @param conn the current database connection
     * @param username the user's username
     * @param ts the current timestamp in string form
     * @return the number of affected rows
     */
    
    private static int updateLastLogin(Connection conn, String username, String ts) {
        try (PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_LAST_LOGIN_SQL)) {

            conn.setAutoCommit(false);
            stmt.setString(1, ts);
            stmt.setString(2, username);

            if (stmt.executeUpdate() == 1) {
                conn.commit();

                return 1;
            } else {
                conn.rollback();

                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();

            return -1;
        }
    }

    /*
     * Builds the database. Executes a SQL script from a configuration file to
     * create the tables, setup the primary and foreign keys, and load sample data.
     */

    private static void reset() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             BufferedReader br = new BufferedReader(new FileReader(new File("config/setup.sql")))) {

            String line;
            StringBuffer sql = new StringBuffer();

            // read the configuration file line-by-line to get SQL commands

            while ((line = br.readLine()) != null) {
                sql.append(line);
            }

            // execute SQL commands one-by-one

            for (String command : sql.toString().split(";")) {
                if (!command.strip().isEmpty()) {
                    stmt.executeUpdate(command);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: Unable to load SQL configuration file.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error: Unable to open and/or read SQL configuration file.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error: Unable to execute SQL script from configuration file.");
            e.printStackTrace();
        }
    }
    
    /**
     * Resets a user's password.
     * 
     * @param username the user's username
     */

    public static void resetPassword(String username) {
        //
        // get a connection to the database
        // create a prepared statement (both of thses should go in a try-with-resources statement)
        //
        // insert parameters into the prepared statement
        //      - the user's hashed username
        //      - the user's plaintext username
        //
        // execute the update statement
        //
    	
    	try ( Connection conn = getConnection();
        		PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_PASSWORD_AND_TIME_SQL)) {

            conn.setAutoCommit(false);
            stmt.setString(1, Utils.getHash(username));
            stmt.setString(2, "0000-00-00 00:00:00.000");
            stmt.setString(3, username);

            if (stmt.executeUpdate() == 1) {
                conn.commit();

            } else {
                conn.rollback();

            }
            
        } catch (SQLException e) {
            e.printStackTrace();

            
        }
    	
    }
    
    public static int updatePassword(String username, String password) {
    	
        try ( Connection conn = getConnection();
        		PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_PASSWORD_SQL)) {

            conn.setAutoCommit(false);
            stmt.setString(1, password);
            stmt.setString(2, username);

            if (stmt.executeUpdate() == 1) {
                conn.commit();

                return 1;
            } else {
                conn.rollback();

                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();

            return -1;
        }
    }
    
    /**
     * Retrieves all faculty members.
     * 
     * @return a list of teachers
     */
     
     public static ArrayList<Teacher> getTeachers() {
        ArrayList<Teacher> teachers = new ArrayList<Teacher>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
                        
            try (ResultSet rs = stmt.executeQuery(QueryUtils.GET_ALL_TEACHERS_SQL)) {
                while (rs.next()) {
                    teachers.add(new Teacher(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return teachers;
    }
     
     public static ArrayList<Teacher> getTeachersByDepartment(int department_id) {
    	 ArrayList<Teacher> teachers = new ArrayList<Teacher>();
         
         try (Connection conn = getConnection();
        	PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_TEACHERS_BY_DEPARTMENT_SQL)) {

             conn.setAutoCommit(false);
             stmt.setString(1, Integer.toString(department_id));
        	 
                         
             try (ResultSet rs = stmt.executeQuery()) {

                 while (rs.next()) {
                	 teachers.add(new Teacher(rs));
                 }
             }
             return teachers;
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return teachers;
     }
     
     public static String getDepartmentTitle(int department_id) {
    	 try(Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_DEPARTMENT_TITLE)){
    		 stmt.setString(1, Integer.toString(department_id));
    		 
    		 try(ResultSet rs = stmt.executeQuery()){
    			 while(rs.next()) {
    				 return rs.getString("title");
    			 }
    		 }
    	 }catch(SQLException e) {
    			 e.printStackTrace();
    	 }
    	 return "Error";
     }
     
     public static ArrayList<Student> getStudents(){
    	 ArrayList<Student> students = new ArrayList<Student>();
         
         try (Connection conn = getConnection();
              Statement stmt = conn.createStatement()) {
                         
             try (ResultSet rs = stmt.executeQuery(QueryUtils.GET_ALL_STUDENTS_SQL)) {
                 while (rs.next()) {
                     students.add(new Student(rs));
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return students;
     }
     
     public static ArrayList<Student> getStudentsByGradeAndRank(int grade_level){
    	 ArrayList<Student> students = new ArrayList<Student>();
         
         try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENTS_BY_GRADE_SORT_RANK_SQL)){
    		 
        	 stmt.setString(1, Integer.toString(grade_level));
                         
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     students.add(new Student(rs));
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return students;
     }
     
     public static ArrayList<Student> getStudentsByCourse(int course_id){
    	 ArrayList<Student> students = new ArrayList<Student>();
         
         try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENTS_FROM_COURSE_ID)){
    		 
        	 stmt.setString(1, Integer.toString(course_id));
                         
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     students.add(new Student(rs));
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return students;
     }
     
     public static int getCourseIdFromNo(String course_no) {
    	 try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSE_ID_FROM_NO)){
    		 
        	 stmt.setString(1, course_no);
        
        	 try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     return rs.getInt("course_id");
                 }
             }
        	 
	     }catch (SQLException e) {
	         e.printStackTrace();
	     }
     
     return -1;
     }
     
     public static int testCourseNo(String courseNo) {
         
         try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSE_ID_FROM_NO)){
    		 
        	 stmt.setString(1, courseNo);
                         
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     return rs.getInt("course_id");
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return -1;
     }
     
     public static ArrayList<String> getCoursesByTeacher(int teacher_id){
    	 ArrayList<String> courses = new ArrayList<String>();
         
         try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSES_BY_TEACHER_SQL)){
    		 
        	 stmt.setString(1, Integer.toString(teacher_id));
                         
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     courses.add(rs.getString("course_no"));
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return courses;
     }
     
     public static boolean addAssignment(int course_id, int assignment_id, int marking_period, int is_midterm, int is_final, String title, double point_value) {
    	 try ( Connection conn = getConnection();
         		PreparedStatement stmt = conn.prepareStatement(QueryUtils.ADD_ASSIGNMENT_SQL)) {

             conn.setAutoCommit(false);
             stmt.setString(1, Integer.toString(course_id));
             stmt.setString(2, Integer.toString(assignment_id));
             stmt.setString(3, Integer.toString(marking_period));
             stmt.setString(4, Integer.toString(is_midterm));
             stmt.setString(5, Integer.toString(is_final));
             stmt.setString(6, title);
             stmt.setString(7, Double.toString(point_value));

             if (stmt.executeUpdate() == 1) {
                 conn.commit();

                 return true;
             } else {
                 conn.rollback();

                 return false;
             }
         } catch (SQLException e) {
             e.printStackTrace();

             return false;
         }
     }
     
     public static int getTeacherId(int user_id) {
         
         try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_TEACHER_ID_FROM_USER_ID_SQL)){
    		 
        	 stmt.setString(1, Integer.toString(user_id));
        	 
        	 int teacher_id = -1;
                         
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     return rs.getInt("teacher_id");
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return -1;
     }
     
     public static int getAssignmentNo(int course_id) {
         
         try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENTS_SQL)){
    		
        	 ArrayList<String> assignments = new ArrayList<String>();
        	 
        	 stmt.setString(1, Integer.toString(course_id));
                         
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                      assignments.add(rs.getString("assignment_id"));
                 }
             }
             
             return assignments.size();
             
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return -1;
     }
     
     public static boolean delAssignment(int assignment_id, int course_id) {
    	 try ( Connection conn = getConnection();
          		PreparedStatement stmt = conn.prepareStatement(QueryUtils.DELETE_ASSIGNMENT_SQL)) {

              conn.setAutoCommit(false);
              stmt.setString(1, Integer.toString(assignment_id));
              stmt.setString(2, Integer.toString(course_id));

              if (stmt.executeUpdate() == 1) {
                  conn.commit();

                  return true;
              } else {
                  conn.rollback();

                  return false;
              }
          } catch (SQLException e) {
              e.printStackTrace();

              return false;
          }
     }
     
     public static ArrayList<String> getAssignmentsByCourseAndMP(int course_id, int marking_period, int is_midterm, int is_final){
    	 ArrayList<String> assignments = new ArrayList<String>();
         
         try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENTS_BY_COURSE_MP)){
    		 
        	 stmt.setString(1, Integer.toString(course_id));
        	 stmt.setString(2, Integer.toString(marking_period));
        	 stmt.setString(3, Integer.toString(is_midterm));
        	 stmt.setString(4, Integer.toString(is_final));
                         
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     assignments.add(rs.getString("title") + "(" + rs.getInt("point_value") + " pts)");
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return assignments;
     }
     
     public static ArrayList<Integer> getAssignmentsByCourseAndMPIds(int course_id, int marking_period, int is_midterm, int is_final){
    	 ArrayList<Integer> assignments = new ArrayList<Integer>();
         
         try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENTS_BY_COURSE_MP)){
    		 
        	 stmt.setString(1, Integer.toString(course_id));
        	 stmt.setString(2, Integer.toString(marking_period));
        	 stmt.setString(3, Integer.toString(is_midterm));
        	 stmt.setString(4, Integer.toString(is_final));
                         
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     assignments.add(rs.getInt("assignment_id"));
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return assignments;
     }
     
     public static boolean enterAssignmentGrade(int assignment_id, int course_id, int student_id, double points_earned, double points_possible) {
    	 try ( Connection conn = getConnection();
         		PreparedStatement stmt = conn.prepareStatement(QueryUtils.CREATE_ASSIGNMENT_GRADE_SQL)) {

             conn.setAutoCommit(false);
             stmt.setString(1, Integer.toString(course_id));
             stmt.setString(2, Integer.toString(assignment_id));
             stmt.setString(3, Integer.toString(student_id));
             stmt.setString(4, Double.toString(points_earned));
             stmt.setString(5, Double.toString(points_possible));
             

             if (stmt.executeUpdate() == 1) {
                 conn.commit();

                 return true;
             } else {
                 conn.rollback();

                 return false;
             }
         } catch (SQLException e) {
             e.printStackTrace();

             return false;
         }
     }
     
     public static double getAssignmentPointsPossible(int assignment_id, int course_id) {
    	 	try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_POINTS_POSSIBLE_FROM_ASSIGNMENT_SQL)){
    		 
        	 stmt.setString(1, Integer.toString(course_id));
        	 stmt.setString(2,  Integer.toString(assignment_id));
        	                          
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     return rs.getDouble("point_value");
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return -1;
     }
     
     public static String getAssignmentName( int assignment_id, int course_id) {
    	 try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_NAME)){
    		 
        	 stmt.setString(2, Integer.toString(course_id));
        	 stmt.setString(1,  Integer.toString(assignment_id));
        	                          
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     return rs.getString("title");
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return null;
     	}
     
     public static String getStudentName(int student_id) {
    	 try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_NAME)){
    		 
        	 stmt.setString(1, Integer.toString(student_id));
        	                          
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     return (rs.getString("last_name") + ", " + rs.getString("first_name"));
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return null;
  
     }
     
     public static boolean doesItHaveGrade(int course_id, int assignment_id, int student_id) {
    	 try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_GRADE)){
    		 
    		 stmt.setString(1, Integer.toString(course_id));
    		 stmt.setString(2, Integer.toString(assignment_id));
        	 stmt.setString(3, Integer.toString(student_id));
        	                          
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                	 return true;
                     
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return false;
     }
     
     public static double getAssignmentGrade(int course_id, int assignment_id, int student_id) {
    	 try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_GRADE)){
    		 
    		 stmt.setString(1, Integer.toString(course_id));
    		 stmt.setString(2, Integer.toString(assignment_id));
        	 stmt.setString(3, Integer.toString(student_id));
        	                          
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     return rs.getDouble("points_earned");
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return -1;
     }
     
     public static boolean updateCourseGrade(int grade_period, int student_id, int course_id, double grade_value) {
    	 try ( Connection conn = getConnection();
         		PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_COURSE_GRADE)) {

    		 String markingPeriod = "";
    		 
    		 switch(grade_period) {
    		 	case 1: markingPeriod = "mp1";break;
    		 	case 2: markingPeriod = "mp2";break;
    		 	case 3: markingPeriod = "mp3";break;
    		 	case 4: markingPeriod = "mp4";break;
    		 	case 5: markingPeriod = "midterm_exam";break;
    		 	case 6: markingPeriod = "final_exam";break;
    		 	case 7: markingPeriod = "grade";break;
    		 }
    		     		
             conn.setAutoCommit(false);
             stmt.setString(1, markingPeriod);
             stmt.setString(3, Integer.toString(student_id));
             stmt.setString(4, Integer.toString(course_id));
             stmt.setString(2, Double.toString(grade_value));

             if (stmt.executeUpdate() == 1) {
                 conn.commit();

                 return true;
             } else {
                 conn.rollback();

                 return false;
             }
         } catch (SQLException e) {
             e.printStackTrace();

             return false;
         }
     }
     
     public static boolean doesItHaveGrade(int course_id, int student_id) {
    	 try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_GRADE)){
    		 
    		 stmt.setString(2, Integer.toString(course_id));
        	 stmt.setString(1, Integer.toString(student_id));
        	                          
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                	 return true;
                     
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return false;
     }
     
     public static Double[] getCourseGrades(int student_id, int course_id) {
    	 Double[] gradeArray = new Double[6];
    	 
    	 try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_SPECIFIC_STUDENT_COURSE_INFO)){
    		 
    		 stmt.setString(2, Integer.toString(course_id));
        	 stmt.setString(1, Integer.toString(student_id));
        	                          
             try (ResultSet rs = stmt.executeQuery()) {
                 	
                     gradeArray[0] = (rs.getDouble("mp1"));
                     gradeArray[1] = (rs.getDouble("mp2"));
                     gradeArray[3] = (rs.getDouble("mp3"));
                     gradeArray[4] = (rs.getDouble("mp4"));
                     gradeArray[2] = (rs.getDouble("midterm_exam"));
                     gradeArray[5] = (rs.getDouble("final_exam"));
                 
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return null;
     }
     
     public static double getTotalPointsPossible(int student_id, int course_id) {
    	 try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENTS_BY_STUDENT_AND_COURSE)){
    		 
    		 stmt.setString(2, Integer.toString(course_id));
        	 stmt.setString(1, Integer.toString(student_id));
        	 
        	 double pointsPossible = 0;
        	 
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     pointsPossible += rs.getDouble("points_possible");
                 }
             }
             
             return pointsPossible;
            		 
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return -1;
    	
     }
     
     public static double getTotalPointsEarned(int student_id, int course_id) {
    	 try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENTS_BY_STUDENT_AND_COURSE)){
    		 
    		 stmt.setString(2, Integer.toString(course_id));
        	 stmt.setString(1, Integer.toString(student_id));
        	 
        	 double pointsEarned = 0;
        	 
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     pointsEarned += rs.getDouble("points_earned");
                 }
             }
             
             return pointsEarned;
            		 
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return -1;
    	
     }
     
     
     public static double getTotalPointsEarnedByMP(int student_id, int course_id, int marking_period) {
    	 try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENTS_BY_STUDENT_COURSE_MP)){
    		 
    		 stmt.setString(2, Integer.toString(course_id));
        	 stmt.setString(1, Integer.toString(student_id));
        	 
        	 if(marking_period <= 4) {
//        		 stmt.setString(3, "marking_period");
        		 stmt.setString(3, Integer.toString(marking_period));
        	 }else if(marking_period > 4) {
        		 if(marking_period == 5) {
        			 stmt.setString(5, "is_midterm");
        			 stmt.setString(6, "1");
        		 }else if(marking_period == 6) {
        			 stmt.setString(5, "is_final");
        			 stmt.setString(6, "1");
        		 }
        	 }
        	 
        	 double pointsEarned = 0;
        	 
        	 System.out.println("Executing query\n");
        	 
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                	 System.out.println("points_possible");
                	 System.out.println(rs.getString("points_earned"));
                     pointsEarned += rs.getDouble("points_earned");
                 }
             }
             
             return pointsEarned;
            		 
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return -1;
    	
     }
     
     public static double getTotalPointsPossibleByMP(int student_id, int course_id, int marking_period) {
    	 try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENTS_BY_STUDENT_COURSE_MP)){
    		 
    		 stmt.setString(2, Integer.toString(course_id));
        	 stmt.setString(1, Integer.toString(student_id));
        	 
        	 if(marking_period <= 4) {
        		 stmt.setString(3, "marking_period");
        		 stmt.setString(4, Integer.toString(marking_period));
        	 }else if(marking_period > 4) {
        		 if(marking_period == 5) {
        			 stmt.setString(5, "is_midterm");
        			 stmt.setString(6, "1");
        		 }else if(marking_period == 6) {
        			 stmt.setString(5, "is_final");
        			 stmt.setString(6, "1");
        		 }
        	 }
        	         	 
        	 double pointsEarned = 0;
        	 
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                	 System.out.println(rs.getString("points_possible"));
                     pointsEarned += rs.getDouble("points_possible");
                 }
             }
             
             return pointsEarned;
            		 
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         return -1;
    	
     }
     
     
     public static double generateGrade(int marking_period, int course_id, int student_id) {
    	 
    	 System.out.println(getTotalPointsEarnedByMP(student_id, course_id, marking_period));
    	 System.out.println(getTotalPointsPossibleByMP(student_id, course_id, marking_period));
    	 
    	 double grade = (getTotalPointsEarnedByMP(student_id, course_id, marking_period) )/ (getTotalPointsPossibleByMP(student_id,course_id, marking_period));
    	 
    	 return grade;
    	 
     }
     
     
}