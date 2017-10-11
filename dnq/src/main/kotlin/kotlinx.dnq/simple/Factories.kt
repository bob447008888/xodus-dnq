package kotlinx.dnq.simple

import com.jetbrains.teamsys.dnq.database.PropertyConstraint
import kotlinx.dnq.XdEntity
import kotlin.reflect.KProperty

typealias Constraints<R, T> = PropertyConstraintBuilder<R, T?>.() -> Unit

fun <R : XdEntity, T : Comparable<*>> Constraints<R, T>?.collect(): List<PropertyConstraint<T?>> {
    return if (this != null) {
        PropertyConstraintBuilder<R, T?>()
                .apply(this)
                .constraints
    } else {
        emptyList()
    }
}

inline fun <R : XdEntity, reified T : Comparable<*>> xdProp(
        dbName: String? = null,
        noinline constraints: Constraints<R, T?>? = null,
        require: Boolean = false,
        unique: Boolean = false,
        noinline default: (R, KProperty<*>) -> T): XdProperty<R, T> {

    return XdProperty(T::class.java, dbName, constraints.collect(), when {
        unique -> XdPropertyRequirement.UNIQUE
        require -> XdPropertyRequirement.REQUIRED
        else -> XdPropertyRequirement.OPTIONAL
    }, default)
}

inline fun <R : XdEntity, reified T : Comparable<*>> xdNullableProp(
        propertyName: String? = null,
        noinline constraints: Constraints<R, T?>? = null): XdNullableProperty<R, T> {
    return XdNullableProperty(T::class.java, propertyName, constraints.collect())
}

fun <R : XdEntity, B, T> XdConstrainedProperty<R, B>.wrap(wrap: (B) -> T, unwrap: (T) -> B): XdWrappedProperty<R, B, T> {
    return XdWrappedProperty(this, wrap, unwrap)
}