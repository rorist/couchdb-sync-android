package info.lamatricexiste.couchdb;

import info.lamatricexiste.couchdb.ICouchClient;

interface ICouchService
{
    /* Starts couchDB, calls "couchStarted" callback when 
     * complete 
     */
    void initCouchDB(ICouchClient callback, String url, String pkg);
    
    /*
     * 
     */
    void quitCouchDB();
}
