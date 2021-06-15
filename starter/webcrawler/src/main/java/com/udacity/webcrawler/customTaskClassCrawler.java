package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;
import org.checkerframework.checker.units.qual.A;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public class customTaskClassCrawler extends RecursiveTask <Boolean> {
//need recursiveTask instead of action to return boolean, not sure how to make recursiveAction return boolean
	private final Clock clock;
	private final PageParserFactory pageParserFactory;
	private final Duration timeout;
	private final int popularWordCount;
	private final int maxDepth;
	private final List < Pattern > ignoredUrls;
	private final String url;
	private final Map < String , Integer > counts;
	private final Instant finish;
	private final Set < String > visited;

	public customTaskClassCrawler (
			Clock clock, PageParserFactory pageParserFactory,
			Duration timeout, int popularWordCount,
			int maxDepth, List < Pattern > ignoredUrls,
			String url, Map < String, Integer > counts, Instant finish,
			Set < String > visited ) {
		this.clock = clock;
		this.pageParserFactory = pageParserFactory;
		this.timeout = timeout;
		this.popularWordCount = popularWordCount;
		this.maxDepth = maxDepth;
		this.ignoredUrls = ignoredUrls;
		this.url = url;
		this.counts = counts;
		this.finish = finish;
		this.visited = visited;
		////bit messy, constructors dont match up to parallelwebcrawler, maybe clean it up later, needlessly confusing
	}

	@Override
	protected Boolean compute ( ) {
		if ( ( maxDepth == 0 ) || clock.instant ( ).isAfter ( finish ) ) {
			return false;
		}
		for ( Pattern pattern : ignoredUrls ) {
			if ( pattern.matcher( url ).matches ( )) {
				return false;
			}
		}
		if ( visited.contains ( url ) ) {
			return false;
		}
		visited.add ( url );
		PageParser.Result result = pageParserFactory.get ( url ).parse ( );
		for ( ConcurrentHashMap.Entry < String, Integer > tempEntry : result.getWordCounts ( ).entrySet ( ) ) {
			counts.compute ( tempEntry.getKey ( ), ( k, v ) -> ( v == null ) ? tempEntry.getValue ( ) : tempEntry.getValue ( ) + v );
		}
		List < customTaskClassCrawler > subTasks = new ArrayList <> (  );
		for ( String link : result.getLinks ( ) ) {
			subTasks.add ( new customTaskClassCrawler (
					clock, pageParserFactory,
					timeout, popularWordCount,
					maxDepth - 1, ignoredUrls,
					link, counts,
					finish, visited
			));
			}
		invokeAll ( subTasks );
		return true;
		}
	}