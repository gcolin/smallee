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

package net.gcolin.di.atinject.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TransactionRequiredException;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.transaction.TransactionalException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the state of the transaction.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class PersistenceContexts {

  public static final Logger LOG = LoggerFactory.getLogger("net.gcolin.di.cdi.jpa");
  private static final ThreadLocal<TransactionState> states = new ThreadLocal<>();

  static EntityManager getEntityManager(EntityManagerFactory emf) {
    TransactionState state = states.get();
    if (state == null) {
      return emf.createEntityManager();
    } else {
      return state.getEm(emf);
    }
  }

  static boolean push(Transactional.TxType tx) {
    return push(tx, false);
  }

  static boolean push(Transactional.TxType tx, boolean force) {
    TransactionState current = states.get();
    if (!force && current != null && current.type() == tx) {
      return false;
    }
    switch (tx) {
      case MANDATORY:
        if (current == null) {
          throw new TransactionRequiredException();
        }
        break;
      case NEVER:
        if (current != null) {
          throw new TransactionalException("transaction not supported", null);
        }
        break;
      case NOT_SUPPORTED:
    	  states.set(new TransactionStateNotSupported(states.get()));
        break;
      case REQUIRED:
    	  states.set(new TransactionStateRequiered(states.get()));
        break;
      case REQUIRES_NEW:
    	  states.set(new TransactionStateRequieredNew(states.get()));
        break;
      default:
    }
    return true;
  }

  static void pop(Transactional.TxType tx) {
    switch (tx) {
      case NOT_SUPPORTED:
      case REQUIRED:
      case REQUIRES_NEW:
        TransactionState state = states.get();
        state.close();
        states.set(state.getPrec());
        break;
      default:
        break;
    }
  }

  public static void rollback(Exception ex) {
    LOG.debug("error in transaction", ex);
    states.get().rollback();
  }

  public interface TransactionState {
    EntityManager getEm(EntityManagerFactory emf);

    boolean hasEm();

    void close();

    void rollback();

    TransactionState getPrec();

    Transactional.TxType type();
  }

  private static class TransactionStateRequiered implements TransactionState {
    Map<EntityManagerFactory, EntityManager> em;
    TransactionState prec;

    public TransactionStateRequiered(TransactionState prec) {
      this.prec = prec;
    }

    public EntityManager getEm(EntityManagerFactory emf) {
      if (em == null) {
        em = new HashMap<>();
      }
      EntityManager manager = em.get(emf);
      if (manager == null) {
        if (prec != null) {
          em.put(emf, manager = prec.getEm(emf));
        } else {
          LOG.debug("start transaction");
          manager = emf.createEntityManager();
          manager.getTransaction().begin();
          em.put(emf, manager);
        }
      }
      return manager;
    }

    public void close() {
      if (em != null && prec == null) {
        LOG.debug("end transaction");
        try {
          for (EntityManager e : em.values()) {
            e.getTransaction().commit();
          }
        } finally {
          for (EntityManager e : em.values()) {
            e.close();
          }
          em = null;
        }
      }
    }

    @Override
    public void rollback() {
      if (prec == null) {
        if (em != null) {
          LOG.debug("rollback transaction");
          try {
            for (EntityManager e : em.values()) {
              e.getTransaction().rollback();
            }
          } finally {
            for (EntityManager e : em.values()) {
              e.close();
            }
            em = null;
          }
        }
      } else {
        prec.rollback();
      }
    }

    @Override
    public TransactionState getPrec() {
      return prec;
    }

    @Override
    public boolean hasEm() {
      return em != null;
    }

    @Override
    public TxType type() {
      return Transactional.TxType.REQUIRED;
    }
  }

  private static class TransactionStateNotSupported implements TransactionState {
    Map<EntityManagerFactory, EntityManager> em;
    TransactionState prec;

    public TransactionStateNotSupported(TransactionState prec) {
      this.prec = prec;
    }

    public EntityManager getEm(EntityManagerFactory emf) {
      if (em == null) {
        em = new HashMap<>();
      }
      EntityManager manager = em.get(emf);
      if (manager == null) {
        LOG.debug("start entitymanager without transaction");
        manager = emf.createEntityManager();
        em.put(emf, manager);
      }
      return manager;
    }

    public void close() {
      if (em != null) {
        LOG.debug("close entitymanager without transaction");
        try {
          for (EntityManager e : em.values()) {
            e.close();
          }
        } finally {
          em = null;
        }
      }
    }

    @Override
    public void rollback() {
      LOG.debug("cannot rollback entitymanager without transaction");
    }

    @Override
    public TransactionState getPrec() {
      return prec;
    }

    @Override
    public boolean hasEm() {
      return em != null;
    }

    @Override
    public TxType type() {
      return Transactional.TxType.NOT_SUPPORTED;
    }
  }

  private static class TransactionStateRequieredNew implements TransactionState {
    Map<EntityManagerFactory, EntityManager> em;
    TransactionState prec;

    public TransactionStateRequieredNew(TransactionState prec) {
      this.prec = prec;
    }

    public EntityManager getEm(EntityManagerFactory emf) {
      if (em == null) {
        em = new HashMap<>();
      }
      EntityManager manager = em.get(emf);
      if (manager == null) {
        LOG.debug("start transaction rn");
        manager = emf.createEntityManager();
        manager.getTransaction().begin();
        em.put(emf, manager);
      }
      return manager;
    }

    public void close() {
      if (em != null) {
        LOG.debug("end transaction rn");
        try {
          for (EntityManager e : em.values()) {
            e.getTransaction().commit();
          }
        } finally {
          for (EntityManager e : em.values()) {
            e.close();
          }
          em = null;
        }
      }
    }

    @Override
    public TransactionState getPrec() {
      return prec;
    }

    @Override
    public void rollback() {
      if (em != null) {
        try {
          for (EntityManager e : em.values()) {
            e.getTransaction().rollback();
          }
        } finally {
          em = null;
        }
        LOG.debug("rollback transaction rn");
      }
    }

    @Override
    public boolean hasEm() {
      return em != null;
    }

    @Override
    public TxType type() {
      return Transactional.TxType.REQUIRES_NEW;
    }
  }

}
