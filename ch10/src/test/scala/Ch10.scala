import org.scalatest._

trait Monoid[A] {
  def op(a1: A, a2: A) : A
  def zero : A
}

sealed trait WC
case class Stub(chars: String) extends WC
case class Part(lStub: String, words: Int, rStub: String) extends WC

class TestChapter10 extends FlatSpec with Matchers {

  val intAddition = new Monoid[Int] {
    def op(a1: Int, a2: Int) = a1 + a2
    val zero = 0
  }

  val intMultiplication = new Monoid[Int] {
    def op(a1: Int, a2: Int) = a1 * a2
    val zero = 1
  }

  val booleanOr = new Monoid[Boolean] {
    def op(a1: Boolean, a2: Boolean) = a1 || a2
    val zero = false
  }

  val booleanAnd = new Monoid[Boolean] {
    def op(a1: Boolean, a2: Boolean) = a1 && a2
    val zero = true
  }


  val optionMonoid = new Monoid[Option[Int]] {
    def op(a1: Option[Int], a2: Option[Int]) = a1 orElse a2
    val zero = None
  }

  def endoMonoid[A] = new Monoid[A => A] {
    def op(a1: A => A, a2: A => A) = a1 compose a2
    val zero = (a: A) => a
  }

  def foldMap[A, B](as: List[A], m: Monoid[B])(f: A => B): B = {
    as.foldLeft(m.zero)((a, e) => m.op(a, f(e)))
  }

  def foldLeft2[A, B](as: List[A], z: B)(f: (B, A) => B): B = {
    foldMap(as, endoMonoid[B])(a => b => f(b, a))(z)
  }

  def foldMapV[A, B](v: IndexedSeq[A], m: Monoid[B])(f: A => B): B = {
    if (v.length == 1)
      f(v(0))
    else {
      val point = v.length / 2
      m.op(foldMapV(v.take(point), m)(f), foldMapV(v.drop(point), m)(f))
    }
  }

  def isSorted[A](v: IndexedSeq[A])(f: (A, A) => Boolean): Boolean = {

    val m = new Monoid[A => Boolean] {
      def op(a1: A => Boolean, a2: A => Boolean): A => Boolean =
        x => a1(x) && a2(x)
      def zero: A => Boolean = a => true
    }
    foldMapV(v.drop(1), m)(a => x => f(x, a))(v(0))
  }


  // 연습문제 10.1
  it should "10.1 test Monoid" in {
    intAddition.op(1, intAddition.op(2, 3)) should
      be (intAddition.op(intAddition.op(1, 2), 3))
    intAddition.op(intAddition.zero, 1) should
      be (intAddition.op(1, intAddition.zero))

    intAddition.op(1, intAddition.op(2, 3)) should
      be (intAddition.op(intAddition.op(1, 2), 3))
    intAddition.op(intAddition.zero, 1) should
      be (intAddition.op(1, intAddition.zero))

    booleanOr.op(false, booleanOr.op(true, false)) should
      be (booleanOr.op(false, booleanOr.op(true, false)))
    booleanOr.op(booleanOr.zero, false)
    booleanOr.op(false, booleanOr.zero)
    booleanOr.op(booleanOr.zero, true)
    booleanOr.op(true, booleanOr.zero)

    booleanAnd.op(false, booleanAnd.op(true, false)) should
      be (booleanAnd.op(false, booleanAnd.op(true, false)))
    booleanAnd.op(booleanAnd.zero, false) should 
      be (booleanAnd.op(false, booleanAnd.zero))
    booleanAnd.op(booleanAnd.zero, true) should
      be (booleanAnd.op(true, booleanAnd.zero))
  }

  // 연습문제 10.2
  it should "10.2 option Monoid" in {

    val monoid = optionMonoid

    monoid.op(Some(1), monoid.op(Some(2), Some(3))) should
      be (monoid.op(Some(1), monoid.op(Some(2), Some(3))))
    monoid.op(monoid.zero, Some(1)) should
      be (monoid.op(Some(1), monoid.zero))
  }

