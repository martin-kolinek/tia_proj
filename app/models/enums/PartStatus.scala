package models.enums

object PartStatus extends Enumeration {
	type PartStatus = Value
	val ToCut, Finished, Damaged = Value
} 