package de.schauderhaft.architecture.example.thomas.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.schauderhaft.architecture.example.CrosswordGame;
import de.schauderhaft.architecture.example.library.Library;

public class TCPServer {

	private final Set<String> words;
	// private ConcurrentMap<String, CrosswordGame> hostname2gameMultiMap;
	private Map<String, CrosswordGame> hostname2game;

	public TCPServer(final Set<String> words) {
		this.words = words;
		initMultiMap();

		DatagramSocket s;
	}

	private void initMultiMap() {
		// Config config = new Config();
		// HazelcastInstance h = Hazelcast.newHazelcastInstance(config);
		// this.hostname2gameMultiMap =
		// h.getMap("hostname-to-crosswordgame-map");
		this.hostname2game = new HashMap<String, CrosswordGame>();
	}

	public void startServer(int port) throws IOException {
		String clientSentence;
		ServerSocket welcomeSocket = new ServerSocket(port);
		System.out.println("Server startet...");
		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			InetAddress ipClient = connectionSocket.getInetAddress();
			BufferedReader inFromClient = new BufferedReader(
					new InputStreamReader(connectionSocket.getInputStream()));
			clientSentence = inFromClient.readLine();
			System.out.println("Received: " + clientSentence);

			String hostname = ipClient.getHostAddress();
			CrosswordGame game = getOrCreateCrossworGame(hostname);

			String word = clientSentence;
			int answer = game.submit(word);

			responseAnswer(connectionSocket, answer);

			inFromClient.close();
			connectionSocket.close();

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Architecture-Sleep :-(
			}
		}
	}

	private CrosswordGame getOrCreateCrossworGame(String hostname) {
		// fehlende Validierung des Hostnamens
		if (!hostname2game.containsKey(hostname)) {
			hostname2game.put(hostname, new CrosswordGame(words));
		}
		return hostname2game.get(hostname);

	}

	private void responseAnswer(Socket connectionSocket, int answer)
			throws IOException {
		DataOutputStream outToClient = new DataOutputStream(
				connectionSocket.getOutputStream());
		outToClient.writeBytes(String.valueOf(answer));
		outToClient.close();
	}

	public static void main(String argv[]) throws Exception {
		HashSet<String> knownWords = new HashSet<String>();
		knownWords.addAll(Library.getEnglishDictionary());
		System.out.println("Words total: " + knownWords.size());
		int port = 10000;
		if (argv.length > 0) {
			port = Integer.valueOf(argv[0]);
		}
		TCPServer server = new TCPServer(knownWords);
		server.startServer(port);
	}
}
