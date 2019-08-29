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

package net.gcolin.json.test.jsonb;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Cycle test.
 * 
 * @author Gaël COLIN
 */
public class CycleTest extends AbstractMultiCharsetTest {

  private static class Obj {
    String value;
    List<Obj> list;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((list == null) ? 0 : list.hashCode());
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      Obj other = (Obj) obj;
      if (list == null) {
        if (other.list != null) {
          return false;
        }
      } else if (!list.equals(other.list)) {
        return false;
      }
      if (value == null) {
        if (other.value != null) {
          return false;
        }
      } else if (!value.equals(other.value)) {
        return false;
      }
      return true;
    }
  }

  @Test
  public void test() {
    Obj o1 = new Obj();
    o1.value = "root";
    Obj o2 = new Obj();
    o2.value = "node1";
    Obj o3 = new Obj();
    o3.value = "node2";
    o1.list = Arrays.asList(o2, o3);

    Obj o4 = test0(Obj.class, o1,
        "{\"value\":\"root\",\"list\":[{\"value\":\"node1\"},{\"value\":\"node2\"}]}");

    Assert.assertEquals(o1, o4);
  }

}
