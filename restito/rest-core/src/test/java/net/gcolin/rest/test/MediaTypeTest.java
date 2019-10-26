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

package net.gcolin.rest.test;

import net.gcolin.common.collection.Collections2;
import net.gcolin.rest.FastMediaType;
import net.gcolin.rest.RuntimeDelegateImpl;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class MediaTypeTest {

  @BeforeClass
  public static void init() {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
  }

  @Test
  public void iteratorTest() {
    Assert.assertNull(FastMediaType.iterator(null));
    List<FastMediaType> list =
        Collections2.toList(FastMediaType.iterator(MediaType.APPLICATION_JSON));
    Assert.assertEquals(1, list.size());
    Assert.assertEquals(FastMediaType.valueOf(MediaType.APPLICATION_JSON), list.get(0));
  }

  @Test
  public void valueOfTest() {
    Assert.assertNull(FastMediaType.valueOf((MediaType) null));
    Assert.assertEquals(FastMediaType.valueOf(MediaType.APPLICATION_JSON),
        FastMediaType.valueOf(MediaType.APPLICATION_JSON));
    Assert.assertEquals(FastMediaType.valueOf(MediaType.APPLICATION_JSON),
        MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void testMediaTypeCreate() {
    test(new Runnable() {

      @Override
      public void run() {
        MediaType.valueOf(MediaType.APPLICATION_JSON);
      }
    }, "MediaTypeCreate");
  }

  @Test
  public void testMediaTypeCompatible() {
    test(new Runnable() {

      @Override
      public void run() {
        MediaType.APPLICATION_JSON_TYPE.isCompatible(MediaType.WILDCARD_TYPE);
        MediaType.APPLICATION_JSON_TYPE.isCompatible(MediaType.APPLICATION_JSON_TYPE);
      }
    }, "MediaTypeCompatible");

  }

  @Test
  public void testCompatible() {
    Assert.assertTrue(FastMediaType.valueOf(MediaType.APPLICATION_JSON)
        .isCompatible(MediaType.APPLICATION_JSON_TYPE));
    Assert.assertTrue(MediaType.APPLICATION_JSON_TYPE
        .isCompatible(FastMediaType.valueOf(MediaType.APPLICATION_JSON)));

    Assert.assertFalse(FastMediaType.valueOf(MediaType.APPLICATION_JSON).isCompatible(null));

    Assert.assertTrue(FastMediaType.valueOf(MediaType.WILDCARD)
        .isCompatible(FastMediaType.valueOf(MediaType.WILDCARD)));
    Assert.assertTrue(FastMediaType.valueOf(MediaType.WILDCARD)
        .isCompatible(FastMediaType.valueOf(MediaType.APPLICATION_JSON)));
    Assert.assertTrue(FastMediaType.valueOf(MediaType.APPLICATION_JSON)
        .isCompatible(FastMediaType.valueOf(MediaType.WILDCARD)));

    Assert.assertTrue(FastMediaType.valueOf(MediaType.APPLICATION_JSON)
        .isCompatible(FastMediaType.valueOf("application/*")));
    Assert.assertTrue(FastMediaType.valueOf("application/*")
        .isCompatible(FastMediaType.valueOf(MediaType.APPLICATION_JSON)));

    Assert.assertTrue(FastMediaType.valueOf(MediaType.APPLICATION_JSON)
        .isCompatible(FastMediaType.valueOf(MediaType.APPLICATION_JSON)));

    Assert.assertTrue(FastMediaType.valueOf(MediaType.WILDCARD).isWildcard());
    Assert.assertTrue(0 < FastMediaType.valueOf(MediaType.WILDCARD).getId());
  }

  @Test
  public void testFastMediaTypeCreate() {
    test(new Runnable() {

      @Override
      public void run() {
        FastMediaType.valueOf(MediaType.APPLICATION_JSON);
      }
    }, "FastMediaTypeCreate");
  }

  @Test
  public void testFastMediaTypeCompatible() {
    final FastMediaType json = FastMediaType.valueOf(MediaType.APPLICATION_JSON);
    final FastMediaType wilcard = FastMediaType.valueOf(MediaType.WILDCARD);
    test(new Runnable() {

      @Override
      public void run() {
        json.isCompatible(wilcard);
        json.isCompatible(json);
      }
    }, "FastMediaTypeCompatible");
  }

  @Test
  public void testFor() {
    final List<String> list = Arrays.asList("1", "2", "3");
    test(new Runnable() {

      @SuppressWarnings("unused")
      @Override
      public void run() {
        List<String> ll = list;
        String str;
        for (int i = 0, len = ll.size(); i < len; i++) {
          str = ll.get(i);
        }
      }
    }, "For");
  }

  @Test
  public void testForEach() {
    final List<String> list = Arrays.asList("1", "2", "3");
    test(new Runnable() {

      @SuppressWarnings("unused")
      @Override
      public void run() {
        List<String> ll = list;
        String str;
        for (String s : ll) {
          str = s;
        }
      }
    }, "ForEach");
  }

  @Test
  public void testArrayFor() {
    final String[] list = {"1", "2", "3"};
    test(new Runnable() {

      @SuppressWarnings("unused")
      @Override
      public void run() {
        String[] ll = list;
        String str;
        for (int i = 0, len = ll.length; i < len; i++) {
          str = ll[i];
        }
      }
    }, "ArrayFor");
  }

  @Test
  public void testArrayForEach() {
    final String[] list = {"1", "2", "3"};
    test(new Runnable() {

      @SuppressWarnings("unused")
      @Override
      public void run() {
        String[] ll = list;
        String str;
        for (String s : ll) {
          str = s;
        }
      }
    }, "ArrayForEach");
  }

  private void test(Runnable run, String msg) {
    //Time.tick();
    for (int i = 0; i < 1; i++) {
      run.run();
    }
    //Time.tock(msg);
  }

  @Test
  public void testMediaTypeIterator() {
    Iterator<FastMediaType> it =
        FastMediaType.iterator("text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
    Assert.assertTrue(it.hasNext());
    Assert.assertEquals("text/html", it.next().toString());
    Assert.assertTrue(it.hasNext());
    Assert.assertEquals("image/gif", it.next().toString());
    Assert.assertTrue(it.hasNext());
    Assert.assertEquals("image/jpeg", it.next().toString());
    Assert.assertTrue(it.hasNext());
    FastMediaType fm = it.next();
    Assert.assertEquals("*", fm.getSubtype());
    Assert.assertEquals("*", fm.getType());
    Assert.assertEquals(".2", fm.getParameters().get("q"));
    Assert.assertTrue(it.hasNext());
    Assert.assertEquals("*/*; q=.2", it.next().toString());
  }

}
