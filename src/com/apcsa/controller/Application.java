package com.apcsa.controller;

import java.util.Scanner;
import com.apcsa.data.PowerSchool;
import com.apcsa.model.User;

public class Application {

    private Scanner in;
    private User activeUser;

    enum RootAction { PASSWORD, DATABASE, LOGOUT, SHUTDOWN }
    
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

            try {
                if (login(username, password)) {
                    activeUser = activeUser.isAdministrator()
                        ? PowerSchool.getAdministrator(activeUser) : activeUser.isTeacher()
                        ? PowerSchool.getTeacher(activeUser) : activeUser.isStudent()
                        ? PowerSchool.getStudent(activeUser) : activeUser.isRoot()
                        ? activeUser : null;
                        
                    if (isFirstLogin() && !activeUser.isRoot()) {
                        // first-time users need to change their passwords from the default provided
                    }
                    
                    createAndShowUI();
                } else {
                    System.out.println("\nInvalid username and/or password.");
                }
            } catch (Exception e) {
                shutdown(e);
            }
            
            /**
             * Displays an user type-specific menu with which the user
             * navigates and interacts with the application.
             */
            
//            try {
//                if (login(username, password)) {
//                    activeUser = activeUser.isAdministrator()
//                        ? PowerSchool.getAdministrator(activeUser) : activeUser.isTeacher()
//                        ? PowerSchool.getTeacher(activeUser) : activeUser.isStudent()
//                        ? PowerSchool.getStudent(activeUser) : activeUser.isRoot()
//                        ? activeUser : null;
//                        
//                    if (isFirstLogin() && !activeUser.isRoot()) {
//                        // first-time users need to change their passwords from the default provided
//                    	System.out.println("Test");
//                    }
//
//                    // create and show the user interface
//                    //
//                    // remember, the interface will be difference depending on the type
//                    // of user that is logged in (root, administrator, teacher, student)
//                    
//                    if (activeUser.isRoot()) {
//                    	
//                    }
//                } else {
//                	System.out.println(login(username, password));
//                	System.out.println(PowerSchool.login(username, password));
//                    System.out.println("\nInvalid username and/or password.");
//                }
//            } catch (Exception e) {
//                shutdown(e);
//            }

        }
    }
    
    public void createAndShowUI() {
        System.out.println("\nHello, again, " + activeUser.getFirstName() + "!");

        if (activeUser.isRoot()) {
            showRootUI();
        } else {
            // TODO - add cases for admin, teacher, student, and unknown
        }
    }
    
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

    /**
     * Determines whether or not the user has logged in before.
     *
     * @return true if the user has never logged in; false otherwise
     */

    public boolean isFirstLogin() {
        return activeUser.getLastLogin().equals("0000-00-00 00:00:00.000");
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
    
    /* Allows a root user to reset another user's password.
    */

   private void resetPassword() {
       //
       // prompt root user to enter username of user whose password needs to be reset
       //
       // ask root user to confirm intent to reset the password for that username
       //
       // if confirmed...
       //      call database method to reset password for username
       //      print success message
       //
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