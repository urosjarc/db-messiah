package com.urosjarc.dbmessiah.domain.engine

import java.sql.Connection

/**
 * Because giving raw connection to the user is to dangerous!!!
 * Protect user at all cost!!
 */
class ConnectionWrapper(private val conn: Connection){

}
