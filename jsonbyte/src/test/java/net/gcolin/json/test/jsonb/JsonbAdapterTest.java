package net.gcolin.json.test.jsonb;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.adapter.JsonbAdapter;
import javax.json.bind.annotation.JsonbTypeAdapter;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.common.lang.Locales;

public class JsonbAdapterTest {

  public static class LocaleAdapter implements JsonbAdapter<Locale, String> {

    @Override
    public String adaptToJson(Locale obj) throws Exception {
      return obj.toString();
    }

    @Override
    public Locale adaptFromJson(String obj) throws Exception {
      return Locales.fromString(obj);
    }

  }

  public static class LocaleAdapter2 implements JsonbAdapter<Locale, Integer> {

    private Map<Integer, Locale> map = new HashMap<>();
    private Map<Locale, Integer> remap = new HashMap<>();

    {
      map.put(1, Locale.CANADA);
      map.put(2, Locale.CHINA);

      remap.put(Locale.CANADA, 1);
      remap.put(Locale.CHINA, 2);
    }

    @Override
    public Integer adaptToJson(Locale obj) throws Exception {
      return remap.get(obj);
    }

    @Override
    public Locale adaptFromJson(Integer obj) throws Exception {
      return map.get(obj);
    }

  }

  public static class A {

    @JsonbTypeAdapter(LocaleAdapter.class)
    private Locale locale;

    public Locale getLocale() {
      return locale;
    }

    public void setLocale(Locale locale) {
      this.locale = locale;
    }

  }

  public static class B {

    @JsonbTypeAdapter(LocaleAdapter2.class)
    private Locale locale;

    public Locale getLocale() {
      return locale;
    }

    public void setLocale(Locale locale) {
      this.locale = locale;
    }

  }

  @Test
  public void testLocale() {
    Jsonb jsonb = JsonbBuilder.create();
    StringWriter writer = new StringWriter();
    A a = new A();
    a.locale = Locale.UK;
    jsonb.toJson(a, writer);
    String str = writer.toString();
    Assert.assertEquals("{\"locale\":\"en_GB\"}", str);
    A a2 = jsonb.fromJson(str, A.class);
    Assert.assertEquals(Locale.UK, a2.getLocale());
  }
  
  @Test
  public void testLocaleInteger() {
    Jsonb jsonb = JsonbBuilder.create();
    StringWriter writer = new StringWriter();
    B a = new B();
    a.locale = Locale.CANADA;
    jsonb.toJson(a, writer);
    String str = writer.toString();
    Assert.assertEquals("{\"locale\":1}", str);
    B a2 = jsonb.fromJson(str, B.class);
    Assert.assertEquals(Locale.CANADA, a2.getLocale());
  }

}
