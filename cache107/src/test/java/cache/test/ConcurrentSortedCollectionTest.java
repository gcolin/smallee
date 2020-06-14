/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cache.test;

import net.gcolin.cache.ConcurrentSortedCollection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ConcurrentSortedCollection test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ConcurrentSortedCollectionTest {

  ConcurrentSortedCollection<Integer> cc;

  @Before
  public void before() {
    cc = new ConcurrentSortedCollection<>((a1, a2) -> Integer.compare(a1, a2));
  }

  @Test
  public void insertTest() {
    ConcurrentSortedCollection<Integer>.Node n0 = cc.insert(1);
    ConcurrentSortedCollection<Integer>.Node n1 = cc.insert(5);
    ConcurrentSortedCollection<Integer>.Node n2 = cc.insert(4);
    Assert.assertEquals(n1, n2.prec);
    Assert.assertEquals(n0, n2.next);
    Assert.assertEquals(n1, cc.getTail());

    n1.remove();
    Assert.assertNull(n2.prec);
    Assert.assertEquals(n0, n2.next);
    Assert.assertEquals(n2, cc.getTail());

    n0.remove();
    Assert.assertNull(n2.prec);
    Assert.assertNull(n2.next);
    Assert.assertEquals(n2, cc.getTail());
  }

  @Test
  public void updateTest() {
    ConcurrentSortedCollection<Integer>.Node n0 = cc.insert(1);
    ConcurrentSortedCollection<Integer>.Node n1 = cc.insert(5);
    ConcurrentSortedCollection<Integer>.Node n2 = cc.insert(4);
    Assert.assertEquals(n1, n2.prec);
    Assert.assertEquals(n0, n2.next);
    Assert.assertEquals(n1, cc.getTail());

    n2.element = 5;
    n2.update();
    Assert.assertEquals(n1, n2.prec);
    Assert.assertEquals(n0, n2.next);
    Assert.assertEquals(n1, cc.getTail());

    n2.element = 1;
    n2.update();
    Assert.assertEquals(n1, n2.prec);
    Assert.assertEquals(n0, n2.next);
    Assert.assertEquals(n1, cc.getTail());

    n2.element = 6;
    n2.update();
    Assert.assertNull(n2.prec);
    Assert.assertEquals(n1, n2.next);
    Assert.assertEquals(n2, cc.getTail());

    n2.element = 0;
    n2.update();
    Assert.assertNull(n2.next);
    Assert.assertEquals(n0, n2.prec);
  }

}