  // 연습문제 10.3
  it should "10.3 endo Monoid" in {

    val monoid = endoMonoid[Int]
    def f1 = (a: Int) => a * 10
    def f2 = (a: Int) => a + 10
    def f3 = (a: Int) => a * 20 + 2

    monoid.op(f1, monoid.op(f2, f3))(10) should
      be (monoid.op(f1, monoid.op(f2, f3))(10))
    monoid.op(monoid.zero, f1)(10) should
      be (monoid.op(f1, monoid.zero)(10))
  }

  // 연습문제 10.5
  it should "10.5 foldMap" in {
    val m = new Monoid[String] {
      def op(a1: String, a2: String) = a1 + a2
      val zero: String = ""
    }

      foldMap(List(1,2,3,4), m)(_.toString) should be ("1234")
  }

  // 연습문제 10.6
  it should "10.6 foldLeft & foldRight using foldMap" in {
    foldLeft2(List(1, 2, 3, 4), 0)((a, e) => a + e) should be (10)
  }

  // 연습문제 10.7
  it should "10.7 foldMapV" in {
    val m = new Monoid[String] {
      def op(a1: String, a2: String) = a1 + a2
      val zero: String = ""
    }

    foldMapV(IndexedSeq(1, 2, 3, 4), m)(_.toString) should be ("1234")
    foldMapV(IndexedSeq(1, 2, 3), m)(_.toString) should be ("123")
  }

  // 연습문제 10.9
  //it should "10.9 is sorting IndexedSeq" in {
  //  isSorted(IndexedSeq(1, 2, 3, 4))(_ <= _) should be (true)
  //  isSorted(IndexedSeq(2, 1, 3, 4))(_ <= _) should be (false)
  //  isSorted(IndexedSeq(1, 3, 2, 4))(_ <= _) should be (false)
  //}

  val wcMonoid = new Monoid[WC] {
    def op(a1: WC, a2: WC): WC = (a1, a2) match {
      case (Part(lStub1, words1, rStub1), Part(lStub2, words2, rStub2)) => {
        val stubWords = if (Seq(rStub1, lStub2).forall(_.isEmpty)) 0 else 1
        Part(lStub1, words1 + words2 + stubWords, rStub2)
      }
      case (Stub(str1), Stub(str2)) => Stub(str1 + str2)
      case (Part(lStub, words, rStub), Stub(str)) => Part(lStub, words, rStub + str)
      case (Stub(str), Part(lStub, words, rStub)) => Part(str + lStub, words, rStub)
    }
    def zero: WC = Stub("")
  }

  // 연습문제 10.10
  it should "10.10 wcMonoid" in {
    val part1 = Part("lorem", 1, "do")
    val part2 = Part("lor", 2, "")
    val part3 = Part("", 2, "dfdf")
    wcMonoid.op(part1, part2) should be (Part("lorem", 4, ""))
    wcMonoid.op(part2, part3) should be (Part("lor", 4, "dfdf"))
    wcMonoid.op(part1, wcMonoid.zero) should be (part1)
    wcMonoid.op(wcMonoid.zero, part1) should be (part1)
  }

