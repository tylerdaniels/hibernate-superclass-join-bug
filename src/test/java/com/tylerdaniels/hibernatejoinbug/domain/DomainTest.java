package com.tylerdaniels.hibernatejoinbug.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl;
import org.hibernate.service.ServiceRegistry;
import org.hsqldb.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DomainTest
{
	private Server server;
	private SessionFactory sessionFactory;

	@BeforeEach
	public void setUpDatabase()
	{
		Server server = new Server();
		server.setDatabaseName(0, "db");
		server.setDatabasePath(0, "mem:db");
		server.start();

		ServiceRegistry standardRegistry =
				new StandardServiceRegistryBuilder()
						.applySetting(AvailableSettings.DIALECT, new HSQLDialect())
						.applySetting(AvailableSettings.URL, "jdbc:hsqldb:mem:db")
						.applySetting(AvailableSettings.USER, "sa")
						.applySetting(AvailableSettings.PASS, "")
						.applySetting(AvailableSettings.CONNECTION_PROVIDER, new DriverManagerConnectionProviderImpl())
						.applySetting(AvailableSettings.HBM2DDL_AUTO, "create-drop")
						.applySetting(AvailableSettings.SHOW_SQL, true)
						.build();

		MetadataSources sources =
				new MetadataSources(standardRegistry)
						.addAnnotatedClass(Address.class)
						.addAnnotatedClass(Customer.class)
						.addAnnotatedClass(ValuedCustomer.class)
						.addAnnotatedClass(Order.class);
		Metadata metadata = sources.getMetadataBuilder().build();

		this.sessionFactory = metadata.getSessionFactoryBuilder().build();
	}

	@AfterEach
	public void shutDownDatabase()
	{
		try
		{
			if (this.sessionFactory != null)
			{
				this.sessionFactory.close();
				this.sessionFactory = null;
			}
			if (this.server != null)
			{
				this.server.shutdown();
				this.server = null;
			}
		}
		catch (Exception e)
		{
		}
	}

	@Test
	void testBasicQuery()
	{
		try (Session s = this.sessionFactory.openSession())
		{
			CriteriaBuilder cb = s.getCriteriaBuilder();
			CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
			Root<Order> order = query.from(Order.class);
			Join<?, Address> addr =
					order.join(Order_.customer)
						 .join(Customer_.details)
						 .join(CustomerDetails_.addresses);
			query.select(order.get(Order_.id))
				 .where(cb.isNull(addr.get(Address_.id)));
			s.createQuery(query).getResultStream().count();
		}
	}

}
