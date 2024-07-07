package example

import eu.timepit.refined.macros.MyRefineMacro
import eu.timepit.refined.api.RefType
import eu.timepit.refined.api.Refined
import scala.language.experimental.macros
import scala.language.implicitConversions

object MyRefinedAuto {
  implicit def myAutoRefineV[T, P](t: T): Refined[T, P] =
    macro MyRefineMacro.impl[T, P]
}
