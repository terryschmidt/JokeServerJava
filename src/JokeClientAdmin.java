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
import java.io.*;
import java.net.*;
import java.util.*;
//I import widely.  Not an efficiency concern because below the hood java only imports what is actually used by the class.

public class JokeClientAdmin {
	
	public static String serverName;
	public static Socket socket;
	public static int clientAdminPort = 4005;
	public static String whatTheUserEntered;
	public static BufferedReader getStreamFromServer;
	public static String replyFromServer = "";
	public static PrintStream sendStreamToServer;
	
	public static void main(String[] args) throws IOException {
		if (args.length < 1) { // check if there are any command line arguments
            serverName = "localhost";  // if not, just use localhost
        } else {  // if there are...
            serverName = args[0]; // use what was passed in
        }
		
		System.out.println("This is Terry's Client Admin.  Client Admins connect on port: " + clientAdminPort + ".");  // intro
		System.out.println("Based upon your parameters or lack thereof, we will use server name : " + serverName + "."); // alert user what server is being used
		System.out.println("Enter commands joke-mode, proverb-mode, or maintenance-mode: "); // prompt user to enter a mode
		Scanner scanner = new Scanner(System.in);  // scanner to get input
		
		while(1 > 0) { // forever
			
			whatTheUserEntered = scanner.nextLine();  // get input
	        socket = new Socket(serverName, clientAdminPort);
            getStreamFromServer = new BufferedReader( new InputStreamReader(socket.getInputStream() ) );
            sendStreamToServer = new PrintStream(socket.getOutputStream());

            sendStreamToServer.println(whatTheUserEntered);  // send what the user entered to the server
            sendStreamToServer.flush();
            
            replyFromServer = getStreamFromServer.readLine(); // get server answer
            System.out.println(replyFromServer); // print response
		}
	}
}