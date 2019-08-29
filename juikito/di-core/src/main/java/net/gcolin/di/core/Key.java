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
package net.gcolin.di.core;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * A binding key.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Key implements Serializable {

  private static final long serialVersionUID = -2829770926172327431L;

  private String type;

  private Annotation[] qualifiers;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Annotation[] getQualifiers() {
    return qualifiers;
  }

  public void setQualifiers(Annotation[] qualifiers) {
    this.qualifiers = qualifiers;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(qualifiers);
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    Key other = (Key) obj;
    if (!Arrays.equals(qualifiers, other.qualifiers))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Key [type=" + type + ", qualifiers=" + Arrays.toString(qualifiers) + "]";
  }
}
