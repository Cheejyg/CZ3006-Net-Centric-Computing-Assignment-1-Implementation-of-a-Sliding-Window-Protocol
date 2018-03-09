/*==================================================================*
 *  File: SWP.java (Updated 8/3/2018)								*
 *																	*
 *  This class implements the sliding window protocol				*
 *  Used by VMach class												*
 *  Uses the following classes: SWE, Packet, PFrame, PEvent,		*
 *																	*
 *  Author: Chee Jun Yuan Glenn, Kaydon Chong						*
 *			School of Computer Science and Engineering				*
 *			Nanyang Technological University						*
 *			Singapore 639798										*
 *==================================================================*/
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

 
/*===============================================================*
 *  File: SWP.java											   *
 *															   *
 *  This class implements the sliding window protocol			*
 *  Used by VMach class							 *
 *  Uses the following classes: SWE, Packet, PFrame, PEvent,	 *
 *															   *
 *  Author: Professor SUN Chengzheng							 *
 *		  School of Computer Engineering					   *
 *		  Nanyang Technological University					 *
 *		  Singapore 639798									 *
 *===============================================================*/
public class SWP {

	/*========================================================================*
	 the following are provided, do not change them!!
	 *========================================================================*/
	//the following are protocol constants.
	public static final int MAX_SEQ = 7;
	public static final int NR_BUFS = (MAX_SEQ + 1) / 2;

	// the following are protocol variables
	private int oldest_frame = 0;
	private PEvent event = new PEvent();
	private Packet out_buf[] = new Packet[NR_BUFS];

	//the following are used for simulation purpose only
	private SWE swe = null;
	private String sid = null;

	//Constructor
	public SWP(SWE sw, String s) {
		swe = sw;
		sid = s;
	}

	//the following methods are all protocol related
	private void init() {
		for (int i = 0; i < NR_BUFS; i++) {
			out_buf[i] = new Packet();
		}
	}

	private void wait_for_event(PEvent e) {
		swe.wait_for_event(e); //may be blocked
		oldest_frame = e.seq; //set timeout frame seq
	}

	private void enable_network_layer(int nr_of_bufs) {
		//network layer is permitted to send if credit is available
		swe.grant_credit(nr_of_bufs);
	}

	private void from_network_layer(Packet p) {
		swe.from_network_layer(p);
	}

	private void to_network_layer(Packet packet) {
		swe.to_network_layer(packet);
	}

	private void to_physical_layer(PFrame fm) {
		System.out.println("SWP: Sending frame: seq = " + fm.seq +
			" ack = " + fm.ack + " kind = " +
			PFrame.KIND[fm.kind] + " info = " + fm.info.data);
		System.out.flush();
		swe.to_physical_layer(fm);
	}

	private void from_physical_layer(PFrame fm) {
		PFrame fm1 = swe.from_physical_layer();
		fm.kind = fm1.kind;
		fm.seq = fm1.seq;
		fm.ack = fm1.ack;
		fm.info = fm1.info;
	}


	/*===========================================================================*
	 	implement your Protocol Variables and Methods below: 
	 *==========================================================================*/
	private boolean no_nak = true; //no nak has been sent yet
	
	private Timer[] timers = new Timer[NR_BUFS]; //timer
	private Timer ackTimer = new Timer(); //acknoledgement timer
	
	static boolean between(int a, int b, int c) {
		//returns true if a <= b < c circularly; false otherwise.
		
		//Same as between in protocol5, but shorter and more obscure.
		return ((a <= b) && (b < c)) || ((c < a) && (a <= b)) || ((b < c) && (c < a));
	}
	
	void send_frame(int fk, int frame_nr, int frame_expected, Packet buffer[]) {
		//Construct and send a data, ack, or nak frame.
		PFrame s = new PFrame(); //scratch variable
		s.kind = fk; //kind == data, ack, or nak
		if(fk == PFrame.DATA) {
			 s.info = buffer[frame_nr % NR_BUFS];
		}
		s.seq = frame_nr; //only meaningful for data frames
		s.ack = (frame_expected + MAX_SEQ) % (MAX_SEQ + 1);
		if (fk == PFrame.NAK) {
			no_nak = false; //one nak per frame, please
		}
		to_physical_layer(s); // transmit the frame
		if (fk == PFrame.DATA) {
			start_timer(frame_nr);
		}
		stop_ack_timer(); //no need for separate ack frame
	}

	int inc(int seq) {
		//circular increment
		return (seq + 1) % (MAX_SEQ + 1);
	}

