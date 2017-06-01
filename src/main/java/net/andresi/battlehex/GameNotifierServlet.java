package battlehex;

import java.io.IOException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.*;
import javax.net.ssl.HttpsURLConnection;

public class GameNotifierServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Hello, World!");
	}

	private void notifyPlayers() throws Exception {

		String url = "https://fcm.googleapis.com/fcm/send";

		URL urlObj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection)urlObj.openConnection();
	}
}

