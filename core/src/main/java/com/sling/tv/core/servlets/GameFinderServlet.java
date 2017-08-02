package com.sling.tv.core.servlets;

import java.io.IOException;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sling.tv.core.services.GameFinderService;
import com.sling.tv.core.utils.GameResultsUtil;

/**
 * 
 * Game Finder Servlet: This servlet takes the filtered text String 
 * and the offset from the request and gets the results in json format
 *
 */
@SlingServlet(description = "JSON response to fetch games for the given team, city, state and zipcode", methods = { "GET" }, paths = "/bin/sling/gamefinder", extensions = { "json" }, metatype = true)
public class GameFinderServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GameFinderServlet.class);

	@Reference
	private transient GameFinderService gameFinderService;

	@Override
	protected void doGet(final SlingHttpServletRequest request,
			final SlingHttpServletResponse response) throws IOException {

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		StringBuilder jsonResponse = new StringBuilder();
		String[] selectors = request.getRequestPathInfo().getSelectors();

		if (selectors.length >= 3) {
			String offset = selectors[0];
			String limit = selectors[1];
			String filteredText = selectors[2];

			GameResultsUtil gameResultsUtil = new GameResultsUtil();
			String gameResultResponse;
			
			if(filteredText!=null && !filteredText.equalsIgnoreCase("")) {
			    gameResultResponse = gameResultsUtil.getGameJSONResults(gameFinderService.getFilteredGames(filteredText, offset, limit));
			} else{
				gameResultResponse = gameResultsUtil.getGameJSONResults(gameFinderService.getGames(offset, limit));
			}
			jsonResponse.append(gameResultResponse);
		} else {
			LOGGER.error("Invalid Request");
		}

		response.getWriter().write(jsonResponse.toString());

	}
}
