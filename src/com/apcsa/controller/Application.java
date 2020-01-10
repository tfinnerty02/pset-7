package com.apcsa.controller;

import java.util.Scanner;
import java.util.ArrayList;
import com.apcsa.data.PowerSchool;
import com.apcsa.model.Teacher;
import com.apcsa.model.User;
import com.apcsa.model.Student;

public class Application {
	
	String userResetName;

    private Scanner in;
    private User activeUser;
    
    enum RootAction { PASSWORD, DATABASE, LOGOUT, SHUTDOWN }
    enum AdministratorAction { FACULTY, DEPARTMENT, STUDENTS, GRADE, COURSE, PASSWORD, LOGOUT }
    enum TeacherAction { STUDENTS_COURSE, ADD_ASS, DEL_ASS, GRADE_ASS, PASSWORD, LOGOUT }
    enum StudentAction { COURSE_GRADE, COURSE_ASS, PASSWORD, LOGOUT }

    /**
     * Creates an instance of the Application class, which is responsible for interacting
     * with the user via the command line interface.
     */

    public Application() {
        this.in = new Scanner(System.in);

        try {
            PowerSchool.initialize(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the PowerSchool application.
     */

    public void startup() {
        System.out.println("PowerSchool -- now for students, teachers, and school administrators!");

        // continuously prompt for login credentials and attempt to login

        while (true) {
            System.out.print("\nUsername: ");
            String username = in.next();

            System.out.print("Password: ");
            String password = in.next();

            // if login is successful, update generic user to administrator, teacher, or student

            if (login(username, password)) {
                activeUser = activeUser.isAdministrator()
                    ? PowerSchool.getAdministrator(activeUser) : activeUser.isTeacher()
                    ? PowerSchool.getTeacher(activeUser) : activeUser.isStudent()
                    ? PowerSchool.getStudent(activeUser) : activeUser.isRoot()
                    ? activeUser : null;

                if (isFirstLogin() && !activeUser.isRoot()) {
                    // first-time users need to change their passwords from the default provided
                	changePassword(true);
                }

                // create and show the user interface
                //
                // remember, the interface will be difference depending on the type
                // of user that is logged in (root, administrator, teacher, student)
                
                createAndShowUI();
                
            } else {
                System.out.println("\nInvalid username and/or password.");
            }
        }
    }
    
    public void createAndShowUI() {
        System.out.println("\nHello, again, " + activeUser.getFirstName() + "!");
        
        if (activeUser.isRoot()) {
            showRootUI();
        } else if (activeUser.isAdministrator()) {
            showAdministratorUI();
        } else if(activeUser.isTeacher()){
            showTeacherUI();
        }
    }
    
    /*
     * Displays an interface for root users.
     */

    private void showRootUI() {
        while (activeUser != null) {
            switch (getRootMenuSelection()) {
                case PASSWORD: resetPassword(); break;
                case DATABASE: factoryReset(); break;
                case LOGOUT: logout(); break;
                case SHUTDOWN: shutdown(); break;
                default: System.out.println("\nInvalid selection."); break;
            }
        }
    }
    
    /*
     * Displays an interface for administrators users.
     */
        
    private void showAdministratorUI() {
        while (activeUser != null) {
            switch (getAdministratorMenuSelection()) {
                case FACULTY: viewFaculty(); break;
                case DEPARTMENT: viewFacultyByDepartment(); break;
                case STUDENTS: viewStudents(); break;
                case GRADE: viewStudentsByGrade(); break;
                case COURSE: viewStudentsByCourse(); break;
                case PASSWORD: changePassword(false); break;
                case LOGOUT: logout(); break;
                default: System.out.println("\nInvalid selection."); break;
            }
        }
    }
    
    private void showTeacherUI() {
        while (activeUser != null) {
            switch (getTeacherMenuSelection()) {
                case STUDENTS_COURSE: viewStudentsByCourseTeacher(); break;
                case ADD_ASS: addAssignment(); break;
                case DEL_ASS: delAssignment(); break;
//                case GRADE_ASS: enterAssignmentGrade(); break;
                case PASSWORD: changePassword(false); break;
                case LOGOUT: logout(); break;
                default: System.out.println("\nInvalid selection."); break;
            }
        }
    }
    
    /*
     * Retrieves a root user's menu selection.
     * 
     * @return the menu selection
     */

    private RootAction getRootMenuSelection() {
        System.out.println();
        
        System.out.println("[1] Reset user password.");
        System.out.println("[2] Factory reset database.");
        System.out.println("[3] Logout.");
        System.out.println("[4] Shutdown.");
        System.out.print("\n::: ");
        
        switch (Utils.getInt(in, -1)) {
            case 1: return RootAction.PASSWORD;
            case 2: return RootAction.DATABASE;
            case 3: return RootAction.LOGOUT;
            case 4: return RootAction.SHUTDOWN;
            default: return null;
        }
     }
    
    /*
     * Retrieves a admin user's menu selection.
     * 
     * @return the menu selection
     */

    private AdministratorAction getAdministratorMenuSelection() {
        System.out.println();
        
        System.out.println("[1] View faculty.");
        System.out.println("[2] View faculty by department.");
        System.out.println("[3] View student enrollment.");
        System.out.println("[4] View student enrollment by grade.");
        System.out.println("[5] View student enrollment by course.");
        System.out.println("[6] Change password.");
        System.out.println("[7] Logout.");
        System.out.print("\n::: ");

        switch (Utils.getInt(in, -1)) {
            case 1: return AdministratorAction.FACULTY;
            case 2: return AdministratorAction.DEPARTMENT;
            case 3: return AdministratorAction.STUDENTS;
            case 4: return AdministratorAction.GRADE;
            case 5: return AdministratorAction.COURSE;
            case 6: return AdministratorAction.PASSWORD;
            case 7: return AdministratorAction.LOGOUT;
        }
        
        return null;
    }
    
    private TeacherAction getTeacherMenuSelection() {
        System.out.println();
        
        System.out.println("[1] View student enrollment by course");
        System.out.println("[2] Add assignment.");
        System.out.println("[3] Delete Assignment.");
        System.out.println("[4] Enter grade.");
        System.out.println("[5] Change password.");
        System.out.println("[6] Logout.");
        System.out.print("\n::: ");

        switch (Utils.getInt(in, -1)) {
            case 1: return TeacherAction.STUDENTS_COURSE;
            case 2: return TeacherAction.ADD_ASS;
            case 3: return TeacherAction.DEL_ASS;
            case 4: return TeacherAction.GRADE_ASS;
            case 5: return TeacherAction.PASSWORD;
            case 6: return TeacherAction.LOGOUT;
        }
        
        return null;
    }
    
    /*
     * Retrieves the user's department selection.
     * 
     * @return the selected department
     */

    private int getDepartmentSelection() {
        int selection = -1;
        System.out.println("\nChoose a department.");
        
        while (selection < 1 || selection > 6) {
            System.out.println("\n[1] Computer Science.");
            System.out.println("[2] English.");
            System.out.println("[3] History.");
            System.out.println("[4] Mathematics.");
            System.out.println("[5] Physical Education.");
            System.out.println("[6] Science.");
            System.out.print("\n::: ");
            
            selection = Utils.getInt(in, -1);
        }
        
        return selection;
    }
    
    
    /*
     * Retrieves a user's grade selection.
     * 
     * @return the selected grade
     */

    private int getGradeSelection() {
        int selection = -1;
        System.out.println("\nChoose a grade level.");
        
        while (selection < 1 || selection > 4) {
            System.out.println("\n[1] Freshman.");
            System.out.println("[2] Sophomore.");
            System.out.println("[3] Junior.");
            System.out.println("[4] Senior.");
            System.out.print("\n::: ");
            
            selection = Utils.getInt(in, -1);
        }
        
        return selection + 8;   // +8 because you want a value between 9 and 12
    }
    
    /*
     * Retrieves a user's course selection.
     * 
     * @return the selected course
     */

    private String getCourseSelection() {
        boolean valid = false;
        String courseNo = null;
        
        while (!valid) {
            System.out.print("\nCourse No.: ");
            courseNo = in.next();
            
            if (PowerSchool.testCourseNo(courseNo) > 0) {
                valid = true;
            } else if(PowerSchool.testCourseNo(courseNo) < 0){
                System.out.println("\nCourse not found.");
            }
        }
        
        return courseNo;
    }
    
    private int getMarkingPeriodSelection() {
        int selection = -1;
        System.out.println("\nChoose a marking period or exam status.");
        
        while (selection < 1 || selection > 6) {
            System.out.println("\n[1] MP1 assignment.");
            System.out.println("[2] MP2 assignment.");
            System.out.println("[3] MP3 assignment.");
            System.out.println("[4] MP4 assignment.");
            System.out.println("[5] Midterm exam.");
            System.out.println("[6] Final exam.");
            System.out.print("\n::: ");
            
            selection = Utils.getInt(in, -1);
        }
        
        return selection;
    }
    
    

    /**
     * Logs in with the provided credentials.
     *
     * @param username the username for the requested account
     * @param password the password for the requested account
     * @return true if the credentials were valid; false otherwise
     */

    public boolean login(String username, String password) {
        activeUser = PowerSchool.login(username, password);

        return activeUser != null;
    }
    
    /*
     * Allows a root user to reset another user's password.
     */

    private void resetPassword() {
        
    	System.out.print("\nEnter username of account to be reset: ");
 	   userResetName = in.next();
 	   
 	   if(Utils.confirm(in, "\nAre you sure you want to reset the password for " + userResetName + "? (y/n)")){
 		  PowerSchool.resetPassword(userResetName);
 		  System.out.println("Successfully reset password for " + userResetName + ".");
 	   }else if(!Utils.confirm(in, "\nAre you sure you want to reset the password for " + userResetName + "? (y/n)")) {
 		   System.out.println("Password reset for account " + userResetName + " cancelled.");
 	   }
 	   
    }
    
    /*
     * Displays all faculty members.
     */
        
    private void viewFaculty() {        
        ArrayList<Teacher> teachers = PowerSchool.getTeachers();
        
        if (teachers.isEmpty()) {
            System.out.println("\nNo teachers to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Teacher teacher : teachers) {
                System.out.println(i++ + ". " + teacher.getName() + " / " + teacher.getDepartmentName());
            } 
        }
    }
    
    /*
     * Displays all faculty members by department.
     */

    private void viewFacultyByDepartment() {
        //
        // get a list of teachers by department (this requires a database call)
        //      to do this, you'll need to prompt the user to choose a department (more on this later)
        //
        // if list of teachers is empty...
        //      print a message saying exactly that
        // otherwise...
        //      print the list of teachers by name an department (just like last time)
        //
    	
    	int inputDeptId = getDepartmentSelection();
    	
    	ArrayList<Teacher> teachers = PowerSchool.getTeachersByDepartment(inputDeptId);
        
        if (teachers.isEmpty()) {
            System.out.println("\nNo teachers to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Teacher teacher : teachers) {
                System.out.println(i++ + ". " + teacher.getName() + " / " + PowerSchool.getDepartmentTitle(inputDeptId));
            } 
        }
    	
    }
    
    /*
     * Displays all students.
     */

    private void viewStudents() {
        //
        // get a list of students
        //
        // if list of students is empty...
        //      print a message saying exactly that
        // otherwise...
        //      print the list of students by name and graduation year
        //
    	
    	ArrayList<Student> students = PowerSchool.getStudents();
        
        if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / " + student.getGradYear());
            } 
        }
    }
    
    /*
     * Displays all students by grade.
     */

    private void viewStudentsByGrade() {
        //
        // get list of students by grade
        //      to do this, you'll need to prompt the user to choose a grade level (more on this later)
        //
        // if the list of students is empty...
        //      print a message saying exactly that
        // otherwise...
        //      print the list of students by name and class rank
        //
    	
    	int inputStudentGrade = getGradeSelection();
    	
    	ArrayList<Student> students = PowerSchool.getStudentsByGradeAndRank(inputStudentGrade);
        
        if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / #" + student.getRank());
            } 
        }
    }
    
    /*
     * Displays all students by course.
     */

    private void viewStudentsByCourse(){
        //
        // get a list of students by course
        //      to do this, you'll need to prompt the user to choose a course (more on this later)
        //
        // if the list of students is empty...
        //      print a message saying exactly that
        // otherwise...
        //      print the list of students by name and grade point average
        //
    	
    	int inputCourseNo = PowerSchool.getCourseIdFromNo(getCourseSelection());

    	ArrayList<Student> students = PowerSchool.getStudentsByCourse(inputCourseNo);
        
        if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Student student : students) {
            	if(student.getGpa() == -1) {
            		System.out.println(i++ + ". " + student.getName() + " / --");
            	}else if(student.getGpa() != -1) {
            		System.out.println(i++ + ". " + student.getName() + " / " + student.getGpa());
            	}
                
            } 
        }
    }
    
    private void viewStudentsByCourse(String courseNo){
        //
        // get a list of students by course
        //      to do this, you'll need to prompt the user to choose a course (more on this later)
        //
        // if the list of students is empty...
        //      print a message saying exactly that
        // otherwise...
        //      print the list of students by name and grade point average
        //
    	
    	int inputCourseNo = PowerSchool.getCourseIdFromNo(courseNo);

    	ArrayList<Student> students = PowerSchool.getStudentsByCourse(inputCourseNo);
        
        if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Student student : students) {
            	if(student.getGpa() == -1) {
            		System.out.println(i++ + ". " + student.getName() + " / --");
            	}else if(student.getGpa() != -1) {
            		System.out.println(i++ + ". " + student.getName() + " / " + student.getGpa());
            	}
                
            } 
        }
    }
    
    private String getCourseByTeacherSelection(int teacher_id) {
    	
    	ArrayList<String> courseNames = PowerSchool.getCoursesByTeacher(teacher_id);
    	
    	if (courseNames.isEmpty()) {
            System.out.println("\nNo courses to display.");
        } else {
            System.out.println();
            int selection = -1;
        	System.out.println("\nChoose a course.\n");
        	while (selection < 1 || selection > 6) {
       
	            int i = 1;
	            for (String course : courseNames) {
	                System.out.println("[" + i++ + "] " + course);
	            } 
	            
	            selection = Utils.getInt(in, -1);
	            
	            String selectedCourse = courseNames.get(selection-1);
	            return selectedCourse;
	            
        	}
        }
    	
    	return null;
    	
    
    }
    
    private int getAssignmentIdSelection(int course_id, int marking_period, int is_midterm, int is_final) {
    	ArrayList<String> assignments = PowerSchool.getAssignmentsByCourseAndMP(course_id, marking_period, is_midterm, is_final);
    	
    	if (assignments.isEmpty()) {
            System.out.println("\nNo courses to display.");
        } else {
            System.out.println();
            int selection = -1;
        	System.out.println("\nChoose a course.\n");
        	while (selection < 1 || selection > 6) {
       
	            int i = 1;
	            for (String assignment : assignments) {
	                System.out.println("[" + i++ + "] " + assignment);
	            } 
	            
	            selection = Utils.getInt(in, -1);

	            return selection;
	            
        	}
        }
    	
    	return -1;
    }
    
    private void viewStudentsByCourseTeacher() {
    	
    	String courseName = getCourseByTeacherSelection(PowerSchool.getTeacherId(activeUser.getUserId()));
    	
    	viewStudentsByCourse(courseName);
    	
    }
    
    private void addAssignment() {
    	String courseName = getCourseByTeacherSelection(PowerSchool.getTeacherId(activeUser.getUserId()));
    	int inputIsFinal = -1;
    	int inputIsMidterm = -1;
    	int inputMarkingPeriod = -1;
    	
    	int inputMPSelection = getMarkingPeriodSelection();
    	
    	if(inputMPSelection == 5) {
    		inputIsMidterm = 1;
    	}else if(inputMPSelection == 6) {
    		inputIsFinal = 1;
    	}else if(inputMPSelection != 5 && inputMPSelection != 6) {
    		inputMarkingPeriod = inputMPSelection;
    	}
    	
    	System.out.println("Assignment Title: ");
    	String inputTitle = in.nextLine();
    	
    	double inputPointValue = -1;
    	boolean validInput = false;
    	
    	while(!(validInput)) {
    	
	    	System.out.println("Point Value: ");
	    	inputPointValue = in.nextDouble();
	    	
	    	if(inputPointValue >= 1 && inputPointValue <= 100) {
	    		validInput = true;
	    	}else if(!(inputPointValue > 1 && inputPointValue < 100)) {
	    		System.out.println("Point values must be between 1 and 100.");
	    	}
    	}
    	
    	PowerSchool.addAssignment(PowerSchool.getCourseIdFromNo(courseName), PowerSchool.getAssignmentNo(PowerSchool.getCourseIdFromNo(courseName))+1, inputMarkingPeriod, inputIsMidterm, inputIsFinal, inputTitle, inputPointValue);
    	
    	
    }
    
    public void delAssignment() {
    	
    	String courseName = getCourseByTeacherSelection(PowerSchool.getTeacherId(activeUser.getUserId()));
    	int courseId = PowerSchool.getCourseIdFromNo(courseName);
    	int inputIsFinal = -1;
    	int inputIsMidterm = -1;
    	int inputMarkingPeriod = -1;
    	
    	int inputMPSelection = getMarkingPeriodSelection();
    	
    	if(inputMPSelection == 5) {
    		inputIsMidterm = 1;
    	}else if(inputMPSelection == 6) {
    		inputIsFinal = 1;
    	}else if(inputMPSelection != 5 && inputMPSelection != 6) {
    		inputMarkingPeriod = inputMPSelection;
    	}
    	
    	int assignmentId = getAssignmentIdSelection(courseId, inputMarkingPeriod, inputIsMidterm, inputIsFinal);
    	
    	PowerSchool.delAssignment(assignmentId, courseId);
    	
    	
    }
    
    

    /**
     * Determines whether or not the user has logged in before.
     *
     * @return true if the user has never logged in; false otherwise
     */

    public boolean isFirstLogin() {
        return activeUser.getLastLogin().equals("0000-00-00 00:00:00.000");
    }
    
    /*
     * Allows a user to change his or her password.
     * 
     * @param firstLogin true if the user has never logged in; false otherwise
     */
         
    private void changePassword(boolean firstLogin) {
        // if it isn't the user's first login...
        //      ask the user for his or her current password
        //
        // ask all users (first login or not) to enter a new password
        //
        // change the password (this will require a call to the database)
        //      this requires three pieces of information: the username, the old password, and the new password
        //      the old password will either be something the use entered (if it isn't his or her first login) or
        //      it'll be the same as their username
    	
    	if(isFirstLogin()) {
    		System.out.println("Enter new password: ");
    		String newPass = in.next();
    		PowerSchool.updatePassword(activeUser.getUsername(), Utils.getHash(newPass));
    	}else if(!isFirstLogin()) {
    		System.out.println("Enter current password: ");
    		String currentPass = in.next();
    		System.out.println("Enter new password: ");
    		String newPass = in.next();
    		if(PowerSchool.login(activeUser.getUsername(), currentPass) != null) {
    			PowerSchool.updatePassword(activeUser.getUsername(), Utils.getHash(newPass));
    			System.out.println("Successfully changed password.");
    		}else if(PowerSchool.login(activeUser.getUsername(), currentPass) == null) {
    			System.out.println("Invalid current password.");
    		}
    	}
    	
    }
    
    /*
     * Shuts down the application after encountering an error.
     * 
     * @param e the error that initiated the shutdown sequence
     */

    private void shutdown(Exception e) {
        if (in != null) {
            in.close();
        }
        
        System.out.println("Encountered unrecoverable error. Shutting down...\n");
        System.out.println(e.getMessage());
                
        System.out.println("\nGoodbye!");
        System.exit(0);
    }

    /*
     * Releases all resources and kills the application.
     */

    private void shutdown() {        
        System.out.println();
            
        if (Utils.confirm(in, "Are you sure? (y/n) ")) {
            if (in != null) {
                in.close();
            }
            
            System.out.println("\nGoodbye!");
            System.exit(0);
        }
    }
    
    /*
     * Resets the database to its factory settings.
     */

    private void factoryReset() {
        //
        // ask root user to confirm intent to reset the database
        //
        // if confirmed...
        //      call database initialize method with parameter of true
        //      print success message
        //
    	
    	if(Utils.confirm(in, "Are you sure you want to reset all settings and data? (y/n) ")) {
    		PowerSchool.initialize(true);
    		System.out.println("Successfully reset database.");
    	}else {
    		System.out.println("Factory reset cancelled.");
    	}
    	
    }
    
    /*
     * Logs out of the application.
     */

    private void logout() {
        //
        // ask user to confirm intent to logout
        //
        // if confirmed...
        //      set activeUser to null
        //
    	
    	if(Utils.confirm(in, "Are you sure you want to logout? (y/n) ")){
    		activeUser = null;
    	}else {
    		System.out.println("Logout cancelled.");
    	}
    	
    }

    /////// MAIN METHOD ///////////////////////////////////////////////////////////////////

    /*
     * Starts the PowerSchool application.
     *
     * @param args unused command line argument list
     */

    public static void main(String[] args) {
        Application app = new Application();

        app.startup();
    }
}