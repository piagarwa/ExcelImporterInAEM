package com.sling.tv.core.utils;

import java.util.Arrays;
import java.util.List;

import com.sling.tv.core.beans.GameResultBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GameResultsUtil {

	private int count;
	private List<GameResultBean> featuredGames;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<GameResultBean> getFeaturedGames() {
		return featuredGames;
	}

	public void setFeaturedGames(List<GameResultBean> featuredGames) {
		this.featuredGames = featuredGames;
	}

	public String getGameJSONResults(List<GameResultBean> results) {
		GameResultsUtil gameResults = new GameResultsUtil();
		gameResults.setFeaturedGames(results);
		if (results != null && !results.isEmpty()) {

			gameResults.setCount(results.size());

		} else {

			gameResults.setCount(0);

		}
		return getJSONObject(gameResults);

	}

	private String getJSONObject(GameResultsUtil gamesResultUtil) {
		Gson gson = new GsonBuilder().create();
		return gson.toJson(gamesResultUtil);
		 
	}
	public static boolean arrayContainsElements(String[] arr, String targetValue) {	
		return Arrays.asList(arr).contains(targetValue);
	}
}
