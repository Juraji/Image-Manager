package nl.juraji.imagemanager.model;

import nl.juraji.imagemanager.util.ExceptionUtils;
import nl.juraji.imagemanager.util.concurrent.AtomicObject;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Session;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public class Dao {
    private static final AtomicObject<EntityManagerFactory> EMF_REF = new AtomicObject<>();
    private static final String DATA_STORE_FILE = "./store.mv.db";

    public <T> List<T> get(Class<T> entityClass, Order... orderBy) {
        try (Session session = getSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<T> query = criteriaBuilder.createQuery(entityClass);

            if (orderBy.length > 0) {
                query.orderBy(orderBy);
            }

            query.select(query.from(entityClass));

            return session.createQuery(query).getResultList();
        }
    }

    public <T> T get(Class<T> entityClass, String id) {
        try (Session session = getSession()) {
            return session.get(entityClass, id);
        }
    }

    public <T> long count(Class<T> entityClass) {
        try (Session session = getSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

            Root<T> entityRoot = countQuery.from(entityClass);
            countQuery.select(criteriaBuilder.count(entityRoot));

            return session.createQuery(countQuery).getSingleResult();
        }
    }

    public <T> void save(T entity) {
        try (Session session = getSession()) {
            session.getTransaction().begin();
            final Object o = this.merge(entity, session);
            session.saveOrUpdate(o);
            session.flush();
            session.getTransaction().commit();
            ExceptionUtils.catchAll(() -> BeanUtils.copyProperties(entity, o));
        }
    }

    public <T> void save(Collection<T> entities) {
        try (Session session = getSession()) {
            session.getTransaction().begin();

            for (Object entity : entities) {
                final Object o = this.merge(entity, session);
                session.saveOrUpdate(o);
                ExceptionUtils.catchAll(() -> BeanUtils.copyProperties(entity, o));
            }
            session.flush();
            session.getTransaction().commit();
        }
    }

    public <T> void delete(T entity) {
        try (Session session = getSession()) {
            session.getTransaction().begin();
            final Object o = this.merge(entity, session);
            session.delete(o);
            session.flush();
            session.getTransaction().commit();
        }
    }

    public <T> void delete(Collection<T> entities) {
        try (Session session = getSession()) {
            session.getTransaction().begin();
            entities.stream()
                    .map(o -> this.merge(o, session))
                    .forEach(session::delete);
            session.flush();
            session.getTransaction().commit();
        }
    }

    public <T> void load(T entity, String property) {
        try (Session session = getSession()) {
            final Object merged = session.merge(entity);
            final Object value = PropertyUtils.getProperty(merged, property);
            PropertyUtils.setProperty(entity, property, value);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public <T> void load(Collection<T> entities, String property) {
        try (Session session = getSession()) {
            for (T entity : entities) {
                final Object merged = session.merge(entity);
                final Object value = PropertyUtils.getProperty(merged, property);
                PropertyUtils.setProperty(entity, property, value);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private Session getSession() {
        if (EMF_REF.isEmpty()) {
            EMF_REF.set(Dao::initEMF);
        }

        return EMF_REF.get().createEntityManager()
                .unwrap(Session.class)
                .getSession();
    }

    private Object merge(Object entity, Session session) {
        if (!session.contains(entity)) {
            try {
                return session.merge(entity);
            } catch (EntityNotFoundException ignored) {
                return entity;
            }
        } else {
            return entity;
        }
    }

    public static void shutDown() {
        final EntityManagerFactory factory = EMF_REF.get();

        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }

    private static EntityManagerFactory initEMF() {
        Map<String, Object> config = new HashMap<>();
        config.put("hibernate.hbm2ddl.auto", new File(DATA_STORE_FILE).exists() ? "update" : "create");

        HibernatePersistenceProvider provider = new HibernatePersistenceProvider();
        return provider.createEntityManagerFactory("ImageManager-H2-DS", config);
    }
}

