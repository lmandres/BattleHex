package battlehex;

import java.io.IOException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.net.ssl.HttpsURLConnection;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.cloud.datastore.Key;

public class GameNotifierServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doPost(req, resp);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		Key gameKey = null;

		HttpSession session = req.getSession();

		if (user == null) {
			resp.sendRedirect("http://battle-hex.appspot.com/index.jsp");
		}

		if (session.getMaxInactiveInterval() != 600) {
			session.setMaxInactiveInterval(600);
		}

		if (session.getAttribute("gameKey") != null) {

			BoardHelper boardHelper = new BoardHelper();

			gameKey = Key.fromUrlSafe(session.getAttribute("gameKey").toString());
			boardHelper.setGameKey(gameKey);

			if (req.getParameter("fbtoken") != null) {
				boardHelper.addGameListener((String)req.getParameter("fbtoken"));
			}

			boardHelper.notifyPlayers();
		}

		resp.setContentType("application/json");
		resp.getWriter().println("{");
		resp.getWriter().println("\"gameKey\" : \"" + gameKey.toUrlSafe() + "\",");
		resp.getWriter().println("\"player1State\" : \"\",");
		resp.getWriter().println("\"player2State\" : \"\"");
		resp.getWriter().println("}");

		resp.getWriter().close();
	}
}

