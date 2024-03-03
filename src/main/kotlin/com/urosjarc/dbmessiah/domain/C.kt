package com.urosjarc.dbmessiah.domain

/**
 * Enumeration class that represents different constraints for a table column.
 * Constraints AUTO_INCREMENT and NOT_NULL are inferred from kotlin properties directly
 * that's why they are not listed in this enumerator.
 *
 * @property UNIQUE Constraint that represents a unique column.
 * @property CASCADE_UPDATE Constraint that represents a column that cascades updates.
 * @property CASCADE_DELETE Constraint that represents a column that cascades deletes.
 */
public enum class C { UNIQUE, CASCADE_UPDATE, CASCADE_DELETE }
