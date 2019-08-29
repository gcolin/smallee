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
package example;

import java.io.IOException;

import javax.inject.Provider;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gcolin.di.atinject.web.BindHttpServlet;

/**
 * Servlet example.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@WebServlet("/hello")
public class HelloServlet extends BindHttpServlet {

  private static final long serialVersionUID = 1L;

  @Inject
  private Provider<CountService> countService;

  @Inject
  private Provider<GlobalCountService> gcountService;

  @Inject
  private transient HelloService helloService;

  @Inject
  private Provider<WelcomeService> welcomeService;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/plain");
    resp.getWriter().write("helloService: " + helloService.sayHello() + "\nsession: " + countService.get().next() + "\nglobal: "
        + gcountService.get().next() + "\nwelcomeService: " + welcomeService.get().say());
  }

}
