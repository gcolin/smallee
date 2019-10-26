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

package net.gcolin.jsonb.build;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.json.bind.JsonbException;
import javax.json.bind.adapter.JsonbAdapter;
import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.jsonb.JsonbSerializerExtended;

/**
 * A factory for adapting a {@code JsonbSerializer} to a
 * {@code JsonbSerializerExtended}.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonbSerializerAdapter {

	private JsonbSerializerAdapter() {
		// nothing.
	}

	/**
	 * Adapt a JsonbSerializer to a JsonbSerializerExtended.
	 * 
	 * @param sr a JsonbSerializer
	 * @return a JsonbSerializerExtended
	 */
	public static JsonbSerializerExtended<Object> createSerializer(JsonbSerializer<Object> sr) {
		return new JsonbSerializerExtended<Object>() {

			@Override
			public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
				generator.writeStartObject();
				sr.serialize(obj, generator, ctx);
				generator.writeEnd();
			}

			@Override
			public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
				generator.writeStartObject(key);
				sr.serialize(obj, generator, ctx);
				generator.writeEnd();
			}

		};
	}

	/**
	 * Create a JsonbSerializerExtended from a standard type and an adapter.
	 * 
	 * @param adapter an adapter
	 * @param builder a builder
	 * @param parent  type of the declaring POJO
	 * @return a JsonbSerializerExtended
	 */
	@SuppressWarnings("unchecked")
	public static JsonbSerializerExtended<Object> createSerializer(JsonbAdapter<Object, Object> adapter,
			JNodeBuilder builder, Type parent) {
		Type type = Reflect.getGenericTypeArguments(JsonbAdapter.class, adapter.getClass(), parent).get(1);
		if (type instanceof TypeVariable) {
			type = Object.class;
		}
		Class<?> clazz = Reflect.toClass(type);
		if (Number.class.isAssignableFrom(clazz)) {
			if (clazz == Integer.class) {
				return new JsonbSerializerExtended<Object>() {

					@Override
					public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							Integer nb = (Integer) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull();
							} else {
								generator.write(nb);
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

					@Override
					public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							Integer nb = (Integer) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull(key);
							} else {
								generator.write(key, nb);
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

				};
			} else if (clazz == Long.class) {
				return new JsonbSerializerExtended<Object>() {

					@Override
					public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							Long nb = (Long) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull();
							} else {
								generator.write(nb);
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

					@Override
					public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							Long nb = (Long) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull(key);
							} else {
								generator.write(key, nb);
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

				};
			} else if (clazz == Double.class) {
				return new JsonbSerializerExtended<Object>() {

					@Override
					public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							Double nb = (Double) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull();
							} else {
								generator.write(nb);
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

					@Override
					public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							Double nb = (Double) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull(key);
							} else {
								generator.write(key, nb);
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

				};
			} else if (clazz == Float.class) {
				return new JsonbSerializerExtended<Object>() {

					@Override
					public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							Float nb = (Float) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull();
							} else {
								generator.write(nb.doubleValue());
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

					@Override
					public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							Float nb = (Float) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull(key);
							} else {
								generator.write(key, nb.doubleValue());
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

				};
			} else if (clazz == Short.class) {
				return new JsonbSerializerExtended<Object>() {

					@Override
					public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							Short nb = (Short) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull();
							} else {
								generator.write(nb.intValue());
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

					@Override
					public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							Short nb = (Short) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull(key);
							} else {
								generator.write(key, nb.intValue());
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

				};
			} else if (clazz == Byte.class) {
				return new JsonbSerializerExtended<Object>() {

					@Override
					public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							Byte nb = (Byte) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull();
							} else {
								generator.write(nb.intValue());
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

					@Override
					public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							Byte nb = (Byte) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull(key);
							} else {
								generator.write(key, nb.intValue());
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

				};
			} else if (clazz == BigDecimal.class) {
				return new JsonbSerializerExtended<Object>() {

					@Override
					public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							BigDecimal nb = (BigDecimal) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull();
							} else {
								generator.write(nb);
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

					@Override
					public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							BigDecimal nb = (BigDecimal) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull(key);
							} else {
								generator.write(key, nb);
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

				};
			} else if (clazz == BigInteger.class) {
				return new JsonbSerializerExtended<Object>() {

					@Override
					public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							BigInteger nb = (BigInteger) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull();
							} else {
								generator.write(nb);
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

					@Override
					public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
						try {
							BigInteger nb = (BigInteger) adapter.adaptToJson(obj);
							if (nb == null) {
								generator.writeNull(key);
							} else {
								generator.write(key, nb);
							}
						} catch (Exception ex) {
							throw new JsonbException(ex.getMessage(), ex);
						}
					}

				};
			} else {
				throw new JsonbException("number not supported : " + clazz);
			}
		} else if (clazz == Boolean.class) {
			return new JsonbSerializerExtended<Object>() {

				@Override
				public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
					try {
						Boolean nb = (Boolean) adapter.adaptToJson(obj);
						if (nb == null) {
							generator.writeNull();
						} else {
							generator.write(nb);
						}
					} catch (Exception ex) {
						throw new JsonbException(ex.getMessage(), ex);
					}
				}

				@Override
				public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
					try {
						Boolean nb = (Boolean) adapter.adaptToJson(obj);
						if (nb == null) {
							generator.writeNull(key);
						} else {
							generator.write(key, nb);
						}
					} catch (Exception ex) {
						throw new JsonbException(ex.getMessage(), ex);
					}
				}

			};
		} else if (clazz == String.class) {
			return new JsonbSerializerExtended<Object>() {

				@Override
				public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
					try {
						String nb = (String) adapter.adaptToJson(obj);
						if (nb == null) {
							generator.writeNull();
						} else {
							generator.write(nb);
						}
					} catch (Exception ex) {
						throw new JsonbException(ex.getMessage(), ex);
					}
				}

				@Override
				public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
					try {
						String nb = (String) adapter.adaptToJson(obj);
						if (nb == null) {
							generator.writeNull(key);
						} else {
							generator.write(key, nb);
						}
					} catch (Exception ex) {
						throw new JsonbException(ex.getMessage(), ex);
					}
				}

			};
		} else {
			JNode node = builder.build(parent, (Class<Object>) clazz, type, null, null, new JContext());

			return new JsonbSerializerExtended<Object>() {

				@Override
				public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
					try {
						node.getSerializer().serialize(adapter.adaptToJson(obj), generator, ctx);
					} catch (Exception ex) {
						throw new JsonbException(ex.getMessage(), ex);
					}
				}

				@Override
				public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
					try {
						node.getSerializer().serialize(key, adapter.adaptToJson(obj), generator, ctx);
					} catch (Exception ex) {
						throw new JsonbException(ex.getMessage(), ex);
					}
				}

			};
		}
	}

}
