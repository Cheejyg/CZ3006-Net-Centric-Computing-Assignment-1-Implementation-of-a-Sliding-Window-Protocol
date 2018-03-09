import java.util.Vector;

public class PacketQueue
{
  private Vector queue = new Vector(8);
  
  public synchronized void put(Packet paramPacket)
  {
    this.queue.addElement(paramPacket);
    notify();
  }
  
  public synchronized Packet get()
  {
    while (this.queue.isEmpty()) {
      try
      {
        wait();
      }
      catch (InterruptedException localInterruptedException) {}
    }
    Packet localPacket = (Packet)this.queue.firstElement();
    this.queue.removeElementAt(0);
    return localPacket;
  }
}
