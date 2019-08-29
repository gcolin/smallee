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
package net.gcolin.di.atinject.test;

import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Engine;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;

import junit.framework.Test;
import net.gcolin.di.atinject.Environment;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class TestTck {

  public static Test suite() {
    // Create and start the TestContainer, which takes care of starting the container, deploying the
    // classes, starting the contexts etc.
    Environment env = new Environment();
    env.addBinding(Car.class).implementedBy(Convertible.class);
    env.addBinding(Seat.class).named(Drivers.class.getName())
            .implementedBy(DriversSeat.class);
    env.addBinding(Engine.class).implementedBy(V8Engine.class);
    env.addBinding(Tire.class).named("spare")
            .implementedBy(SpareTire.class);
    env.add(Seat.class, Tire.class, Cupholder.class, SpareTire.class, FuelTank.class);

    Car instance = (Car) env.get(Car.class);

    return Tck.testsFor(instance, false /* supportsStatic */, true /* supportsPrivate */);
  }
  
}
