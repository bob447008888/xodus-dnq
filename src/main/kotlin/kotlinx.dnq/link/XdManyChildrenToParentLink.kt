package kotlinx.dnq.link

import jetbrains.exodus.query.metadata.AssociationEndCardinality
import jetbrains.exodus.query.metadata.AssociationEndType
import kotlinx.dnq.RequiredPropertyUndefinedException
import kotlinx.dnq.XdEntity
import kotlinx.dnq.XdEntityType
import kotlinx.dnq.query.XdMutableQuery
import kotlinx.dnq.util.reattach
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class XdManyChildrenToParentLink<R : XdEntity, T : XdEntity>(
        val entityType: XdEntityType<T>,
        override val oppositeField: KProperty1<T, XdMutableQuery<R>>,
        dbPropertyName: String?
) : ReadWriteProperty<R, T>, XdLink<R, T>(
        entityType,
        dbPropertyName,
        null,
        AssociationEndCardinality._1,
        AssociationEndType.ChildEnd,
        onDelete = OnDeletePolicy.CLEAR,
        onTargetDelete = OnDeletePolicy.CASCADE
) {

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        val parent = thisRef.reattach().getLink(property.name) ?:
                throw RequiredPropertyUndefinedException(thisRef, property)

        return entityType.wrap(parent)
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        value.reattach().addChild(oppositeField.name, property.name, thisRef.reattach())
    }

    override fun isDefined(thisRef: R, property: KProperty<*>): Boolean {
        return thisRef.reattach().getLink(property.name) != null
    }
}