  // 연습문제 10.11
  it should "10.11 counts words" in {
    def countWords(text: String) = {
      val half = text.length / 2
      val stubs = List(Stub(text.take(half)), Stub(text.drop(half)))
      def f(s: Stub): Part = {
        if (s.chars.trim.isEmpty)
          Part("", 0, "")
        else {
          val words = s.chars.split(' ').filter(!_.isEmpty)
          (s.chars.head, s.chars.last) match {
            case (' ', ' ') => Part("", words.length, "")
            case (' ', _) => Part("", words.length - 1, words.last)
            case (_, ' ') => Part(words.head, words.length - 1, "")
            case (_, _) => Part(words.head, words.length - 2, words.last)
          }
        }
      }
      foldMap(stubs, wcMonoid)(f) match {
        case Part(lStub, words, rStub) =>
          words + Seq(lStub, rStub).filter(!_.isEmpty).length
        case _ => 0
      }
    }

    val text = "It's predefined and is called exists. And forall would be the function you are looking for."
    countWords(text) should be (16)

    countWords("12 34 ") should be (2)
    countWords(" 12 34") should be (2)
    countWords(" 12 34 ") should be (2)
    countWords(" 12  34 ") should be (2)
    countWords("") should be (0)
    countWords(" ") should be (0)
    countWords("1271309847213908471290384712093487123049812734092$") should be (1)
  }




  def productMonoid[A, B](a: Monoid[A], b: Monoid[B]): Monoid[(A, B)] = {
    new Monoid[(A, B)] {
      def op(x: (A, B), y: (A, B)): (A, B) = (a.op(x._1, y._1), b.op(x._2, y._2))
      def zero: (A, B) = (a.zero, b.zero)
    }
  }

  // 연습문제 10.16
  it should "10.16 productMonoid" in {

    val m1 = new Monoid[Int] {
      def op(a1: Int, a2: Int) = a1 + a2
      val zero = 0
    }
    val m2 = new Monoid[Int] {
      def op(a1: Int, a2: Int) = a1 * a2
      val zero = 1
    }

    val m3 = productMonoid(m1, m2)

    m3.op(m3.op((1, 2), (3, 4)), (5, 6)) should be (m3.op((1, 2), m3.op((3, 4), (5, 6))))
  }

  def mapMergeMonoid[K, V](V: Monoid[V]): Monoid[Map[K, V]] =
    new Monoid[Map[K, V]] {
      def zero = Map[K, V]()
      def op(a: Map[K, V], b: Map[K, V]) =
        (a.keySet ++ b.keySet).foldLeft(zero) { (acc, k) =>
          acc.updated(k, V.op(a.getOrElse(k, V.zero),
                              b.getOrElse(k, V.zero)))
        }
    }

  it should "merge two maps" in {
    val map1 = Map("a" -> 1, "b" -> 2)
    val map2 = Map("b" -> 3, "c" -> 4)

    val m = mapMergeMonoid[String, Int](intAddition)
    m.op(map1, map2) should be (Map("a" -> 1, "b" -> 5, "c" -> 4))
  }

  it should "merge nested maps" in {
    val map1 = Map("o1" -> Map("i1" -> 1, "i2" -> 2))
    val map2 = Map("o1" -> Map("i2" -> 3))

    val M: Monoid[Map[String, Map[String, Int]]] =
      mapMergeMonoid(mapMergeMonoid(intAddition))

    M.op(map1, map2) should be (Map("o1" -> Map("i1" -> 1, "i2" -> 5)))
  }

  def functionMonoid[A,B](B: Monoid[B]): Monoid[A=>B] = new Monoid[A=>B] {
    def op(f1: A => B, f2: A => B): A => B = (a: A) => B.op(f1(a), f2(a))
    def zero: A => B = (a: A) => B.zero
  }

  // 연습문제 10.17
  it should "create a monoid for function" in {
    val m: Monoid[String => Int] = functionMonoid[String, Int](intAddition)
    def f1(x: String) = x.toInt * 2
    def f2(x: String) = x.toInt * 3
    m.op(f1, f2)("10") should be (50)
  }

  def bag[A](as: IndexedSeq[A]): Map[A, Int] = {
    val m = mapMergeMonoid[A, Int](intAddition)
    foldMapV(as, m)(a => Map(a -> 1))
  }

  // 연습문제 10.18
  it should "count elements" in {
    bag(Vector("a", "rose", "is", "a", "rose")) should be (Map("a" -> 2, "rose" -> 2, "is" -> 1))
  }
}

