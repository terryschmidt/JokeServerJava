/*--------------------------------------------------------

1. Terry Schmidt / Due: April 19

2. Java version used, if not the official version for the class:

1.8

3. Precise command-line compilation examples / instructions:

> javac JokeServer.java
> javac JokeClient.java
> javac JokeClientAdmin.java


4. Precise examples / instructions to run this program:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

5. List of files needed for running the program.

 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

5. Notes:

I know some things are inefficient and can be optimized.  I was mostly going for just getting it to work and for readability.
I could reimplement to remove some of the efficiency issues.  State is kept track of by strings passed between server and client.

----------------------------------------------------------*/
import java.util.*;
import java.io.*;
import java.net.*;
//I import widely.  Not an efficiency concern because below the hood java only imports what is actually used by the class.

public class JokeClient {

    public static String stateOfJokeMode = "UUUUU"; // all jokes are untold initially
    public static String stateOfProverbMode = "UUUUU"; // all proverbs are untold initially
    public static String serverName;
    public static int clientPort = 4003;
    public static String userName;
    public static String whatUserEntered;
    public static BufferedReader streamFromServer;
    public static PrintStream streamToServer;
    public static String stringFromServer1;
    public static String stringFromServer2;
    public static String stringFromServer3;
    public static String jokeOrProverbFromServer;
    public static Scanner input;
    public static Socket socket;
    
    public static void main (String[] args) throws UnknownHostException, IOException {
    		System.out.println("This is Terry's JokeClient.  JokeClients connect at port: " + clientPort); // intro
    		
            if (args.length < 1) { // check if there are any command line arguments
                serverName = "localhost";  // if not, just use localhost
            } else {  // if there are...
                serverName = args[0]; // use what was passed in
            }
            
        System.out.println("Based upon your parameters or lack thereof, we will use server name : " + serverName);
        
        input = new Scanner(System.in); // need scanner to get user input
        System.out.println("What is your name?"); // prompt for name
       
        userName = input.nextLine();  // get the users name, put it into userName
        System.out.println("If you want to hear a joke/proverb use command yes, y, or simply hit enter: ");
        
        		while (1 > 0) { // forever
        			System.out.flush();
        			whatUserEntered = input.nextLine();
        			if(whatUserEntered.equalsIgnoreCase("yes") || whatUserEntered.equalsIgnoreCase("y") || whatUserEntered.equals("")) {
        				socket = new Socket(serverName, clientPort);  // make socket with correct servername and port
        				streamFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream())); // make new stream to get stuff from server
        				streamToServer = new PrintStream(socket.getOutputStream());  // make new stream to send stuff to server
        				streamToServer.println(whatUserEntered);  // send the users input to the server
        				streamToServer.println(stateOfJokeMode); // send state of joke mode to server
        				streamToServer.println(stateOfProverbMode); // send state of proverb mode to server
        				streamToServer.println(userName); // send username to server
        				streamToServer.flush();
        				// everything above this is sending stuff to server-------------------------------------------
        				
        				// everything below this is getting stuff from server------------------------------------------
        				stringFromServer1 = streamFromServer.readLine(); // get the joke or proverb (or if its in maintenance mode, the check back message)
        				stringFromServer2 = streamFromServer.readLine(); // get state of joke mode
        				stringFromServer3 = streamFromServer.readLine(); // get state of proverb mode
        				System.out.println(stringFromServer1); // print the joke or proverb (or maintenance message, if server is in maintenance mode)
        				stateOfJokeMode = stringFromServer2; // update state
        				stateOfProverbMode = stringFromServer3; // update state
        			}
        		}
        } 
    }