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

import java.util.function.Supplier;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Session;

/**
 * Cache interface to synchronously prepare CQL statements.
 * <p />
 * Implementing classes of {@link PreparedStatementCache} define synchronization and cache implementation behavior.
 * Depending on the cache implementation, the returned {@link PreparedStatement} may or may not be thread safe. Prepared
 * statements are mutable regarding their options.
 *
 * @author Mark Paluch
 * @since 2.0
 * @see PreparedStatement
 */
public interface PreparedStatementCache {

	/**
	 * Obtain a {@link PreparedStatement} by {@link Session} and {@link RegularStatement}.
	 *
	 * @param session must not be {@literal null}.
	 * @param statement must not be {@literal null}.
	 * @param preparer must not be {@literal null}.
	 * @return the {@link PreparedStatement}.
	 */
	PreparedStatement getPreparedStatement(Session session, RegularStatement statement,
			Supplier<PreparedStatement> preparer);

	/**
	 * Obtain a {@link PreparedStatement} by {@link Session} and {@code statement}.
	 *
	 * @param session must not be {@literal null}.
	 * @param statement must not be {@literal null} or empty.
	 * @param preparer must not be {@literal null}.
	 * @return the {@link PreparedStatement}.
	 */
	PreparedStatement getPreparedStatement(Session session, String statement, Supplier<PreparedStatement> preparer);

	/**
	 * Create a default cache backed by a {@link java.util.concurrent.ConcurrentHashMap}.
	 *
	 * @return a new {@link MapPreparedStatementCache}.
	 */
	static PreparedStatementCache create() {
		return MapPreparedStatementCache.create();
	}
}
