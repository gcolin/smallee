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

package net.gcolin.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;

/**
 * The VariantListBuilder implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class VariantListBuilderImpl extends VariantListBuilder {

  private List<Variant> all = new ArrayList<>();
  private List<Locale> languagesList = new ArrayList<>();
  private List<String> encodingsList = new ArrayList<>();
  private List<MediaType> mediaTypesList = new ArrayList<>();

  @Override
  public List<Variant> build() {
    return all;
  }

  @Override
  public VariantListBuilder add() {
    if (languagesList.isEmpty() && encodingsList.isEmpty() && mediaTypesList.isEmpty()) {
      return this;
    }
    ensureNotEmpty();
    for (Locale language : languagesList) {
      for (String encoding : encodingsList) {
        for (MediaType mediaType : mediaTypesList) {
          all.add(new Variant(mediaType, language, encoding));
        }
      }
    }
    languagesList.clear();
    encodingsList.clear();
    mediaTypesList.clear();
    return this;
  }

  private void ensureNotEmpty() {
    if (languagesList.isEmpty()) {
      languagesList.add(null);
    }
    if (encodingsList.isEmpty()) {
      encodingsList.add(null);
    }
    if (mediaTypesList.isEmpty()) {
      mediaTypesList.add(null);
    }
  }

  @Override
  public VariantListBuilder languages(Locale... languages) {
    for (Locale e : languages) {
      languagesList.add(e);
    }
    return this;
  }

  @Override
  public VariantListBuilder encodings(String... encodings) {
    for (String e : encodings) {
      encodingsList.add(e);
    }
    return this;
  }

  @Override
  public VariantListBuilder mediaTypes(MediaType... mediaTypes) {
    for (MediaType e : mediaTypes) {
      mediaTypesList.add(e);
    }
    return this;
  }

}
