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

package net.gcolin.validation.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.lang.annotation.ElementType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.TraversableResolver;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.groups.ConvertGroup;
import javax.validation.metadata.Scope;

/**
 * A big test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ValidationTest {


  static BigInteger S = new BigInteger("99");
  static Calendar FUTURE1 = Calendar.getInstance();
  static Calendar PAST1 = Calendar.getInstance();

  static {
    FUTURE1.setTime(new Date(System.currentTimeMillis() + 10000));
    PAST1.setTime(new Date(System.currentTimeMillis() - 10000));
  }

  interface Group1 {}

  interface Group2 {}

  public static class A {

    @Valid
    @NotNull
    public A() {}

    public A(@Size(min = 1, max = 10) String name) {

    }

    @AssertTrue(groups = Group1.class)
    public boolean boolGroup1;

    @AssertTrue
    public boolean bool;

    @Digits(fraction = 1, integer = 1)
    public BigDecimal digits1 = new BigDecimal("1.0");

    @DecimalMax("2")
    byte deciamlMax1 = 1;

    @DecimalMax("2")
    Integer deciamlMax2 = 1;

    @DecimalMax("2")
    long deciamlMax3 = 1;

    @DecimalMax("2")
    short deciamlMax4 = 1;

    @DecimalMax("2")
    BigInteger deciamlMax5 = BigInteger.ONE;

    @DecimalMax("2")
    String deciamlMax6 = "1";

    @DecimalMax("2")
    Byte deciamlMax13;

    @DecimalMax("2")
    Integer deciamlMax8;

    @DecimalMax("2")
    Long deciamlMax9;

    @DecimalMax("2")
    Short deciamlMax10;

    @DecimalMax("2")
    BigInteger deciamlMax11;

    @DecimalMax("2")
    String deciamlMax12;

    @DecimalMax("2")
    BigDecimal deciamlMax14;

    @DecimalMax("2")
    BigDecimal deciamlMax7 = BigDecimal.ONE;

    @DecimalMin("0")
    byte deciamlMin1 = 1;

    @DecimalMin("0")
    Integer deciamlMin2 = 1;

    @DecimalMin("0")
    long deciamlMin3 = 1;

    @DecimalMin("0")
    short deciamlMin4 = 1;

    @DecimalMin("0")
    BigInteger deciamlMin5 = BigInteger.ONE;

    @DecimalMin("0")
    String deciamlMin6 = "1";

    @DecimalMin("0")
    BigDecimal deciamlMin7 = BigDecimal.ONE;

    @Max(2)
    int max = 1;

    @Max(2)
    Integer max2 = 1;

    @Min(0)
    int min = 1;

    @Min(0)
    Integer min2 = 1;

    @Future
    Calendar future1 = FUTURE1;

    @Future
    Date future2 = FUTURE1.getTime();

    @Past
    Calendar past1 = PAST1;

    @Past
    Date past2 = PAST1.getTime();

    @Pattern(regexp = ".+")
    String pattern = "1";

    @Digits(integer = 2, fraction = 2)
    private BigInteger bigint = S;

    @Size(min = 1, max = 10)
    private String[] array = {"1"};

    @Size(min = 1, max = 10)
    private boolean[] array2 = {true};

    @Size(min = 1, max = 10)
    private byte[] array3 = {1};

    @Size(min = 1, max = 10)
    private char[] array4 = {' '};

    @Size(min = 1, max = 10)
    private double[] array5 = {1};

    @Size(min = 1, max = 10)
    private Collection<String> array6 = Arrays.asList("");

    @Size(min = 1, max = 10)
    private float[] array7 = {1};

    @Size(min = 1, max = 10)
    private int[] array8 = {1};

    @Size(min = 1, max = 10)
    private long[] array9 = {1};

    @Size(min = 1, max = 10)
    private Map<String, String> array10 = new HashMap<>();

    {
      array10.put("1", "");
    }

    @AssertFalse
    boolean af;

    @Null
    String string;

    @Size(min = 1, max = 10)
    private short[] array11 = {1};

    @AssertTrue
    public boolean mbool() {
      return false;
    }

    @AssertTrue
    public boolean getBool2() {
      return true;
    }

    public boolean check(@NotNull String val) {
      return val.length() > 0;
    }
  }

  public static class B {

    public B() {}

    public B(@Valid A aa) {}

    public void check(@Valid A aa) {}

    @Valid
    public A get() {
      return new A();
    }

    @Valid
    @ConvertGroup.List({@ConvertGroup(from = Group2.class, to = Group1.class)})
    A aa = new A();
  }

  public static class C {

    @Valid
    B bb;
  }

  public static class D {

    @Valid
    B getB() {
      return new B();
    }
  }

  Validator validator;
  ValidatorFactory factory;

  @Before
  public void before() {
    factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @After
  public void after() {
    factory.close();
  }

  @org.junit.Test
  public void testSimple() {
    Assert.assertTrue(validator.getConstraintsForClass(A.class).findConstraints().hasConstraints());
    Assert.assertTrue(validator.getConstraintsForClass(A.class).findConstraints()
        .lookingAt(Scope.LOCAL_ELEMENT).hasConstraints());

    Set<ConstraintViolation<A>> violations = validator.validate(new A());
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<A> cv = violations.iterator().next();
    Assert.assertNotNull(cv.getMessage());
    Assert.assertEquals("bool", cv.getPropertyPath().toString());
    Assert.assertEquals(AssertTrue.class,
        cv.getConstraintDescriptor().getAnnotation().annotationType());

    A aa = new A();
    aa.array11 = null;
    aa.array = null;
    aa.array10 = null;
    aa.array2 = null;
    aa.array3 = null;
    aa.array4 = null;
    aa.array5 = null;
    aa.array6 = null;
    aa.array7 = null;
    aa.array8 = null;
    aa.array9 = null;
    aa.pattern = null;
    aa.future1 = null;
    aa.future2 = null;
    aa.past1 = null;
    aa.past2 = null;
    aa.bool = true;
    aa.string = "hello";
    aa.min2 = null;
    aa.max2 = null;
    aa.digits1 = null;
    aa.bigint = null;
    aa.deciamlMax10 = null;
    aa.deciamlMax11 = null;
    aa.deciamlMax12 = null;
    aa.deciamlMax13 = null;
    aa.deciamlMax14 = null;
    aa.deciamlMax2 = null;
    aa.deciamlMax5 = null;
    aa.deciamlMax6 = null;
    aa.deciamlMax7 = null;
    aa.deciamlMax8 = null;
    aa.deciamlMax9 = null;
    aa.deciamlMin2 = null;
    aa.deciamlMin5 = null;
    aa.deciamlMin6 = null;
    aa.deciamlMin7 = null;

    violations = validator.validate(aa);
    Assert.assertEquals(1, violations.size());

  }

  @org.junit.Test
  public void testGroup() {
    Set<ConstraintViolation<A>> violations = validator.validate(new A(), Group1.class);
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<A> cv = violations.iterator().next();
    Assert.assertEquals("boolGroup1", cv.getPropertyPath().toString());
    Assert.assertEquals(AssertTrue.class,
        cv.getConstraintDescriptor().getAnnotation().annotationType());
  }

  @org.junit.Test
  public void testConvertGroup() {
    Set<ConstraintViolation<B>> violations = validator.validate(new B(), Group2.class);
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<B> cv = violations.iterator().next();
    Assert.assertEquals("aa.boolGroup1", cv.getPropertyPath().toString());
    Assert.assertEquals(AssertTrue.class,
        cv.getConstraintDescriptor().getAnnotation().annotationType());
  }

  @org.junit.Test
  public void testValidField() {
    Set<ConstraintViolation<B>> violations = validator.validate(new B());
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<B> cv = violations.iterator().next();
    Assert.assertEquals("aa.bool", cv.getPropertyPath().toString());
    Assert.assertEquals(AssertTrue.class,
        cv.getConstraintDescriptor().getAnnotation().annotationType());
  }

  @org.junit.Test
  public void testValidMethodParameter() throws NoSuchMethodException, SecurityException {
    Set<ConstraintViolation<B>> violations = validator.forExecutables().validateParameters(new B(),
        B.class.getMethod("check", A.class), new Object[] {new A()});
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<B> cv = violations.iterator().next();
    Assert.assertEquals("check.arg0.bool", cv.getPropertyPath().toString());
    Assert.assertEquals(AssertTrue.class,
        cv.getConstraintDescriptor().getAnnotation().annotationType());
  }

  @org.junit.Test
  public void testValidMethodReturnValue() throws NoSuchMethodException, SecurityException {
    Set<ConstraintViolation<B>> violations =
        validator.forExecutables().validateReturnValue(new B(), B.class.getMethod("get"), new A());
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<B> cv = violations.iterator().next();
    Assert.assertEquals("get().bool", cv.getPropertyPath().toString());
    Assert.assertEquals(AssertTrue.class,
        cv.getConstraintDescriptor().getAnnotation().annotationType());
  }

  @org.junit.Test
  public void testValidConstructorReturnValue() throws NoSuchMethodException, SecurityException {
    Set<ConstraintViolation<A>> violations = validator.forExecutables()
        .validateConstructorReturnValue(A.class.getConstructor(), new A());
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<A> cv = violations.iterator().next();
    Assert.assertEquals("new A().bool",
        cv.getPropertyPath().toString());
    Assert.assertEquals(AssertTrue.class,
        cv.getConstraintDescriptor().getAnnotation().annotationType());
  }

  @org.junit.Test
  public void testValidConstructorParameter() throws NoSuchMethodException, SecurityException {
    Set<ConstraintViolation<B>> violations = validator.forExecutables()
        .validateConstructorParameters(B.class.getConstructor(A.class), new Object[] {new A()});
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<B> cv = violations.iterator().next();
    Assert.assertEquals("new B.arg0.bool",
        cv.getPropertyPath().toString());
    Assert.assertEquals(AssertTrue.class,
        cv.getConstraintDescriptor().getAnnotation().annotationType());
  }

  @org.junit.Test
  public void testValidCascade() {
    Assert.assertEquals(0, validator.validate(new C()).size());
    Set<ConstraintViolation<D>> violations = validator.validate(new D());
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<D> cv = violations.iterator().next();
    Assert.assertEquals("b.aa.bool", cv.getPropertyPath().toString());
    Assert.assertEquals(AssertTrue.class,
        cv.getConstraintDescriptor().getAnnotation().annotationType());
  }

  @org.junit.Test
  public void testValidateProperty() {
    Set<ConstraintViolation<A>> violations = validator.validateProperty(new A(), "bool");
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<A> cv = violations.iterator().next();
    Assert.assertEquals("bool", cv.getPropertyPath().toString());
    Assert.assertEquals(AssertTrue.class,
        cv.getConstraintDescriptor().getAnnotation().annotationType());
  }

  @org.junit.Test
  public void testValidateValue() {
    Set<ConstraintViolation<A>> violations = validator.validateValue(A.class, "bool", false);
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<A> cv = violations.iterator().next();
    Assert.assertEquals("bool", cv.getPropertyPath().toString());
    Assert.assertEquals(AssertTrue.class,
        cv.getConstraintDescriptor().getAnnotation().annotationType());
    Assert.assertEquals(0, validator.validateValue(A.class, "bool", true).size());
  }

  @org.junit.Test
  public void testValidateParameters() throws NoSuchMethodException, SecurityException {
    Set<ConstraintViolation<A>> violations = validator.forExecutables().validateParameters(new A(),
        A.class.getMethod("check", String.class), new Object[] {null});
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<A> cv = violations.iterator().next();
    Assert.assertEquals("check[0]", cv.getPropertyPath().toString());
    Assert.assertEquals(NotNull.class,
        cv.getConstraintDescriptor().getAnnotation().annotationType());
    Assert.assertEquals(0, validator.forExecutables()
        .validateParameters(new A(), A.class.getMethod("check", String.class), new Object[] {""})
        .size());
  }

  @org.junit.Test
  public void testValidateReturnValue() throws NoSuchMethodException, SecurityException {
    Set<ConstraintViolation<A>> violations =
        validator.forExecutables().validateReturnValue(new A(), A.class.getMethod("mbool"), false);
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<A> cv = violations.iterator().next();
    Assert.assertEquals("mbool", cv.getPropertyPath().toString());
    Assert.assertEquals(AssertTrue.class,
        cv.getConstraintDescriptor().getAnnotation().annotationType());
    Assert.assertEquals(0, validator.forExecutables()
        .validateReturnValue(new A(), A.class.getMethod("mbool"), true).size());
  }

  @org.junit.Test
  public void testValidateConstructorParameters() throws NoSuchMethodException, SecurityException {
    Set<ConstraintViolation<A>> violations = validator.forExecutables()
        .validateConstructorParameters(A.class.getConstructor(String.class), new Object[] {""});
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<A> cv = violations.iterator().next();
    Assert.assertNotNull(cv.getMessage());
    Assert.assertEquals("new A[0]",
        cv.getPropertyPath().toString());
    Assert.assertEquals(Size.class, cv.getConstraintDescriptor().getAnnotation().annotationType());
    Assert.assertEquals(0, validator.forExecutables()
        .validateConstructorParameters(A.class.getConstructor(String.class), new Object[] {"12"})
        .size());
  }

  @org.junit.Test
  public void testValidateConstructorReturnValue() throws NoSuchMethodException, SecurityException {
    validator = factory.usingContext().traversableResolver(new TraversableResolver() {

      @Override
      public boolean isReachable(Object paramObject, Node paramNode, Class<?> paramClass,
          Path paramPath, ElementType paramElementType) {
        return false;
      }

      @Override
      public boolean isCascadable(Object paramObject, Node paramNode, Class<?> paramClass,
          Path paramPath, ElementType paramElementType) {
        return false;
      }
    }).getValidator();
    Set<ConstraintViolation<A>> violations =
        validator.forExecutables().validateConstructorReturnValue(A.class.getConstructor(), null);
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<A> cv = violations.iterator().next();
    Assert.assertEquals("new A",
        cv.getPropertyPath().toString());
    Assert.assertEquals(NotNull.class,
        cv.getConstraintDescriptor().getAnnotation().annotationType());
    Assert.assertEquals(0, validator.forExecutables()
        .validateConstructorReturnValue(A.class.getConstructor(), new A()).size());
  }

}
