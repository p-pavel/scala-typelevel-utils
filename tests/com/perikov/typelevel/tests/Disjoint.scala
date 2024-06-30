package com.perikov.typelevel.tests


import com.perikov.typelevel.{disjoint, Disjoint, absurd, given}

val d1: true                                             = disjoint[String, Int]
val d2: true                                             = disjoint[Int, String]
val d3: false                                            = disjoint[Int, Int]
val d4: false                                            = disjoint[String, "sdf"]
val d5: false                                            = disjoint["sdf", String]
val d6: false                                            = disjoint[(1 | 2 | 3), (5 | 2 | 7)]
val d7: true                                             = disjoint[(1 | 2 | 3), (5 | 7 | "a")]
val d8                                                   = summon[Disjoint[Int, String]]
val d9                                                   = disjoint[1 | Nothing, 2 | Nothing]
val d10: true                                            = disjoint[1, 2 | Nothing]
val d11: true                                            = disjoint[1 | Nothing, 2]
