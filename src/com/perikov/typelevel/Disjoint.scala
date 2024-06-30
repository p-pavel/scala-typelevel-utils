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
