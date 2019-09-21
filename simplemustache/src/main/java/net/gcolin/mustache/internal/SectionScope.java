package net.gcolin.mustache.internal;

public class SectionScope implements Scope {

	private Scope delegate;
	private boolean first;
	private int index;
	private boolean last;

	public SectionScope(Scope delegate, int index, boolean first, boolean last) {
		this.delegate = delegate;
		this.first = first;
		this.index = index;
		this.last = last;
	}

	@Override
	public Object get() {
		return delegate.get();
	}

	@Override
	public Object get(String name) {
		if (name.equals("-index")) {
			return index;
		}
		if (name.equals("-last")) {
			return last;
		}
		if (name.equals("-first")) {
			return first;
		}
		return delegate.get(name);
	}

	@Override
	public boolean has(String name) {
		// TODO Auto-generated method stub
		return name.equals("-index") || name.equals("-last") || name.equals("-first") || delegate.has(name);
	}

}
