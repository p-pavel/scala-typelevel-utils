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

package com.perikov.typelevel.tests

class Tests:
  import com.perikov.typelevel.{ConstantSet,*}
  import scala.compiletime.testing.typeChecks

  val validSet: ConstantSet[(1, 2, 3, 4)]   = constantSet[(1, 2, 3, 4)]
  val validSet2: ConstantSet[("a", 3.141d)] = constantSet[("a", 3.141)]
  val emptySet                              = constantSet[EmptyTuple]
  val singletonSet: ConstantSet[Tuple1[1]]  = constantSet[Tuple1[1]]
  val invalidSetNotCostants: false          = typeChecks("constantSet[(Int, String)]")
  val invalidSetDuplicates: false           = typeChecks("constantSet[(1, 1)]")
  val invalidSetNotTuple: false             = typeChecks("constantSet[Int]")

  val add1: ConstantSet[(1, 2, 3, 4, 5)] = validSet.add[5]
  val addToEmpty: ConstantSet[Tuple1[1]] = emptySet.add[1]
  val addDuplicate: false                = typeChecks("validSet.add[1]")

  val remove1: ConstantSet[(1, 2, 4)] = validSet.remove[3]
  val removeAbsent: false             = typeChecks("validSet.remove[5]")

  val union1: ConstantSet[(1, 2, 3, 4, "a", 3.141d)]   = validSet.union(validSet2)
  val unionIntersecting: ConstantSet[(1, 2, 3, 4, -1)] = validSet.union(constantSet[(2, -1)])
  val unionEmptyLeft: ConstantSet[(1, 2, 3, 4)]        = emptySet.union(validSet)
  val unionEmptyRight: ConstantSet[(1, 2, 3, 4)]       = validSet.union(emptySet)
  val unionTwoEmpties: ConstantSet[EmptyTuple]         = emptySet.union(emptySet)

  val intersection1: ConstantSet[(2, 3)]                  = validSet.intersection(constantSet[(5, 3, 2, 8)])
  val intersectionWithEmptyLeft: ConstantSet[EmptyTuple]  = emptySet.intersection(validSet)
  val intersectionWithEmptyRight: ConstantSet[EmptyTuple] = validSet.intersection(emptySet)

  val difference1: ConstantSet[(1, 4)]                    = validSet.difference(constantSet[(2, 3)])
  val differenceWithEmptyLeft: ConstantSet[EmptyTuple]    = emptySet.difference(validSet)
  val differenceWithEmptyRight: ConstantSet[(1, 2, 3, 4)] = validSet.difference(emptySet)

  val contains1: true  = validSet.contains[1]
  val contains2: false = validSet.contains[5]

end Tests
