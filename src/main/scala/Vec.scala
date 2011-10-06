package Chisel {

import scala.collection.mutable.ArrayBuffer

object Vec {
/*
  def apply[T <: Data](n: Int)(gen: => T): Vec[T] = {
    val res = new Vec[T]();
    for(i <- 0 until n){
      val t = gen;
      res += t;
      t.name += i;
    }
    res
  }
*/
  def apply[T <: Data](n: Int): (=> T) => Vec[T] = {
   gen(n, _);
  }

  def gen[T <: Data](n: Int, gen: => T): Vec[T] = {
    val res = new Vec[T]();
    for(i <- 0 until n){
      val t = gen;
      res += t;
      t.name += i;
    }
    res
  }
  
  def apply[T <: Data: Manifest](n: Int, fixed: T): Vec[T] = {
    val res = new Vec[T]();
    for(i <- 0 until n){
        res += fixed
    }
    res
  }

  def apply[T <: Data: Manifest](n: Int, args: Any*): Vec[T] = {
    val res = new Vec[T]();
    for(i <- 0 until n) {
      val t = (if(args == List(None)) Fab[T]() else Fab[T](args: _*));
      res += t;
      t.name += i;
    }
    res
  }

}

class Vec[T <: Data]() extends Data {
  val bundleVector = new ArrayBuffer[T];
  def +=(b: T) = bundleVector += b;
  def apply(ind: Int): T = {
    bundleVector(ind)
  };
  def apply(ind: UFix): T = {
    var res = bundleVector(0);
    for(i <- 1 until bundleVector.length)
      res = Mux(UFix(i) === ind, bundleVector(i), res)
    res
  }
  override def flatten: Array[(String, IO)] = {
    val res = new ArrayBuffer[(String, IO)];
    for (elm <- bundleVector)
      elm match {
	case bundle: Bundle => res ++= bundle.flatten;
	case io: IO => res += ((io.name, io));
      }
    res.toArray
  }

  override def <>(src: Node) = {
    src match {
      case other: Vec[T] => {
	for((b, o) <- bundleVector zip other.bundleVector)
	  b <> o
      }
    }
  }

  override def ^^(src: Node) = {
    src match {
      case other: Vec[T] => 
	for((b, o) <- bundleVector zip other.bundleVector)
	  b ^^ o
    }
  }

  def <>(src: List[Node]) = {
    for((b, e) <- bundleVector zip src)
      e match {
	case other: Bundle =>
	  b <> e;
      }
  }

  override def findNodes(depth: Int, c: Component): Unit = {
    for(bundle <- bundleVector)
      bundle.findNodes(depth, c);
  }

  override def flip(): this.type = {
    for(b <- bundleVector)
      b.flip();
    this
  }

  override def name_it (path: String, named: Boolean = true) = {
    for (i <- bundleVector) {
      i.name = (if (path.length > 0) path + "_" else "") + i.name;
      i.name_it(i.name, named);
      // println("  ELT " + n + " " + i);
    }
  }

}

}
