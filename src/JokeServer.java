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
I can reimplement to remove some of the efficiency issues.  State is kept track of by strings passed between server and client.

----------------------------------------------------------*/

import java.io.*;
import java.net.*;
import java.util.*;
//  I import widely.  Not an efficiency concern because below the hood java only imports what is actually used by the class.

public class JokeServer {
	static int regularClientPort;
	static int clientAdminPort;
	static boolean isInMaintenanceMode = false;
	static boolean isInProverbMode = false;
	static boolean isInJokeMode = true;  // server starts as a default in jokeMode
	
	public static void main (String[] args) throws IOException {
		int numberOfRequestsToQueue = 10; // number of clients
		Socket clientSocket;  // new socket
		regularClientPort = 4003;  // client port
		clientAdminPort = 4005;  // admin port
		ListenForJokeClientAdmins LFJCA = new ListenForJokeClientAdmins();  // create object to listen for clients
		Thread thread = new Thread(LFJCA); // thread object with a ListenForJokeClientAdmins object as its runnable.
		thread.start(); // start up
		ServerSocket ss = new ServerSocket(regularClientPort, numberOfRequestsToQueue);
		
		System.out.println("Terry's JokeServer Waiting for Clients to connect on port: " + regularClientPort); // basic introduction
		System.out.println("Terry's JokeServer Waiting for ClientAdmins to connect on port: " + clientAdminPort); // basic introduction

		while (1 > 0) { // infinite loop
			clientSocket = ss.accept(); // connect
			new WorkerForClient(clientSocket).start(); // make a new worker
		}
	}
}
		// inner class for looking for ClientAdmins-----------------------------------------------------------------
		class ListenForJokeClientAdmins implements Runnable {
			int portToUse;
			public void run() {  // implementing runnable means we must have a run() function
				portToUse = 4005;  // port for clientAdmins to connect
				int numberOfRequestsToQueue = 10; // number of clientAdmins to queue
				Socket clientAdminSocket;  // socket
				
				try {  // try block
					ServerSocket clientAdminServerSocket = new ServerSocket(portToUse, numberOfRequestsToQueue);  // initialize server socket
					while (1 > 0) {  // forever
						clientAdminSocket = clientAdminServerSocket.accept(); // connect
						new WorkerForClientAdmin(clientAdminSocket).start(); // make a new worker for the client
					}
				} catch (IOException Exc) {
					System.out.println("Something went wrong! Exiting program."); // error
					System.exit(1);  // exit
				}
			}
		}
		
		// inner class for creating ClientAdmin threads-------------------------------------------------------------
		class WorkerForClientAdmin extends Thread {  // extend the Thread superclass
			Socket clientAdminSocket = new Socket();
			WorkerForClientAdmin(Socket clientAdminSocket) { // constructor
				this.clientAdminSocket = clientAdminSocket;
			}
			
			public void run() {
				try {
					PrintStream outputToAdmin = new PrintStream(clientAdminSocket.getOutputStream());
					Scanner inputFromAdmin = new Scanner(new InputStreamReader(clientAdminSocket.getInputStream()));
					String whatTheUserTyped = inputFromAdmin.nextLine(); // get user input, put it in string variable
					
					// figure out what the admin client user typed, act accordingly
					switch (whatTheUserTyped) {
					case "joke-mode": // what if the user typed joke-mode?
							JokeServer.isInMaintenanceMode = false; // then server isn't in maintenance mode.
							JokeServer.isInProverbMode = false; // then the server isn't in proverb mode either.
							JokeServer.isInJokeMode = true; // server IS now in joke mode.
							outputToAdmin.println("Server is now in joke mode.");  // alert
							break;  // get out of switch
					case "proverb-mode":  // what if the user typed proverb-mode?
							JokeServer.isInMaintenanceMode = false;  // then the server can't be in maintenance mode.
							JokeServer.isInJokeMode = false;  // server is not in joke mode either.
							JokeServer.isInProverbMode = true;  // the server IS in proverb mode now
							outputToAdmin.println("Server is now in proverb mode.");  // alert
							break; // get out of switch
					case "maintenance-mode":  // what if the user typed maintenance-mode?
							JokeServer.isInMaintenanceMode = true;  // then set the server in maintenance mode.
							JokeServer.isInJokeMode = false;  // server is not in joke mode.
							JokeServer.isInProverbMode = false;  // server is not in proverb mode.
							outputToAdmin.println("Server is now in maintenance mode.");  // alert
							break;  // get out of switch
					default:   // user didn't type a correct command
							outputToAdmin.println("I'm sorry, I didn't understand your command.  Please try the exact commands: joke-mode, proverb-mode, or maintenance-mode.");  // alert
							break;  // get out of switch
					}
					inputFromAdmin.close();  // close to remove resource leak
			        } catch (IOException Exc) {
			            System.out.println("Something went wrong."); // error
			            System.exit(1); // exit
			        }
			    }
			}
		
		// inner class for creating thread for each client----------------------------------------------------------
		class WorkerForClient extends Thread { // extend thread superclass
			String stateOfJokeMode;  // TTTTT means all jokes have been told.  UUUUU means no jokes have been told.  TUUUU means only the first joke has been told, etc.
			String stateOfProverbMode;  // TTTTT means all proverbs have been told.  UUUUU means no proverbs have been told.  UUUUT means only the last proverb has been told, etc.
			Socket clientSocket;
			
			WorkerForClient(Socket clientSocket) { // constructor
				this.clientSocket = clientSocket;
			}
			
			public void run() {  // run method
				PrintStream outputToClient;
				Scanner inputFromClient;
				try {
					outputToClient = new PrintStream(clientSocket.getOutputStream());  // for output
					 inputFromClient = new Scanner(new InputStreamReader(clientSocket.getInputStream()));  // for input
					
				
						try {
							String lineFromClient1 = inputFromClient.nextLine();  // get what the user typed in response to question asking of they want a Joke/Proverb
						
							stateOfJokeMode = inputFromClient.nextLine(); // get the state of JokeMode.  What jokes have been told?  Which haven't?
							if(stateOfJokeMode.equals("TTTTT")) { // if all jokes have been told
								stateOfJokeMode = "UUUUU";  // reset all to untold again.
							}
							
							stateOfProverbMode = inputFromClient.nextLine();  // get state of Proverb Mode
							if(stateOfProverbMode.equals("TTTTT")) { // check if all proverbs have been told.
								stateOfProverbMode = "UUUUU"; // if they have, then set all proverbs to untold.
							}
							
							String name = inputFromClient.nextLine();  // get what the user entered as their name
							
							if(lineFromClient1.equalsIgnoreCase("yes") || lineFromClient1.equalsIgnoreCase("y") || lineFromClient1.equals("")) {
								if(JokeServer.isInJokeMode == true && JokeServer.isInProverbMode == false && JokeServer.isInMaintenanceMode == false) {
									System.out.println("Getting a joke for client now."); // alert
									outputToClient.println(chooseRandomJoke(name));  // send joke to client
									outputToClient.println(stateOfJokeMode);  // send joke state to client
									outputToClient.println(stateOfProverbMode); // send proverb state to client
								} else if (JokeServer.isInProverbMode == true && JokeServer.isInJokeMode == false && JokeServer.isInMaintenanceMode == false) {
									System.out.println("Getting a proverb for client now."); // alert
									outputToClient.println(chooseRandomProverb(name)); // send proverb to client
									outputToClient.println(stateOfJokeMode); // send joke state to client
									outputToClient.println(stateOfProverbMode); // send proverb state to client
								} else if (JokeServer.isInMaintenanceMode == true && JokeServer.isInJokeMode == false && JokeServer.isInProverbMode == false) {
									System.out.println("I'm in maintenance mode now."); // alert
									outputToClient.println("Admin put the server in maintenance mode."); // alert to client about mode
									outputToClient.println(stateOfJokeMode); // send joke state to client
									outputToClient.println(stateOfProverbMode); // send proverb state to client
								} 
							}
						} catch (RuntimeException Exc) {
							System.out.println("Something went wrong! Exiting program.");  // error
							System.exit(1); // exit
						}
					inputFromClient.close(); // close to remove resource leak
			} catch (IOException Exc) {
				System.out.println("Something went wrong! Exiting program."); // error
				System.exit(1); // exit
			}
		}
		
			
			 public int generateRandomInteger (int minimumPossible, int maximumPossible) { // method for generating a random integer
					Random RNG = new Random();  // create a new random number generator
					int randomNumber = RNG.nextInt((maximumPossible - minimumPossible) + 1) + minimumPossible; // have to add 1 in order to make it inclusive of the max number.
					return randomNumber;  // return the random number
				}
			 
			 // Probably not a very efficient way of doing this, but gets the job done.  Can reimplement at some point.
			 public String chooseRandomJoke(String theirName) {
				 String jokeToReturn = ""; // put joke in here
				 
				 while (1 > 0) { // in this loop for as long as it takes for random number to match up with an untold joke
					 int randomNumber = generateRandomInteger(0, 4);  // get a random number
					 switch (randomNumber) {
					 case 0:  // randomNumber is 0
						 if(stateOfJokeMode.charAt(0) == 'U') { // if this joke is untold
							 jokeToReturn = "Joke A: What does a nosey pepper do, " + theirName + "? Gets jalapeno business!";
							 stateOfJokeMode = "T" + stateOfJokeMode.substring(1);  // update the state to reflect that this joke was chosen
							 return jokeToReturn;
						 }
					 case 1: // randomNumber is 1
						 if(stateOfJokeMode.charAt(1) == 'U') { // if this joke is untold
							 jokeToReturn = "Joke B: What do you call a fake noodle, " + theirName + "? An Impasta!";
							 stateOfJokeMode = stateOfJokeMode.substring(0, 1) + "T" + stateOfJokeMode.substring(2);  // update the state to reflect that this joke was chosen
							 return jokeToReturn;
						 }
					 case 2: // randomNumber is 2
						 if(stateOfJokeMode.charAt(2) == 'U') { // if this joke is untold
							 jokeToReturn = "Joke C: What did Bacon say to Tomato, " + theirName + "? Lettuce get together!";
							 stateOfJokeMode = stateOfJokeMode.substring(0, 2) + "T" + stateOfJokeMode.substring(3);   // update the state to reflect that this joke was chosen
							 return jokeToReturn;
						 }
					 case 3: // randomNumber is 3
						 if(stateOfJokeMode.charAt(3) == 'U') { // if this joke is untold
							 jokeToReturn = "Joke D: What do you call an illegally parked frog, " + theirName + "? Toad.";
							 stateOfJokeMode = stateOfJokeMode.substring(0, 3) + "T" + stateOfJokeMode.substring(4);   // update the state to reflect that this joke was chosen
							 return jokeToReturn;
						 }
					 case 4: // randomNumber is 4
						 if(stateOfJokeMode.charAt(4) == 'U') { // if this joke is untold
							 jokeToReturn = "Joke E: Why don't skeletons fight each other, " + theirName + "? They don't have the guts.";
							 stateOfJokeMode = stateOfJokeMode.substring(0, 4) + "T";   // update the state to reflect that this joke was chosen
							 return jokeToReturn;
						 }
					 }
				 }
			 }
			 
			 // Probably not a very efficient way of doing this, but gets the job done.  Can reimplement at some point.
			 public String chooseRandomProverb(String theirName) {
				 String proverbToReturn = ""; // put proverb in here
				 
				 while (1 > 0) { // forever
					 int randomNumber = generateRandomInteger(0, 4);  // get a random number
					 switch (randomNumber) {
					 case 0:  // randomNumber is 0
						 if(stateOfProverbMode.charAt(0) == 'U') {  // if this proverb is untold
							 proverbToReturn = "Proverb A: Discretion is the greater part of valor, " + theirName + ".";
							 stateOfProverbMode = "T" + stateOfProverbMode.substring(1);  // update the state to reflect that this proverb was chosen
							 return proverbToReturn;
						 }
					 case 1: // randomNumber is 1
						 if(stateOfProverbMode.charAt(1) == 'U') { // if this proverb is untold
							 proverbToReturn = "Proverb B: Fortune favors the bold, " + theirName + ".";
							 stateOfProverbMode = stateOfProverbMode.substring(0, 1) + "T" + stateOfProverbMode.substring(2);  // update the state to reflect that this proverb was chosen
							 return proverbToReturn;
						 }
					 case 2: // randomNumber is 2
						 if(stateOfProverbMode.charAt(2) == 'U') { // if this proverb is untold
							 proverbToReturn = "Proverb C: You can't make an omelet without breaking a few eggs, " + theirName + ".";
							 stateOfProverbMode = stateOfProverbMode.substring(0, 2) + "T" + stateOfProverbMode.substring(3);   // update the state to reflect that this proverb was chosen
							 return proverbToReturn;
						 }
					 case 3: // randomNumber is 3
						 if(stateOfProverbMode.charAt(3) == 'U') { // if this proverb is untold
							 proverbToReturn = "Proverb D: You can lead a horse to water, but you can't make him drink, " + theirName + ".";
							 stateOfProverbMode = stateOfProverbMode.substring(0, 3) + "T" + stateOfProverbMode.substring(4);   // update the state to reflect that this proverb was chosen
							 return proverbToReturn;
						 }
					 case 4: // randomNumber is 4
						 if(stateOfProverbMode.charAt(4) == 'U') { // if this proverb is untold
							 proverbToReturn = "Proverb E: The pen is mightier than the sword, " + theirName + ".";
							 stateOfProverbMode = stateOfProverbMode.substring(0, 4) + "T";   // update the state to reflect that this proverb was chosen
							 return proverbToReturn;
						 }
					 }
				 }
			 }
		}