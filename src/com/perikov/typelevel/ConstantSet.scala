/* BSD 3-Clause License
 *
 * Copyright (c) 2024, Pavel Perikov
 *
 * Contact: pavel@perikov.consulting
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.perikov.typelevel

/** Proof certificate type, guarantees that `T` is a constant tuple with unique elements
  * @example
  *   {{{
  *
  * val validSet: ConstantSet[(1, 2, 3, 4)]   = constantSet[(1, 2, 3, 4)]
  * val validSet2: ConstantSet[("a", 3.141d)] = constantSet[("a", 3.141)]
  * val emptySet                              = constantSet[EmptyTuple]
  * val singletonSet: ConstantSet[Tuple1[1]]  = constantSet[Tuple1[1]]
  * val invalidSetNotCostants: false          = typeChecks("constantSet[(Int, String)]")
  * val invalidSetDuplicates: false           = typeChecks("constantSet[(1, 1)]")
  * val invalidSetNotTuple: false             = typeChecks("constantSet[Int]")
  * 
  *
  * val add1: ConstantSet[(1, 2, 3, 4, 5)] = validSet.add[5]
  * val addToEmpty: ConstantSet[Tuple1[1]] = emptySet.add[1]
  * val addDuplicate: false                = typeChecks("validSet.add[1]")
  *
  * val remove1: ConstantSet[(1, 2, 4)] = validSet.remove[3]
  * val removeAbsent: false             = typeChecks("validSet.remove[5]")
  *
  * val union1: ConstantSet[(1, 2, 3, 4, "a", 3.141d)]   = validSet.union(validSet2)
  * val unionIntersecting: ConstantSet[(1, 2, 3, 4, -1)] = validSet.union(constantSet[(2, -1)])
  * val unionEmptyLeft: ConstantSet[(1, 2, 3, 4)]        = emptySet.union(validSet)
  * val unionEmptyRight: ConstantSet[(1, 2, 3, 4)]       = validSet.union(emptySet)
  * val unionTwoEmpties: ConstantSet[EmptyTuple]         = emptySet.union(emptySet)
  *
  * val intersection1: ConstantSet[(2, 3)]                  = validSet.intersection(constantSet[(5, 3, 2, 8)])
  * val intersectionWithEmptyLeft: ConstantSet[EmptyTuple]  = emptySet.intersection(validSet)
  * val intersectionWithEmptyRight: ConstantSet[EmptyTuple] = validSet.intersection(emptySet)
  *
  * val difference1: ConstantSet[(1, 4)]                    = validSet.difference(constantSet[(2, 3)])
  * val differenceWithEmptyLeft: ConstantSet[EmptyTuple]    = emptySet.difference(validSet)
  * val differenceWithEmptyRight: ConstantSet[(1, 2, 3, 4)] = validSet.difference(emptySet)
  *
  * val contains1: true  = validSet.contains[1]
  * val contains2: false = validSet.contains[5]
  *   }}}
  * @since 1.0.0
  * @author
  *   Pavel Perikov <pavel@perikov.consulting>
  */
opaque type ConstantSet[T <: Tuple] = ConstantSet.type

/** Creates a [[ConstantSet[T]]
  * @note
  *   Will fail in compile time if `T` is not a constant tuple or contains duplicates
  */
transparent inline def constantSet[T <: Tuple] = ${ createSetImpl[T] }

extension [T <: Tuple](c1: ConstantSet[T])
  inline def values: T = constValueTuple[T]

  /** @note
    *   Will fail in compile time if `A` is already in the set
    * @todo
    *   can possibly implement without resorting to macros if [[contains]] is implemented
    */
  transparent inline def add[A] = ${ addImpl[A, T] }

  /** @note Will fail in compile time if `A` is not in the set */
  transparent inline def remove[A]                                      = ${ removeImpl[A, T] }
  transparent inline def union[T1 <: Tuple](c2: ConstantSet[T1])        = ${ unionImpl[T, T1] }
  transparent inline def intersection[T1 <: Tuple](c2: ConstantSet[T1]) = ${ intersectionImpl[T, T1] }
  transparent inline def difference[T1 <: Tuple](c2: ConstantSet[T1])   = ${ differenceImpl[T, T1] }
  transparent inline def contains[A]: Boolean                           = ${ containsImpl[A, T] }

////// Implementation

/** Proof certificate object */
private object ConstantSet

/** Work around the compiler bug when `transparent inline def`s leak `opaque type` information */
private type CS[A <: Tuple] = com.perikov.typelevel.ConstantSet[A]
import compiletime.*
import scala.quoted.*

private def constant(using q: Quotes)(r: q.reflect.TypeRepr): q.reflect.Constant =
  import q.reflect.*
  r.dealias.asMatchable match
    case c: ConstantType => c.constant
    case _               =>
      val originalType  = r.show(using Printer.TypeReprAnsiCode)
      val dealiasedType = r.dealias.show(using Printer.TypeReprAnsiCode)
      report.errorAndAbort(s"Type $originalType ($dealiasedType) is not a constant type")

