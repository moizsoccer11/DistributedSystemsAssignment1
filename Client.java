import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    //FUNCTIONS
    //Function to Check If user login exists
    public static String[] GetLoginDetails(String loginID){
        String[] loginDetails = {};
        try {
        File myObj = new File("login.txt");
        Scanner myReader = new Scanner(myObj);
        while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if(data.contains(loginID)){
                  loginDetails= data.split("/");
                }
        }
        myReader.close();
        } catch (Exception e) {
           
        }
        return loginDetails;
    }
    //Function to Create new User Login ID
    public static void CreateLoginID(String userName, String loginID){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("login.txt",true));
                writer.write(loginID+"/"+userName);
                writer.newLine();
                writer.close();
            } catch (IOException e) {
        }
    }
    public static void main(String[] args) {
        Socket server;
        PrintWriter serverOut;
        BufferedReader serverIn;
        BufferedReader stdIn;
        String userInput;
        boolean loggedIn =false;
        String loginID;
        String userName="";
        String[] loginDetails={};
        try {
            //Create Socket and Input/Output Streams
            server = new Socket("localhost", 3500);
            serverOut= new PrintWriter(server.getOutputStream(), true);
            serverIn= new BufferedReader(new InputStreamReader(server.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            //
            while(true){
                //Login Sequence
                System.out.println("Welcome to Threaded Rooms, If you have a Login ID please Enter, If you don't type '1'");
                userInput= stdIn.readLine();
                if(userInput.equals("1")){
                    System.out.println("Please enter a Login ID:");
                    userInput = stdIn.readLine();
                    loginID=userInput;
                    System.out.println("Please enter your user name:");
                    userInput = stdIn.readLine();
                    userName=userInput;
                    CreateLoginID(loginID,userName);
                    loggedIn=true;
                }
                else{
                    loginDetails = GetLoginDetails(userInput);
                    if(loginDetails.length > 1){
                        loggedIn=true;
                        userName=loginDetails[1];
                    }
                }
                while(!loggedIn){
                    System.out.println("Incorrect Login ID, If you don't have one type '1'");
                    userInput= stdIn.readLine();
                    if(userInput.equals("1")){
                        System.out.println("Please enter a Login ID:");
                        userInput = stdIn.readLine();
                        loginID=userInput;
                        System.out.println("Please enter your user name:");
                        userInput = stdIn.readLine();
                        userName=userInput;
                        CreateLoginID(loginID,userName);
                        loggedIn=true;
                    }
                    else{
                        loginDetails = GetLoginDetails(userInput);
                        if(loginDetails.length > 1){
                            loggedIn=true;
                            userName=loginDetails[1];
                        }
                    }
                }
                //Since user logged In, Send Username to Server;
                serverOut.println(userName);
                //Interact with server:::
                // Create a separate thread for reading messages from the server
                Thread readThread = new Thread(() -> {
                    try {
                        String serverResponse;
                        while ((serverResponse = serverIn.readLine()) != null) {
                            System.out.println(serverResponse);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                readThread.start();

                // Main thread for sending user messages
                String message;
                while (true) {
                    message = stdIn.readLine();
                    serverOut.println(message);
                    if (message.equalsIgnoreCase("exit")) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
