package com.urosjarc.dbmessiah.domain

/**
 * Enumeration class that represents different constraints for a table column.
 *
 * @property AUTO_INC Constraint that represents an auto-incrementing primary key column.
 * @property UNIQUE Constraint that represents a unique column.
 * @property CASCADE_UPDATE Constraint that represents a column that cascades updates.
 * @property CASCADE_DELETE Constraint that represents a column that cascades deletes.
 */
public enum class C { AUTO_INC, UNIQUE, CASCADE_UPDATE, CASCADE_DELETE }
