package net.gcolin.common.test;

import java.lang.reflect.Type;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.common.reflect.Reflect;

public class ResolveTypesTest {
	
	interface Dao<T, K> {

		T find(K id);
	}
	
	interface SuperDao<T> extends Dao<T, Long> {

		T findMany();
	}

	
	static class DaoImpl<T> implements Dao<T, Long> {

		@Override
		public T find(Long id) {
			return null;
		}
		
	}
	
	static class SuperDaoImpl extends DaoImpl<String> implements SuperDao<String> {

		@Override
		public String findMany() {
			return null;
		}		
		
	}
	
	@Test
	public void test() {
		Map<Type, Type> types = Reflect.getResolveTypes(SuperDao.class, SuperDaoImpl.class).getValue();
		Assert.assertEquals(1, types.size());
		Assert.assertTrue(types.containsValue(String.class));
	}

}
