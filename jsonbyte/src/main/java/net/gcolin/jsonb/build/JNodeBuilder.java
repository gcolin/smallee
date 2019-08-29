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

import net.gcolin.common.collection.IgnoreCaseMap;
import net.gcolin.common.lang.Locales;
import net.gcolin.common.lang.Pair;
import net.gcolin.common.lang.Strings;
import net.gcolin.common.reflect.Reflect;
import net.gcolin.jsonb.JsonbDeserializerExtended;
import net.gcolin.jsonb.JsonbSerializerExtended;
import net.gcolin.jsonb.serializer.BigDecimalDeserializer;
import net.gcolin.jsonb.serializer.BigDecimalSerializer;
import net.gcolin.jsonb.serializer.BigIntegerDeserializer;
import net.gcolin.jsonb.serializer.BigIntegerSerializer;
import net.gcolin.jsonb.serializer.BooleanDeserializer;
import net.gcolin.jsonb.serializer.BooleanSerializer;
import net.gcolin.jsonb.serializer.ByteDeserializer;
import net.gcolin.jsonb.serializer.ByteSerializer;
import net.gcolin.jsonb.serializer.CharacterDeserializer;
import net.gcolin.jsonb.serializer.CharacterSerializer;
import net.gcolin.jsonb.serializer.DoubleDeserializer;
import net.gcolin.jsonb.serializer.DoubleSerializer;
import net.gcolin.jsonb.serializer.DurationDeserializer;
import net.gcolin.jsonb.serializer.EnumDeserializer;
import net.gcolin.jsonb.serializer.EnumSerializer;
import net.gcolin.jsonb.serializer.FloatDeserializer;
import net.gcolin.jsonb.serializer.FloatSerializer;
import net.gcolin.jsonb.serializer.IntDeserializer;
import net.gcolin.jsonb.serializer.IntSerializer;
import net.gcolin.jsonb.serializer.JNodeArrayDeserializer;
import net.gcolin.jsonb.serializer.JNodeArraySerializer;
import net.gcolin.jsonb.serializer.JNodeCollectionDeserializer;
import net.gcolin.jsonb.serializer.JNodeCollectionSerializer;
import net.gcolin.jsonb.serializer.JNodeEnumMapDeserializer;
import net.gcolin.jsonb.serializer.JNodeEnumSetDeserializer;
import net.gcolin.jsonb.serializer.JNodeListSerializer;
import net.gcolin.jsonb.serializer.JNodeMapDeserializer;
import net.gcolin.jsonb.serializer.JNodeMapSerializer;
import net.gcolin.jsonb.serializer.JNodeObjectDeserializer;
import net.gcolin.jsonb.serializer.JNodeObjectSerializer;
import net.gcolin.jsonb.serializer.JsonArrayDeserializer;
import net.gcolin.jsonb.serializer.JsonNumberDeserializer;
import net.gcolin.jsonb.serializer.JsonObjectDeserializer;
import net.gcolin.jsonb.serializer.JsonStringDeserializer;
import net.gcolin.jsonb.serializer.JsonStructureDeserializer;
import net.gcolin.jsonb.serializer.JsonValueDeserializer;
import net.gcolin.jsonb.serializer.JsonValueSerializer;
import net.gcolin.jsonb.serializer.LongDeserializer;
import net.gcolin.jsonb.serializer.LongSerializer;
import net.gcolin.jsonb.serializer.ObjectDeserializer;
import net.gcolin.jsonb.serializer.ObjectSerializer;
import net.gcolin.jsonb.serializer.OptionalDeserializer;
import net.gcolin.jsonb.serializer.OptionalDoubleDeserializer;
import net.gcolin.jsonb.serializer.OptionalDoubleSerializer;
import net.gcolin.jsonb.serializer.OptionalIntDeserializer;
import net.gcolin.jsonb.serializer.OptionalIntSerializer;
import net.gcolin.jsonb.serializer.OptionalLongDeserializer;
import net.gcolin.jsonb.serializer.OptionalLongSerializer;
import net.gcolin.jsonb.serializer.OptionalSerializer;
import net.gcolin.jsonb.serializer.PeriodDeserializer;
import net.gcolin.jsonb.serializer.ShortDeserializer;
import net.gcolin.jsonb.serializer.ShortSerializer;
import net.gcolin.jsonb.serializer.SimpleTimeZoneDeserializer;
import net.gcolin.jsonb.serializer.StringDeserializer;
import net.gcolin.jsonb.serializer.StringSerializer;
import net.gcolin.jsonb.serializer.TimeZoneDeserializer;
import net.gcolin.jsonb.serializer.TimeZoneSerializer;
import net.gcolin.jsonb.serializer.ToStringSerializer;
import net.gcolin.jsonb.serializer.UriDeserializer;
import net.gcolin.jsonb.serializer.UrlDeserializer;
import net.gcolin.jsonb.serializer.ZoneIdDeserializer;
import net.gcolin.jsonb.serializer.ZoneOffsetDeserializer;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.bind.JsonbConfig;
import javax.json.bind.JsonbException;
import javax.json.bind.adapter.JsonbAdapter;
import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.annotation.JsonbNillable;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.json.bind.annotation.JsonbTypeDeserializer;
import javax.json.bind.annotation.JsonbTypeSerializer;
import javax.json.bind.config.PropertyNamingStrategy;
import javax.json.bind.config.PropertyOrderStrategy;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.bind.serializer.JsonbSerializer;

