public class PEvent
{
  public static final int FRAME_ARRIVAL = 0;
  public static final int NETWORK_LAYER_READY = 1;
  public static final int TIMEOUT = 2;
  public static final int ACK_TIMEOUT = 3;
  public static final int CKSUM_ERR = 4;
  public static final String[] KIND = { "FRAME_ARRIVAL", "NETWORK_LAYER_READY", "TIMEOUT", "ACK_TIMEOUT", "CKSUM_ERR" };
  public int type = 0;
  public int seq = 0;
  public PFrame frame = null;
}
