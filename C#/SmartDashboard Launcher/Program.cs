using System;
using System.Net;
using System.Diagnostics;
using System.IO;
using System.Net.Sockets;
using Microsoft.Win32;

namespace SmartDashboard_Launche {
    class Program {

        //Gets the installation path of java
        private static String GetJavaInstallationPath()
        {
            String javaKey = "SOFTWARE\\JavaSoft\\Java Runtime Environment";
            using (var baseKey = RegistryKey.OpenBaseKey(RegistryHive.LocalMachine, RegistryView.Registry64).OpenSubKey(javaKey))
            {
                String currentVersion = baseKey.GetValue("CurrentVersion").ToString();
                using (var homeKey = baseKey.OpenSubKey(currentVersion))
                    return homeKey.GetValue("JavaHome").ToString();
            }
        }

        static void Main(string[] args) {
            // set all the parameters for launching SmartDashboard
            string hostName;
            string ExeName = GetJavaInstallationPath() + "\\bin\\java.exe";
            string userHomePath = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
            string arguments = "-jar " + userHomePath + "\\SmartDashboard\\SmartDashboard.jar";

            // set these parameters for the process to start with
            ProcessStartInfo start = new ProcessStartInfo(ExeName);
            start.Arguments = arguments;
            start.WindowStyle = ProcessWindowStyle.Normal;
            start.CreateNoWindow = false;

            // host name can be set at runtime
            if (args.Length == 0)
                hostName = "roborio-341-frc"; // default hostname
            else
                hostName = args[0];

            // tries to resolve the IP of the host name.
            // if it cannot be resolved, SmartDashboard is started with
            // no ip as an argument, so it will use mDNS
            try {
                Console.WriteLine("Attempting to resolve host name: " + hostName);
                IPAddress[] hostIPAdresses = Dns.GetHostAddresses(hostName);
                string IPAddress = hostIPAdresses[0].ToString();
                Console.WriteLine("Resolved host name to the IP: " + IPAddress);
                arguments += " ip " + IPAddress;
            } catch (SocketException e) {
                Console.WriteLine("Could not resolve host name: " + hostName);
            }

            Console.WriteLine("Starting SmartDashboard with argument: " +
                ExeName + " " + arguments);

            // starts SmartDashboard
            Process proc = Process.Start(start);
            if (proc == null) {
                Console.WriteLine("Could not start process. Press enter to continue.");
                Console.ReadLine();
            } else {
                proc.WaitForExit();
            }
        }
    }
}