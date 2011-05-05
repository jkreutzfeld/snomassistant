using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using System.Threading;
using Microsoft.Win32;
using System.Net;
using System.IO;


namespace EventListener
{
    class Program
    {

        static string snomNr;
        static string user;
        static string password;
        static string request_data;
        static Boolean identity1;
        static Boolean identity2;


        static AutoResetEvent signal;
        static void Main(string[] args)
        {
            Console.WriteLine(args);
            
            snomNr = args[0];
            user = args[1];
            password = args[2];
            identity1 = Boolean.Parse(args[3]);
            identity2 =  Boolean.Parse(args[4]);

            //during init of your application bind to this event  
            SystemEvents.SessionEnding += 
            new SessionEndingEventHandler(SystemEvents_SessionEnding);

            signal = new AutoResetEvent(false);
            EventLog myNewLog = new EventLog("Security");

            myNewLog.EntryWritten += new EntryWrittenEventHandler(MyOnEntryWritten);
            myNewLog.EnableRaisingEvents = true;
      
            signal.WaitOne();  

        }

        public static void MyOnEntryWritten(object source, EntryWrittenEventArgs e)
        {
            int id = e.Entry.EventID;
            if (id == 4800 || id == 4801 || id == 4802 || id == 4803 || id == 4647)
            {
                Console.WriteLine(id);
            }
        }

        static void SystemEvents_SessionEnding(object sender, SessionEndingEventArgs e)
        {
            if (identity1)
            {
                Logout(1);
            }
            if (identity2)
            {
                Logout(2); 
            }

        }

        static void Logout(int identity)
        {
            String param = "l=" + identity
            + "&Settings=Save&user_active" + identity + "=off";
            String url = "http://snom-" + snomNr + "/line_login.htm?" + param;

            // create a request
            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(url); 
            request.KeepAlive = false;
            request.ProtocolVersion = HttpVersion.Version10;
            request.Method = "POST";
            request.Credentials = new NetworkCredential(user, password);

            // turn our request string into a byte stream
            byte[] postBytes = Encoding.ASCII.GetBytes(param);

            // this is important - make sure you specify type this way
            request.ContentType = "application/x-www-form-urlencoded";
            request.ContentLength = postBytes.Length;
            Stream requestStream = request.GetRequestStream();

            // now send it
            requestStream.Write(postBytes, 0, postBytes.Length);
            requestStream.Close();

            // grab te response and print it out to the console along with the status code
            HttpWebResponse response = (HttpWebResponse)request.GetResponse();
            Console.WriteLine(new StreamReader(response.GetResponseStream()).ReadToEnd());
            Console.WriteLine(response.StatusCode);
        } // end HttpPost 
    }
}


