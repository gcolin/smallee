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

package net.gcolin.rest.test;

import net.gcolin.common.io.Io;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

/**
 * Test login with ssl.
 * 
 * @author Gael COLIN
 *
 */
public class Test {

  /**
   * Main method.
   * 
   * @param args args
   * @throws Exception if a problem occur
   */
  public static void main(String[] args) throws Exception {

    KeyStore trust = KeyStore.getInstance("jks");
    KeyStore store = KeyStore.getInstance("jks");
    trust.load(new FileInputStream("/home/gcolin/git/netgiv/config/ssl/trustStore"),
        "netgiv".toCharArray());
    store.load(new FileInputStream("/home/gcolin/git/netgiv/config/ssl/keystore"),
        "netgiv".toCharArray());

    try (InputStream inStream =
        new FileInputStream("/home/gcolin/git/netgiv/config/ssl/server.crt")) {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      Certificate cert = cf.generateCertificate(inStream);
      trust.setCertificateEntry("gcolin", cert);
      store.setCertificateEntry("gcolin", cert);
    }

    Client client = ClientBuilder.newBuilder().hostnameVerifier(new HostnameVerifier() {

      @Override
      public boolean verify(String arg0, SSLSession arg1) {
        return true;
      }
    }).trustStore(trust).keyStore(store, "netgiv").build();

    Response resp =
        client.target("https://netgiv.local/api/auth/state").request().buildGet().invoke();
    try {
      System.out.println(Io.toString((InputStream) resp.getEntity()));
    } finally {
      resp.close();
    }

    resp = client.target("https://netgiv.local/api/auth?login=admin&password=sourdine141").request()
        .buildGet().invoke();
    try {
      System.out.println(Io.toString((InputStream) resp.getEntity()));
    } finally {
      resp.close();
    }

    resp = client.target("https://netgiv.local/api/auth/state").request().buildGet().invoke();
    try {
      System.out.println(Io.toString((InputStream) resp.getEntity()));
    } finally {
      resp.close();
    }

  }

}
