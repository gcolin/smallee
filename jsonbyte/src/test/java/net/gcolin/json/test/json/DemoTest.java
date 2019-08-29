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
package net.gcolin.json.test.json;

import java.util.ArrayList;
import java.util.List;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.junit.Assert;
import org.junit.Test;

/**
 * Dog tests.
 * 
 * @author Jitendra Kotamraju
 */
public class DemoTest {

  public static class Dog {
    public String name;
    public int age;
    public boolean bitable;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + age;
      result = prime * result + (bitable ? 1231 : 1237);
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Dog other = (Dog) obj;
      if (age != other.age)
        return false;
      if (bitable != other.bitable)
        return false;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      return true;
    }
  }

  @Test
  public void test() {
    // Create a dog instance
    Dog dog = new Dog();
    dog.name = "Falco";
    dog.age = 4;
    dog.bitable = false;

    // Create Jsonb and serialize
    Jsonb jsonb = JsonbBuilder.create();
    String result = jsonb.toJson(dog);

    Assert.assertEquals("{\"name\":\"Falco\",\"age\":4,\"bitable\":false}", result);

    // Deserialize back
    Assert.assertEquals(dog, jsonb.fromJson(result, Dog.class));
  }

  @SuppressWarnings("serial")
  @Test
  public void testCollection() {
    Dog falco = new Dog();
    falco.name = "Falco";
    falco.age = 4;
    falco.bitable = false;

    Dog cassidy = new Dog();
    cassidy.name = "Cassidy";
    cassidy.age = 3;
    cassidy.bitable = true;

    List<Dog> dogs = new ArrayList<>();
    dogs.add(falco);
    dogs.add(cassidy);

    // Create Jsonb and serialize
    Jsonb jsonb = JsonbBuilder.create();
    String result = jsonb.toJson(dogs);

    Assert.assertEquals(
        "[{\"name\":\"Falco\",\"age\":4,\"bitable\":false},{\"name\":\"Cassidy\",\"age\":3,\"bitable\":true}]",
        result);

    // Deserialize back
    Assert.assertEquals(dogs,
        jsonb.fromJson(result, new ArrayList<Dog>() {}.getClass().getGenericSuperclass()));
  }

}
