package edu.brown.cs.student.main.Server.BroadbandRouteUtility;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import edu.brown.cs.student.main.Server.Exceptions.InvalidArgsException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

/**
 * The CachedACSAPIBroadbandSource class implements caching for ACS API broadband data retrieval.
 */
public class CachedACSAPIBroadbandSource implements ACSAPISource {

  // The wrapped data source for ACS API broadband data
  private final ACSAPISource wrappedSource;

  // Loading cache to store cached broadband data
  private final LoadingCache<List<String>, Map<String, String>> cache;

  /**
   * Constructs a CachedACSAPIBroadbandSource instance with a wrapped data source and caching
   * configuration.
   *
   * @param toWrap The data source to wrap and cache.
   */
  public CachedACSAPIBroadbandSource(ACSAPISource toWrap, int maxEntries, int minutesToExpire) {
    this.wrappedSource = toWrap;

    // Configure the cache using Google Guava's CacheBuilder
    this.cache =
        CacheBuilder.newBuilder()
            // How many entries maximum in the cache?
            .maximumSize(maxEntries)
            // How long should entries remain in the cache?
            .expireAfterWrite(minutesToExpire, TimeUnit.MINUTES)
            // Keep statistical info around for profiling purposes
            .recordStats()
            .build(
                // CacheLoader defines how to load data into the cache when needed
                new CacheLoader<>() {
                  @NotNull
                  @Override
                  public Map<String, String> load(@NotNull List<String> vals)
                      throws DatasourceException, InvalidArgsException {
                    return wrappedSource.getBroadbandUsage(vals.get(0), vals.get(1));
                  }
                });
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, String> getBroadbandUsage(String state, String county)
      throws DatasourceException, InvalidArgsException {
    try {
      return cache.get(Arrays.asList(state, county));
    } catch (ExecutionException e) {
      Throwable throwable = e.getCause();
      if (e.getCause() instanceof DatasourceException) throw (DatasourceException) throwable;
      if (e.getCause() instanceof InvalidArgsException) throw (InvalidArgsException) throwable;
    }
    return null;
  }
}