private def constantExpr(using q: Quotes)(r: q.reflect.TypeRepr): Expr[Any] =
  import q.reflect.*
  val repr = r.dealias
  repr.asMatchable match
    case c: ConstantType =>
      c.constant match
        case BooleanConstant(value) => Expr(value)
        case ByteConstant(value)    => Expr(value)
        case ShortConstant(value)   => Expr(value)
        case IntConstant(value)     => Expr(value)
        case LongConstant(value)    => Expr(value)
        case FloatConstant(value)   => Expr(value)
        case DoubleConstant(value)  => Expr(value)
        case StringConstant(value)  => Expr(value)
        case CharConstant(value)    => Expr(value)
        case _: UnitConstant        => '{}
        case _: NullConstant        => '{ null }
        case ClassOfConstant(value) =>
          report.errorAndAbort(s"ClassOfConstant $value is not supported")
    case _               =>
      report.errorAndAbort(s"Not a constant type: ${repr.show(using Printer.TypeReprAnsiCode)}")
end constantExpr

private def constTupleTypes[T <: Tuple: Type](using q: Quotes): Seq[q.reflect.TypeRepr] =
  import q.reflect.*
  val t = TypeRepr.of[T].dealias
  t.asMatchable match
    case AppliedType(t, args) => args.map(_.dealias)
    case _                    =>
      Type.of[T] match
        case '[EmptyTuple] => Seq.empty
        case _             => report.errorAndAbort(s"Not a constant tuple: ${t.show(using Printer.TypeReprAnsiCode)}")
end constTupleTypes

private def addImpl[A: Type, T <: Tuple: Type](using Quotes): Expr[Any] =
  import quotes.reflect.*
  val a     = constant(TypeRepr.of[A]).value
  val elems = constTupleTypes[T].map(constant(_).value).toSet
  if elems.contains(a) then report.errorAndAbort(s"Element $a already exists in the set $elems")
  else constSet(constTupleTypes[T] :+ TypeRepr.of[A])
end addImpl

private def removeImpl[A: Type, T <: Tuple: Type](using Quotes): Expr[Any] =
  import quotes.reflect.*
  val elemRepr   = TypeRepr.of[A]
  val tupleTypes = constTupleTypes[T]
  val filtered   = tupleTypes.filterNot(_ =:= elemRepr)

  if filtered.size == tupleTypes.size then
    val elems = tupleTypes.map(constant(_).value).mkString("(", ", ", ")")
    report.errorAndAbort(
      s"Element ${constant(elemRepr).value} does not exist in the set $elems"
    )

  constSet(filtered)
end removeImpl

private def constSet(using q: Quotes)(elems: Seq[q.reflect.TypeRepr]): Expr[Any] =
  import quotes.reflect.*
  val tupExpr = Expr.ofTupleFromSeq(elems.map(constantExpr))
  val csTerm  = tupExpr.asTerm.tpe

  val res = Typed('{ ConstantSet }.asTerm, Inferred(TypeRepr.of[CS].appliedTo(csTerm)))
  res.asExpr
end constSet

private def createSetImpl[T <: Tuple: Type](using Quotes): Expr[Any] =
  import quotes.reflect.*
  val elems = constTupleTypes[T]
  if elems.distinct.toSet.size != elems.size then report.errorAndAbort(s"Duplicate elements in the tuple: $elems")
  else constSet(constTupleTypes[T])
end createSetImpl

private def unionImpl[T <: Tuple: Type, T1 <: Tuple: Type](using Quotes): Expr[Any] =
  val elems1 = constTupleTypes[T]
  val elems2 = constTupleTypes[T1]
  val union  = (elems1 ++ elems2).distinct
  constSet(union)
end unionImpl

private def intersectionImpl[T <: Tuple: Type, T1 <: Tuple: Type](using Quotes): Expr[Any] =
  val elems1       = constTupleTypes[T]
  val elems2       = constTupleTypes[T1]
  val intersection = elems1.intersect(elems2)
  constSet(intersection)
end intersectionImpl

private def differenceImpl[T <: Tuple: Type, T1 <: Tuple: Type](using Quotes): Expr[Any] =
  val elems1     = constTupleTypes[T]
  val elems2     = constTupleTypes[T1]
  val difference = elems1.diff(elems2)
  constSet(difference)
end differenceImpl

private def containsImpl[A: Type, T <: Tuple: Type](using Quotes): Expr[Boolean] =
  import quotes.reflect.*
  val elemRepr = TypeRepr.of[A]
  val elems    = constTupleTypes[T]
  Expr(elems.contains(elemRepr))
end containsImpl
