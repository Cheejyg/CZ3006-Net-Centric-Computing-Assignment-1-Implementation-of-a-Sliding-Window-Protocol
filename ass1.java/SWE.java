import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public class SWE
{
  private Socket myso = null;
  private DataInputStream mydis = null;
  private DataOutputStream mydos = null;
  private EventQueue equeue = new EventQueue();
  private PacketQueue pqueue = new PacketQueue();
  private int credit = 0;
  private String siteid = null;
  
  public SWE(String paramString)
  {
    this.siteid = paramString;
  }
  
  public void init()
  {
    try
    {
      System.out.println("VMach is making a connection with NetSim...");
      
      this.myso = new Socket(InetAddress.getLocalHost(), 54321);
      
      System.out.println("VMach(" + this.myso.getLocalPort() + ") <===> NetSim(" + this.myso.getInetAddress() + ":" + this.myso.getPort() + ")");
      
      this.mydos = new DataOutputStream(this.myso.getOutputStream());
      this.mydis = new DataInputStream(this.myso.getInputStream());
      
      PFrameMsg localPFrameMsg = new PFrameMsg();
      localPFrameMsg.kind = 4;
      localPFrameMsg.info.data = this.siteid;
      localPFrameMsg.send(this.mydos);
      localPFrameMsg.receive(this.mydis);
      
      FrameHandler localFrameHandler = new FrameHandler(this.mydis, this);
      localFrameHandler.start();
      
      NetworkSender localNetworkSender = new NetworkSender(this, this.siteid);
      localNetworkSender.start();
      
      NetworkReceiver localNetworkReceiver = new NetworkReceiver(this, this.siteid);
      localNetworkReceiver.start();
    }
    catch (IOException localIOException)
    {
      System.err.println("ERROR: unable to make connection: " + localIOException + " quitting...");
      System.exit(0);
    }
    catch (Exception localException)
    {
      System.err.println("ERROR in SWE(): " + localException + " quitting ....");
      System.exit(0);
    }
  }
  
  public void wait_for_event(PEvent paramPEvent)
  {
    PEvent localPEvent = this.equeue.get();
    paramPEvent.type = localPEvent.type;
    paramPEvent.seq = localPEvent.seq;
  }
  
  public void to_datalink_layer(Packet paramPacket)
  {
    PEvent localPEvent = new PEvent();
    PFrame localPFrame = new PFrame();
    localPEvent.type = 1;
    localPFrame.info = paramPacket;
    localPEvent.frame = localPFrame;
    this.equeue.put(localPEvent);
  }
  
  public void from_network_layer(Packet paramPacket)
  {
    PFrame localPFrame = this.equeue.get_frame();
    paramPacket.data = localPFrame.info.data;
  }
  
  public void to_physical_layer(PFrame paramPFrame)
  {
    PFrameMsg localPFrameMsg = new PFrameMsg();
    localPFrameMsg.kind = paramPFrame.kind;
    localPFrameMsg.ack = paramPFrame.ack;
    localPFrameMsg.info = paramPFrame.info;
    localPFrameMsg.seq = paramPFrame.seq;
    try
    {
      localPFrameMsg.send(this.mydos);
    }
    catch (Exception localException)
    {
      System.out.println("SWE: Error on sending frame: " + localException);
    }
  }
  
  public PFrame from_physical_layer()
  {
    PFrame localPFrame = this.equeue.get_frame();
    return localPFrame;
  }
  
  public void to_network_layer(Packet paramPacket)
  {
    this.pqueue.put(paramPacket);
  }
  
  public void from_datalink_layer(Packet paramPacket)
  {
    Packet localPacket = this.pqueue.get();
    paramPacket.data = localPacket.data;
  }
  
  public synchronized void get_credit()
  {
    while (this.credit <= 0) {
      try
      {
        wait();
      }
      catch (InterruptedException localInterruptedException) {}
    }
    this.credit -= 1;
  }
  
  public synchronized void grant_credit(int paramInt)
  {
    this.credit = (paramInt + this.credit);
    notify();
  }
  
  public void generate_timeout_event(int paramInt)
  {
    PEvent localPEvent = new PEvent();
    localPEvent.type = 2;
    localPEvent.seq = paramInt;
    this.equeue.put(localPEvent);
  }
  
  public void removeTimer(int paramInt)
  {
    this.equeue.removeTimer(paramInt);
  }
  
  public void generate_acktimeout_event()
  {
    PEvent localPEvent = new PEvent();
    localPEvent.type = 3;
    this.equeue.put(localPEvent);
  }
  
  public void removeAckTimer()
  {
    this.equeue.removeAckTimer();
  }
  
  public void generate_frame_arrival_event(PFrame paramPFrame)
  {
    PEvent localPEvent = new PEvent();
    if (paramPFrame.kind == 3) {
      localPEvent.type = 4;
    } else {
      localPEvent.type = 0;
    }
    localPEvent.frame = paramPFrame;
    this.equeue.put(localPEvent);
  }
}
