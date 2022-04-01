object FakeAkkaRuntime {
  def create(): AkkaRuntime = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    implicit val mat: Materializer = Materializer(system)
    AkkaRuntime.createNew()
  }
  def createMaterialier(): Materializer = {
    val system: ActorSystem = ActorSystem()
    val mat: Materializer = Materializer(system)
    mat

  }
}
