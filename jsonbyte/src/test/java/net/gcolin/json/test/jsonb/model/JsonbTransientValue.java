/*******************************************************************************
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0 and
 * Eclipse Distribution License v. 1.0 which accompanies this distribution. The Eclipse Public
 * License is available at http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution
 * License is available at http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors: Roman Grigoriadi
 ******************************************************************************/

package net.gcolin.json.test.jsonb.model;

import javax.json.bind.annotation.JsonbTransient;

/**
 * JsonbTransientValue test.
 * 
 * @author Roman Grigoriadi
 */
public class JsonbTransientValue {

  @JsonbTransient
  private String transientProperty;

  private transient String transientProperty2;

  private String transientProperty3;

  private String transientProperty4;

  private String property;

  public String getTransientProperty() {
    return transientProperty;
  }

  public void setTransientProperty(String transientProperty) {
    this.transientProperty = transientProperty;
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public String getTransientProperty2() {
    return transientProperty2;
  }

  public void setTransientProperty2(String transientProperty2) {
    this.transientProperty2 = transientProperty2;
  }

  @JsonbTransient
  public String getTransientProperty3() {
    return transientProperty3;
  }

  public void setTransientProperty3(String transientProperty3) {
    this.transientProperty3 = transientProperty3;
  }

  public String getTransientProperty4() {
    return transientProperty4;
  }

  @JsonbTransient
  public void setTransientProperty4(String transientProperty4) {
    this.transientProperty4 = transientProperty4;
  }
}
