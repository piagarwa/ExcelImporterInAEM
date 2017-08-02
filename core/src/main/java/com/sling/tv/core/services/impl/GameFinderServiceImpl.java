package com.sling.tv.core.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sling.tv.core.services.GameFinderService;
import com.sling.tv.core.beans.GameResultBean;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;

@Component(metatype = false)
@Service(value = GameFinderService.class)
public class GameFinderServiceImpl implements GameFinderService {

	@Reference
	QueryBuilder queryBuilder;

	@Reference
	SlingRepository slingRepository;

	@Reference
	ResourceResolverFactory resolverFactory;

	private ResourceResolver resolver = null;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GameFinderServiceImpl.class);

	private int totalGameCount = 0;

	@Activate
	public void activate() throws LoginException {
		getResourceResolver();
	}


	private GameResultBean getGameObjectFromHit(Hit hit) {
		Resource res;
		GameResultBean resultBean = null;
		try {
			resultBean = new GameResultBean();
			res = resolver.getResource(hit.getPath());
			if (res != null) {
				ValueMap gameResultMap = res.adaptTo(ValueMap.class);
				if (gameResultMap != null) {
					resultBean.setTeam1(gameResultMap.get("team1", StringUtils.EMPTY));
					resultBean.setTeam2(gameResultMap.get("team2", StringUtils.EMPTY));
					resultBean.setBlackout((int)(gameResultMap.get("blackout",0)));
					resultBean.setCallsign(gameResultMap.get("callsign", StringUtils.EMPTY));
					resultBean.setChannelid(gameResultMap.get("channelid", StringUtils.EMPTY));
					resultBean.setDisplaydata(gameResultMap.get("display-data", StringUtils.EMPTY));
					resultBean.setLocation(gameResultMap.get("location", StringUtils.EMPTY));
					resultBean.setPackageName(gameResultMap.get("package", StringUtils.EMPTY));
				}
			}
		} catch (RepositoryException e) {
			LOGGER.error(" Error in retreivig the Result Bean object: " + e);
		}

		return resultBean;
	}


	private Map<String, String> createFilteredPredicateMap(String filteredText, String offset, String limit) {
		Map<String, String> predicateMap = new HashMap<>();

		predicateMap.put("type", "nt:unstructured");
		predicateMap.put("path", "/content/sling-tv/game-data");
		predicateMap.put("fulltext", filteredText);
		predicateMap.put("orderby", "@jcr:created");
		predicateMap.put("orderby.sort", "desc");
		predicateMap.put("p.offset", offset);
		predicateMap.put("p.limit", limit);

		return predicateMap;

	}

	private Map<String, String> createPredicateMap(String offset, String limit) {
		Map<String, String> predicateMap = new HashMap<>();

		predicateMap.put("type", "nt:unstructured");
		predicateMap.put("path", "/content/sling-tv/game-data");
		predicateMap.put("orderby", "@jcr:created");
		predicateMap.put("orderby.sort", "desc");
		predicateMap.put("p.offset", offset);
		predicateMap.put("p.limit", limit);

		return predicateMap;

	}

	private void getResourceResolver() {
		Map<String, Object> serviceParams = new HashMap<>();
		serviceParams.put(ResourceResolverFactory.SUBSERVICE,"game-finder-user-service");

		try {
			resolver = resolverFactory.getServiceResourceResolver(serviceParams);
		} catch (LoginException e) {
			LOGGER.error("Error in getting resolver " + e);
		}
	}


	@Override
	public List<GameResultBean> getGames(String offset, String limit) {
		Map<String, String> predicateMap = createPredicateMap(offset, limit);
		return getSearchResults(predicateMap);
	}

	@Override
	public List<GameResultBean> getFilteredGames(String filteredString, String offset, String limit) {
		Map<String, String> predicateMap = createFilteredPredicateMap(filteredString, offset, limit);
		return getSearchResults(predicateMap);
	}

	@Override
	public int getTotalGameCount() {
		
		return totalGameCount;
	}
	
	private List<GameResultBean> getSearchResults(
			Map<String, String> predicateMap) {
		List<GameResultBean> results = null;

		this.totalGameCount = 0;

		Session session = resolver.adaptTo(Session.class);

		Query queryObj = this.queryBuilder.createQuery(
				PredicateGroup.create(predicateMap), session);
		LOGGER.info("Search Query" + queryObj.getPredicates().toString());

		SearchResult searchResults = queryObj.getResult();

		if (searchResults != null) {
			LOGGER.info("Total number of search matches are: "
					+ searchResults.getTotalMatches());
			this.totalGameCount = (int) searchResults.getTotalMatches();
			final List<Hit> hitsList = searchResults.getHits();
			if (hitsList != null && !hitsList.isEmpty()) {
				results = new ArrayList<>();
				// populate the search result hit properties
				for (Hit hit : searchResults.getHits()) {

					GameResultBean resultBean = getGameObjectFromHit(hit);
					LOGGER.info("Inside Hit :" + resultBean.getTeam1());
					results.add(resultBean);

				}
			}

		}
		return results;
	}

}
