package battlehex;

import java.io.IOException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.*;
import javax.net.ssl.HttpsURLConnection;

public class GameNotifierServlet extends HttpServlet {

	static final String AUTH_KEY = "AAAAWaB3uw0:APA91bFq04mXdoMaiLN0hNEF6KZvsdpW8UKKVGrKaVNST7sqFGDBXB4bdeQFFlwqujrROsGWGGyxmBZJ0saDh0Ou5JPdeeRntgS9pbhShN8W12d7P95Txp6aO8itTOW8y5USreaQMcVH";

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Hello, World!");
	}

	private void notifyPlayers() throws Exception {

		String url = "https://fcm.googleapis.com/fcm/send";

		URL urlObj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection)urlObj.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Authorization", "key=" + AUTH_KEY);
	}
}

