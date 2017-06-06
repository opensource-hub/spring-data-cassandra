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

import org.springframework.data.cql.core.PreparedStatementCreator;
import org.springframework.data.cql.core.QueryOptions;
import org.springframework.data.cql.core.QueryOptionsUtil;
import org.springframework.util.Assert;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.exceptions.DriverException;

/**
 * {@link PreparedStatementCreator} implementation using caching of prepared statements.
 * <p />
 * Regular CQL statements are prepared on first use and executed as prepared statements. Prepared statements are cached
 * by Cassandra itself (invalidation/eviction possible), in the driver to be able to re-prepare a statement and in this
 * {@link CachedPreparedStatementCreator} using {@link PreparedStatementCache}.
 *
 * @author Mark Paluch
 * @since 2.0
 * @see PreparedStatementCache
 */
public class CachedPreparedStatementCreator implements PreparedStatementCreator {

	private final PreparedStatementCache cache;

	private final RegularStatement statement;

	/**
	 * Create a new {@link CachedPreparedStatementCreator}.
	 *
	 * @param cache must not be {@literal null}.
	 * @param statement may be {@literal null} of {@code cql} is provided.
	 * @param cql may be {@literal null} of {@code statement} is provided.
	 */
	protected CachedPreparedStatementCreator(PreparedStatementCache cache, RegularStatement statement) {

		Assert.notNull(cache, "Cache must not be null");

		this.cache = cache;
		this.statement = statement;
	}

	/**
	 * Create a new {@link CachedPreparedStatementCreator} given {@link PreparedStatementCache} and
	 * {@link RegularStatement} to prepare. Subsequent calls require the same {@link RegularStatement} object for a cache
	 * hit. Otherwise, the statement will be re-prepared.
	 *
	 * @param cache must not be {@literal null}.
	 * @param cql must not be {@literal null} or empty.
	 * @return the {@link CachedPreparedStatementCreator} for {@link RegularStatement}.
	 */
	public static CachedPreparedStatementCreator of(PreparedStatementCache cache, RegularStatement statement) {

		Assert.notNull(cache, "Cache must not be null");
		Assert.notNull(statement, "Statement must not be null");

		return new CachedPreparedStatementCreator(cache, statement);
	}

	/**
	 * Create a new {@link CachedPreparedStatementCreator} given {@link PreparedStatementCache} and {@code cql} to
	 * prepare. Subsequent calls require the a CQL statement that {@link String#equals(Object)} the previously used CQL
	 * string for a cache hit. Otherwise, the statement will be re-prepared.
	 *
	 * @param cache must not be {@literal null}.
	 * @param cql must not be {@literal null} or empty.
	 * @return the {@link CachedPreparedStatementCreator} for {@code cql}.
	 */
	public static CachedPreparedStatementCreator of(PreparedStatementCache cache, String cql) {

		Assert.notNull(cache, "Cache must not be null");
		Assert.hasText(cql, "CQL statement must not be null");

		return new CachedPreparedStatementCreator(cache, new SimpleStatement(cql));
	}

	/**
	 * Create a new {@link CachedPreparedStatementCreator} given {@link PreparedStatementCache} and {@code cql} to
	 * prepare. This method applies {@link QueryOptions} to the {@link com.datastax.driver.core.Statement} before
	 * preparing it. Subsequent calls require the a CQL statement that {@link String#equals(Object)} the previously used
	 * CQL string for a cache hit. Otherwise, the statement will be re-prepared.
	 *
	 * @param cache must not be {@literal null}.
	 * @param cql must not be {@literal null} or empty.
	 * @param queryOptions must not be {@literal null}.
	 * @return the {@link CachedPreparedStatementCreator} for {@code cql}.
	 */
	public static CachedPreparedStatementCreator of(PreparedStatementCache cache, String cql, QueryOptions queryOptions) {

		Assert.notNull(cache, "Cache must not be null");
		Assert.hasText(cql, "CQL statement must not be null");
		Assert.notNull(queryOptions, "QueryOptions must not be null");

		SimpleStatement statement = new SimpleStatement(cql);

		QueryOptionsUtil.addQueryOptions(statement, queryOptions);

		return new CachedPreparedStatementCreator(cache, statement);
	}

	/**
	 * @return the underlying {@link PreparedStatementCache}.
	 */
	public PreparedStatementCache getCache() {
		return cache;
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.cql.core.PreparedStatementCreator#createPreparedStatement(com.datastax.driver.core.Session)
	 */
	@Override
	public PreparedStatement createPreparedStatement(Session session) throws DriverException {
		return cache.getPreparedStatement(session, statement, () -> session.prepare(statement));
	}
}
