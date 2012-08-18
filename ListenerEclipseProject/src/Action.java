import java.net.InetAddress;

/**
 * @author Ivano Malavolta - 169201
 *
 */
public interface Action {
	public void execute(InetAddress address) throws FirewallException, ExecuteScriptException;
}
