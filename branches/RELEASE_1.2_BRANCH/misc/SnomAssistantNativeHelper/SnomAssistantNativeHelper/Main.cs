using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Linq;
using System.Text;
using System.Diagnostics;
using System.Net.Sockets;
using System.Threading;
using Microsoft.Win32;
using System.Net;
using System.IO;

namespace SnomAssistantNativeHelper
{

	public class MainClass
	{
		public string snomNr = "";
		public string user = "";
		public string password = "";
		public Boolean identity1 = false;
		public Boolean identity2 = false;
		public Boolean logoutOnShutdown = false;
		public Boolean watchLockScreen = false;
		public int port = 8800;
		public String lineToWrite = "";
		
		public Boolean alive = true;

		public AutoResetEvent signal;


		static void Main (string[] args)
		{
			
			new MainClass (args);
			
			
		}


		public MainClass (string[] args)
		{
			
			snomNr = args[0];
			user = args[1];
			password = args[2];
			identity1 = Boolean.Parse (args[3]);
			identity2 = Boolean.Parse (args[4]);
			port = int.Parse (args[5]);
			logoutOnShutdown = Boolean.Parse (args[6]);
			watchLockScreen = Boolean.Parse (args[7]);
			
			new Server (port + 1, this);
			
			
			//during init of your application bind to this event  
			SystemEvents.SessionEnding += new SessionEndingEventHandler (SystemEvents_SessionEnding);
			
			signal = new AutoResetEvent (false);
			EventLog myNewLog = new EventLog ("Security");
			
			myNewLog.EntryWritten += new EntryWrittenEventHandler (MyOnEntryWritten);
			myNewLog.EnableRaisingEvents = true;
			signal.WaitOne ();
		}

		public void MyOnEntryWritten (object source, EntryWrittenEventArgs e)
		{
			if (watchLockScreen) {
				long id = e.Entry.InstanceId;
				if (id == 4800 || id == 4801 || id == 4802 || id == 4803 || id == 4647) {
					Console.WriteLine(id+"");
					new Client (port).send (id + "");
				}
			}
		}

		void SystemEvents_SessionEnding (object sender, SessionEndingEventArgs e)
		{
			if (logoutOnShutdown) {
				if (identity1) {
					Logout (1);
				}
				if (identity2) {
					Logout (2);
				}
			}
		}

		void Logout (int identity)
		{
			String param = "l=" + identity + "&Settings=Save&user_active" + identity + "=off";
			String url = "http://snom-" + snomNr + "/line_login.htm?" + param;
			WebClient c = new WebClient();
            NameValueCollection list = new NameValueCollection();
			list["Settings"]="Save";
			list["user_active"+identity]="off";
			
			
			c.Encoding = System.Text.Encoding.UTF8;
		
			CredentialCache cache = new CredentialCache ();
			cache.Add (new Uri ("http://snom-"+snomNr+"/"), "Basic", new NetworkCredential (user, password));
			c.Credentials = cache;
			
			
			
			c.UploadValues(new Uri(url),"POST",list);
		}
		// end HttpPost 




		public void read (string response)
		{
			Console.WriteLine ("SnomAssistant sent: " + response);
			if (response.Equals ("watchShutdown=false")) {
				this.logoutOnShutdown = false;
			} else if (response.Equals ("watchShutdown=true")) {
				this.logoutOnShutdown = true;
			} else if (response.Equals ("watchLockScreen=false")) {
				this.watchLockScreen = false;
			} else if (response.Equals ("watchLockScreen=true")) {
				this.watchLockScreen = true;
			} else if (response.StartsWith ("user=")) {
				this.user = response.Substring (response.IndexOf ('=') + 1);
			} else if (response.StartsWith ("password=")) {
				this.password = response.Substring (response.IndexOf ('=') + 1);
			} else if (response.StartsWith ("phone=")) {
				this.snomNr = response.Substring (response.IndexOf ('=') + 1);
			} else if (response.StartsWith ("identity1=")) {
				this.identity1 = Boolean.Parse (response.Substring (response.IndexOf ('=') + 1));
			} else if (response.StartsWith ("identity2=")) {
				this.identity2 = Boolean.Parse (response.Substring (response.IndexOf ('=') + 1));
			} else if (response.Equals("exit")) {
				this.alive = false;
			}
		}
	}

	class Server
	{
		private TcpListener tcpListener;
		private Thread listenThread;
		private MainClass c;


		public Server (int port, MainClass c)
		{
			
			
			this.c = c;
			IPAddress ipAddress = Dns.GetHostEntry("localhost").AddressList[0];
			this.tcpListener = new TcpListener (ipAddress, port);
			this.listenThread = new Thread (new ThreadStart (ListenForClients));
			this.listenThread.Start ();
		}

		private void ListenForClients ()
		{
			this.tcpListener.Start ();
			
			while (c.alive) {
				//blocks until a client has connected to the server
				TcpClient client = this.tcpListener.AcceptTcpClient ();
				
				//create a thread to handle communication 
				//with connected client
				Thread clientThread = new Thread (new ParameterizedThreadStart (HandleClientComm));
				clientThread.Start (client);
			}
		}

		private void HandleClientComm (object client)
		{
			TcpClient tcpClient = (TcpClient)client;
			NetworkStream clientStream = tcpClient.GetStream ();
			
			byte[] message = new byte[4096];
			int bytesRead;
			
			while (c.alive) {
				bytesRead = 0;
				
				try {
					//blocks until a client sends a message
					bytesRead = clientStream.Read (message, 0, 4096);
				} catch {
					//a socket error has occured
					break;
				}
				
				if (bytesRead == 0) {
					//the client has disconnected from the server
					break;
				}
				
				//message has successfully been received
				ASCIIEncoding encoder = new ASCIIEncoding ();
				c.read (encoder.GetString (message, 0, bytesRead));
			}
			
			tcpClient.Close ();
		}
	}
	class Client
	{

		int port;


		public Client (int port)
		{
			this.port = port;
		}

		public void send (string data)
		{
			TcpClient client = new TcpClient ();
			
			IPEndPoint serverEndPoint = new IPEndPoint (IPAddress.Parse ("127.0.0.1"), port);
			
			client.Connect (serverEndPoint);
			
			NetworkStream clientStream = client.GetStream ();
			
			ASCIIEncoding encoder = new ASCIIEncoding ();
			byte[] buffer = encoder.GetBytes (data);
			
			clientStream.Write (buffer, 0, buffer.Length);
			clientStream.Flush ();
			client.Close();
		}
	}
	
}

