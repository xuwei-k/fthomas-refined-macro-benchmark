package eu.timepit.refined.macros

import eu.timepit.refined.api.{RefType, Refined, Validate}
import eu.timepit.refined.char.{Digit, Letter, LowerCase, UpperCase, Whitespace}
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.internal.Resources
import eu.timepit.refined.numeric.{Negative, NonNegative, NonPositive, Positive}
import scala.reflect.macros.blackbox

class MyRefineMacro(val c: blackbox.Context) extends MacroUtils with LiteralMatchers {

  import c.universe._

  def impl[T: c.WeakTypeTag, P: c.WeakTypeTag](t: c.Expr[T]): c.Tree = {
    val tValue: T = t.tree match {
      case Literal(Constant(value)) => value.asInstanceOf[T]
      case BigDecimalMatcher(value) => value.asInstanceOf[T]
      case BigIntMatcher(value)     => value.asInstanceOf[T]
      case _                        => abort(Resources.refineNonCompileTimeConstant)
    }

    val validate = validateInstance[T, P].getOrElse(
      abort(s"could not found Validate[${weakTypeOf[T]}, ${weakTypeOf[P]}]")
    )
    val res = validate.validate(tValue)

    if (res.isFailed)
      abort(validate.showResult(tValue, res))

    c.universe.reify(reify(RefType[Refined]).splice.unsafeWrap[T, P](t.splice)).tree
  }

  private def validateInstance[T, P](implicit
      T: c.WeakTypeTag[T],
      P: c.WeakTypeTag[P]
  ): Option[Validate[T, P]] =
    validateInstances
      .collectFirst {
        case (tpeT, instancesForT) if tpeT =:= T.tpe =>
          instancesForT.collectFirst {
            case (tpeP, validate) if tpeP =:= P.tpe =>
              validate.asInstanceOf[Validate[T, P]]
          }
      }
      .flatten

  private val validateInstances: List[(Type, List[(Type, Any)])] = {
    def instance[T, P](implicit P: c.WeakTypeTag[P], v: Validate[T, P]): (Type, Validate[T, P]) =
      P.tpe -> v

    List(
      weakTypeOf[Int] -> List(
        instance[Int, Positive],
        instance[Int, NonPositive],
        instance[Int, Negative],
        instance[Int, NonNegative]
      ),
      weakTypeOf[Long] -> List(
        instance[Long, Positive],
        instance[Long, NonPositive],
        instance[Long, Negative],
        instance[Long, NonNegative]
      ),
      weakTypeOf[Double] -> List(
        instance[Double, Positive],
        instance[Double, NonPositive],
        instance[Double, Negative],
        instance[Double, NonNegative]
      ),
      weakTypeOf[String] -> List(
        instance[String, NonEmpty]
      ),
      weakTypeOf[Char] -> List(
        instance[Char, Digit],
        instance[Char, Letter],
        instance[Char, LowerCase],
        instance[Char, UpperCase],
        instance[Char, Whitespace]
      ),
      weakTypeOf[BigInt] -> List(
        instance[BigInt, Positive],
        instance[BigInt, NonPositive],
        instance[BigInt, Negative],
        instance[BigInt, NonNegative]
      ),
      weakTypeOf[BigDecimal] -> List(
        instance[BigDecimal, Positive],
        instance[BigDecimal, NonPositive],
        instance[BigDecimal, Negative],
        instance[BigDecimal, NonNegative]
      )
    )
  }
}
