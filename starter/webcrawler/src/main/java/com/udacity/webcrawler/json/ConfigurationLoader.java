package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * A static utility class that loads a JSON configuration file.
 */
public final class ConfigurationLoader {

	private final Path path;

	/**
	 * Create a {@link ConfigurationLoader} that loads configuration from the given {@link Path}.
	 */
	public ConfigurationLoader ( Path path ) {
		this.path = Objects.requireNonNull ( path );
	}

	/**
	 * Loads configuration from this {@link ConfigurationLoader}'s path
	 *
	 * @return the loaded {@link CrawlerConfiguration}.
	 */
	@JsonDeserialize ( builder = CrawlerConfiguration.Builder.class )
	public CrawlerConfiguration load ( ) {
      System.out.println ( "Method load ( ) start..." );
      try {
        Reader reader = Files.newBufferedReader ( path, StandardCharsets.UTF_8 );
        this.read ( reader );
        reader.close ( );
      } catch ( IOException e ) {
        e.printStackTrace ( );
      }

      return new CrawlerConfiguration.Builder ( ).build ( );
	}

	/**
	 * Loads crawler configuration from the given reader.
	 *
	 * @param reader a Reader pointing to a JSON string that contains crawler configuration.
	 * @return a crawler configuration
	 */

	public static CrawlerConfiguration read ( Reader reader ) throws IOException {
		// This is here to get rid of the unused variable warning.
		Objects.requireNonNull ( reader );
		ObjectMapper objMapper = new ObjectMapper (  );
		objMapper.disable ( JsonParser.Feature.AUTO_CLOSE_SOURCE );

		return objMapper.readValue ( reader, CrawlerConfiguration.Builder.class ).build ( );
	}
}
