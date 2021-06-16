package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public class CustomTaskClassCrawler extends RecursiveTask < Boolean > {
//need recursiveTask instead of action to return boolean, not sure how to make recursiveAction return boolean
	private final Clock clock;
	private final PageParserFactory parserFactory;
	private final Duration timeout;
	private final int popularWordCount;
	private final int maxDepth;
	private final List < Pattern > ignoredUrls;
	private final Instant finish;
	private final Set < String > visited;
	private final String url;
	private final Map < String, Integer > counts;

	public CustomTaskClassCrawler (
			Clock clock,
			PageParserFactory parserFactory,
			Duration timeout,
			int popularWordCount,
			int maxDepth,
			List < Pattern > ignoredUrls,
			Instant finish,
			Set < String > visited,
			String url,
			Map < String, Integer > counts ) {
		this.clock = clock;
		this.parserFactory = parserFactory;
		this.timeout = timeout;
		this.popularWordCount = popularWordCount;
		this.maxDepth = maxDepth;
		this.ignoredUrls = ignoredUrls;
		this.finish = finish;
		this.visited = visited;
		this.url = url;
		this.counts = counts;
		////bit messy, constructors dont match up to parallelwebcrawler, maybe clean it up later, needlessly confusing
	}

	private boolean isIgnored ( String url ) {
		return ignoredUrls
				.stream ( )
				.anyMatch ( pattern -> pattern.matcher ( url ).matches ( ) );
	}

	@Override
	protected Boolean compute ( ) {
		if ( isIgnored ( url ) || maxDepth == 0 || clock.instant ( ).isAfter ( finish ) || !visited.add ( url ) ) {
			return false;
		}
//		for ( Pattern pattern : ignoredUrls ) {
//			if ( pattern.matcher( url ).matches ( )) {
//				return false;
//			}
//		}
//		if ( visited.contains ( url ) ) {
//			return false;
//		}
//		visited.add ( url );
//		PageParser.Result result = pageParserFactory.get ( url ).parse ( );
		visited.add ( url );
		PageParser.Result result = parserFactory.get ( url ).parse ( );
		for ( ConcurrentMap.Entry < String, Integer > tempEntry : result.getWordCounts ( ).entrySet ( ) ) {
			counts.compute ( tempEntry.getKey ( ), ( k, v ) -> ( v == null ) ? tempEntry.getValue ( ) : tempEntry.getValue ( ) + v );
		}
		List < CustomTaskClassCrawler > subTasks = new ArrayList <> ( );
		for ( String link : result.getLinks ( ) ) {
			subTasks.add ( new CustomTaskClassCrawler (
					clock, parserFactory,
					timeout, popularWordCount,
					maxDepth - 1, ignoredUrls,
					finish, visited,
					link, counts ) );
		}
		invokeAll ( subTasks );
		return true;
	}
}