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
package net.gcolin.di.atinject.jsp;

import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Extension;
import net.gcolin.di.atinject.el.EnvElResolver;

/**
 * Enable injection in jsp through Expression language.
 *
 * @author Gaël COLIN
 * @since 1.0
 */
public class JspExtension implements Extension {
  
  @Override
  public void doStart(Environment env) {
    JspFactory jspFactory = JspFactory.getDefaultFactory();
    ServletContext context = env.get(ServletContext.class);
    if (jspFactory != null) {
      JspApplicationContext jspContext = jspFactory.getJspApplicationContext(context);
      try {
        jspContext.addELResolver(new EnvElResolver(env));
      } catch (Exception ex) {
    	  env.getLog().log(Level.FINER, ex.getMessage(), ex);
      }
    }
  }

}
