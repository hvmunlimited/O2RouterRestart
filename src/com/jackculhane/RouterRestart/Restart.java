package com.jackculhane.RouterRestart;

// Quick and (Very) Dirty telnet client

import java.net.*;
import java.io.*;

public class Restart {
    	
		static Socket telnetSocket = null;
    	static PrintWriter outPrnt = null;
    	//static BufferedReader in = null;
    	static InputStream in = null;
    	static OutputStream out = null;
    	
		static public Boolean DoReset(String Password)
		{
			if (!CreateSocket())
				return false;
			
			if (!Negotiate(Password))
				return false;
	
			if (!DoRestart())
				return false;

			try {
				out.close();
				in.close();
				telnetSocket.close();
			} catch (IOException e) {
				return true;
			}
			
			return true;
		}
		
		
		// Open socket to router
		static private Boolean CreateSocket()
		{
	        try {
	        	telnetSocket = new Socket("192.168.1.254", 23);
	        	out = telnetSocket.getOutputStream();
	        	outPrnt = new PrintWriter(out, true);
	            in = telnetSocket.getInputStream();
	            
	        } catch (UnknownHostException e) {
	        	return false;
	        } catch (IOException e) {
	        	return false;
	        }
	        return true;
		}
		
		// Do login, not very elegant! :D
		static private Boolean Negotiate(String password)
		{
			Boolean SentUserName = false;
			byte[] Buf = new byte[100];

			int len;		
			try {
				while( (len = in.read(Buf,0,100)) != 0)
				{
					if (!SentUserName)
					{
                        // Just tell the router we will do whatever it wan't until it gives us a login prompt
						for (int i=0; i<len;)
						{
							System.out.println((int) Buf[i]);
							
							if ((Buf[i] == (byte)0xFF) && (Buf[i+1] == (byte)0xFB))
							{
								out.write(new byte[] {(byte) 0xFF, (byte) 0xFB, Buf[i+2]});
								
								if ((Buf[i+1] >= (byte)0xFB) && (Buf[i+1] <= (byte)0xFE))
								{
									i += 3;
								}
								else
								{
									i +=2;
								}
							}
							else
							{
								i++;
							}
	
						}
					}
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					
					String DataStr = new String(Buf);
					
					if (!SentUserName && DataStr.indexOf("Username") != -1)
					{
						outPrnt.write("SuperUser\r\n");
						SentUserName = true;
					}
					if (DataStr.indexOf("Password") != -1)
					{
						outPrnt.write(password);
						outPrnt.write("\r\n");
					}
					if (DataStr.indexOf("SuperUser}=>") != -1)
					{
						return true;
					}
					outPrnt.flush();
					out.flush();
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					
					Buf = new byte[100];
				}
				
				return false;
				
			} catch (IOException e) {
				return false;
			}
		}
		
		// Send reboot command
		static private Boolean DoRestart()
		{
			byte[] Buf = new byte[100];
			
			outPrnt.write("\r\n");
			outPrnt.flush();
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			
			try {
				if (in.read(Buf, 0, 100) != 0)
				{
					String data = new String(Buf);
					if (data.indexOf("SuperUser}=>") != -1)
					{
						outPrnt.write("system reboot");
						outPrnt.flush();
						outPrnt.write("\r\n");
						outPrnt.flush();
						return true;
					}
					else
					{
						return false;
					}
				}
			} catch (IOException e) {
				return false;
			}
			
			return false;
		}
}
