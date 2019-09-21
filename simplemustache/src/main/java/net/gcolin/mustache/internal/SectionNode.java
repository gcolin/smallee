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

package net.gcolin.mustache.internal;

import net.gcolin.mustache.WrapperFunction;
import net.gcolin.mustache.MustacheException;
import net.gcolin.mustache.internal.Compiler.CompileContext;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.gcolin.mustache.FilterFunction;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class SectionNode extends Node {

	private String name;
	private Node body;
	private boolean not;
	private CompileContext context;
	private volatile Section section;
	private String[] filters;

	/**
	 * Create a section node.
	 * 
	 * @param name    name of section
	 * @param body    inner of section
	 * @param not     true if it is a not section
	 * @param context compile context
	 * @param filters filters
	 */
	public SectionNode(String name, Node body, boolean not, CompileContext context, String[] filters) {
		this.name = name;
		this.body = body;
		this.not = not;
		this.context = context;
		this.filters = filters;
	}

	public String getName() {
		return name;
	}

	public Node getBody() {
		return body;
	}

	@Override
	public void write(Context ctx) {
		execute(ctx, ctx.scope());
	}

	protected void execute(Context ctx, Object obj) {
		if (obj == null) {
			if (not && body != null) {
				body.write(ctx);
			}
			return;
		}
		Section section = this.section;
		if (section == null || !section.isCompatible(obj)) {
			section = createSection(obj, true);
			this.section = section;
		}

		section.execute(ctx, obj);
	}

	protected Section createSection(Object obj, boolean filter) {
		Section section;
		if (filter && filters != null && filters.length > 0) {
			section = new FilterSection(obj.getClass(), this, filters[filters.length - 1]);
			for (int i = filters.length - 2; i >= 0; i--) {
				section = new FilterChainSection(filters[i], section);
			}
		} else if (obj instanceof Boolean) {
			if (body != null) {
				if (not) {
					section = new BooleanNotSection(obj.getClass(), body);
				} else {
					section = new BooleanSection(obj.getClass(), body);
				}
			} else {
				section = new EmptySection(obj.getClass());
			}
		} else if (obj instanceof CharSequence) {
			if (body != null) {
				if (not) {
					section = new StringNotSection(obj.getClass(), body);
				} else {
					section = new StringSection(obj.getClass(), body);
				}
			} else {
				section = new EmptySection(obj.getClass());
			}
		} else if (obj instanceof Number) {
			if (body != null) {
				if (not) {
					section = new NumberNotSection(obj.getClass(), body);
				} else {
					section = new NumberSection(obj.getClass(), body);
				}
			} else {
				section = new EmptySection(obj.getClass());
			}
		} else if (obj instanceof Collection) {
			if (body != null) {
				if (not) {
					section = new CollectionNotSection(obj.getClass(), body);
				} else if (obj instanceof List) {
					section = new ListSection(obj.getClass(), body);
				} else if (obj.getClass().isArray()) {
					section = new ArraySection(obj.getClass(), body);
				} else if (obj instanceof Iterator) {
					section = new IteratorSection(obj.getClass(), body);
				} else {
					section = new IterableSection(obj.getClass(), body);
				}
			} else {
				section = new EmptySection(obj.getClass());
			}
		} else if (obj instanceof WrapperFunction) {
			if (not) {
				section = new EmptySection(obj.getClass());
			} else {
				section = new WrapperSection(obj.getClass(), body, context);
			}
		} else {
			if (not || body == null) {
				section = new EmptySection(obj.getClass());
			} else {
				section = new SimpleSection(obj.getClass(), body);
			}
		}
		return section;
	}

	private abstract static class Section {
		Class<?> type;

		public Section(Class<?> type) {
			this.type = type;
		}

		public boolean isCompatible(Object obj) {
			return type == obj.getClass();
		}

		public abstract void execute(Context ctx, Object obj);
	}

	private static class FilterSection extends Section {

		private SectionNode node;
		private String filter;
		private volatile Section delegate;

		public FilterSection(Class<?> type, SectionNode node, String filter) {
			super(type);
			this.node = node;
			this.filter = filter;
		}

		@Override
		public void execute(Context ctx, Object obj) {
			Object filterFun = ctx.get(filter);
			if (filterFun == null || !(filterFun instanceof FilterFunction)) {
				throw new MustacheException("bad filter " + filter + ". filter must be a FilterFunction");
			}
			Object val = ((FilterFunction) filterFun).apply(obj);
			if (val != null) {
				Section delegate = this.delegate;
				if (delegate == null || !delegate.isCompatible(val)) {
					delegate = node.createSection(val, false);
					this.delegate = delegate;
				}
				delegate.execute(ctx, val);
			}
		}
	}

	private static class FilterChainSection extends Section {

		private Section section;
		private String filter;

		public FilterChainSection(String filter, Section section) {
			super(section.type);
			this.filter = filter;
			this.section = section;
		}

		@Override
		public void execute(Context ctx, Object obj) {
			Object filterFun = ctx.get(filter);
			if (filterFun == null || !(filterFun instanceof FilterFunction)) {
				throw new MustacheException("bad filter " + filter + ". filter must be a function");
			}
			Object val = ((FilterFunction) filterFun).apply(obj);
			if (val != null) {
				section.execute(ctx, val);
			}
		}
	}

	private abstract static class CondSection extends Section {

		private Node body;

		public CondSection(Class<?> type, Node body) {
			super(type);
			this.body = body;
		}

		public abstract boolean is(Object obj);

		@Override
		public void execute(Context ctx, Object obj) {
			if (is(obj)) {
				ctx.inScope(obj);
				body.write(ctx);
				ctx.outScope();
			}
		}
	}

	private static class WrapperSection extends Section {

		private volatile BiFunctionWrapper wrapper;
		private Node body;
		private CompileContext context;

		public WrapperSection(Class<?> type, Node body, CompileContext context) {
			super(type);
			this.body = body;
			this.context = context;
		}

		@Override
		public void execute(Context ctx, Object obj) {
			BiFunctionWrapper wrapper = this.wrapper;
			if (wrapper == null || obj != wrapper.get()) {
				this.wrapper = wrapper = new BiFunctionWrapper(body, (WrapperFunction) obj, context);
			}
			Object val = wrapper.get(ctx, obj);
			if (val != null) {
				ctx.write((String) val);
			}
		}

	}

	private static class BooleanNotSection extends CondSection {

		public BooleanNotSection(Class<?> type, Node body) {
			super(type, body);
		}

		@Override
		public boolean is(Object obj) {
			return !((Boolean) obj);
		}

	}

	private static class BooleanSection extends CondSection {

		public BooleanSection(Class<?> type, Node body) {
			super(type, body);
		}

		@Override
		public boolean is(Object obj) {
			return ((Boolean) obj);
		}

	}

	private static class StringNotSection extends CondSection {

		public StringNotSection(Class<?> type, Node body) {
			super(type, body);
		}

		@Override
		public boolean is(Object obj) {
			return ((CharSequence) obj).length() == 0;
		}

	}

	private static class StringSection extends CondSection {

		public StringSection(Class<?> type, Node body) {
			super(type, body);
		}

		@Override
		public boolean is(Object obj) {
			return ((CharSequence) obj).length() > 0;
		}

	}

	private static class NumberNotSection extends CondSection {

		public NumberNotSection(Class<?> type, Node body) {
			super(type, body);
		}

		@Override
		public boolean is(Object obj) {
			return ((Number) obj).intValue() == 0;
		}

	}

	private static class NumberSection extends CondSection {

		public NumberSection(Class<?> type, Node body) {
			super(type, body);
		}

		@Override
		public boolean is(Object obj) {
			return ((Number) obj).intValue() > 0;
		}

	}

	private static class CollectionNotSection extends CondSection {

		public CollectionNotSection(Class<?> type, Node body) {
			super(type, body);
		}

		@Override
		public boolean is(Object obj) {
			return ((Collection<?>) obj).isEmpty();
		}

	}

	private static class EmptySection extends Section {

		public EmptySection(Class<?> type) {
			super(type);
		}

		@Override
		public void execute(Context ctx, Object obj) {
		}
	}

	private static class SimpleSection extends Section {

		private Node body;

		public SimpleSection(Class<?> type, Node body) {
			super(type);
			this.body = body;
		}

		@Override
		public void execute(Context ctx, Object obj) {
			ctx.inScope(obj);
			body.write(ctx);
			ctx.outScope();
		}
	}

	private static class ArraySection extends Section {

		private Node body;

		public ArraySection(Class<?> type, Node body) {
			super(type);
			this.body = body;
		}

		@Override
		public void execute(Context ctx, Object obj) {
			for (int i = 0, len = Array.getLength(obj); i < len; i++) {
				Object so = Array.get(obj, i);
				if (so != null) {
					ctx.inScope(so);
					body.write(ctx);
					ctx.outScope();
				}
			}
		}
	}

	private static class ListSection extends Section {

		private Node body;

		public ListSection(Class<?> type, Node body) {
			super(type);
			this.body = body;
		}

		@Override
		public void execute(Context ctx, Object obj) {
			List<?> list = (List<?>) obj;
			for (int i = 0; i < list.size(); i++) {
				Object so = list.get(i);
				if (so != null) {
					ctx.inScope(so, i, i == 0, i == list.size() - 1);
					body.write(ctx);
					ctx.outScope();
				}
			}
		}
	}

	private static class IteratorSection extends Section {

		private Node body;

		public IteratorSection(Class<?> type, Node body) {
			super(type);
			this.body = body;
		}

		@Override
		public void execute(Context ctx, Object obj) {
			Iterator<?> it = (Iterator<?>) obj;
			while (it.hasNext()) {
				Object so = it.next();
				if (so != null) {
					ctx.inScope(so);
					body.write(ctx);
					ctx.outScope();
				}
			}
		}
	}

	private static class IterableSection extends IteratorSection {

		public IterableSection(Class<?> type, Node body) {
			super(type, body);
		}

		@Override
		public void execute(Context ctx, Object obj) {
			super.execute(ctx, ((Iterable<?>) obj).iterator());
		}
	}

	@Override
	public String toString() {
		return "{{" + (not ? "^" : "#") + name + "}}" + (body == null ? "" : body.toString()) + "{{/" + name + "}}";
	}

}
