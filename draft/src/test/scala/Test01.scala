object Test01{
    def main(args: Array[String]): Unit = {
        val str = "xx"

        str match {
            case "yy" => println("yy")
            case "xx" => println("xx")
            case _ => println("nothing")
        }
    }
}