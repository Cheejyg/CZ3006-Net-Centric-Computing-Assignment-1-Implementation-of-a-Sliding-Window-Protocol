import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class NetSim
{
  public static final int PORT = 54321;
  public static final int NUM = 2;
  
  public static void main(String[] paramArrayOfString)
  {
    int i = 0;
    try
    {
      i = Integer.parseInt(paramArrayOfString[0]);
    }
    catch (Exception localException1)
    {
      System.err.println("\nERROR: Quality level required!");
      System.err.println("\nUsage: java NetSim [quality level] where quality level = 0 -> 3");
      
      System.exit(0);
    }
    if ((i < 0) || (i > 3))
    {
      System.err.println("\nERROR: Invalid quality level: " + i);
      
      System.err.println("\nUsage: java NetSim [quality level] where quality level = 0 -> 3");
      
      System.exit(0);
    }
    Socket[] arrayOfSocket = new Socket[2];
    
    DataInputStream[] arrayOfDataInputStream = new DataInputStream[2];
    
    DataOutputStream[] arrayOfDataOutputStream = new DataOutputStream[2];
    
    ServerSocket localServerSocket = null;
    try
    {
      localServerSocket = new ServerSocket(54321);
    }
    catch (Exception localException2)
    {
      System.err.println("NetSim: ServerSocket()" + localException2);
      System.exit(0);
    }
    for (int j = 0; j < 2; j++) {
      try
      {
        System.err.println("NetSim(Port= 54321) is waiting for connection ... ");
        
        arrayOfSocket[j] = localServerSocket.accept();
        arrayOfDataInputStream[j] = new DataInputStream(arrayOfSocket[j].getInputStream());
        arrayOfDataOutputStream[j] = new DataOutputStream(arrayOfSocket[j].getOutputStream());
        System.err.println("NetSim accepted connection from: " + arrayOfSocket[j].getInetAddress().getHostName() + " : " + arrayOfSocket[j].getPort());
      }
      catch (Exception localException3)
      {
        System.err.println("NetSim: accept Exception " + localException3);
        System.exit(0);
      }
    }
    for (int k = 0; k < 2; k++)
    {
      Forwarder localForwarder = new Forwarder(arrayOfDataInputStream[k], arrayOfDataOutputStream[(1 - k)]);
      localForwarder.setQuality(i);
      localForwarder.start();
    }
  }
}
