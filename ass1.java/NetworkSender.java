import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class NetworkSender
  extends Thread
{
  private SWE swe = null;
  private String sendfile = null;
  private BufferedReader inputStream = null;
  
  public NetworkSender(SWE paramSWE, String paramString)
  {
    this.swe = paramSWE;
    this.sendfile = ("send_file_" + paramString + ".txt");
  }
  
  public void run()
  {
    init();
    Packet localPacket = produce_packet();
    while (localPacket.data != null)
    {
      this.swe.get_credit();
      this.swe.to_datalink_layer(localPacket);
      localPacket = produce_packet();
    }
  }
  
  private void init()
  {
    try
    {
      this.inputStream = new BufferedReader(new FileReader(this.sendfile));
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      System.out.println("NetworkSender: File not found quitting...");
      System.exit(0);
    }
    catch (IOException localIOException)
    {
      System.out.println("Error from init(): " + localIOException + " quitting...");
      System.exit(0);
    }
  }
  
  private Packet produce_packet()
  {
    Packet localPacket = new Packet();
    try
    {
      localPacket.data = this.inputStream.readLine();
    }
    catch (IOException localIOException)
    {
      System.out.println("NetworkSender: Error from produce_packet(): " + localIOException);
    }
    return localPacket;
  }
}
