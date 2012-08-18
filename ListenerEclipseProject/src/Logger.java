import java.util.Date;

/**
 * 
 */

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class Logger {

		private Launcher launcher;
		private String log = "";
		
		public Logger(Launcher launcher) {
			this.launcher = launcher;
		}
		
		public void addMessage(String message) {
			if(this.launcher.getListener().isBackgroundMode()) {
				log += message;
				System.out.print(message);
				Utility.writeFile(this.log, "logging/logging.txt");
			} else {
				this.launcher.getLogging().append(message);
				this.launcher.getLogging().setCaretPosition(this.launcher.getLogging().getText().lastIndexOf("---"));
			}
		}
		
		public void registerFile() {
			Utility.writeFile(this.launcher.getLogging().getText(), "logging/logging.txt");
		}

		public void notifyPacketNotEncrypted(Packet p, Date time) {
			p.setForcedPayload("not valid");
			String result = "";
			result += "\nINTRUSION DETECTED --- " + time.toString() + " --- received packet with bad-encrypted payload from IP address " + p.getAddress().getHostAddress() + "\n\t" +  p;
			this.addMessage(result);
		}

		public void notifyPacketNotInserted(Packet p, Date time) {
			String result = "";
			result += "\nWARNING --- " + time.toString() + " --- received packet with well-formed payload, but does not belonging to any knock sequence.\n\t" + p;
			this.addMessage(result);
		}

		public void notifyPacketInserted(Packet p, Date time, int nextIndex, int id) {
			String result = "";
			if(nextIndex == 1) {
				result += "\nSTART --- receiving knock sequence with id " + id;
			}
			result += "\n\t" + time.toString() + " --- received well-formed packet; it is the "; 
			if(nextIndex == 1) {
				result += "1st ";
			}
			if(nextIndex == 2) {
				result += "2nd ";
			}
			if(nextIndex == 3) {
				result += "3rd ";
			}
			if(nextIndex > 3) {
				result += " " + nextIndex + "th ";
			}
			result += "packet of knock sequence with id " + id;
			this.addMessage(result);
		}
		
		public void notifyReplayAttack(Packet p, Date time) {
			String result = "";
			result += "\nREPLAY-ATTACK DETECTED --- " + time.toString() + " --- replay attack detected from IP address " + p.getAddress().getHostAddress() + "\n\t" +  p;
			this.addMessage(result);
		}
}
