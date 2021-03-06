package com.kanishk.tweetstream.operations;

import java.util.List;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.kanishk.tweetstream.data.DBUpdateManager;
import com.kanishk.tweetstream.data.TweetDataConstants;
import com.kanishk.tweetstream.model.Tweet;

public class SearchActivity extends TweetActivity {

	/** The search text. The text of the current search. */
	private String searchText;

	/**
	 * The is new search result. A variable to check the state of the current
	 * search. Whether it's a new search or not.
	 */
	private boolean isNewSearchResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent launchIntent = getIntent();
		boolean isRecreated = false;
		if (savedInstanceState != null) {
			isRecreated = savedInstanceState
					.getBoolean(TweetDataConstants.RECREATED);
		}
		if (isRecreated) {
			searchText = savedInstanceState
					.getString(TweetDataConstants.SEARCH_TEXT);
			isNewSearchResult = savedInstanceState
					.getBoolean(TweetDataConstants.IS_NEW);
		} else {
			String searchQuery = launchIntent.getExtras().getString(
					SearchManager.QUERY);
			setUpSearch(searchQuery);
			displayFragment.setIsLoading(true);
			taskFragment.setupInitSearch(searchQuery);
		}
	}
	
	protected Uri getDataUri() {
		return TweetDataConstants.CONTENT_SEARCH_URI;
	}
	
	@Override
	protected void checkIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String searchQuery = intent.getStringExtra(SearchManager.QUERY);
			if(taskFragment.isLoading()) {
				displayMessage(getString(R.string.search_loading));
			} else if (!searchQuery.equals(this.searchText)) {
				setUpSearch(searchQuery);
				loadTweets();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(TweetDataConstants.RECREATED, true);
		outState.putString(TweetDataConstants.SEARCH_TEXT, searchText);
		outState.putBoolean(TweetDataConstants.IS_NEW, isNewSearchResult);
	}

	@Override
	public void loadTweets() {
		if (hasNetAccess()) {
			taskFragment.refresh(searchText, isNewSearchResult);
			isNewSearchResult = false;
		} else {
			displayFragment.removeRefresh();
			displayMessage(getString(R.string.net_connect_error));
		}
	}

	@Override
	public void onUpdateTweets(List<Tweet> tweet) {
		displayFragment.removeRefresh();
		if (tweet != null && !tweet.isEmpty()) {
			DBUpdateManager.getInstance().insertSearchResults(tweet,
					getContentResolver(), isNewSearchResult);
		}
		isNewSearchResult = false;
	}

	/**
	 * Sets the up the search from the search text box.
	 * 
	 * @param searchString
	 *            the string on which to filter the stream
	 */
	private void setUpSearch(String searchString) {
		this.searchText = searchString;
		this.isNewSearchResult = true;
		DBUpdateManager.getInstance().clearSearchTable(getContentResolver());
        if(displayFragment.isInitialized()) {
            displayFragment.resetScrollListener();
            displayFragment.displayRefresh();
        }
	}
}
