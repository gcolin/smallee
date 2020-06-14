package net.gcolin.di.atinject.producer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import javax.inject.Provider;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.SupplierProvider;
import net.gcolin.di.core.InjectException;

public class DisposableSupplierProvider extends SupplierProvider<Object> {

	private Method dispose;
	private Provider<Object> parent;
	
	public DisposableSupplierProvider(Class<Object> type, Supplier<Object> supplier, Class<? extends Annotation> scope,
			Environment env, Method dispose, Provider<Object> parent) {
		super(type, supplier, scope, env);
		this.dispose = dispose;
		this.parent = parent;
	}
	
	@Override
    public boolean hasDestroyMethods() {
      return true;
    }

    @Override
    public void destroyInstance(Object o) {
      try {
        dispose.invoke(parent.get(), o);
      } catch (IllegalAccessException | IllegalArgumentException
          | InvocationTargetException ex) {
        throw new InjectException(ex);
      }
    }

}