/**
 * Generates Json bean metadata.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class JNodeBuilder {

	private static class Property {
		Field field;
		Method getter;
		Method setter;
	}

	static final Map<Class<?>, SerializerFactory> STANDARD_FACTORY = new HashMap<>();

	static final Map<Class<?>, SerializerPair> STANDARD_ADAPTER = new HashMap<>();

	static {
		STANDARD_FACTORY.put(Date.class, new DateSerializerFactory());
		STANDARD_FACTORY.put(Calendar.class, new CalendarSerializerFactory());
		STANDARD_FACTORY.put(GregorianCalendar.class, new GregorianCalendarSerializerFactory());
		STANDARD_FACTORY.put(Instant.class,
				new TemporalAccessorSerializerFactory<>(DateTimeFormatter.ISO_INSTANT, Instant::from));
		STANDARD_FACTORY.put(LocalDate.class,
				new TemporalAccessorSerializerFactory<>(DateTimeFormatter.ISO_LOCAL_DATE, LocalDate::from));
		STANDARD_FACTORY.put(LocalTime.class,
				new TemporalAccessorSerializerFactory<>(DateTimeFormatter.ISO_LOCAL_TIME, LocalTime::from));
		STANDARD_FACTORY.put(LocalDateTime.class,
				new TemporalAccessorSerializerFactory<>(DateTimeFormatter.ISO_LOCAL_DATE_TIME, LocalDateTime::from));
		STANDARD_FACTORY.put(ZonedDateTime.class,
				new TemporalAccessorSerializerFactory<>(DateTimeFormatter.ISO_ZONED_DATE_TIME, ZonedDateTime::from));
		STANDARD_FACTORY.put(OffsetTime.class,
				new TemporalAccessorSerializerFactory<>(DateTimeFormatter.ISO_OFFSET_TIME, OffsetTime::from));
		STANDARD_FACTORY.put(OffsetDateTime.class,
				new TemporalAccessorSerializerFactory<>(DateTimeFormatter.ISO_OFFSET_DATE_TIME, OffsetDateTime::from));

		STANDARD_ADAPTER.put(ZoneId.class, new SerializerPair((JsonbSerializerExtended) new ToStringSerializer(),
				(JsonbDeserializerExtended) new ZoneIdDeserializer()));
		STANDARD_ADAPTER.put(ZoneOffset.class, new SerializerPair((JsonbSerializerExtended) new ToStringSerializer(),
				(JsonbDeserializerExtended) new ZoneOffsetDeserializer()));
		STANDARD_ADAPTER.put(Period.class, new SerializerPair((JsonbSerializerExtended) new ToStringSerializer(),
				(JsonbDeserializerExtended) new PeriodDeserializer()));
		STANDARD_ADAPTER.put(TimeZone.class, new SerializerPair((JsonbSerializerExtended) new TimeZoneSerializer(),
				(JsonbDeserializerExtended) new TimeZoneDeserializer()));
		STANDARD_ADAPTER.put(SimpleTimeZone.class,
				new SerializerPair((JsonbSerializerExtended) new TimeZoneSerializer(),
						(JsonbDeserializerExtended) new SimpleTimeZoneDeserializer()));
		STANDARD_ADAPTER.put(BigDecimal.class, new SerializerPair((JsonbSerializerExtended) new BigDecimalSerializer(),
				(JsonbDeserializerExtended) new BigDecimalDeserializer()));
		STANDARD_ADAPTER.put(BigInteger.class, new SerializerPair((JsonbSerializerExtended) new BigIntegerSerializer(),
				(JsonbDeserializerExtended) new BigIntegerDeserializer()));
		STANDARD_ADAPTER.put(Boolean.class, new SerializerPair((JsonbSerializerExtended) new BooleanSerializer(),
				(JsonbDeserializerExtended) new BooleanDeserializer()));
		STANDARD_ADAPTER.put(Byte.class, new SerializerPair((JsonbSerializerExtended) new ByteSerializer(),
				(JsonbDeserializerExtended) new ByteDeserializer()));
		STANDARD_ADAPTER.put(Character.class, new SerializerPair((JsonbSerializerExtended) new CharacterSerializer(),
				(JsonbDeserializerExtended) new CharacterDeserializer()));
		STANDARD_ADAPTER.put(Double.class, new SerializerPair((JsonbSerializerExtended) new DoubleSerializer(),
				(JsonbDeserializerExtended) new DoubleDeserializer()));
		STANDARD_ADAPTER.put(Float.class, new SerializerPair((JsonbSerializerExtended) new FloatSerializer(),
				(JsonbDeserializerExtended) new FloatDeserializer()));
		STANDARD_ADAPTER.put(Integer.class, new SerializerPair((JsonbSerializerExtended) new IntSerializer(),
				(JsonbDeserializerExtended) new IntDeserializer()));
		STANDARD_ADAPTER.put(Long.class, new SerializerPair((JsonbSerializerExtended) new LongSerializer(),
				(JsonbDeserializerExtended) new LongDeserializer()));
		STANDARD_ADAPTER.put(Short.class, new SerializerPair((JsonbSerializerExtended) new ShortSerializer(),
				(JsonbDeserializerExtended) new ShortDeserializer()));
		STANDARD_ADAPTER.put(String.class, new SerializerPair((JsonbSerializerExtended) new StringSerializer(),
				(JsonbDeserializerExtended) new StringDeserializer()));
		STANDARD_ADAPTER.put(URI.class, new SerializerPair((JsonbSerializerExtended) new ToStringSerializer(),
				(JsonbDeserializerExtended) new UriDeserializer()));
		STANDARD_ADAPTER.put(URL.class, new SerializerPair((JsonbSerializerExtended) new ToStringSerializer(),
				(JsonbDeserializerExtended) new UrlDeserializer()));
		STANDARD_ADAPTER.put(Duration.class, new SerializerPair((JsonbSerializerExtended) new ToStringSerializer(),
				(JsonbDeserializerExtended) new DurationDeserializer()));
		STANDARD_ADAPTER.put(OptionalInt.class,
				new SerializerPair((JsonbSerializerExtended) new OptionalIntSerializer(),
						(JsonbDeserializerExtended) new OptionalIntDeserializer()));
		STANDARD_ADAPTER.put(OptionalLong.class,
				new SerializerPair((JsonbSerializerExtended) new OptionalLongSerializer(),
						(JsonbDeserializerExtended) new OptionalLongDeserializer()));
		STANDARD_ADAPTER.put(OptionalDouble.class,
				new SerializerPair((JsonbSerializerExtended) new OptionalDoubleSerializer(),
						(JsonbDeserializerExtended) new OptionalDoubleDeserializer()));

		STANDARD_ADAPTER.put(JsonArray.class, new SerializerPair((JsonbSerializerExtended) new JsonValueSerializer(),
				(JsonbDeserializerExtended) new JsonArrayDeserializer()));
		STANDARD_ADAPTER.put(JsonObject.class, new SerializerPair((JsonbSerializerExtended) new JsonValueSerializer(),
				(JsonbDeserializerExtended) new JsonObjectDeserializer()));
		STANDARD_ADAPTER.put(JsonStructure.class,
				new SerializerPair((JsonbSerializerExtended) new JsonValueSerializer(),
						(JsonbDeserializerExtended) new JsonStructureDeserializer()));
		STANDARD_ADAPTER.put(JsonValue.class, new SerializerPair((JsonbSerializerExtended) new JsonValueSerializer(),
				(JsonbDeserializerExtended) new JsonValueDeserializer()));
		STANDARD_ADAPTER.put(JsonString.class, new SerializerPair((JsonbSerializerExtended) new JsonValueSerializer(),
				(JsonbDeserializerExtended) new JsonStringDeserializer()));
		STANDARD_ADAPTER.put(JsonNumber.class, new SerializerPair((JsonbSerializerExtended) new JsonValueSerializer(),
				(JsonbDeserializerExtended) new JsonNumberDeserializer()));
		STANDARD_ADAPTER.put(int.class, STANDARD_ADAPTER.get(Integer.class));
		STANDARD_ADAPTER.put(long.class, STANDARD_ADAPTER.get(Long.class));
		STANDARD_ADAPTER.put(float.class, STANDARD_ADAPTER.get(Float.class));
		STANDARD_ADAPTER.put(double.class, STANDARD_ADAPTER.get(Double.class));
		STANDARD_ADAPTER.put(short.class, STANDARD_ADAPTER.get(Short.class));
		STANDARD_ADAPTER.put(boolean.class, STANDARD_ADAPTER.get(Boolean.class));
		STANDARD_ADAPTER.put(char.class, STANDARD_ADAPTER.get(Character.class));
		STANDARD_ADAPTER.put(byte.class, STANDARD_ADAPTER.get(Byte.class));
	}

	public final Map<Class<?>, JsonbAdapter<Object, Object>> adapters = new ConcurrentHashMap<>();
	public final Map<Class<?>, JsonbDeserializerExtended<Object>> deserializers = new ConcurrentHashMap<>();
	public final Map<Class<?>, JsonbSerializerExtended<Object>> serializers = new ConcurrentHashMap<>();
	public final Map<Type, JNode> nodesByClass = new ConcurrentHashMap<>();
	private JsonbConfig config;
	private PropertyNamingStrategy nameStrategy;
	private Comparator<JProperty> orderStrategy;
	private Map<Type, JsonbDeserializerExtended<Object>> configDeserializers = new HashMap<>();
	private Map<Type, JsonbSerializerExtended<Object>> configSerializers = new HashMap<>();
	private Map<Class<?>, SerializerPair> configAdapter = new HashMap<>();

	/**
	 * Create a JNodeBuilder.
	 * 
	 * @param config configuration
	 */
	public JNodeBuilder(JsonbConfig config) {
		this.config = config;

		Optional<Object> propertyNameStrategy = config.getProperty(JsonbConfig.PROPERTY_NAMING_STRATEGY);
		if (propertyNameStrategy.isPresent()) {
			Object val = propertyNameStrategy.get();
			if (val instanceof PropertyNamingStrategy) {
				nameStrategy = (PropertyNamingStrategy) val;
			} else {
				switch (val.toString()) {
				case PropertyNamingStrategy.CASE_INSENSITIVE:
					nameStrategy = new IgnoreCaseStrategy();
					break;
				case PropertyNamingStrategy.LOWER_CASE_WITH_DASHES:
					nameStrategy = new LowerCaseWithDashesStrategy();
					break;
				case PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES:
					nameStrategy = new LowerCaseWithUnderscoresStrategy();
					break;
				case PropertyNamingStrategy.UPPER_CAMEL_CASE:
					nameStrategy = x -> Strings.capitalize(x);
					break;
				case PropertyNamingStrategy.UPPER_CAMEL_CASE_WITH_SPACES:
					nameStrategy = new UpperCamelCaseWithSpacesStrategy();
					break;
				case PropertyNamingStrategy.IDENTITY:
					nameStrategy = x -> x;
					break;
				default:
					throw new JsonbException("PropertyNamingStrategy " + val.toString() + " is not supported");
				}
			}
		} else {
			nameStrategy = x -> x;
		}

		Optional<String> orderProperty = (Optional) config.getProperty(JsonbConfig.PROPERTY_ORDER_STRATEGY);
		if (orderProperty.isPresent()) {
			switch (orderProperty.get()) {
			case PropertyOrderStrategy.LEXICOGRAPHICAL:
				orderStrategy = (p1, p2) -> p1.getName().compareTo(p2.getName());
				break;
			case PropertyOrderStrategy.REVERSE:
				orderStrategy = (p1, p2) -> p2.getName().compareTo(p1.getName());
				break;
			default:
				throw new JsonbException("PropertyOrderStrategy " + orderProperty.get() + " is not supported");
			}
		}

		Optional<JsonbDeserializer<Object>[]> deserializersProperty = (Optional) config
				.getProperty(JsonbConfig.DESERIALIZERS);
		if (deserializersProperty.isPresent()) {
			for (JsonbDeserializer<Object> ds : deserializersProperty.get()) {
				configDeserializers.put(
						Reflect.getGenericTypeArguments(JsonbDeserializer.class, ds.getClass(), null).get(0),
						JsonbDeserializerAdapter.createDeserializer(ds));
			}
		}

		Optional<JsonbSerializer<Object>[]> serializersProperty = (Optional) config
				.getProperty(JsonbConfig.SERIALIZERS);
		if (serializersProperty.isPresent()) {
			for (JsonbSerializer<Object> ds : serializersProperty.get()) {
				configSerializers.put(
						Reflect.getGenericTypeArguments(JsonbSerializer.class, ds.getClass(), null).get(0),
						JsonbSerializerAdapter.createSerializer(ds));
			}
		}

		Optional<JsonbAdapter<Object, Object>[]> adapterProperty = (Optional) config.getProperty(JsonbConfig.ADAPTERS);
		if (adapterProperty.isPresent()) {
			for (JsonbAdapter<Object, Object> ds : adapterProperty.get()) {
				Class<?> clazz = Reflect.getTypeArguments(JsonbAdapter.class, ds.getClass(), null).get(1);
				configAdapter.put(clazz, new SerializerPair(JsonbSerializerAdapter.createSerializer(ds, this, null),
						JsonbDeserializerAdapter.createDeserializer(ds, this, null)));
			}
		}
	}

	/**
	 * Build a node for JSON generation.
	 * 
	 * @param parent       parent generic type
	 * @param type         type
	 * @param genericType  generic type
	 * @param deserializer deserializer
	 * @param serializer   serializer
	 * @param context      builder context
	 * @return a node
	 * @throws JsonbException if an error occurs.
	 */
	public JNode build(Type parent, Class<Object> type, Type genericType,
			JsonbDeserializerExtended<Object> deserializer, JsonbSerializerExtended<Object> serializer,
			JContext context) throws JsonbException {
		JNode node = nodesByClass.get(genericType);
		if (node == null) {
			node = new JNode();
		} else {
			return node;
		}

		Map<Type, JNode> currents = context.getCurrents();

		if (currents.containsKey(genericType)) {
			return currents.get(genericType);
		}

		currents.put(genericType, node);

		if (deserializer == null) {
			deserializer = deserializer(type.getAnnotation(JsonbTypeDeserializer.class), null);
		}
		if (serializer == null) {
			serializer = serializer(type.getAnnotation(JsonbTypeSerializer.class), null);
		}

		JContext di = withAnnotation(type.getAnnotation(JsonbDateFormat.class), type.getAnnotation(JsonbNillable.class),
				context);

		node.setBoundType(type);

		if (deserializer == null && configDeserializers.containsKey(genericType)) {
			deserializer = configDeserializers.get(genericType);
		}

		if (serializer == null && configSerializers.containsKey(genericType)) {
			serializer = configSerializers.get(genericType);
		}

		if (deserializer != null && serializer != null) {
			node.setDeserializer(deserializer);
			node.setSerializer(serializer);
		} else if (Object.class == type) {
			node.setDeserializer((JsonbDeserializerExtended) new ObjectDeserializer());
			node.setSerializer((JsonbSerializerExtended) new ObjectSerializer(this, di));
		} else if (Enum.class.isAssignableFrom(type)) {
			node.setDeserializer((JsonbDeserializerExtended) new EnumDeserializer((Class) type));
			node.setSerializer((JsonbSerializerExtended) new EnumSerializer());
		} else if (configAdapter.containsKey(type)) {
			Pair<JsonbSerializerExtended<Object>, JsonbDeserializerExtended<Object>> std = configAdapter.get(type);
			node.setDeserializer(std.getRight());
			node.setSerializer(std.getLeft());
		} else if (STANDARD_ADAPTER.containsKey(type)) {
			Pair<JsonbSerializerExtended<Object>, JsonbDeserializerExtended<Object>> std = STANDARD_ADAPTER.get(type);
			node.setDeserializer(std.getRight());
			node.setSerializer(std.getLeft());
		} else if (STANDARD_FACTORY.containsKey(type)) {
			Pair<JsonbSerializerExtended<Object>, JsonbDeserializerExtended<Object>> std = STANDARD_FACTORY.get(type)
					.create(genericType, this, di);
			node.setDeserializer(std.getRight());
			node.setSerializer(std.getLeft());
		} else if (type.isArray()) {
			node.setDeserializer(new JNodeArrayDeserializer(parent, genericType, this, di));
			node.setSerializer(new JNodeArraySerializer(parent, genericType, this, di));
		} else if (EnumSet.class.isAssignableFrom(type)) {
			node.setDeserializer(new JNodeEnumSetDeserializer(parent, genericType, this, di));
			node.setSerializer(new JNodeCollectionSerializer(parent, genericType, this, di));
		} else if (EnumMap.class.isAssignableFrom(type)) {
			node.setDeserializer(new JNodeEnumMapDeserializer(parent, genericType, this, di));
			Class<Enum> enumClass = (Class<Enum>) Reflect.getGenericTypeArguments(EnumMap.class, genericType, parent)
					.get(0);
			char[][] ch = new char[enumClass.getEnumConstants().length][];
			for (int i = 0; i < ch.length; i++) {
				ch[i] = ('"' + enumClass.getEnumConstants()[i].name() + "\":").toCharArray();
			}
			node.setSerializer(new JNodeMapSerializer(parent, genericType, this, di) {
				@Override
				protected String serializeKey(Object key) {
					return ((Enum<?>) key).name();
				}

				@Override
				protected char[] serializeKeyCh(Object key) {
					return ch[((Enum<?>) key).ordinal()];
				}
			});
		} else if (Collection.class.isAssignableFrom(type)) {
			node.setDeserializer(new JNodeCollectionDeserializer(parent, genericType, this, di));
			if (List.class.isAssignableFrom(type)) {
				node.setSerializer(new JNodeListSerializer(parent, genericType, this, di));
			} else {
				node.setSerializer(new JNodeCollectionSerializer(parent, genericType, this, di));
			}
		} else if (Map.class.isAssignableFrom(type)) {
			node.setDeserializer(new JNodeMapDeserializer(parent, genericType, this, di));
			node.setSerializer(new JNodeMapSerializer(parent, genericType, this, di));
		} else {
			extractAccessor(type, genericType, node, context);
			node.setDeserializer(new JNodeObjectDeserializer(node));
			node.setSerializer(new JNodeObjectSerializer(node));
		}

		if (serializer != null) {
			node.setSerializer(serializer);
		}

		if (deserializer != null) {
			node.setDeserializer(deserializer);
		}

		if (serializer != null || deserializer != null) {
			node.setNocache(true);
		}

		if (!node.isNocache()) {
			nodesByClass.put(genericType, node);
		}

		currents.remove(genericType);

		return node;
	}

	private JContext withAnnotation(JsonbDateFormat df, JsonbNillable jn, JContext dateInfo) {
		if (df != null || jn != null) {
			JContext di = dateInfo;
			di = di.clone();
			if (df != null) {
				if (!JsonbDateFormat.DEFAULT_FORMAT.equals(df.value())) {
					if (JsonbDateFormat.TIME_IN_MILLIS.equals(df.value())) {
						di.setTimeInMillisecond(true);
					} else {
						di.setTimeInMillisecond(false);
						di.setFormat(df.value());
					}
				}
				if (!JsonbDateFormat.DEFAULT_LOCALE.equals(df.locale())) {
					di.setLocale(Locales.fromString(df.locale()));
				}
			} else if (jn != null) {
				di.setNillable(jn.value());
			}
			return di;
		} else {
			return dateInfo;
		}
	}

	private void extractAccessor(Class<Object> type, Type genericType, JNode node, JContext dateInfo)
			throws JsonbException {
		JContext diAll = dateInfo;
		if (type.getEnclosingClass() == null) {
			diAll = new JContext();
			diAll.getCurrents().putAll(dateInfo.getCurrents());
		}

		Optional<Boolean> nillableConfig = (Optional) config.getProperty(JsonbConfig.NULL_VALUES);
		if (nillableConfig.isPresent() && nillableConfig.get()) {
			diAll = diAll.clone();
			diAll.setNillable(true);
		}

		Package pack = type.getPackage();
		if (pack != null) {
			diAll = withAnnotation(pack.getAnnotation(JsonbDateFormat.class), pack.getAnnotation(JsonbNillable.class),
					diAll);
		}
		diAll = withAnnotation(type.getAnnotation(JsonbDateFormat.class), type.getAnnotation(JsonbNillable.class),
				diAll);

		Map<String, Property> props = new LinkedHashMap<>();

		List<Class<?>> all = getClassHierarchy(type);
		for (int i = all.size() - 1; i >= 0; i--) {

			// fields
			for (Field f : all.get(i).getDeclaredFields()) {
				if (Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers())) {
					continue;
				}
				Property prop = props.get(f.getName());
				if (prop == null) {
					prop = new Property();
					props.put(f.getName(), prop);
				} else if (prop.getter != null && !Modifier.isPublic(prop.getter.getModifiers())) {
					prop.getter = null;
				} else if (prop.setter != null && !Modifier.isPublic(prop.setter.getModifiers())) {
					prop.setter = null;
				}
				prop.field = f;
			}

			// non public setter/getter methods
			for (Method m : all.get(i).getDeclaredMethods()) {
				if ((Reflect.isGetter(m) || Reflect.isSetter(m)) && !Modifier.isPublic(m.getModifiers())) {
					String name = Reflect.getPropertyName(m);
					Property prop = props.get(name);
					if (prop != null) {
						if (Reflect.isSetter(m)) {
							prop.setter = m;
						} else {
							prop.getter = m;
						}
					}
				}
			}
		}

		// public getter/setter methods
		for (Method m : type.getMethods()) {
			if (m.getDeclaringClass() != Object.class && (Reflect.isGetter(m) || Reflect.isSetter(m))
					&& Modifier.isPublic(m.getModifiers())) {
				String name = Reflect.getPropertyName(m);
				Property prop = props.get(name);
				if (prop == null) {
					prop = new Property();
					props.put(name, prop);
				}
				if (Reflect.isSetter(m)) {
					prop.setter = m;
				} else {
					prop.getter = m;
				}
			}
		}

		List<JProperty> optionalElements = new ArrayList<>();
		List<JProperty> elements = new ArrayList<>();
		Map<String, JProperty> elementsMap = new HashMap<>();
		if (IgnoreCaseStrategy.class == nameStrategy.getClass()) {
			elementsMap = new IgnoreCaseMap<>(elementsMap);
		}

		for (Entry<String, Property> prop : props.entrySet()) {
			Property property = prop.getValue();
			Getter getter = null;
			Setter setter = null;
			Class<?> ptype = null;
			Type pgenericType = null;

			if ((property.field != null && (property.field.isAnnotationPresent(JsonbTransient.class)
					|| Modifier.isTransient(property.field.getModifiers())))
					|| (property.getter != null && property.getter.isAnnotationPresent(JsonbTransient.class))
					|| (property.setter != null && property.setter.isAnnotationPresent(JsonbTransient.class))) {
				continue;
			}

			if (property.field != null) {
				ptype = property.field.getType();
				pgenericType = property.field.getGenericType();
			} else if (property.getter != null) {
				ptype = property.getter.getReturnType();
				pgenericType = property.getter.getGenericReturnType();
			} else if (property.setter != null) {
				ptype = property.setter.getParameterTypes()[0];
				pgenericType = property.setter.getGenericParameterTypes()[0];
			}

			JsonbTypeAdapter adaptA = null;
			JsonbTypeDeserializer deserializerA = null;
			JsonbTypeSerializer serializerA = null;
			JsonbDateFormat dateFormat = null;
			JsonbProperty jsonProperty = null;
			JsonbProperty jsonPropertyGetter = null;
			JsonbProperty jsonPropertySetter = null;

			if (property.field != null) {
				adaptA = retrieve(property.field, adaptA, JsonbTypeAdapter.class);
				deserializerA = retrieve(property.field, deserializerA, JsonbTypeDeserializer.class);
				serializerA = retrieve(property.field, serializerA, JsonbTypeSerializer.class);
				dateFormat = retrieve(property.field, dateFormat, JsonbDateFormat.class);
				jsonProperty = property.field.getAnnotation(JsonbProperty.class);
			}

			if (property.getter != null) {
				adaptA = retrieve(property.getter, adaptA, JsonbTypeAdapter.class);
				deserializerA = retrieve(property.getter, deserializerA, JsonbTypeDeserializer.class);
				serializerA = retrieve(property.getter, serializerA, JsonbTypeSerializer.class);
				dateFormat = retrieve(property.getter, dateFormat, JsonbDateFormat.class);
				jsonPropertyGetter = property.getter.getAnnotation(JsonbProperty.class);
			}

			if (property.setter != null) {
				adaptA = retrieve(property.setter, adaptA, JsonbTypeAdapter.class);
				deserializerA = retrieve(property.setter, deserializerA, JsonbTypeDeserializer.class);
				serializerA = retrieve(property.setter, serializerA, JsonbTypeSerializer.class);
				dateFormat = retrieve(property.setter, dateFormat, JsonbDateFormat.class);
				jsonPropertySetter = property.setter.getAnnotation(JsonbProperty.class);
			}

			if (property.setter == null || Modifier.isPublic(property.setter.getModifiers())) {
				if (property.setter != null) {
					setter = new MethodSetter(property.setter);
				} else if (property.field != null) {
					Reflect.enable(property.field);
					setter = new FieldSetter(property.field);
				}
			}

			if (property.getter == null || Modifier.isPublic(property.getter.getModifiers())) {
				if (property.getter != null) {
					getter = new MethodGetter(property.getter);
				} else if (property.field != null) {
					Reflect.enable(property.field);
					getter = new FieldGetter(property.field);
				}
			}

			JsonbDeserializerExtended<Object> deserializer = null;
			JsonbSerializerExtended<Object> serializer = null;

			if (adaptA != null) {
				JsonbAdapter<Object, Object> adapter = adapters.get(adaptA.value());
				if (adapter == null) {
					adapter = Reflects.newInstance(adaptA.value());
					adapters.put(adaptA.value(), adapter);
				}

				deserializer = JsonbDeserializerAdapter.createDeserializer(adapter, this, type);
				serializer = JsonbSerializerAdapter.createSerializer(adapter, this, type);
			}

			deserializer = deserializer(deserializerA, deserializer);
			serializer = serializer(serializerA, serializer);

			JContext di = withAnnotation(dateFormat, null, diAll);
			JNode pnode;
			if (ptype == Optional.class) {
				Type ogenericType = Reflect.getGenericTypeArguments(Optional.class, pgenericType, type).get(0);
				Class<Object> otype = (Class<Object>) Reflect.toClass(ogenericType);
				pnode = build(type, (Class<Object>) otype, Reflect.toType(genericType, ogenericType), deserializer,
						serializer, di);
				pnode.setDeserializer(new OptionalDeserializer(pnode.getDeserializer()));
				pnode.setSerializer((JsonbSerializerExtended) new OptionalSerializer(pnode.getSerializer()));

				pnode.setDefaultValue(Optional.empty());
			} else {
				pnode = build(type, (Class<Object>) ptype, Reflect.toType(genericType, pgenericType), deserializer,
						serializer, di);
				if (ptype == OptionalInt.class) {
					pnode.setDefaultValue(OptionalInt.empty());
				} else if (ptype == OptionalLong.class) {
					pnode.setDefaultValue(OptionalLong.empty());
				} else if (ptype == OptionalDouble.class) {
					pnode.setDefaultValue(OptionalDouble.empty());
				}
			}

			boolean nillable = false;
			if (di.isNillable()) {
				nillable = true;
			}
			String name = nameStrategy.translateName(prop.getKey());
			if (jsonProperty != null) {
				if (!jsonProperty.value().isEmpty()) {
					name = nameStrategy.translateName(jsonProperty.value());
				}
				nillable = jsonProperty.nillable();
			}

			if (setter != null) {
				String setterName = name;
				boolean nillableSetter = nillable;
				if (jsonPropertySetter != null) {
					if (!jsonPropertySetter.value().isEmpty()) {
						setterName = nameStrategy.translateName(jsonPropertySetter.value());
					}
					nillableSetter = jsonPropertySetter.nillable();
				}
				JProperty jproperty = new JProperty(getter, setter, setterName, pnode, nillableSetter);
				elementsMap.put(setterName, jproperty);
				if (pnode.getDefaultValue() != null) {
					optionalElements.add(jproperty);
				}
			}

			if (getter != null) {
				String getterName = name;
				boolean nillableGetter = nillable;
				if (jsonPropertyGetter != null) {
					if (!jsonPropertyGetter.value().isEmpty()) {
						getterName = nameStrategy.translateName(jsonPropertyGetter.value());
					}
					nillableGetter = jsonPropertyGetter.nillable();
				}
				elements.add(new JProperty(getter, setter, getterName, pnode, nillableGetter));
			}
		}

		if (!elements.isEmpty()) {
			if (orderStrategy != null) {
				Collections.sort(elements, orderStrategy);
			}
			JsonbPropertyOrder jsonPropertyOrder = type.getAnnotation(JsonbPropertyOrder.class);
			if (jsonPropertyOrder != null) {
				String[] values = jsonPropertyOrder.value();
				Map<String, Integer> orderMap = new HashMap<>();
				for (int i = 0; i < values.length; i++) {
					orderMap.put(values[i], i);
				}
				Collections.sort(elements, new Comparator<JProperty>() {

					@Override
					public int compare(JProperty o1, JProperty o2) {
						Integer n1 = orderMap.get(o1.getName());
						Integer n2 = orderMap.get(o2.getName());
						if (n1 != null && n2 != null) {
							return n1.compareTo(n2);
						} else if (n1 == null) {
							return 1;
						} else if (n2 == null) {
							return -1;
						}
						return 0;
					}

				});
			}

			node.setElementList(elements.toArray(new JProperty[elements.size()]));
		}

		if (!elementsMap.isEmpty()) {
			node.setElements(elementsMap);
			if (!optionalElements.isEmpty()) {
				node.setOptionalElements(optionalElements.toArray(new JProperty[optionalElements.size()]));
			}
		}
	}

	private JsonbDeserializerExtended<Object> deserializer(JsonbTypeDeserializer deserializerA,
			JsonbDeserializerExtended<Object> old) {
		JsonbDeserializerExtended<Object> deserializer = old;
		if (deserializerA != null) {
			deserializer = deserializers.get(deserializerA.value());
			if (deserializer == null) {
				JsonbDeserializer<Object> sr = Reflects.newInstance(deserializerA.value());
				deserializer = JsonbDeserializerAdapter.createDeserializer(sr);
				deserializers.put(deserializerA.value(), deserializer);
			}
		}
		return deserializer;
	}

	private JsonbSerializerExtended<Object> serializer(JsonbTypeSerializer serializerA,
			JsonbSerializerExtended<Object> old) {
		JsonbSerializerExtended<Object> serializer = old;
		if (serializerA != null) {
			serializer = serializers.get(serializerA.value());
			if (serializer == null) {
				JsonbSerializer<Object> sr = Reflects.newInstance(serializerA.value());
				serializer = JsonbSerializerAdapter.createSerializer(sr);
				serializers.put(serializerA.value(), serializer);
			}
		}
		return serializer;
	}

	private <T extends Annotation> T retrieve(AccessibleObject ao, T existing, Class<T> type) {
		if (existing != null) {
			return existing;
		} else {
			return ao.getAnnotation(type);
		}
	}

	private static List<Class<?>> getClassHierarchy(Class<?> type) {
		List<Class<?>> all = new ArrayList<>();
		Class<?> cl = type;
		while (cl != null && cl != Object.class) {
			all.add(cl);
			cl = cl.getSuperclass();
		}
		return all;
	}

}
