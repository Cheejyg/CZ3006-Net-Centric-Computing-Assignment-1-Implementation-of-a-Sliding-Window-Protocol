import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

public class NetworkReceiver
  extends Thread
{
  private PrintWriter outputStream = null;
  private SWE swe = null;
  private String receivefile = null;
  
  public NetworkReceiver(SWE paramSWE, String paramString)
  {
    this.swe = paramSWE;
    this.receivefile = ("receive_file_" + paramString + ".txt");
  }
  
  public void run()
  {
    init();
    Packet localPacket = new Packet();
    for (;;)
    {
      this.swe.from_datalink_layer(localPacket);
      process_packet(localPacket);
    }
  }
  
  private void init()
  {
    try
    {
      File localFile = new File(this.receivefile);
      if (localFile.exists()) {
        localFile.delete();
      }
      localFile.createNewFile();
      this.outputStream = new PrintWriter(new FileOutputStream(localFile));
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      System.out.println("NetworkReceiver: File not found quitting..");
      System.exit(0);
    }
    catch (IOException localIOException)
    {
      System.out.println("Error from init(): " + localIOException + " quitting...");
      System.exit(0);
    }
  }
  
  private void process_packet(Packet paramPacket)
  {
    this.outputStream.println(paramPacket.data);
    this.outputStream.flush();
  }
}
