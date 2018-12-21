package no.nav.sporingslogg.standalone.testconfig;

import no.nav.sporingslogg.ldap.LdapGroupService;

public class DummyLdapGroupService extends LdapGroupService {

    public static String USER_TO_REFUSE = null;
    
	public DummyLdapGroupService() {
		super(null);
	}

	@Override
	public boolean brukerErIGruppe(String bruker) {
		// alle unntatt USER_TO_REFUSE er OK
		if (bruker.equalsIgnoreCase(USER_TO_REFUSE)) {
			return false;
		}
		return true;
	}

}
