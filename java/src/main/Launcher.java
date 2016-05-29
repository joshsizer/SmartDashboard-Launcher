package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Finds the IP of the robot and opens smart dashboard with that IP. mDNS
 * sometimes stops working, so this is an easy way to open SmartDashboard with
 * the correct IP every time
 * 
 * @author joshs
 *
 */
public class Launcher {
	public static void main(String[] args) {
		// this is the domain name for the robot
		String hostName = "roborio-341-frc";

		// we try to connect to the robot
		try {
			// internet address object of the hostname
			InetAddress blah = InetAddress.getByName(hostName);

			// a byte array (because each octet is an 8 bit
			// number) of the octets in the adress
			// {byte, byte, byte, byte} ex {192.168.1.1}
			byte[] adress = blah.getAddress();

			// we need to convert the number address into a string
			// so that we can open SmartDashboard with the IP as
			// a command line argument
			String IP = "";
			for (byte b : adress) {
				IP += Byte.toString(b);

				// if its the last byte in the array, we don't
				// need a "." after it.
				if (b == adress[adress.length - 1])
					break;

				IP += ".";
			}

			// now we try to open SmartDashboard with the newly aquired IP
			try {
				// this is the default location for SmartDashboard
				// in the user's home folder
				File SmartDashboard = new File(System.getProperty("user.home")
						+ "\\SmartDashboard\\Smartdashboard.jar");
				
				// now we append the path of SmartDashboard to the command
				// line arguments
				String arg = "java -jar " + SmartDashboard.getAbsolutePath()
						+ " ip " + IP;
				
				// here we attempt to start SmartDashboard.jar
				Process proc = Runtime.getRuntime().exec(arg);
				
				// this is now just extra stuff for seeing the output
				// of SmartDashboard and the errors
				BufferedReader inputStream = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				BufferedReader errorStream = new BufferedReader(
						new InputStreamReader(proc.getErrorStream()));
				
				// delegate the two streams to new threads so that 
				// they can run concurrently. For some reason the 
				// stream only gets generated once the program is exited,
				// which is kinda weird
				Thread inputStreamThread = new Thread(new Runnable() {
					@Override
					public void run() {
						String line;
						try {
							while ((line = inputStream.readLine()) != null) {
								System.out.println(line);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});

				Thread errorStreamThread = new Thread(new Runnable() {
					@Override
					public void run() {
						String line;
						try {
							while ((line = errorStream.readLine()) != null) {
								System.out.println(line);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				
				// start the threads!
				inputStreamThread.start();
				errorStreamThread.start();
				
				// waits for SmartDashboard to close before
				// ending this program
				try {
					proc.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				System.err.println("Could not open SmartDashboard.jar.");
			}
		} catch (UnknownHostException e) {
			System.err.println("Could not resolve hostname: " + hostName);
		}
	}

}
