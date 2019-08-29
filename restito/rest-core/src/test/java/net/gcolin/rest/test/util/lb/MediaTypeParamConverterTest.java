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

package net.gcolin.rest.test.util.lb;

import net.gcolin.rest.util.lb.MediaTypeParamConverter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class MediaTypeParamConverterTest {

  private String value = "text/html; encoding=\"utf8\"; other= a";
  private String value2 = "text/html; encoding=\"utf8\"; other=\"a\"";
  private MediaType mediaType;

  /**
   * Initialize test.
   */
  @Before
  public void before() {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("encoding", "utf8");
    parameters.put("other", "a");
    mediaType = new MediaType("text", "html", parameters);
  }

  @Test
  public void fromStringTest() {
    Assert.assertEquals(mediaType, new MediaTypeParamConverter().fromString(value));
    Assert.assertEquals(mediaType, new MediaTypeParamConverter().fromString(value + ";emptyParam"));
    Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
        new MediaTypeParamConverter().fromString(MediaType.APPLICATION_JSON));
  }

  @Test
  public void toStringTest() {
    Assert.assertEquals(value2, new MediaTypeParamConverter().toString(mediaType));
    Assert.assertEquals(MediaType.APPLICATION_JSON,
        new MediaTypeParamConverter().toString(MediaType.APPLICATION_JSON_TYPE));
  }

}