	public void protocol6() {
		int ack_expected; //lower edge of senderâ€™s window
		int next_frame_to_send; //upper edge of senderâ€™s window + 1
		int frame_expected; //lower edge of receiverâ€™s window
		int too_far; //upper edge of receiverâ€™s window + 1
		int i; //index into buffer pool
		PFrame r = new PFrame(); //scratch variable
		init(); //buffers for the outbound stream
		Packet in_buf[] = new Packet[NR_BUFS]; //buffers for the inbound stream 
		boolean arrived[] = new boolean[NR_BUFS]; //inbound bit map
		int nbuffered; //how many output buffers currently used
		//PEvent event; //this.event

		enable_network_layer(NR_BUFS); //initialize
		ack_expected = 0; //next ack expected on the inbound stream
		next_frame_to_send = 0; //number of next outgoing frame
		frame_expected = 0;
		too_far = NR_BUFS;
		nbuffered = 0; //initially no packets are buffered
		for (i = 0; i < NR_BUFS; i++) {
			arrived[i] = false;
		}

		while (true) {
			wait_for_event(event); //five possibilities: NETWORK_LAYER_READY, FRAME_ARRIVAL, CHECKSUM ERROR, TIMEOUT, ACK_TIMEOUT
			switch (event.type) { 
				case (PEvent.NETWORK_LAYER_READY): //accept, save, and transmit a new frame
					nbuffered++; //expand the window
					from_network_layer(out_buf[next_frame_to_send % NR_BUFS]); //fetch new packet
					send_frame(PFrame.DATA, next_frame_to_send, frame_expected, out_buf); //transmit the frame
					next_frame_to_send = inc(next_frame_to_send); //advance upper window edge
					break;
				case (PEvent.FRAME_ARRIVAL): //a data or control frame has arrived
					from_physical_layer(r); //fetch incoming frame from physical layer
					if (r.kind == PFrame.DATA) {
						//An undamaged frame has arrived.
						if ((r.seq != frame_expected) && no_nak) {
							send_frame(PFrame.NAK, 0, frame_expected, out_buf);
						} else {
							start_ack_timer();
						}
						if (between(frame_expected, r.seq, too_far) && (arrived[r.seq % NR_BUFS] == false)) {
							//Frames may be accepted in any order.
							arrived[r.seq % NR_BUFS] = true; //mark buffer as full
							in_buf[r.seq % NR_BUFS] = r.info; //insert data into buffer
							while (arrived[frame_expected % NR_BUFS]) {
								//Pass frames and advance window.
								to_network_layer(in_buf[frame_expected % NR_BUFS]);
								no_nak = true;
								arrived[frame_expected % NR_BUFS] = false;
								frame_expected = inc(frame_expected); //advance lower edge of receiver's window
								too_far = inc(too_far); //advance upper edge of receiver's window
								start_ack_timer(); //to see if a separate ack is needed
							}
						}
					}

					if ((r.kind == PFrame.NAK) && between(ack_expected, (r.ack + 1) % (MAX_SEQ + 1), next_frame_to_send)) {
						send_frame(PFrame.DATA, (r.ack + 1) % (MAX_SEQ + 1), frame_expected, out_buf);
					}

					while (between(ack_expected, r.ack, next_frame_to_send)) {
						nbuffered--; //handle piggybacked ack
						stop_timer(ack_expected); //frame arrived intact
						ack_expected = inc(ack_expected); //advance lower edge of sender's window
						enable_network_layer(1);
					}
					break;
				case (PEvent.CKSUM_ERR):
					if (no_nak)
						send_frame(PFrame.NAK, 0, frame_expected, out_buf); //damaged frame
							break;
				case (PEvent.TIMEOUT):
					send_frame(PFrame.DATA, oldest_frame, frame_expected, out_buf); //timer expired; retransmit data
					break;
				case (PEvent.ACK_TIMEOUT):
					send_frame(PFrame.ACK, 0, frame_expected, out_buf); //ack timer expired; send ack
					break;
				default:
					System.out.println("SWP: undefined event type = " +
						event.type);
					System.out.flush();
			}
			
			/*if(nbuffered < NR_BUFS) {
				enable_network_layer(NR_BUFS);
			}
			else {
				disable_network_layer(1);
				enable_network_layer(0);
			}*/
		}
	}

	/* Note: when start_timer() and stop_timer() are called, 
	   the "seq" parameter must be the sequence number, rather 
	   than the index of the timer array, 
	   of the frame associated with this timer, 
	  */
	
	private void start_timer(int seqnr) {
		stop_timer(seqnr);
		timers[seqnr % NR_BUFS] = new Timer();
		timers[seqnr % NR_BUFS].schedule(new TimerTask() {
			@Override
			public void run() {
				swe.generate_timeout_event(seqnr);
			}
		}, 500);
	}

	private void stop_timer(int seqnr) {
		if(timers[seqnr % NR_BUFS] != null) {
			timers[seqnr % NR_BUFS].cancel();
			timers[seqnr % NR_BUFS].purge();
			timers[seqnr % NR_BUFS] = null;
		}
	}

	private void start_ack_timer() {
		stop_ack_timer();
		ackTimer = new Timer();
		ackTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				swe.generate_acktimeout_event();
			}
		}, 150);
	}

	private void stop_ack_timer() {
		if(ackTimer != null) {
			ackTimer.cancel();
			ackTimer.purge();
			ackTimer = null;
		}
	}

} //End of class

/* Note: In class SWE, the following two public methods are available:
   . generate_acktimeout_event() and
   . generate_timeout_event(seqnr).

   To call these two methods (for implementing timers),
   the "swe" object should be referred as follows:
	 swe.generate_acktimeout_event(), or
	 swe.generate_timeout_event(seqnr).
*/