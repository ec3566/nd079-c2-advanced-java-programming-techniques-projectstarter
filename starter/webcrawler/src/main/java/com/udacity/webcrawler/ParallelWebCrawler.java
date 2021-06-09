package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
	private final Clock clock;
	private final Duration timeout;
	private final int popularWordCount;
	private final ForkJoinPool pool;
	private final PageParserFactory pageParserFactory;
	private final List < Pattern > ignoredUrls;
	private final int maxDepth;

	@Inject
	ParallelWebCrawler (
			Clock clock,
			@Timeout Duration timeout,
			@PopularWordCount int popularWordCount,
			@TargetParallelism int threadCount,
			PageParserFactory pageParserFactory,
			@IgnoredUrls List < Pattern > ignoredUrls,
			@MaxDepth int maxDepth ) {
		this.clock = clock;
		this.timeout = timeout;
		this.popularWordCount = popularWordCount;
		this.pool = new ForkJoinPool ( Math.min ( threadCount, getMaxParallelism ( ) ) );
		this.pageParserFactory = pageParserFactory;
		this.ignoredUrls = ignoredUrls;
		this.maxDepth = maxDepth;
	}


	@Override
	public CrawlResult crawl ( List < String > startingUrls ) {
		Instant finish = clock.instant ( ).plus ( timeout );
		ConcurrentMap < String, Integer > counts = new ConcurrentHashMap < > ( );
		ConcurrentSkipListSet visited = new ConcurrentSkipListSet (  );
		for ( String url : startingUrls ) {
			pool.invoke ( new customTaskClassCrawler ( clock, pageParserFactory,
					timeout, popularWordCount,
					maxDepth, ignoredUrls,
					url, counts,
					finish, visited ) );
		}
		if ( counts.isEmpty ( ) ) {
			return new CrawlResult.Builder ( )
					.setWordCounts ( WordCounts.sort ( counts, popularWordCount ) )
					.setUrlsVisited ( visited.size ( ) )
					.build ( );
		}
		return new CrawlResult.Builder ( )
				.setWordCounts ( WordCounts.sort ( counts, popularWordCount ) )
				.setUrlsVisited ( visited.size ( ) )
				.build ( );
	}

	@Override
	public int getMaxParallelism ( ) {
		return Runtime.getRuntime ( ).availableProcessors ( );
	}
}


