package com.perikov.typelevel

import scala.compiletime as ct
import ct.*
import com.perikov.typelevel.compiletime.typeAnsiString

/** Here we utilized Scala compiler match type reduction algorithm to detect if two types are disjoint Reduction will
  * not proceed in cases like `a =:= "asdf" & b =:= "String"`
  * @see
  *   https://docs.scala-lang.org/scala3/reference/new-types/match-types.html#match-type-reduction-1
  */
private type Disj[a, b] <: Boolean =
  a match
    case b => false
    case _ =>
      b match
        case a => false
        case _ => true

inline transparent def disjoint[A, B]: Boolean =
  inline constValueOpt[Disj[A, B]] match
    case _: None.type     => false
    case a: Some[Boolean] => constValue[Disj[A, B]]

opaque type Disjoint[-A, -B] = DisjointInstance.type

inline given disjointProof[A, B]: Disjoint[A, B] =
  inline if disjoint[A, B] then DisjointInstance
  else scala.compiletime.error("Types " + typeAnsiString[A] + " and " + typeAnsiString[B] + " are not disjoint")

private object DisjointInstance

inline def absurd[A, B](inline witness: A & B)(using Disjoint[A, B]): Nothing =
  val codeString = codeOf(witness)
  throw new AssertionError(s"Absurd: got instance ($codeString), but the type is uninhabited")
