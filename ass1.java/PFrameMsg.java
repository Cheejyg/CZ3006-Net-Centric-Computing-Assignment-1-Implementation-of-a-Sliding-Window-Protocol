import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PFrameMsg
  extends PFrame
{
  public static final int CKSUM_ERR = 3;
  public static final int CONNECT = 4;
  
  public void send(DataOutputStream paramDataOutputStream)
    throws IOException
  {
    paramDataOutputStream.writeInt(this.kind);
    paramDataOutputStream.writeInt(this.seq);
    paramDataOutputStream.writeInt(this.ack);
    paramDataOutputStream.writeUTF(this.info.data);
  }
  
  public void receive(DataInputStream paramDataInputStream)
    throws IOException
  {
    this.kind = paramDataInputStream.readInt();
    this.seq = paramDataInputStream.readInt();
    this.ack = paramDataInputStream.readInt();
    this.info.data = paramDataInputStream.readUTF();
  }
}
