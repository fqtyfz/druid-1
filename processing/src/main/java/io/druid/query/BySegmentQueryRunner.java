/*
 * Druid - a distributed column store.
 * Copyright 2012 - 2015 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.druid.query;

import com.google.common.collect.Lists;
import com.metamx.common.guava.Sequence;
import com.metamx.common.guava.Sequences;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 */
public class BySegmentQueryRunner<T> implements QueryRunner<T>
{
  private final String segmentIdentifier;
  private final DateTime timestamp;
  private final QueryRunner<T> base;

  public BySegmentQueryRunner(
      String segmentIdentifier,
      DateTime timestamp,
      QueryRunner<T> base
  )
  {
    this.segmentIdentifier = segmentIdentifier;
    this.timestamp = timestamp;
    this.base = base;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Sequence<T> run(final Query<T> query, Map<String, Object> responseContext)
  {
    if (query.getContextBySegment(false)) {
      final Sequence<T> baseSequence = base.run(query, responseContext);
      final List<T> results = Sequences.toList(baseSequence, Lists.<T>newArrayList());
      return Sequences.simple(
          Arrays.asList(
              (T) new Result<BySegmentResultValueClass<T>>(
                  timestamp,
                  new BySegmentResultValueClass<T>(
                      results,
                      segmentIdentifier,
                      query.getIntervals().get(0)
                  )
              )
          )
      );
    }
    return base.run(query, responseContext);
  }
}
