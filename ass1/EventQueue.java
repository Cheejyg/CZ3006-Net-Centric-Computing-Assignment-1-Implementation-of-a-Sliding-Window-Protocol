import java.io.PrintStream;
import java.util.Vector;

public class EventQueue
{
  private Vector queue = new Vector(8);
  private PFrame fm = null;
  
  public synchronized void put(PEvent paramPEvent)
  {
    this.queue.addElement(paramPEvent);
    notify();
  }
  
  public synchronized PEvent get()
  {
    while (this.queue.isEmpty()) {
      try
      {
        wait();
      }
      catch (InterruptedException localInterruptedException) {}
    }
    PEvent localPEvent = (PEvent)this.queue.firstElement();
    this.queue.removeElementAt(0);
    this.fm = localPEvent.frame;
    return localPEvent;
  }
  
  public PFrame get_frame()
  {
    if (this.fm == null) {
      System.out.println("EventQue: wait_for_event()must be called before from_physical_layer()");
    }
    System.out.flush();
    return this.fm;
  }
  
  public synchronized void removeTimer(int paramInt)
  {
    int i = this.queue.size();
    for (int j = 0; j < i; j++)
    {
      PEvent localPEvent = (PEvent)this.queue.elementAt(j);
      System.out.println("EventQue: event: type = " + PEvent.KIND[localPEvent.type] + "seq = " + localPEvent.seq);
      
      System.out.flush();
      if ((localPEvent.type == 2) && (localPEvent.seq == paramInt))
      {
        this.queue.removeElementAt(j);
        i--;
        System.out.println("EventQue: removed time out event for seq: " + paramInt);
        System.out.flush();
        break;
      }
    }
  }
  
  public synchronized void removeAckTimer()
  {
    int i = this.queue.size();
    for (int j = 0; j < i; j++)
    {
      PEvent localPEvent = (PEvent)this.queue.elementAt(j);
      if (localPEvent.type == 3)
      {
        this.queue.removeElementAt(j);
        i--;
        System.out.println("EventQue: removed ack time out event");
        System.out.flush();
      }
    }
  }
}
