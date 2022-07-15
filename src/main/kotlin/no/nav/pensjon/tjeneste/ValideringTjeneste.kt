package no.nav.pensjon.tjeneste


object ValideringTjeneste {

    fun validerIkkeBlank(s: String?, label: String) {
        require(!(s == null || s.trim { it <= ' ' }.length == 0)) { "Kan ikke ta imot tom verdi for $label" }
    }

    fun validerMaxLengde(s: String?, max: Int, label: String) {
        if (s != null && s.length > max) {
            var logString: String = s
            if (logString.length > 100) {
                logString = logString.substring(0, 100) + "...."
            }
            throw IllegalArgumentException("Kan ikke ta imot verdi for $label lenger enn $max tegn: $logString")
        }
    }

}