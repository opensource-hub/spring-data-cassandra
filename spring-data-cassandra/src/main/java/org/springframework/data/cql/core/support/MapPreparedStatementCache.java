/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.cql.core.support;

import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.util.Assert;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Session;

/**
 * {@link PreparedStatementCache} backed by a {@link Map} cache. Defaults to simple {@link ConcurrentHashMap} caching.
 *
 * @author Mark Paluch
 */
public class MapPreparedStatementCache implements PreparedStatementCache {

	private final Map<CacheKey, PreparedStatement> cache;

	/**
	 * Create a new {@link MapPreparedStatementCache}.
	 *
	 * @param cache must not be {@literal null}.
	 */
	protected MapPreparedStatementCache(Map<CacheKey, PreparedStatement> cache) {

		Assert.notNull(cache, "Cache must not be null");

		this.cache = cache;
	}

	/**
	 * Create a {@link MapPreparedStatementCache} using {@link ConcurrentHashMap}.
	 *
	 * @return the new {@link MapPreparedStatementCache} backed by {@link ConcurrentHashMap}.
	 */
	public static MapPreparedStatementCache create() {
		return of(new ConcurrentHashMap<>());
	}

	/**
	 * Create a {@link MapPreparedStatementCache} using the given {@link Map}.
	 *
	 * @return the new {@link MapPreparedStatementCache} backed the given {@link Map}.
	 */
	public static MapPreparedStatementCache of(Map<CacheKey, PreparedStatement> cache) {
		return new MapPreparedStatementCache(cache);
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.cql.core.support.PrepatedStatementCache#getPreparedStatement(com.datastax.driver.core.RegularStatement, com.datastax.driver.core.Session, java.util.function.Supplier)
	 */
	@Override
	public PreparedStatement getPreparedStatement(Session session, RegularStatement statement,
			Supplier<PreparedStatement> preparer) {

		CacheKey cacheKey = new CacheKey(session, statement);

		return cache.computeIfAbsent(cacheKey, key -> session.prepare(statement));
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.cql.core.support.PrepatedStatementCache#getPreparedStatement(java.lang.String, com.datastax.driver.core.Session, java.util.function.Supplier)
	 */
	@Override
	public PreparedStatement getPreparedStatement(Session session, String statement,
			Supplier<PreparedStatement> preparer) {

		CacheKey cacheKey = new CacheKey(session, statement);

		return cache.computeIfAbsent(cacheKey, key -> session.prepare(statement));
	}

	/**
	 * Cache key for {@link PreparedStatement} caching.
	 */
	@EqualsAndHashCode
	public static class CacheKey {

		final Cluster cluster;
		final String keyspace;
		final String cql;
		final int statementHash;

		CacheKey(Session session, String cql) {
			this.cluster = session.getCluster();
			this.keyspace = session.getLoggedKeyspace();
			this.cql = cql;
			this.statementHash = cql.hashCode();
		}

		CacheKey(Session session, RegularStatement statement) {
			this.cluster = session.getCluster();
			this.keyspace = session.getLoggedKeyspace();
			this.cql = statement.toString();
			this.statementHash = statement.hashCode();
		}
	}
}
