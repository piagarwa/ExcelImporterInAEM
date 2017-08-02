package com.sling.tv.core.services;

import java.util.List;
import com.sling.tv.core.beans.GameResultBean;

public interface GameFinderService {

	List<GameResultBean> getGames(String offset, String limit);

	List<GameResultBean> getFilteredGames(String filteredString,String offset, String limit);

	int getTotalGameCount();

}
