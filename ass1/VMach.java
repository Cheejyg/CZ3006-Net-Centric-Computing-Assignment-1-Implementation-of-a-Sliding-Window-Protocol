import java.io.PrintStream;

public class VMach
{
  public static void main(String[] paramArrayOfString)
  {
    String str = "";
    int i = -1;
    try
    {
      str = paramArrayOfString[0];
    }
    catch (Exception localException)
    {
      System.err.println("\nERROR: VMach site-id required!");
      System.err.println("\nUsage: java NetSim [site-id] where site-id = 1 or 2");
      
      System.exit(0);
    }
    i = Integer.parseInt(str);
    if ((i != 1) && (i != 2))
    {
      System.err.println("\nERROR: Invalid site-id: " + str);
      System.err.println("\nUsage: java NetSim [site-id] where site-id = 1 or 2");
      
      System.exit(0);
    }
    SWE localSWE = new SWE(str);
    localSWE.init();
    SWP localSWP = new SWP(localSWE, str);
    localSWP.protocol6();
  }
}
