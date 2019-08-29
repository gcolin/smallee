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

import java.math.BigDecimal;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

/**
 * @author Roman Grigoriadi
 */
public class CrateSerializer implements JsonbSerializer<Crate> {

  @Override
  public void serialize(Crate obj, JsonGenerator generator, SerializationContext ctx) {
    generator.write("crate_str", "REPLACED crate str");
    ctx.serialize("crateInner", obj.crateInner, generator);
    ctx.serialize("crateInnerList", obj.crateInnerList, generator);
    generator.write("crateBigDec", new BigDecimal("54321"));
  }
}
