package nl.juraji.imagemanager.model;

import nl.juraji.imagemanager.model.pinterest.PinterestBoard;
import nl.juraji.imagemanager.util.ModelUtils;
import nl.juraji.imagemanager.util.concurrent.AtomicObject;
import org.hibernate.Session;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.juraji.imagemanager.util.ExceptionUtils.catchAll;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public class Dao {
    private static final AtomicObject<EntityManagerFactory> EMF_REF = new AtomicObject<>();
    private static final String DATA_STORE_FILE = "./store.mv.db";

    public List<Directory> getRootDirectories() {
        try (Session session = getSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            final CriteriaQuery<Directory> query = criteriaBuilder.createQuery(Directory.class);
            final Root<Directory> root = query.from(Directory.class);
            query.select(root);

            query.orderBy(criteriaBuilder.desc(root.get("favorite")),
                    criteriaBuilder.asc(root.get("name")));
            query.where(criteriaBuilder.isNull(root.get("parent")));
            return session.createQuery(query).getResultList();
        }
    }

    public List<Directory> getAllDirectories() {
        try (Session session = getSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            final CriteriaQuery<Directory> query = criteriaBuilder.createQuery(Directory.class);
            final Root<Directory> root = query.from(Directory.class);
            query.select(root);

            query.orderBy(criteriaBuilder.asc(root.get("name")));
            return session.createQuery(query).getResultList();
        }
    }

    public List<PinterestBoard> getAllPinterestBoards() {
        try (Session session = getSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            final CriteriaQuery<PinterestBoard> query = criteriaBuilder.createQuery(PinterestBoard.class);
            final Root<PinterestBoard> root = query.from(PinterestBoard.class);
            query.select(root);

            return session.createQuery(query).getResultList();
        }
    }

    public List<ImageMetaData> getAllImageMetaData() {
        try (Session session = getSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            final CriteriaQuery<ImageMetaData> query = criteriaBuilder.createQuery(ImageMetaData.class);
            final Root<ImageMetaData> root = query.from(ImageMetaData.class);
            query.select(root);

            return session.createQuery(query).getResultList();
        }
    }

    public void refresh(Object entity) {
        final ModelUtils<Object> modelUtils = new ModelUtils<>(entity);
        Object id = modelUtils.getId();

        // If an id could be extracted refresh the entity with the persisted version
        if (id != null) {
            try (Session session = getSession()) {
                final Object result = session.get(entity.getClass(), (Serializable) id);
                catchAll(() -> modelUtils.copyPropertiesFrom(result));
            }
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

    public void save(Object entity) {
        try (Session session = getSession()) {
            session.getTransaction().begin();
            final Object o = this.merge(entity, session);
            session.saveOrUpdate(o);
            session.flush();
            session.getTransaction().commit();
            catchAll(() -> new ModelUtils<>(entity).copyPropertiesFrom(o));
        }
    }

    public void save(Collection<?> entities) {
        try (Session session = getSession()) {
            session.getTransaction().begin();

            for (Object entity : entities) {
                final Object o = this.merge(entity, session);
                session.saveOrUpdate(o);
                catchAll(() -> new ModelUtils<>(entity).copyPropertiesFrom(o));
            }

            session.flush();
            session.getTransaction().commit();
        }
    }

    public void delete(Object entity) {
        try (Session session = getSession()) {
            session.getTransaction().begin();
            final Object o = this.merge(entity, session);
            session.delete(o);
            session.flush();
            session.getTransaction().commit();
        }
    }

    public void delete(Collection<?> entities) {
        try (Session session = getSession()) {
            session.getTransaction().begin();
            entities.stream()
                    .map(o -> this.merge(o, session))
                    .forEach(session::delete);
            session.flush();
            session.getTransaction().commit();
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

