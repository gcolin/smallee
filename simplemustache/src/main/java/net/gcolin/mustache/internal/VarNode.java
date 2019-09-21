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
import net.gcolin.mustache.FilterFunction;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class VarNode extends Node {

	private static final String[] TABLE = new String[255];
	private volatile Var var;

	static {
		TABLE[34] = "&quot;";
		TABLE[38] = "&amp;";
		TABLE[60] = "&lt;";
		TABLE[62] = "&gt;";
		TABLE[160] = "&nbsp;";
		TABLE[161] = "&iexcl;";
		TABLE[162] = "&cent;";
		TABLE[163] = "&pound;";
		TABLE[164] = "&curren;";
		TABLE[165] = "&yen;";
		TABLE[166] = "&brvbar;";
		TABLE[167] = "&sect;";
		TABLE[168] = "&uml;";
		TABLE[169] = "&copy;";
		TABLE[170] = "&ordf;";
		TABLE[172] = "&not;";
		TABLE[173] = "&shy;";
		TABLE[174] = "&reg;";
		TABLE[175] = "&macr;";
		TABLE[176] = "&deg;";
		TABLE[177] = "&plusmn;";
		TABLE[178] = "&sup2;";
		TABLE[179] = "&sup3;";
		TABLE[180] = "&acute;";
		TABLE[181] = "&micro;";
		TABLE[182] = "&para;";
		TABLE[183] = "&middot;";
		TABLE[184] = "&cedil;";
		TABLE[185] = "&sup1;";
		TABLE[186] = "&ordm;";
		TABLE[187] = "&raquo;";
		TABLE[188] = "&frac14;";
		TABLE[189] = "&frac12;";
		TABLE[190] = "&frac34;";
		TABLE[191] = "&iquest;";
		TABLE[192] = "&Agrave;";
		TABLE[193] = "&Aacute;";
		TABLE[194] = "&Acirc;";
		TABLE[195] = "&Atilde;";
		TABLE[196] = "&Auml;";
		TABLE[197] = "&Aring;";
		TABLE[198] = "&AElig;";
		TABLE[199] = "&Ccedil;";
		TABLE[200] = "&Egrave;";
		TABLE[201] = "&Eacute;";
		TABLE[202] = "&Ecirc;";
		TABLE[203] = "&Euml;";
		TABLE[204] = "&Igrave;";
		TABLE[205] = "&Iacute;";
		TABLE[206] = "&Icirc;";
		TABLE[207] = "&Iuml;";
		TABLE[208] = "&ETH;";
		TABLE[209] = "&Ntilde;";
		TABLE[210] = "&Ograve;";
		TABLE[211] = "&Oacute;";
		TABLE[212] = "&Ocirc;";
		TABLE[213] = "&Otilde;";
		TABLE[214] = "&Ouml;";
		TABLE[215] = "&times;";
		TABLE[216] = "&Oslash;";
		TABLE[217] = "&Ugrave;";
		TABLE[218] = "&Uacute;";
		TABLE[219] = "&Ucirc;";
		TABLE[220] = "&Uuml;";
		TABLE[221] = "&Yacute;";
		TABLE[222] = "&THORN;";
		TABLE[223] = "&szlig;";
		TABLE[224] = "&agrave;";
		TABLE[225] = "&aacute;";
		TABLE[226] = "&acirc;";
		TABLE[227] = "&atilde;";
		TABLE[228] = "&auml;";
		TABLE[229] = "&aring;";
		TABLE[230] = "&aelig;";
		TABLE[231] = "&ccedil;";
		TABLE[232] = "&egrave;";
		TABLE[233] = "&eacute;";
		TABLE[234] = "&ecirc;";
		TABLE[235] = "&euml;";
		TABLE[236] = "&igrave;";
		TABLE[237] = "&iacute;";
		TABLE[238] = "&icirc;";
		TABLE[239] = "&iuml;";
		TABLE[240] = "&eth;";
		TABLE[241] = "&ntilde;";
		TABLE[242] = "&ograve;";
		TABLE[243] = "&oacute;";
		TABLE[244] = "&ocirc;";
		TABLE[245] = "&otilde;";
		TABLE[246] = "&ouml;";
		TABLE[247] = "&divide;";
		TABLE[248] = "&oslash;";
		TABLE[249] = "&ugrave;";
		TABLE[250] = "&uacute;";
		TABLE[251] = "&ucirc;";
		TABLE[252] = "&uuml;";
		TABLE[253] = "&yacute;";
		TABLE[254] = "&thorn;";
	}

	private String name;
	private String[] filters;

	public VarNode(String name, String[] filters) {
		this.name = name;
		this.filters = filters;
	}

	public String getName() {
		return name;
	}

	@Override
	public void write(Context ctx) {
		Object obj = get(ctx);
		if (obj != null) {
			Var var = this.var;
			if (var == null || !var.isCompatible(obj)) {
				var = createVar(obj, true);
				this.var = var;
			}
			var.execute(ctx, obj);
		}
	}

	protected void write(Context ctx, String str) {
		boolean esc = false;
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (esc) {
				String val = ch > 255 ? null : TABLE[ch];
				if (val == null) {
					ctx.write(ch);
				} else {
					ctx.write(val);
				}
			} else if (ch < 256 && TABLE[ch] != null) {
				esc = true;
				ctx.write(str.substring(0, i));
				i--;
			}
		}

		if (!esc) {
			ctx.write(str);
		}
	}

	private Var createVar(Object obj, boolean filter) {
		Var var;
		if (filter && filters != null && filters.length > 0) {
			var = new FilterVar(obj.getClass(), filters[filters.length - 1]);
			for (int i = filters.length - 2; i >= 0; i--) {
				var = new FilterChainVar(filters[i], var);
			}
		} else if (obj.getClass() == String.class) {
			var = new StringVar();
		} else if (obj instanceof WrapperFunction) {
			var = new WrapperVar(obj.getClass());
		} else {
			var = new Var(obj.getClass());
		}
		return var;
	}

	private class Var {

		private Class<?> type;

		public Var(Class<?> type) {
			this.type = type;
		}

		public boolean isCompatible(Object obj) {
			return type == obj.getClass();
		}

		public void execute(Context ctx, Object obj) {
			write(ctx, String.valueOf(obj));
		}

	}

	private class FilterVar extends Var {

		private String filter;
		private volatile Var delegate;

		public FilterVar(Class<?> type, String filter) {
			super(type);
			this.filter = filter;
		}

		@Override
		public void execute(Context ctx, Object obj) {
			Object filterFun = ctx.get(filter);
			if (filterFun == null || !(filterFun instanceof FilterFunction)) {
				throw new MustacheException("bad filter " + filter + ". filter must be a FilterFunction");
			}
			Object result = ((FilterFunction) filterFun).apply(obj);
			if (result != null) {
				Var delegate = this.delegate;
				if (delegate == null || !delegate.isCompatible(result)) {
					delegate = createVar(result, false);
					this.delegate = delegate;
				}
				delegate.execute(ctx, result);
			}
		}

	}

	private class FilterChainVar extends Var {

		private String filter;
		private Var delegate;

		public FilterChainVar(String filter, Var delegate) {
			super(delegate.type);
			this.filter = filter;
			this.delegate = delegate;
		}

		@Override
		public void execute(Context ctx, Object obj) {
			Object filterFun = ctx.get(filter);
			if (filterFun == null || !(filterFun instanceof FilterFunction)) {
				throw new MustacheException("bad filter " + filter + ". filter must be a FilterFunction");
			}
			Object result = ((FilterFunction) filterFun).apply(obj);
			if (result != null) {
				delegate.execute(ctx, result);
			}
		}

	}

	private class StringVar extends Var {

		public StringVar() {
			super(String.class);
		}

		@Override
		public void execute(Context ctx, Object obj) {
			write(ctx, (String) obj);
		}

	}

	private class WrapperVar extends Var {

		private volatile BiFunctionWrapper wrapper;

		public WrapperVar(Class<?> type) {
			super(type);
		}

		@Override
		public void execute(Context ctx, Object obj) {
			BiFunctionWrapper wrapper = this.wrapper;
			if (wrapper == null || obj != wrapper.get()) {
				this.wrapper = wrapper = new BiFunctionWrapper(null, (WrapperFunction) obj, new CompileContext());
			}
			obj = wrapper.get(ctx, obj);

			if (obj != null) {
				write(ctx, String.valueOf(obj));
			}
		}

	}

	protected Object get(Context ctx) {
		return ctx.scope();
	}

	public boolean isWritable(Object obj) {
		return obj != null && (!obj.getClass().isArray() || Array.getLength(obj) > 0);
	}

	@Override
	public String toString() {
		return "{{" + name + "}}";
	}

}
