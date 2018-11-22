package no.nav.sporingslogg.tjeneste;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

public class DbClearUtil {

	@PersistenceContext
	EntityManager entityManager;
	
	@Transactional
	public void clearDb() {
		Query query = entityManager.createNativeQuery("delete from sporings_logg");
		query.executeUpdate();
	}
}
