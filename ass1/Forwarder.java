import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintStream;

public class Forwarder
  extends Thread
{
  private String siteid = null;
  private DataInputStream smdis = null;
  private DataOutputStream smdos = null;
  private int quality = 0;
  private int err_count = 0;
  
  public Forwarder(DataInputStream paramDataInputStream, DataOutputStream paramDataOutputStream)
  {
    this.smdis = paramDataInputStream;
    this.smdos = paramDataOutputStream;
  }
  
  public void setQuality(int paramInt)
  {
    this.quality = paramInt;
  }
  
  public void run()
  {
    PFrameMsg localPFrameMsg = new PFrameMsg();
    for (;;)
    {
      get_frame(localPFrameMsg);
      forward_frame(localPFrameMsg);
    }
  }
  
  private void get_frame(PFrameMsg paramPFrameMsg)
  {
    try
    {
      paramPFrameMsg.receive(this.smdis);
      if (paramPFrameMsg.kind == 4) {
        this.siteid = paramPFrameMsg.info.data;
      }
    }
    catch (Exception localException)
    {
      System.out.println("Error on receiving frame: " + localException + " NetSim quitting....");
      
      System.exit(0);
    }
  }
  
  private void forward_frame(PFrameMsg paramPFrameMsg)
  {
    if (paramPFrameMsg.kind == 4)
    {
      try
      {
        paramPFrameMsg.send(this.smdos);
      }
      catch (Exception localException1)
      {
        System.out.println("Error on forwarding connection frame: " + localException1);
        
        System.exit(0);
      }
      return;
    }
    double d = Math.random();
    try
    {
      switch (this.quality)
      {
      case 0: 
        paramPFrameMsg.send(this.smdos); break;
      case 1: 
        if (d < 0.75D)
        {
          paramPFrameMsg.send(this.smdos);
        }
        else
        {
          this.err_count += 1;
          System.out.println("VMach " + this.siteid + " loose frame seq = " + paramPFrameMsg.seq + " error counter = " + this.err_count);
          
          System.out.flush();
        }
        break;
      case 2: 
        if (d < 0.75D)
        {
          paramPFrameMsg.send(this.smdos);
        }
        else
        {
          this.err_count += 1;
          System.out.println("VMach " + this.siteid + " Check sum error for seq = " + paramPFrameMsg.seq + " error counter = " + this.err_count);
          
          System.out.flush();
          paramPFrameMsg.kind = 3;
          paramPFrameMsg.send(this.smdos);
        }
        break;
      case 3: 
        if (d < 0.5D)
        {
          paramPFrameMsg.send(this.smdos);
        }
        else if (d < 0.75D)
        {
          this.err_count += 1;
          System.out.println("VMach " + this.siteid + " Check sum error for seq = " + paramPFrameMsg.seq + " error counter = " + this.err_count);
          
          System.out.flush();
          paramPFrameMsg.kind = 3;
          paramPFrameMsg.send(this.smdos);
        }
        else
        {
          this.err_count += 1;
          System.out.println("VMach " + this.siteid + " loose frame seq = " + paramPFrameMsg.seq + " error counter = " + this.err_count);
          
          System.out.flush();
        }
        break;
      default: 
        System.out.println("VMach " + this.siteid + " undefined quality level " + this.quality);
      }
    }
    catch (Exception localException2)
    {
      System.out.println("Error on forwarding frame: " + localException2);
    }
  }
}
